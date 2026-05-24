# cas-ssdd-2026-project

Project for CAS Secure Software Design &amp; Development at ZHAW School of Engineering.

## TODOs

Design, develop, and secure a full-stack web application using a Java Spring Boot backend and an arbitrary frontend technology.

Implement a variety of security-relevant features, apply best practices in areas such as authentication, authorization, input validation, secure session management, and
protection against common web vulnerabilities.

### Required Features

- API
- Login functionality
- At least two distinct Roles (e.g., users and administrators)
- Secret user data (e.g., password, personal notes, etc.)
- File upload
- Blog / comment functionality (some kind of text input)
- Input-dependent database queries (e.g., search function)

## Project Description

> tbd

## Requirements and Design Considerations

> Specific requirements (functionality) and design considerations

## Getting Started

> [!IMPORTANT]  
> Java 25 is required (see for example https://openjdk.org/install/)

To build and run the project, follow these steps:

1. Clone the repository to your local machine
1. Navigate to the project directory
1. Use the following commands to build and run the application

   ```bash
   .\gradlew build
   .\gradlew bootRun
   ```

Once the application is running, you can access the following URLs:

- http://localhost:8080/h2-console
- http://localhost:8080/api/events
