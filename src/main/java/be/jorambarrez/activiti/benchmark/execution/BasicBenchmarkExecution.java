package be.jorambarrez.activiti.benchmark.execution;

import java.util.Date;
import java.util.List;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.runtime.ProcessInstance;

import be.jorambarrez.activiti.benchmark.Benchmark;
import be.jorambarrez.activiti.benchmark.output.BenchmarkResult;
import be.jorambarrez.activiti.benchmark.util.Utils;

/**
 * Basic way of running the benchmark: 1 thread, sequentially executing the
 * processes on by one.
 *
 * @author jbarrez
 */
public class BasicBenchmarkExecution implements BenchmarkExecution {

    protected String[] processes;

    protected long countBefore;

    protected long countAfter;

    public BasicBenchmarkExecution(String[] processes) {
        this.processes = processes;
    }

    public BenchmarkResult sequentialExecution(String[] processes, int nrOfProcessExecutions, boolean history) {

        countProcessesBeforeBenchmark();
        BenchmarkResult result = new BenchmarkResult(1);

        for (String process : processes) {
            
        	System.out.println(new Date() + " : [SEQ]Starting " + nrOfProcessExecutions + " of process " + process);
            long allProcessesStart = System.currentTimeMillis();
                       
            for (int i = 0; i < nrOfProcessExecutions; i++) {
                ExecuteProcessRunnable executeProcessRunnable = new ExecuteProcessRunnable(process, ProcessEngineHolder.getInstance());
                executeProcessRunnable.run();
                result.addProcessInstanceMeasurement(process, executeProcessRunnable.getDuration());
            }
            
            long allProcessesEnd = System.currentTimeMillis();
            result.addTotalTimeMeasurementForProcess(process, nrOfProcessExecutions, allProcessesEnd - allProcessesStart);
        }

        if (history) {
            countProcessesAfterBenchmark();
            verifyCounts(nrOfProcessExecutions * processes.length);
        }

        cleanAndDeploy();
        return result;
    }

    public BenchmarkResult randomExecution(String[] processes, int totalNrOfExecutions, boolean history) {

        countProcessesBeforeBenchmark();
        BenchmarkResult result = new BenchmarkResult(1);

        String[] randomizedProcesses = Utils.randomArray(processes, totalNrOfExecutions);
        System.out.println(new Date() + " : [RND]Starting " + totalNrOfExecutions + " random processes ");

        long allProcessesStart = System.currentTimeMillis();

        for (String randomProcess : randomizedProcesses) {
        	 new ExecuteProcessRunnable(randomProcess, ProcessEngineHolder.getInstance()).run();
        }

        long allProcessesEnd = System.currentTimeMillis();
        result.addTotalTimeMeasurementForProcess(Utils.toString(processes), totalNrOfExecutions, allProcessesEnd - allProcessesStart);

        if (history) {
            countProcessesAfterBenchmark();
            verifyCounts(totalNrOfExecutions);
        }

        cleanAndDeploy();
        return result;
    }


 /* -----------------------------------
  * HELPER METHODS FOR PROCESS COUNTING
  * ----------------------------------- */


    protected void cleanAndDeploy() {
    	
        System.out.println(new Date() + " : Recreating DB schema");
         
		((ProcessEngineImpl) ProcessEngineHolder.getInstance()).getProcessEngineConfiguration()
				.getCommandExecutor().execute(new Command<Object>() {
					public Object execute(CommandContext commandContext) {
						DbSqlSession dbSqlSession = commandContext.getSession(DbSqlSession.class);
						dbSqlSession.dbSchemaDrop();
						dbSqlSession.dbSchemaCreate();
						return null;
					}
				});
		
		System.out.println("Rebooting process engine");
		ProcessEngineHolder.rebootProcessEngine();
		
		System.out.println("Redeploying test processes");
		for (String process : Benchmark.PROCESSES) {
			ProcessEngineHolder.getInstance().getRepositoryService().createDeployment()
					.addClasspathResource(process + ".bpmn20.xml").deploy();
		}
		
		try {
			Thread.sleep(10000L);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    protected void countProcessesBeforeBenchmark() {
        this.countBefore = countNrOfEndedProcesses();
    }

    protected void countProcessesAfterBenchmark() {
        this.countAfter = countNrOfEndedProcesses();
    }

    protected void verifyCounts(int expected) {
        System.out.print(new Date() + " : Counting finished processes [expected:" + expected +"] ... ");
        long finishedProcesses = countAfter - countBefore;
        if (finishedProcesses != expected) {
            throw new RuntimeException("Error: expected to have " + expected +
                    " processes executions, but got " + finishedProcesses + " instead.");
        }
        System.out.println("OK");
    }

    /**
     * Counts the nr of ended processes.
     */
    private long countNrOfEndedProcesses() {
        return ProcessEngineHolder.getInstance().getHistoryService().createHistoricProcessInstanceQuery().finished().count();
    }
    
    static class DeleteHistoricProcessInstancesRunnable implements Runnable {
    	
    	private HistoryService historyService;
    	private long start;
    	private long pageSize;
    	
    	public DeleteHistoricProcessInstancesRunnable(HistoryService historyService, long start, long pageSize) {
    		this.historyService = historyService;
    		this.start = start;
    		this.pageSize = pageSize;
    	}
    	
    	public void run() {
    		List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().listPage((int)start, (int)pageSize);
    		int counter = 0;
    		for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
    			try {
    				historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    				counter++;
    			} catch (ActivitiException e) {
    				if (!e.getMessage().contains("No historic process instance found")) {
    					throw new RuntimeException(e);
    				}
    			}
    		}
    		 System.out.println(new Date() + " : Deleted " + counter + " historic process instances");
    	}
    	
    }
    
  static class DeleteProcessInstancesRunnable implements Runnable {
    	
    	private RuntimeService runtimeService;
    	private long start;
    	private long pageSize;
    	
    	public DeleteProcessInstancesRunnable(RuntimeService runtimeService, long start, long pageSize) {
    		this.runtimeService = runtimeService;
    		this.start = start;
    		this.pageSize = pageSize;
    	}
    	
    	public void run() {
    		List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery().listPage((int)start, (int)pageSize);
    		int counter = 0;
    		for (ProcessInstance processInstance : processInstances) {
    			runtimeService.deleteProcessInstance(processInstance.getId(), null);
    			counter++;
    		}
    		System.out.print(new Date() + " : Deleted " + counter + " process instances");
    	}
    	
    }

}
