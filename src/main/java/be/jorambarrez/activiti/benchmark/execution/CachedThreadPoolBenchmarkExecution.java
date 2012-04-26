package be.jorambarrez.activiti.benchmark.execution;

import org.activiti.engine.ProcessEngine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadPoolBenchmarkExecution extends ThreadPooledBenchmarkExecution {
	
	public CachedThreadPoolBenchmarkExecution(ProcessEngine processEngine, String[] processes) {
		super(processEngine, processes);
	}
	
	protected ExecutorService getExecutorService() {
		return Executors.newCachedThreadPool();
	}
	
}
