package com.vit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class WebhookApp implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(WebhookApp.class, args);
    }

    @Override
    public void run(String... args) {
        try {
            // Step 1: Generate webhook and token
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";
            
            Map<String, String> generateRequest = new HashMap<>();
            generateRequest.put("name", "Deeptanshu Bhattacharya");
            generateRequest.put("regNo", "22BLC1244");
            generateRequest.put("email", "deeptanshu.b2022@vitstudent.ac.in");
            
            HttpHeaders generateHeaders = new HttpHeaders();
            generateHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> generateEntity = new HttpEntity<>(generateRequest, generateHeaders);
            
            System.out.println("Generating webhook...");
            ResponseEntity<Map> generateResponse = restTemplate.postForEntity(generateUrl, generateEntity, Map.class);
            
            if (generateResponse.getStatusCode() != HttpStatus.OK || generateResponse.getBody() == null) {
                throw new RuntimeException("Failed to generate webhook/token. Status: " + generateResponse.getStatusCode());
            }
            
            Map<String, Object> responseBody = generateResponse.getBody();
            String webhookUrl = (String) responseBody.get("webhook");
            String accessToken = (String) responseBody.get("accessToken");
            
            if (webhookUrl == null || accessToken == null) {
                throw new RuntimeException("Webhook URL or access token is null in response");
            }
            
            System.out.println("Generated successfully!");
            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken.substring(0, 50) + "...");
            
            // Step 2: Prepare the SQL query
            // Since regNo "22BLC1244" ends with 44 (even), using Question 2 solution
            String finalQuery = 
                "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                "FROM EMPLOYEE e1 " +
                "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT AND e2.DOB > e1.DOB " +
                "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                "ORDER BY e1.EMP_ID DESC";
            
            // Step 3: Submit the solution
            HttpHeaders submitHeaders = new HttpHeaders();
            submitHeaders.setContentType(MediaType.APPLICATION_JSON);
            submitHeaders.set("Authorization", accessToken); // Use the full token as received
            
            Map<String, String> submitRequest = new HashMap<>();
            submitRequest.put("finalQuery", finalQuery);
            
            HttpEntity<Map<String, String>> submitEntity = new HttpEntity<>(submitRequest, submitHeaders);
            
            System.out.println("Submitting solution...");
            System.out.println("Using webhook URL: " + webhookUrl);
            
            ResponseEntity<String> submitResponse = restTemplate.postForEntity(webhookUrl, submitEntity, String.class);
            
            System.out.println("Submission successful!");
            System.out.println("Response Status: " + submitResponse.getStatusCode());
            System.out.println("Response Body: " + submitResponse.getBody());
            
        } catch (HttpClientErrorException e) {
            System.err.println("Client Error (4xx): " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
            System.err.println("Headers: " + e.getResponseHeaders());
        } catch (HttpServerErrorException e) {
            System.err.println("Server Error (5xx): " + e.getStatusCode());
            System.err.println("Response Body: " + e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Error during webhook execution:");
            e.printStackTrace();
        }
    }
}
