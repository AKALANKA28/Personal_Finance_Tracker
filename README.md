# Personal Finance Tracker System

## Introduction

This project is a robust and high-quality software solution designed as a Personal Finance Tracker System. The system aims to empower users to effectively manage their financial records, monitor expenses, set budgets, and analyze spending trends. With a strong emphasis on secure access and data integrity, it provides a user-friendly platform to ensure accurate and reliable financial management.

This documentation serves as a comprehensive guide, covering the setup, usage, and testing of the software. Whether you're a developer or an end-user, it will help you get started quickly and understand the key features and functionalities of the system.

## Prerequisites

Before you begin, ensure you have the following installed on your system:

- Java Development Kit (JDK): Version 11 or higher (e.g., OpenJDK 11+)
- Maven: For managing dependencies and building the project
- Database: A supported NoSQL database system, such as MongoDB
- Spring Boot CLI (Optional): To help run Spring Boot applications quickly
- IDE: IntelliJ IDEA (preferred for Spring development), Eclipse, or Visual Studio Code

## Installation

### 1. Clone the repository

Since this is a private repository, you need authentication to clone it. You can use one of the following methods:

#### Method A: Using HTTPS with Personal Access Token

1. Generate a Personal Access Token (PAT):
   - Go to [GitHub → Settings → Developer Settings → Personal Access Tokens](https://github.com/settings/tokens)
   - Click *Generate new token* (Use "Fine-grained tokens" for more control)
   - Select necessary *repository permissions* (at least "read" access)
   - Copy the token (you won't see it again)

2. Clone the repository using the URL:
   ```sh
   git clone https://<your_github_username>:<your_personal_access_token>@github.com/SE1020-IT2070-OOP-DSA-25/project-IT22213730.git
   ```
   When prompted for authentication, use your GitHub username and the personal access token as the password

#### Method B: Clone directly using IDE (Recommended for beginners)

1. Using IntelliJ IDEA:
   - Go to File → New → Project from Version Control
   - Enter the repository URL: `https://github.com/SE1020-IT2070-OOP-DSA-25/project-IT22213730.git`
   - Click Clone
   - When prompted, authenticate with your GitHub credentials or use the IDE's built-in GitHub authentication
   - IntelliJ will handle the authentication and clone the repository automatically

2. Using Eclipse:
   - Go to File → Import → Git → Projects from Git → Clone URI
   - Enter the repository URL: `https://github.com/SE1020-IT2070-OOP-DSA-25/project-IT22213730.git`
   - Enter your GitHub credentials when prompted
   - Follow the wizard to complete the import

3. Using Visual Studio Code:
   - Open VS Code
   - Click on the Source Control icon in the Activity Bar
   - Click "Clone Repository"
   - Enter the repository URL: `https://github.com/SE1020-IT2070-OOP-DSA-25/project-IT22213730.git`
   - Choose a location to store the cloned repository
   - When prompted, authenticate with GitHub through VS Code's authentication flow

### 2. Open the project in IntelliJ IDEA

- Launch IntelliJ IDEA
- Select Open and navigate to the cloned project directory
- IntelliJ will automatically detect the pom.xml file and set up the project as a Maven project

### 3. Install dependencies

- IntelliJ will automatically download the dependencies specified in the pom.xml file
- If not, you can manually trigger the dependency download by running:
  
  ```sh
  mvn clean install
  ```

### 4. Configure environment variables

- Create an application.properties or application.yml file in the src/main/resources directory
- Add the necessary configuration properties. For example:
  
  ```properties
  # MongoDB Configuration
  spring.data.mongodb.uri=mongodb+srv://username:password@cluster0.mongodb.net/?retryWrites=true&w=majority
  spring.data.mongodb.database=db_name

  # JWT Configuration
  jwt.secret=your_jwt_token
  jwt.expiration=expiration_time
  
  # Exchange Rate API Configuration
  exchange.rate.api.key=your_exchange_rate_api_key
  exchange.rate.api.url=https://v6.exchangerate-api.com/
  
  # Server Configuration
  server.port=8080
  
  # Logging Configuration
  logging.level.org.springframework=INFO
  logging.level.com.example=DEBUG
  
  # Actuator Endpoints (for monitoring)
  management.endpoints.web.exposure.include=*
  ```

### 5. Set up the database

- Ensure your database server (e.g., MongoDB) is running
- Create a database with the name specified in the spring.data.mongodb.uri property

### 6. Run the application

- In IntelliJ, navigate to the main application class (e.g., ProjectApplication.java)
- Right-click on the class and select Run
- Alternatively, you can run the application from the terminal:
  
  ```sh
  mvn spring-boot:run
  ```

## Running the Application

Once the application is running, you can access it at:
- Local URL: http://localhost:8080

## API Documentation

This project uses Swagger for API documentation. You can explore the API interactively using the Swagger UI.

### Access Swagger UI
- **Local Environment:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### OpenAPI Specification
The OpenAPI specification is available at:
- **Local Environment:** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Testing

This project includes comprehensive testing to ensure the reliability, security, and performance of the application. The following types of tests are implemented:

### 1. Unit Testing

Unit tests are used to validate the behavior of individual components and functions in isolation.

#### Tools Used:
- **JUnit**: The primary testing framework for Java
- **Mockito**: Used for mocking dependencies in unit tests

#### Running Unit Tests:
To run unit tests, use the following command:

```sh
mvn test
```

### 2. Integration Testing

Integration tests ensure that different parts of the application work together seamlessly, including interactions between controllers, services, and the MongoDB database.

#### Tools Used:
- **Spring Boot Test**: Provides annotations like `@SpringBootTest` and `@DataMongoTest` for integration testing
- **Embedded MongoDB**: Used for testing MongoDB interactions without requiring an external database

### 3. Security Testing

Security tests are performed to identify vulnerabilities within the application, such as SQL injection, cross-site scripting (XSS), and insecure authentication.

#### Tools Used:
- **OWASP ZAP**: An open-source security testing tool for finding vulnerabilities in web applications

#### Steps to Perform Security Testing:
1. **Install OWASP ZAP**:
   - Download and install OWASP ZAP from the official website: [OWASP ZAP](https://www.zaproxy.org/)
   
2. **Run OWASP ZAP**:
   - Start ZAP and configure it to target your application (e.g., http://localhost:8080)
   - Use the Automated Scan or Manual Explore features to identify vulnerabilities

3. **Review Results**:
   - Analyze the scan results and address any identified vulnerabilities

#### Example Vulnerabilities to Test:
- SQL Injection
- Cross-Site Scripting (XSS)
- Insecure Authentication

### 4. Performance Testing

Performance tests evaluate the API's ability to handle multiple requests simultaneously without significant latency.

#### Tools Used:
- **Apache JMeter**: A performance testing tool for load testing and measuring performance

#### Steps to Perform Performance Testing:
1. **Install JMeter**:
   - Download and install Apache JMeter from the official website: [Apache JMeter](https://jmeter.apache.org/)

2. **Create a Test Plan**:
   - Open JMeter and create a new test plan
   - Add a Thread Group to simulate multiple users
   - Add an HTTP Request sampler to target your API endpoints (e.g., http://localhost:8080/api/user/)
   - Add Listeners (e.g., View Results Tree, Summary Report) to view the test results

3. **Run the Test**:
   - Configure the number of threads (users) and ramp-up time
   - Start the test and monitor the results

4. **Analyze Results**:
   - Review metrics such as response time, throughput, and error rate
   - Optimize the application based on the results

#### Example Metrics to Monitor:
- Response Time
- Throughput (requests per second)
- Error Rate

### Running All Tests

To run all tests (unit, integration, security, and performance), follow these steps:

**Unit and Integration Tests**:

In IntelliJ IDEA:
- Right-click on the test class or test method and select **Run**
- Alternatively, you can run all tests in the project by clicking on the **Run** menu and selecting **Run 'All Tests'**
- To run via Maven, use this command in the terminal:
  
  ```sh
  mvn test
  ```
