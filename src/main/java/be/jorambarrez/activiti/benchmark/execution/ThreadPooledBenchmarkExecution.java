package be.jorambarrez.activiti.benchmark.execution;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import be.jorambarrez.activiti.benchmark.output.BenchmarkResult;
import be.jorambarrez.activiti.benchmark.util.Utils;

public abstract class ThreadPooledBenchmarkExecution extends BasicBenchmarkExecution {

    protected int nrOfWorkerThreads;

    public ThreadPooledBenchmarkExecution(String[] processes) {
        super(processes);
    }

    protected abstract ExecutorService getExecutorService();

    @Override
    public BenchmarkResult sequentialExecution(String[] processes, int nrOfProcessExecutions, boolean history) {

        countProcessesBeforeBenchmark();
        BenchmarkResult result = new BenchmarkResult(nrOfWorkerThreads);
        
        for (String currentProcess : processes) {
            
            System.out.println(new Date() + " : [SEQ]Starting " + nrOfProcessExecutions + " of process " + currentProcess);
            ExecutorService executorService = getExecutorService();
            ArrayList<ExecuteProcessRunnable> processExecutions = new ArrayList<ExecuteProcessRunnable>(nrOfProcessExecutions);

            long allProcessesStart = System.currentTimeMillis();
            for (int i = 0; i < nrOfProcessExecutions; i++) {
            	ExecuteProcessRunnable executeProcessRunnable = new ExecuteProcessRunnable(currentProcess, ProcessEngineHolder.getInstance());
            	processExecutions.add(executeProcessRunnable);
                executorService.execute(executeProcessRunnable);
            }

            try {
                executorService.shutdown();
                executorService.awaitTermination(1, TimeUnit.HOURS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            long allProcessesEnd = System.currentTimeMillis();
            result.addTotalTimeMeasurementForProcess(currentProcess, nrOfProcessExecutions, allProcessesEnd - allProcessesStart);
            
            System.out.println("Processing process execution durations ...");
            for (ExecuteProcessRunnable processExecution : processExecutions) {
            	result.addProcessInstanceMeasurement(currentProcess, processExecution.getDuration());
            }
        }

        if (history) {
            countProcessesAfterBenchmark();
            verifyCounts(nrOfProcessExecutions * processes.length);
        }

        cleanAndDeploy();
        return result;
    }

    @Override
    public BenchmarkResult randomExecution(String[] processes, int totalNrOfExecutions, boolean history) {
        countProcessesBeforeBenchmark();
        BenchmarkResult result = new BenchmarkResult(nrOfWorkerThreads);
        final String[] randomizedProcesses = Utils.randomArray(processes, totalNrOfExecutions);

        ExecutorService executorService = getExecutorService();
        System.out.println(new Date() + ": [RND]Starting " + totalNrOfExecutions + " random processes");

        long allProcessesStart = System.currentTimeMillis();
        for (int i = 0; i < randomizedProcesses.length; i++) {
        	  executorService.execute(new ExecuteProcessRunnable(randomizedProcesses[i], ProcessEngineHolder.getInstance()));
        }

        try {
            executorService.shutdown();
            executorService.awaitTermination(2, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        long allProcessesEnd = System.currentTimeMillis();
        result.addTotalTimeMeasurementForProcess(Utils.toString(processes), totalNrOfExecutions, allProcessesEnd - allProcessesStart);

        if (history) {
            countProcessesAfterBenchmark();
            verifyCounts(totalNrOfExecutions);
        }
        
        result.setRandomizedProcesses(randomizedProcesses);

        cleanAndDeploy();
        return result;
    }

}
