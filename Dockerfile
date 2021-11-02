FROM tomcat:9.0.45-jdk11-openjdk-buster
EXPOSE 8080

ADD target/*.war /usr/local/tomcat/webapps/
