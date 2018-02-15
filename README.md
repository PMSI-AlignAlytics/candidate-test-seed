
# Candidate Test

This is a simple REST API, built using Java Spring 

## Development Prerequisites

1.  [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) - Java Development Kit
2.  [STS](https://spring.io/tools/sts/all) - Spring Tool Suite (Eclipse IDE)
3.  [Docker CE](https://store.docker.com/search?type=edition&offering=community) - Docker Community Edition

## Development Setup

Launch STS and open the code folder.  There will be errors, please follow the following steps to prepare the IDE:

1.  Run `docker-compose up -d es-candidate-test` from the jaws repository to get the elastic container up and running.
2.  Within STS go to `Help > Eclipse Marketplace` and find and install "Buildship Gradle Integration 2.0"
3.  Right-click the root in the project explorer and select `Configure > Add Gradle Nature`
4.  Once complete, right-click the root project again and select `Gradle > Refresh Gradle Project`

## Launching the Application from STS and setting up test data

Once the development environment is prepared the application can be launched from STS:

1.  Right-click the root folder and select 'Run As > Spring Boot Application'.
2.  The console will open and show a few lines of build report and finally a line similar to the following show that the application is ready to test on `localhost:8080`:

`2018-02-15 13:58:32.543  INFO 13556 --- [           main] candidatetest.main.Application           : Started Application in 5.659 seconds (JVM running for 6.509)`

## Testing the Application

The application will prepare all required indices in ElasticSearch and populate an administrator user which you may use to interact with the API initially.  The credentials for this user are:

    User: administrator
    Pass: @dministr8or

This user should be used to call a special end point for building development `POST /api/development` data.  This can be done with the following cURL command (or equivilant):
```
curl -X POST \
  http://localhost:8080/api/development/ \
  -H 'accept: application/json' \
  -H 'authorization: Basic YWRtaW5pc3RyYXRvcjpAZG1pbmlzdHI4b3I=' 
```

This will populate the Users mapping with 3 additional users.  You can access the details for any of these users with their user ids ("jbloggs", "aother", "jdoe").  E.g:
```
curl -X GET \
  http://localhost:8080/api/user/jbloggs \
  -H 'Accept: application/json' \
  -H 'Authorization: Basic YWRtaW5pc3RyYXRvcjpAZG1pbmlzdHI4b3I='
```



