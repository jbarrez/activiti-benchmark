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

    private int nrOfThreads;
	private Set<String> processes;
	private Map<String, Long> totalTimeMap;
	private Map<String, Integer> nrOfExecutionsMap;

    private boolean processesRandomized;
    private Map<String, Integer> randomizedProcessesExecutionCounts;
	
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

    public double getThroughputPerSecond(String process) {
        double throughput = 1000.0 / getAverage(process);
        return Math.round(throughput * 100.0) / 100.0; // round on 2 nrs
    }

    public double getThroughputPerHour(String process) {
        double throughput = 3600000.0 / getAverage(process);
        return Math.round(throughput * 100.0) / 100.0; // round on 2 nrs
    }

    public int getNrOfThreads() {
        return nrOfThreads;
    }

    public boolean processesRandomized() {
        return processesRandomized;
    }

    public void setRandomizedProcesses(String[] processes) {
        processesRandomized = true;
        randomizedProcessesExecutionCounts = new HashMap<String, Integer>();

        for (String process : processes) {
            if (!randomizedProcessesExecutionCounts.containsKey(process)) {
                randomizedProcessesExecutionCounts.put(process, 0);
            }
            randomizedProcessesExecutionCounts.put(process, randomizedProcessesExecutionCounts.get(process) + 1);
        }
    }

    public Map<String, Integer> getRandomizedProcessesExecutionCounts() {
        return randomizedProcessesExecutionCounts;
    }

}
