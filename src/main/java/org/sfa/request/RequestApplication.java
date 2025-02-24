package org.sfa.request;

//import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class RequestApplication {
    public static void main(String[] args) {

//         Load .env properties
//        Dotenv dotenv = Dotenv.configure()
//                .directory(System.getProperty("user.dir")) // Set directory to project root to locate .env file
//                .ignoreIfMalformed() // Ignore malformed entries in the .env file
//                .ignoreIfMissing() // Ignore if the .env file is missing
//                .load();
//
//        // Set environment variables to be used by Spring Boot
//        System.setProperty("DB_DEV_HOST_URL", dotenv.get("DB_DEV_HOST_URL", ""));
//        System.setProperty("DB_DEV_USERNAME", dotenv.get("DB_DEV_USERNAME", ""));
//        System.setProperty("DB_DEV_PASSWORD", dotenv.get("DB_DEV_PASSWORD", ""));

        SpringApplication.run(RequestApplication.class, args);
    }
}
