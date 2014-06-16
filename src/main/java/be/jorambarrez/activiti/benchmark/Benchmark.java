package be.jorambarrez.activiti.benchmark;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import be.jorambarrez.activiti.benchmark.execution.BasicBenchmarkExecution;
import be.jorambarrez.activiti.benchmark.execution.BenchmarkExecution;
import be.jorambarrez.activiti.benchmark.execution.FixedThreadPoolBenchmarkExecution;
import be.jorambarrez.activiti.benchmark.execution.ProcessEngineHolder;
import be.jorambarrez.activiti.benchmark.output.BenchmarkOuput;
import be.jorambarrez.activiti.benchmark.output.BenchmarkResult;
import be.jorambarrez.activiti.benchmark.profiling.ProfilingInterceptor;
import be.jorambarrez.activiti.benchmark.profiling.ProfilingLogParser;

/**
 * Main class that contains the logic to execute the benchmark.
 * 
 * @author jbarrez
 */
public class Benchmark {

	public static String[] PROCESSES = { 
		"process01", 
		"process02",
		"process03",
		"process04",
		"process05",
		"process-usertask-01",
		"process-usertask-02",
		"process-usertask-03",
		"process-multi-instance-01"
	};

	private static int maxNrOfThreadsInThreadPool;

	public static String HISTORY_VALUE;
	public static boolean HISTORY_ENABLED;
	public static String CONFIGURATION_VALUE;
	public static boolean PROFILING_ENABLED;

	private static List<BenchmarkResult> fixedPoolSequentialResults = new ArrayList<BenchmarkResult>();
	private static List<BenchmarkResult> fixedPoolRandomResults = new ArrayList<BenchmarkResult>();

	public static void main(String[] args) throws Exception {

		long start = System.currentTimeMillis();

		if (!readAndValidateParams(args)) {
			System.exit(1);
		}

		Benchmark.HISTORY_ENABLED = !HISTORY_VALUE.equals("none");
		System.out.println("History enabled : " + HISTORY_ENABLED);

		int nrOfExecutions = Integer.valueOf(args[0]);
		maxNrOfThreadsInThreadPool = Integer.valueOf(args[1]);

		executeBenchmarks(nrOfExecutions, maxNrOfThreadsInThreadPool);
		writeHtmlReport();

		if (PROFILING_ENABLED) {
			System.out.println();
			System.out.println("Generating profile report");
			System.out.println();
			
			// flushing writer and sleeping a bit, just to be sure
			ProfilingInterceptor.fileWriter.flush();
			Thread.sleep(5000L);
			
			ProfilingLogParser profilingLogParser = new ProfilingLogParser();
			profilingLogParser.execute();
		}

		System.out.println("Benchmark completed. Ran for "
				+ ((System.currentTimeMillis() - start) / 1000L) + " seconds");
	}

	private static void executeBenchmarks(int nrOfProcessExecutions, int maxNrOfThreadsInThreadPool) {

		// Deploy test processes
		System.out.println("Deploying test processes");
		for (String process : PROCESSES) {
			ProcessEngineHolder.getInstance().getRepositoryService().createDeployment()
					.addClasspathResource(process + ".bpmn20.xml").deploy();
		}
		System.out.println("Finished deploying test processes");

		// Single thread benchmark
		System.out.println(new Date() + " - benchmarking with one thread.");
		BenchmarkExecution singleThreadBenchmark = new BasicBenchmarkExecution(PROCESSES);
		fixedPoolSequentialResults.add(singleThreadBenchmark.sequentialExecution(PROCESSES, nrOfProcessExecutions, HISTORY_ENABLED));
		fixedPoolRandomResults.add(singleThreadBenchmark.randomExecution(PROCESSES, nrOfProcessExecutions, HISTORY_ENABLED));

		// Multiple threads - fixed pool benchmark
		for (int nrOfWorkerThreads = 2; nrOfWorkerThreads <= maxNrOfThreadsInThreadPool; nrOfWorkerThreads++) {

			System.out.println(new Date() + " - benchmarking with fixed threadpool of " + nrOfWorkerThreads + " threads.");
			BenchmarkExecution fixedPoolBenchMark = new FixedThreadPoolBenchmarkExecution(nrOfWorkerThreads, PROCESSES);
			fixedPoolSequentialResults.add(fixedPoolBenchMark.sequentialExecution(PROCESSES, nrOfProcessExecutions, HISTORY_ENABLED));
			fixedPoolRandomResults.add(fixedPoolBenchMark.randomExecution(PROCESSES, nrOfProcessExecutions, HISTORY_ENABLED));
		}
	}

	private static void writeHtmlReport() {
		BenchmarkOuput output = new BenchmarkOuput();
		output.start("Activiti 5.10-SNAPSHOT basic benchmark results");

		for (int i = 1; i <= maxNrOfThreadsInThreadPool; i++) {
			output.addBenchmarkResult("Fixed thread pool (" + i + "threads), sequential", fixedPoolSequentialResults.get(i - 1));
		}
		output.generateChartOfPreviousAddedBenchmarkResults(false);

		for (int i = 1; i <= maxNrOfThreadsInThreadPool; i++) {
			output.addBenchmarkResult("Fixed thread pool (" + i + "threads), randomized", fixedPoolRandomResults.get(i - 1));
		}
		output.generateChartOfPreviousAddedBenchmarkResults(true);

		output.writeOut();
	}

	/**
	 * Validates the commandline arguments.
	 * 
	 * @return True if they are OK
	 */
	private static boolean readAndValidateParams(String[] args) {
		// length check
		if (args.length != 2) {
			System.err.println();
			System.err.println("Wrong number of arguments");
			System.err.println();
			System.err.println("Usage: java -Xms512M -Xmx1024M -Dhistory={none|activity|audit|full} -Dconfig={default|spring} -Dprofiling -D{jdbcOption}={jdbcValue} -jar activiti-basic-benchmark.jar "
							+ "<nr_of_executions> <max_nr_of_threads_in_threadpool> ");
			System.err.println();
			System.err.println("Options:");
			System.err.println("-DjdbcUrl={value}");
			System.err.println("-DjdbcDriver={value}");
			System.err.println("-DjdbcUsername={value}");
			System.err.println("-DjdbcPassword={value}");
			System.err.println("-DjdbcMaxActiveConnections={value}");
			System.err.println("-DjdbcMaxIdleConnections={value}");
			System.err.println();
			System.err.println();
			return false;
		}

		if (!System.getProperties().containsKey("history")) {
			System.out
					.println("No history config specified, using default value 'audit");
			System.getProperties().put("history", "audit");
		}
		HISTORY_VALUE = (String) System.getProperties().get("history");

		if (!System.getProperties().containsKey("config")) {
			System.out.println("No config specified, using default");
			System.getProperties().put("config", "default");
		}
		CONFIGURATION_VALUE = (String) System.getProperties().get("config");

		if (CONFIGURATION_VALUE != null && !CONFIGURATION_VALUE.equals("default")
				&& !CONFIGURATION_VALUE.equals("spring")) {
			System.err.println("Invalid configuration option: only default|spring are currently supported");
			return false;
		}

		if (HISTORY_VALUE != null && !HISTORY_VALUE.equals("none")
				&& !HISTORY_VALUE.equals("activity")
				&& !HISTORY_VALUE.equals("audit")
				&& !HISTORY_VALUE.equals("full")) {
			System.err.println("Invalid history option: only none|activity|audit|full are currently supported");
			return false;
		}

		if (System.getProperties().containsKey("profiling")) {
			PROFILING_ENABLED = true;
		}

		try {
			Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("Wrong argument type for nr_of_executions: use an integer");
			return false;
		}

		try {
			Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.err.println("Wrong argument type for max_nr_of_threads_in_threadpool: use an integer");
			return false;
		}

		return true;
	}

}
