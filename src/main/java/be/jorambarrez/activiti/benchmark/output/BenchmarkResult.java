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
	private Map<String, Long> processInstanceTimingsMap;
	private Map<String, Long> processtotalTimeMap;
	private Map<String, Integer> nrOfExecutionsMap;

    private boolean processesRandomized;
    private Map<String, Integer> randomizedProcessesExecutionCounts;
	
	public BenchmarkResult(int nrOfThreads) {
        this.nrOfThreads = nrOfThreads;
		this.processes = new TreeSet<String>();
		this.processInstanceTimingsMap = new HashMap<String, Long>();
		this.processtotalTimeMap = new HashMap<String, Long>();
		this.nrOfExecutionsMap = new HashMap<String, Integer>();
	}
	
	public void addProcessInstanceMeasurement(String process, Long measurement) {
		if (!processInstanceTimingsMap.containsKey(process)) {
			processInstanceTimingsMap.put(process, measurement);
		}
		Long oldValue = processInstanceTimingsMap.get(process);
		processInstanceTimingsMap.put(process, oldValue + measurement);
	}
	
	public void addTotalTimeMeasurementForProcess(String process, Integer nrOfExecutions, Long measurement) {
		processes.add(process);
		processtotalTimeMap.put(process, measurement);
		nrOfExecutionsMap.put(process, nrOfExecutions);
	}
	
	public Set<String> getProcesses() {
		return processes;
	}
	
	public long getTotalTime(String process) {
		return processtotalTimeMap.get(process);
	}
	
	public int getNrOfExecutions(String process) {
		return nrOfExecutionsMap.get(process);
	}

    public double getAverage(String process) {
    	if (!processInstanceTimingsMap.containsKey(process)) {
    		return -1;
    	}
    	
    	double average = (double) processInstanceTimingsMap.get(process) / (double) getNrOfExecutions(process);
        return Math.round(average * 100.0) / 100.0; // round on 2 nrs
    }

    public double getThroughputPerSecond(String process) {
        double throughput = getNrOfExecutions(process) / ((double) processtotalTimeMap.get(process) / 1000.0);
        return Math.round(throughput * 100.0) / 100.0; // round on 2 nrs
    }

    public double getThroughputPerHour(String process) {
    	double throughput = getNrOfExecutions(process) / ((double) processtotalTimeMap.get(process) / 3600000.0);
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
