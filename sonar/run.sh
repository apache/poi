#!/bin/sh

# -s /x1/jenkins/jenkins-master/catalina-base/temp/settings7837234259678677409.xml 
mvn -B -f /workspaces/poi/sonar/pom.xml -Dmaven.repo.local=/workspaces/poi/sonar/repo clean install -U -DskipTests -fae -B && \
mvn -f /workspaces/poi/sonar/pom.xml -e -B org.codehaus.mojo:sonar-maven-plugin:2.6:sonar -Dsonar.jdbc.driver=com.mysql.jdbc.Driver \
	"-Dsonar.jdbc.url=jdbc:mysql://192.168.0.64:3306/sonar?useUnicode=true&characterEncoding=utf8" \
	-Dsonar.host.url=http://localhost:9090 -Dmaven.repo.local=/workspaces/poi/sonar/repo
