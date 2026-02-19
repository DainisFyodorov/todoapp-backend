# ToDoList App - Backend

Backend REST API for a todo list web application.<br><br>
Frontend part of web application can be found here: [link](https://github.com/DainisFyodorov/todoapp-frontend)

## Description
ToDoList App is a web application that allows users to browse and manage their personal tasks.
The backend is implemented using Java and Spring Boot and exposes REST APIs consumed by the frontend application.

## Tech Stack
- Java 21 & Spring Boot 4.0.x
- Spring Security
- Spring Data JPA
- Validation (Hibernate Validator)
- H2 (in-memory database)
- BCrypt (for password encoding)
- JUnit
- Mockito, MockMvc
- REST APIs
- Lombok
- Maven

## Main Features
- User Authentication: registration and authentication with data validation
- Task Management: create, edit, delete and get list of tasks
- Security: access to task management for authenticated users only
- REST API: Clear response structure (JSON) and use of correct HTTP statuses (200, 201, 401, 404)

## Installation
1. Make sure that you have **JDK 21** and **Maven** installed
2. Clone the repository
3. Configure database connection in `application.properties` file
4. Build the project using:
```
mvn clean install
```
6. Run the application using:
```
mvn spring-boot:run
```
API will be available on the following URL: ```http://localhost:8080/```<br>
H2 database console (for debugging): ```http://localhost:8080/h2-console```

**Notice:** for full web application functionality, don't forget to configure and run [the frontend part of the application](https://github.com/DainisFyodorov/todoapp-frontend).

## Architecture
Application follows multi-layer architecture:
1. Controller Layer: Processing HTTP requests and incoming data validation
2. Service Layer: Business logic of application
3. Repository Layer: Database abstraction via Spring Data JPA

## Endpoints
| Method        | Endpoint      | Description   | Access        |
| ------------- | ------------- | ------------- | ------------- |
| POST          | ```/api/auth/register```  | New account registration  | Public  |
| POST          | ```/api/auth/login```     | Authentication            | Public  |
| GET           | ```/api/task/get```       | Get list of tasks         | Secured |
| POST          | ```/api/task/add```       | Create a new task         | Secured |
| PUT           | ```/api/task/update/{id}```       | Update task       | Secured |
| DELETE          | ```/api/task/delete/{id}```     | Delete task       | Secured |

## Status
In active development.
