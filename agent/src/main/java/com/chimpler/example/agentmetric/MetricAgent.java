package com.chimpler.example.agentmetric;

import java.lang.instrument.Instrumentation;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricAgent {
	private final static Logger logger = LoggerFactory.getLogger(MetricAgent.class);

    public static void premain(String agentArguments, Instrumentation instrumentation) {
    	RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
    	logger.info("Runtime: {}: {}", runtimeMxBean.getName(), runtimeMxBean.getInputArguments());
        logger.info("Starting agent with arguments " + agentArguments);

    	MetricReporter.startJmxReporter();

    	if (agentArguments != null) {
        	// parse the arguments:
        	// graphite.host=localhost,graphite.port=2003
        	Map<String, String> properties = new HashMap<String, String>();
        	for(String propertyAndValue: agentArguments.split(",")) {
        		String[] tokens = propertyAndValue.split(":", 2);
        		if (tokens.length != 2) {
        			continue;
        		}
        		properties.put(tokens[0], tokens[1]);
        		
        	}
        	
    		String graphiteHost = properties.get("graphite.host");
        	if (graphiteHost != null) {
        		int graphitePort = 2003;
        		String graphitePrefix = properties.get("graphite.prefix");
        		if (graphitePrefix == null) {
        			graphitePrefix = "test";
        		}
        		String graphitePortString = properties.get("graphite.port");
        		if (graphitePortString != null) {
	        		try {
	        			graphitePort = Integer.parseInt(graphitePortString);
	        		} catch (Exception e) {
	        			logger.info("Invalid graphite port {}: {}", e.getMessage());
	        		}
        		}
        		MetricReporter.startGraphiteReporter(graphiteHost, graphitePort, graphitePrefix);
        	}
        }
    	
    	// define the class transformer to use
        instrumentation.addTransformer(new TimedClassTransformer());
    }
}