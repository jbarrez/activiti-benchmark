package be.jorambarrez.activiti.benchmark.execution;

import be.jorambarrez.activiti.benchmark.output.BenchmarkResult;
import be.jorambarrez.activiti.benchmark.util.Utils;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RuntimeService;

import java.util.Date;

/**
 * Basic way of running the benchmark: 1 thread, sequentially executing the
 * processes on by one.
 * 
 * @author jbarrez
 */
public class BasicBenchmarkExecution implements BenchmarkExecution {

  protected ProcessEngine processEngine;

  protected RuntimeService runtimeService;
  
  protected long countBefore;
  
  protected long countAfter;

  public BasicBenchmarkExecution(ProcessEngine processEngine) {
    this.processEngine = processEngine;
    this.runtimeService = processEngine.getRuntimeService();
  }
  
  public BenchmarkResult sequentialExecution(String[] processes, int nrOfProcessExecutions, boolean history) {
    
    countProcessesBeforeBenchmark();
    BenchmarkResult result = new BenchmarkResult(1);

    for (String process : processes) {
      System.out.println(new Date() + " : [SEQ]Starting " + nrOfProcessExecutions + " of process " + process);
      long start = System.currentTimeMillis();
      
      for (int i = 0; i < nrOfProcessExecutions; i++) {
        runtimeService.startProcessInstanceByKey(process);
      }
      
      long end = System.currentTimeMillis();
      result.addProcessMeasurement(process, nrOfProcessExecutions, end - start);
    }
    
    if (history) {
    	countProcessesAfterBenchmark();
    	verifyCounts(nrOfProcessExecutions * processes.length);
    }

    return result;
  }
  
  public BenchmarkResult randomExecution(String[] processes, int totalNrOfExecutions, boolean history) {  
    
    countProcessesBeforeBenchmark();
    BenchmarkResult result = new BenchmarkResult(1);

    String[] randomizedProcesses = Utils.randomArray(processes, totalNrOfExecutions);
    System.out.println(new Date() + " : [RND]Starting " + totalNrOfExecutions + " random processes ");
    
    long start = System.currentTimeMillis();
    
    for (String randomProcess : randomizedProcesses) {
      runtimeService.startProcessInstanceByKey(randomProcess);
    }
    
    long end = System.currentTimeMillis();
    result.addProcessMeasurement(Utils.toString(processes), totalNrOfExecutions, end - start);

    if (history) {
    	countProcessesAfterBenchmark();
    	verifyCounts(totalNrOfExecutions);
    }
    
    return result;
  }
  
  
  /* -----------------------------------
   * HELPER METHODS FOR PROCESS COUNTING
   * ----------------------------------- */
  
  protected void countProcessesBeforeBenchmark() {
    this.countBefore = countNrOfEndedProcesses();
  }
  
  protected void countProcessesAfterBenchmark() {
    this.countAfter = countNrOfEndedProcesses();
  }
  
  protected void verifyCounts(int expected) {
    System.out.print("Verifying benchmark by counting executed processes... ");
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
    return processEngine.getHistoryService().createHistoricProcessInstanceQuery().count();
  }

}
