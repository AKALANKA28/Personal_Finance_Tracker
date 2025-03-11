
### Introduction  

This project is a robust and high-quality software solution designed as a Personal Finance Tracker System. The system aims to empower users to effectively manage their financial records, monitor expenses, set budgets, and analyze spending trends. With a strong emphasis on secure access and data integrity, it provides a user-friendly platform to ensure accurate and reliable financial management.

This documentation serves as a comprehensive guide, covering the setup, usage, and testing of the software. Whether you're a developer or an end-user, it will help you get started quickly and understand the key features and functionalities of the system.

Setup Instructions

### Prerequisites  
Before you begin, ensure you have the following installed on your system:

- Java Development Kit (JDK): Version 11 or higher (e.g., OpenJDK 11+).
- Maven: For managing dependencies and building the project.
- Database: A supported noSQL database system, such as MongoDB.
- Spring Boot CLI (Optional): To help run Spring Boot applications quickly.
- IDE: IntelliJ IDEA (preferred for Spring development), Eclipse, or Visual Studio Code.


### Installation  

1. **Clone the repository:**  

Since this is a **private repository**, you need authentication to clone it. You can use one of the following methods:

  ### **Method: Using HTTPS with Personal Access Token (Recommended)**
  1. **Generate a Personal Access Token (PAT)**:
     - Go to [GitHub → Settings → Developer Settings → Personal Access Tokens](https://github.com/settings/tokens)
     - Click **Generate new token** (Use "Fine-grained tokens" for more control)
     - Select necessary **repository permissions** (at least "read" access)
     - Copy the token (you won't see it again)
  
  2. **Clone the repository using the token**:
     ```sh
     git clone  https://<your-github-username>@github.com/SE1020-IT2070-OOP-DSA-25/<your-repo-name>.git

  3. **Open the project in IntelliJ IDEA**:
     - Launch IntelliJ IDEA.
     - Select Open and navigate to the cloned project directory.
     - IntelliJ will automatically detect the pom.xml file and set up the project as a Maven project.

  4. **Install dependencies**:

      - IntelliJ will automatically download the dependencies specified in the pom.xml file.
      - If not, you can manually trigger the dependency download by running:
          ```sh
          mvn clean install
          
  5. **Configure environment variables**:

      - Create an application.properties or application.yml file in the src/main/resources directory.
      - Add the necessary configuration properties. For example:
        
          properties
          spring.data.mongodb.uri=mongodb+srv://username:password@cluster0.mongodb.net/?retryWrites=true&w=majority
          spring.data.mongodb.database=db_name
        
          jwt.secret=your_jwt_token
          jwt.expiration=expiration_time
        
          server.port=8080 (optional)
        
          exchange.rate.api.key=your_exchange_rate_api_key
          exchange.rate.api.url=https://v6.exchangerate-api.com/

     
  6. **Set up the database**:
     - Ensure your database server (e.g., MongoDB) is running.
     - Create a database with the name specified in the spring.data.mongodb.uri property.
       
  7. **Run the application:**:
    - In IntelliJ, navigate to the main application class (e.g., ProjectApplication.java).
    - Right-click on the class and select Run.
    - Alternatively, you can run the application from the terminal:
     
          mvn spring-boot:run

### Running the Application

Once the application is running, you can access it at:
 - Local URL: http://localhost:8080


