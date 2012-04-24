package be.jorambarrez.activiti.benchmark.execution;

import org.activiti.engine.ProcessEngine;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CachedThreadPoolBenchmarkExecution extends ThreadPooledBenchmarkExecution {
	
	public CachedThreadPoolBenchmarkExecution(ProcessEngine processEngine) {
		super(processEngine);
	}
	
	protected ExecutorService getExecutorService() {
		return Executors.newCachedThreadPool();
	}
	
}
