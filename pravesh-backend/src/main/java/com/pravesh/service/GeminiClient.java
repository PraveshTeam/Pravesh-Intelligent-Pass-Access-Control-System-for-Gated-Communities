package com.pravesh.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    @Value("${gemini.api-key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    @Value("${gemini.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper = new ObjectMapper();

    public String generateReply(String systemPrompt, List<Map<String, String>> history, String userMessage) {
        try {
            String url = baseUrl + "/models/" + model + ":generateContent?key=" + apiKey;

            List<Map<String, Object>> contents = new ArrayList<>();

            for (Map<String, String> turn : history) {
                String role = "assistant".equalsIgnoreCase(turn.get("role")) ? "model" : "user";
                contents.add(Map.of(
                        "role", role,
                        "parts", List.of(Map.of("text", turn.get("text")))
                ));
            }

            contents.add(Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", userMessage))
            ));

            Map<String, Object> body = Map.of(
                    "systemInstruction", Map.of(
                            "parts", List.of(Map.of("text", systemPrompt))
                    ),
                    "contents", contents,
                    "generationConfig", Map.of(
                            "temperature", 0.4,
                            "maxOutputTokens", 400
                    )
            );

            String rawResponse = restClient.post()
                    .uri(url)
                    .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = mapper.readTree(rawResponse);
            JsonNode textNode = root
                    .path("candidates").path(0)
                    .path("content").path("parts").path(0)
                    .path("text");

            if (textNode.isMissingNode()) {
                log.warn("Gemini response had no text — raw: {}", rawResponse);
                return "Sorry, I couldn't come up with a reply just now. Please try rephrasing your question.";
            }

            return textNode.asText().trim();

        } catch (RestClientResponseException e) {
            log.error("Gemini API HTTP error: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            if (e.getStatusCode().value() == 429) {
                return "I'm getting a lot of questions right now — please wait a few seconds and try again.";
            }
            return "The assistant is temporarily unavailable. Please try again in a moment.";

        } catch (Exception e) {
            log.error("Gemini API call failed: {}", e.getMessage(), e);
            return "The assistant is temporarily unavailable. Please try again in a moment.";
        }
    }
}