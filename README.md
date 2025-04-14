# AWS Serverless Spring Boot Application

This project demonstrates how to create a serverless Spring Boot application using AWS Lambda and the `aws-serverless-java-container` plugin. It allows you to run traditional Spring Boot controllers in a serverless environment.

## Overview

The application uses the `aws-serverless-java-container` plugin to wrap your Spring Boot application into a Lambda function. This enables you to:
- Use traditional Spring Boot controllers and annotations
- Maintain the familiar Spring Boot development experience
- Deploy your application as a serverless function
- Scale automatically based on demand

## Database Configuration

The application uses PostgreSQL as its database. The database configuration is managed through environment variables in the Lambda function. Here are the required environment variables:

- `DB_URL`: The JDBC URL for your PostgreSQL database
- `DB_USERNAME`: Database username
- `DB_PASSWORD`: Database password

These variables are configured in the `application.properties` file and will be replaced with actual values from the Lambda environment variables.

## Building and Deployment

### 1. Building the Application

To build the application JAR file (skipping tests):

```bash
mvn clean package -DskipTests
```

This will create a JAR file in the `target` directory.

### 2. Uploading to S3

1. Create an S3 bucket (if you don't have one already)
2. Upload the JAR file to S3:
   ```bash
   aws s3 cp target/your-application.jar s3://your-bucket-name/
   ```

### 3. Creating the Lambda Function

1. Go to AWS Lambda Console
2. Click "Create function"
3. Choose "Author from scratch"
4. Configure the function:
   - Runtime: Java 17
   - Architecture: x86_64 or arm64
   - Handler: org.sfa.request.lambdahandler.LambdaHandler::handleRequest
   - Code source: Upload from S3 (select your uploaded JAR)
5. Set environment variables:
   - `DB_URL`: Your PostgreSQL JDBC URL
   - `DB_USERNAME`: Database username
   - `DB_PASSWORD`: Database password
6. Configure the function's execution role with appropriate permissions
7. Set memory and timeout values according to your needs
8. Enable SnapStart for better performance:
   - In the function configuration, go to "SnapStart" section
   - Click "Enable SnapStart"
   - Create a published version:
     - Click "Publish new version"
     - Add a description (e.g., "Initial version with SnapStart")
     - Click "Publish"

### 4. Optimizing with SnapStart

SnapStart is a free feature for Java Lambda functions that significantly reduces cold start latency by:
- Taking a snapshot of the initialized execution environment
- Restoring from the snapshot for subsequent invocations

To get the best performance with SnapStart:
1. Always use published versions and aliases
2. Configure your function with sufficient memory (recommended: 1024MB or more)
3. Monitor the function's performance in CloudWatch

### 5. Testing the Lambda Function

1. Create a test event in the Lambda console
2. Use the API Gateway template or create a custom test event
3. Run the test and verify the response

## Project Structure

- `src/main/java`: Contains your Spring Boot application code
- `src/main/resources`: Contains configuration files
- `pom.xml`: Maven configuration with AWS dependencies
- `application.properties`: Application configuration with environment variables

## Dependencies

The project uses:
- Spring Boot
- AWS Serverless Java Container
- PostgreSQL JDBC Driver
- Spring Data JPA

## License

This project is licensed under the terms specified in the LICENSE file. 