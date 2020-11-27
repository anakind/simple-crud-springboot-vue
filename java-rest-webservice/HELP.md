# Read Me First
Java backend rest api services with spring boot and elasticsearch for data persistence

* make sure mvn and java8 is installed on the machine
* make sure to start the elasticsearch before executing jar
* default server is running at localhost, default port is 8102

# Compile, Run, and Test
### Compile with unit testing
* $ cd java-rest-webservice
* $ mvn clean install

### Compile without unit testing
* $ cd java-rest-webservice
* $ mvn clean install -DskipTests

### Run jar
* rest-webservice-0.0.1-SNAPSHOT.jar should be available under target folder after compile
* $ java -jar rest-webservice-0.0.1-SNAPSHOT.jar

### Test the REST API
* server should be running on http://localhost:8102
* follow the documentation below, try to make a rest request from cURL or any REST client(eg. Insomnia)

# REST API Documentation
For REST API documentation, please see the following like AFTER server is up and running

* [Click to see documentation](http://localhost:8102//swagger-ui.html#/contact-controller)