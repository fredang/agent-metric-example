java -javaagent:../agent/target/metric-agent-jar-with-dependencies.jar=graphite.host:localhost,graphite.port:2003,graphite.prefix:test \
	-cp target/agent-test-jar-with-dependencies.jar \
	com.chimpler.example.agentmetric.example.RunExample
