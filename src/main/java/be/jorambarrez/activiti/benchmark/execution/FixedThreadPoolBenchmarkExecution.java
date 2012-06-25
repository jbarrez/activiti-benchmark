package be.jorambarrez.activiti.benchmark.execution;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FixedThreadPoolBenchmarkExecution extends ThreadPooledBenchmarkExecution {
	
	public FixedThreadPoolBenchmarkExecution(int nrOfWorkerThreads, String[] processes) {
		super(processes);
		this.nrOfWorkerThreads = nrOfWorkerThreads;		
	}
	
	protected ExecutorService getExecutorService() {
		return Executors.newFixedThreadPool(nrOfWorkerThreads);
	}

}
