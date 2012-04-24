package be.jorambarrez.activiti.benchmark.output;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Just a wrapper around some hashmaps, to make the code easier to read.
 *
 * Stores the results of one benchmark run,
 * 
 * @author jbarrez
 */
public class BenchmarkResult {
	 
	private Set<String> processes;
	
	private Map<String, Long> totalTimeMap;
	
	private Map<String, Integer> nrOfExecutionsMap;

    private int nrOfThreads;
	
	public BenchmarkResult(int nrOfThreads) {
        this.nrOfThreads = nrOfThreads;
		this.processes = new TreeSet<String>();
		this.totalTimeMap = new HashMap<String, Long>();
		this.nrOfExecutionsMap = new HashMap<String, Integer>();
	}
	
	public void addProcessMeasurement(String process, 
			Integer nrOfExecutions, Long measurement) {
		processes.add(process);
		totalTimeMap.put(process, measurement);
		nrOfExecutionsMap.put(process, nrOfExecutions);
	}
	
	public Set<String> getProcesses() {
		return processes;
	}
	
	public long getTotalTime(String process) {
		return totalTimeMap.get(process);
	}
	
	public int getNrOfExecutions(String process) {
		return nrOfExecutionsMap.get(process);
	}

    public double getAverage(String process) {
        double average = (double) getTotalTime(process) / (double) getNrOfExecutions(process);
        return Math.round(average * 100.0) / 100.0; // round on 2 nrs
    }

    public int getNrOfThreads() {
        return nrOfThreads;
    }

}
