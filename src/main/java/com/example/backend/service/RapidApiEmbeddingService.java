package com.example.backend.service;

import org.asynchttpclient.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class RapidApiEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(RapidApiEmbeddingService.class);

    @Value("${rapidapi.key}")
    private String rapidApiKey;

    private static final String RAPIDAPI_HOST = "openai-embedding-v3-large.p.rapidapi.com";
    private static final String EMBEDDING_URL = "https://" + RAPIDAPI_HOST + "/embeddings";

    private final AsyncHttpClient client;

    public RapidApiEmbeddingService() {
        this.client = new DefaultAsyncHttpClient();
    }

    public CompletableFuture<List<Double>> generateEmbedding(String inputText) {
        String requestBody = String.format(
                "{\"input\":\"%s\", \"model\":\"text-embedding-3-large\", \"encoding_format\":\"float\"}",
                inputText.replace("\"", "\\\"")
        );

        return client.prepare("POST", EMBEDDING_URL)
                .setHeader("x-rapidapi-key", rapidApiKey)
                .setHeader("x-rapidapi-host", RAPIDAPI_HOST)
                .setHeader("Content-Type", "application/json")
                .setBody(requestBody)
                .execute()
                .toCompletableFuture()
                .thenApply(response -> {
                    try {
                        // Parse JSON response to extract the embedding vector array
                        var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                        Map<?, ?> jsonResponse = mapper.readValue(response.getResponseBody(), Map.class);
                        List<Map<String, Object>> data = (List<Map<String, Object>>) jsonResponse.get("data");
                        return (List<Double>) data.get(0).get("embedding");
                    } catch (IOException e) {
                        logger.error("Error parsing embedding response", e);
                        throw new RuntimeException(e);
                    }
                });
    }

    public void close() throws IOException {
        client.close();
    }
}
