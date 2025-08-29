package com.vit;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

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
            // -------------------------
            // Step 1: Generate fresh webhook + JWT token
            // -------------------------
            String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

            Map<String, String> generateRequest = new HashMap<>();
            generateRequest.put("name", "Deeptanshu Bhattacharya");
            generateRequest.put("regNo", "22BLC1244");
            generateRequest.put("email", "deeptanshu.b2022@vitstudent.ac.in");

            ResponseEntity<Map> generateResponse = restTemplate.postForEntity(generateUrl, generateRequest, Map.class);

            if (generateResponse.getStatusCode() != HttpStatus.OK || generateResponse.getBody() == null) {
                throw new RuntimeException("Failed to generate webhook/token");
            }

            String webhookUrl = (String) generateResponse.getBody().get("webhook");
            String accessToken = ((String) generateResponse.getBody().get("accessToken")).trim();

            System.out.println("Webhook URL: " + webhookUrl);
            System.out.println("Access Token: " + accessToken);

            // -------------------------
            // Step 2: Solve SQL problem based on regNo last digit
            // -------------------------
            char lastDigit = '4'; // From "22BLC1244"
            String finalQuery;

            if (Character.getNumericValue(lastDigit) % 2 == 0) {
                // Even → Question 2 (replace with actual SQL query)
                finalQuery =
                        "SELECT e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME, " +
                        "COUNT(e2.EMP_ID) AS YOUNGER_EMPLOYEES_COUNT " +
                        "FROM EMPLOYEE e1 " +
                        "JOIN DEPARTMENT d ON e1.DEPARTMENT = d.DEPARTMENT_ID " +
                        "LEFT JOIN EMPLOYEE e2 ON e1.DEPARTMENT = e2.DEPARTMENT " +
                        "AND e2.DOB > e1.DOB " +
                        "GROUP BY e1.EMP_ID, e1.FIRST_NAME, e1.LAST_NAME, d.DEPARTMENT_NAME " +
                        "ORDER BY e1.EMP_ID DESC";
            } else {
                // Odd → Question 1 (replace with actual SQL query)
                finalQuery =
                        "SELECT * FROM YOUR_TABLE_WHERE_CONDITION"; // Placeholder
            }

            // -------------------------
            // Step 3: Submit solution to webhook
            // -------------------------
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + accessToken);

            Map<String, String> queryBody = new HashMap<>();
            queryBody.put("finalQuery", finalQuery); // ONLY finalQuery

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(queryBody, headers);

            ResponseEntity<String> submitResponse =
                restTemplate.postForEntity(webhookUrl, entity, String.class);


        } catch (Exception e) {
            System.err.println("Error during webhook execution:");
            e.printStackTrace();
        }
    }
}
