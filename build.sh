# have some issue with running assembly:single on the parent pom
# so run it individually on the two submodules
(cd agent ; mvn clean package install assembly:single)
(cd agent-test ; mvn clean package install assembly:single)

