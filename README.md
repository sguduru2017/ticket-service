# Simple Ticket Service

A Simple ticket service implementation to hold seats on behalf of customer and final reservation of seats within a high demand performance venue.

## Assumptions

 * Venue capacity is fixed before the service starts hold and reservations
 * A fixed hold interval duration at which all the temporary holds expire
 * SeatHold guarantees the number of seats committed if reserved before the hold time interval expires
 * Multiple holds can be made using same customer email
 
 

## Development environment/tools

    Java 1.8+
    Maven 3.0+ as build tool
    Spring boot, REST used for implementation
    JUnit for running the Unit tests.


# Overview

1. Bootstrap project using spring initializer
   
   Launch spring initializer ([`start.spring.io`])(https://start.spring.io/) and create Group and artifact with the following dependencies
      (a) Web
      (b) Actuator
      (c) DevTool
       
   Additionally springfox dependency needs to be added to pom.xml manually to generate the REST API
       
2. Implemented business service "TicketServiceImpl" with interface provided - TicketService

3. Implement the API using TicketServiceController (GET and POST methods to hold/reserve seats)
4. Write JUnit tests for validating the API


# Building the Project

    1. Clone the project
       got clone https://github.com/sguduru/ticket-service.git
    2. Make JAVA_HOME is set to jdk 1.8 when building the application using command line
    3. cd ticket-service
    4. mvn install -Dmaven.test.skip=true
    5. mvn spring-boot:run (to test using postman or curl)
    
# Test using Postman or Curl

1. Find number of seats offered/available by the Venue.
    
    ```
    GET on http://localhost:8080/ticketservice/status
    ```
    or
    
    ```
    curl http://localhost:8080/ticketservice/status
    ```

2. Find and Hold requested number of seats for customer (using email)

    ```
   POST - http://localhost:8080/ticketservice/holdseats
   ```
   with JSON request body:
   
   ```
   {
     "numSeats": 5,
     "customerEmail": "noname@gmail.com"
   }
   ```
   and response received contains the hold ID which can be later used to reserve seats
   
   ```   
   {
    "numSeats": 5,
    "seatHoldId": 1,
    "customerEmail": "noname@gmail.com",
    "reserveUntil": {
        "nano": 29000000,
        "epochSecond": 1517328146
      }
    }
    ```
    
    or
    
    ```
    curl -H "Content-Type: application/json" -X POST -d '{"numSeats":"5","customerEmail":"noname@gmail.com"}' http://localhost:8080/ticketservice/holdseats/
    ```
    
3. Reserve the hold seats committed for the customer
    ```
   POST - http://localhost:8080/ticketservice/reserveseats
    ```

   with JSON request body:
    ```
   {
      "seatHoldId": 1,
      "customerEmail": "noname@gmail.com"
   }
   ```
   
    or

    ```
    curl -H "Content-Type: application/json" -X POST -d '{"seatHoldId":"1","customerEmail":"noname@gmail.com"}' http://localhost:8080/ticketservice/reserveseats
    ```

# Running JUnit tests

1. Run the following command to run the tests.

    ```
    mvn clean test

    Results :

    Tests run: 6, Failures: 0, Errors: 0, Skipped: 0

    [INFO] ------------------------------------------------------------------------
    [INFO] BUILD SUCCESS
    [INFO] ------------------------------------------------------------------------
    [INFO] Total time: 01:10 min
    [INFO] Finished at: 2018-01-30T12:34:43-05:00
    [INFO] Final Memory: 31M/315M
    [INFO] ------------------------------------------------------------------------
    ```

## RESTful API Documentation

You can view RESTful API as human readable structured documentation with swagger UI by using following link
in web browser

    [`http://localhost:8080/swagger-ui.html`](http://localhost:8080/swagger-ui.html) 
        