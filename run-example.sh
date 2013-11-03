java -javaagent:agent/target/metric-agent-jar-with-dependencies.jar=graphite.host=localhost,graphite.port=2003 \
	-cp agent-example/target/agent-example-0.0.1-SNAPSHOT-jar-with-dependencies.jar com.chimpler.example.metricagent.RunExample
