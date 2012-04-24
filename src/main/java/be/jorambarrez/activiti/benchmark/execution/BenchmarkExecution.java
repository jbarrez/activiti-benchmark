package be.jorambarrez.activiti.benchmark.execution;

import be.jorambarrez.activiti.benchmark.output.BenchmarkResult;

/**
 * Interface for running a benchmark for process executions.
 * 
 * Concrete implementations can change the way the processes are executed: using
 * a threadpool, caching and other parameters.
 * 
 * This interfaces requires the implementation of a sequential and random
 * execution of the processes.
 * 
 * @author jbarrez
 */
public interface BenchmarkExecution {

  /**
   * Sequentially execute the given processes. Each process is executed
   * <i>nrOfProcessExecutions</i> times, before the next process is executed.
   */
  BenchmarkResult sequentialExecution(String[] processes, int nrOfProcessExecutions, boolean history);

  /**
   * Execute the processes in a randomly way. Processes will be selected and
   * executed randomly, until <i>totalNrOfExecutions</i> executions have been
   * run.
   */
  BenchmarkResult randomExecution(String[] processes, int totalNrOfExecutions, boolean history);

}
