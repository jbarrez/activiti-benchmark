package be.jorambarrez.activiti.benchmark.execution;

import java.util.ArrayList;
import java.util.List;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.interceptor.CommandInterceptor;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import be.jorambarrez.activiti.benchmark.Benchmark;
import be.jorambarrez.activiti.benchmark.profiling.ProfilingInterceptor;

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

			if (Benchmark.PROFILING_ENABLED) {
				List<CommandInterceptor> interceptors = new ArrayList<CommandInterceptor>();
				interceptors.add(new ProfilingInterceptor());
				((ProcessEngineConfigurationImpl) processEngineConfiguration)
						.setCustomPreCommandInterceptorsTxRequired(interceptors);
			}

			return processEngineConfiguration.buildProcessEngine();
		} else if (Benchmark.CONFIGURATION_VALUE.equals("spring")) {
			System.out.println("Using SPRING config");
			APP_CTX = new ClassPathXmlApplicationContext("spring-context.xml");

			if (Benchmark.PROFILING_ENABLED) {
				throw new RuntimeException("Profiling is currently only possible in default configuration");
			}

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
