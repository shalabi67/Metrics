# Metrics
This project implements a system that generates operating system metrics and passes the events through Aiven Kafka instance to Aiven PostgreSQL database.


## Notice
- please do not forget to have a look at wiki it has important information: https://github.com/shalabi67/Metrics/wiki
- please have a look at issues. https://github.com/shalabi67/Metrics/issues
- Through this code I did not use branches, and merge request. I thought it is irrelevant for this example.
- Both consumers and producers are running in the same application. They could be separated.
- I am having secure information exposed in source control. this usually will not be done like this. Notice how these information are hidden in docker file.

## Execute application
### Run locally
- start containers by: docker-compose up
- build application: mvn clean install
- run application: java -jar target/metrics-0.0.1-SNAPSHOT.jar

### Run locally using docker
- docker-compose -f docker-compose-test.yml build
- docker-compose -f docker-compose-test.yml up

### Run production
This is exposing secure information. This will be hidden through CI.
- build application: mvn clean install
- run application: java -jar -Dspring.profiles.active=prod target/metrics-0.0.1-SNAPSHOT.jar

### Run production using docker
Notice how security file are hidden through docker file build. 
Even though password information are still exposed for simplicity but they can be hidden in environment variables.

- docker-compose -f docker-compose-prod.yml build
- docker-compose -f docker-compose-prod.yml up

### Send REST POST request
curl -X POST \
  http://localhost:8080/metrics \
  -H 'Accept: */*' \
  -H 'Accept-Encoding: gzip, deflate' \
  -H 'Cache-Control: no-cache' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 70' \
  -H 'Content-Type: application/json' \
  -d '{
	"machineId":111,
	"cpu": 13,
	"metricsDate":"2019-10-12T12:12:00"
}'
