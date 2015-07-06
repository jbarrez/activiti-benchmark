package be.jorambarrez.activiti.benchmark.execution;

import be.jorambarrez.activiti.benchmark.Benchmark;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ProcessEngineHolder {
	
	private static ProcessEngine PROCESS_ENGINE;
	private static ClassPathXmlApplicationContext APP_CTX;
	
	public static ProcessEngine getInstance() {
		if (PROCESS_ENGINE == null) {
			PROCESS_ENGINE = getProcessEngine();
		}
		return PROCESS_ENGINE;
	}
	
	private static ProcessEngine getProcessEngine() {
		if (Benchmark.CONFIGURATION_VALUE.equals("default")) {
			
			System.out.println("Using DEFAULT config");
			// Not doing the 'official way' here, but it is needed if we want
			// the property resolving to work

			APP_CTX = new ClassPathXmlApplicationContext("activiti.cfg.xml");
			ProcessEngineConfiguration processEngineConfiguration = APP_CTX.getBean(ProcessEngineConfiguration.class);

			System.out.println("[Process engine configuration info]:");
			System.out.println("url : " + processEngineConfiguration.getJdbcUrl());
			System.out.println("driver : " + processEngineConfiguration.getJdbcDriver());

			return processEngineConfiguration.buildProcessEngine();
		} else if (Benchmark.CONFIGURATION_VALUE.equals("spring")) {
			System.out.println("Using SPRING config");
			APP_CTX = new ClassPathXmlApplicationContext("spring-context.xml");
			return APP_CTX.getBean(ProcessEngine.class);
		}
		throw new RuntimeException("Invalid config: only 'default' and 'spring' are supported");
	}
	
	
	/**
	 * @return The rebooted process engine.
	 */
	public static ProcessEngine rebootProcessEngine() {
		if (APP_CTX != null) {
			System.out.println("Shutting down application context");
			APP_CTX.close();
			APP_CTX = null;
		}
		
		PROCESS_ENGINE = getProcessEngine();
		return getInstance();
	}

}
