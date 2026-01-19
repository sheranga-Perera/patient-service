# patient-service

Patient management system


Tech Stack

\- Spring Boot 3

\- JPA / Hibernate

\- PostgreSQL

\- React

\- OpenAPI (Swagger)



How to Run



Prerequisites

\- Java 21

\- PostgreSQL

\- Node.js (only for build)



Steps

1\. Configure database in `application.yml`

2\. Build the project:

&nbsp;  mvn clean package

3\. Run:

&nbsp;  java -jar target/patient-0.0.1-SNAPSHOT.jar



URLs

\- API: http://localhost:8080/patient

\- Swagger: http://localhost:8080/swagger-ui.html

\- Dashboard: http://localhost:8080/dashboard



Tests

Run:

mvn test

