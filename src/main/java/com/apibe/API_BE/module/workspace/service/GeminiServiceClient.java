package com.apibe.API_BE.module.workspace.service;

import com.apibe.API_BE.global.exception.AppException;
import com.apibe.API_BE.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.apibe.API_BE.global.config.GeminiProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("null")
public class GeminiServiceClient {

    private final GeminiProperties geminiProperties;
    private final RestClient geminiRestClient;

    // Helper records for Gemini Request structure
    public record GeminiRequest(
            List<Content> contents,
            SystemInstruction systemInstruction
    ) {
        public record Content(String role, List<Part> parts) {}
        public record Part(String text) {}
        public record SystemInstruction(List<Part> parts) {}
    }

    // Helper classes for Gemini Response structure (using standard JSON properties)
    public static class GeminiResponse {
        @JsonProperty("candidates")
        public List<Candidate> candidates;

        public static class Candidate {
            @JsonProperty("content")
            public Content content;

            public static class Content {
                @JsonProperty("parts")
                public List<Part> parts;
            }
        }

        public static class Part {
            @JsonProperty("text")
            public String text;
        }
    }

    /**
     * Calls Gemini API to generate content.
     */
    public Map<String, Object> generateContent(String mode, List<Map<String, String>> chatHistory, String newMessage) {
        String apiKey = geminiProperties.getApiKey();
        String modelName = geminiProperties.getModel();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST, "Gemini API key is not configured. Please set GEMINI_API_KEY environment variable.");
        }

        try {
            // Build the contents list
            List<GeminiRequest.Content> contents = new ArrayList<>();

            // Add previous history
            for (Map<String, String> msg : chatHistory) {
                String role = msg.get("role");
                String content = msg.get("content");
                if (role == null || content == null) continue;

                // Map 'assistant' role to 'model' for Gemini
                String geminiRole = "assistant".equals(role) ? "model" : "user";
                contents.add(new GeminiRequest.Content(
                        geminiRole,
                        List.of(new GeminiRequest.Part(content))
                ));
            }

            // Add new user message
            contents.add(new GeminiRequest.Content(
                    "user",
                    List.of(new GeminiRequest.Part(newMessage))
            ));

            // Get system instructions based on the mode
            String systemInstructionText = getSystemInstruction(mode);
            GeminiRequest.SystemInstruction systemInstruction = new GeminiRequest.SystemInstruction(
                    List.of(new GeminiRequest.Part(systemInstructionText))
            );

            // Construct payload request
            GeminiRequest requestPayload = new GeminiRequest(contents, systemInstruction);

            String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s", modelName, apiKey);

            // Send request using configured geminiRestClient bean
            GeminiResponse response = geminiRestClient.post()
                    .uri(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(GeminiResponse.class);

            if (response == null || response.candidates == null || response.candidates.isEmpty()) {
                throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Empty response received from Gemini API.");
            }

            // Extract the generated text
            String replyText = response.candidates.get(0).content.parts.get(0).text;

            // Extract metadata (API Request / DB Schema)
            Map<String, Object> metadata = extractMetadata(replyText);

            // Return combined result
            Map<String, Object> result = new HashMap<>();
            result.put("content", replyText);
            result.put("metadata", metadata);
            return result;

        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error calling Gemini API: ", e);
            throw new AppException(ErrorCode.INTERNAL_SERVER_ERROR, "Failed to call Gemini API: " + e.getMessage());
        }
    }

    private String getSystemInstruction(String mode) {
        if (mode == null) mode = "chat";
        return switch (mode.toLowerCase()) {
            case "code" ->
                    "You are ChatDMP, a Senior Software Engineer specializing in code generation. Write clean, optimal, and secure code blocks based on the user's request. Keep explanations brief and focus on the code implementation. Always specify the programming language in code blocks.";
            case "api" ->
                    "You are ChatDMP, a Lead API Architect. Help the user design REST APIs and HTTP requests. For API requests, always include a code block starting with '```http' containing the HTTP method, URL, headers, and request body. For example:\n" +
                            "```http\n" +
                            "POST {{baseUrl}}/auth/login\n" +
                            "Content-Type: application/json\n" +
                            "\n" +
                            "{\n" +
                            "  \"email\": \"user@example.com\",\n" +
                            "  \"password\": \"password\"\n" +
                            "}\n" +
                            "```\n" +
                            "and follow up with the expected JSON response.";
            case "debug" ->
                    "You are ChatDMP, a Senior Debugger. Help the user find bugs, analyze error messages, stack traces, and API responses. Provide step-by-step guidance on how to fix the issue.";
            case "database" ->
                    "You are ChatDMP, a Database Architect. Help the user design database schemas and tables. Always write clean SQL scripts with tables, columns, primary/foreign keys inside a code block starting with '```sql'. For example:\n" +
                            "```sql\n" +
                            "CREATE TABLE users (\n" +
                            "  id UUID PRIMARY KEY,\n" +
                            "  email VARCHAR(255) UNIQUE NOT NULL\n" +
                            ");\n" +
                            "```";
            case "docs" ->
                    "You are ChatDMP, a Technical Writer specializing in API documentation. Help write clean, readable API docs, markdown files, and specs.";
            default ->
                    "You are ChatDMP, an expert AI software developer assistant. Answer questions concisely and clearly with helpful code examples where appropriate.";
        };
    }

    private Map<String, Object> extractMetadata(String replyText) {
        Map<String, Object> metadata = new HashMap<>();

        // 1. Extract API request from ```http code blocks
        Pattern httpPattern = Pattern.compile("```http\\s*\\n(GET|POST|PUT|PATCH|DELETE)\\s+(\\S+)\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
        Matcher httpMatcher = httpPattern.matcher(replyText);
        if (httpMatcher.find()) {
            String method = httpMatcher.group(1).toUpperCase();
            String url = httpMatcher.group(2);
            String rest = httpMatcher.group(3);

            // Parse headers and body
            List<Map<String, String>> headers = new ArrayList<>();
            String body = "";

            String[] lines = rest.split("\\n");
            boolean parsingHeaders = true;
            StringBuilder bodyBuilder = new StringBuilder();

            for (String line : lines) {
                if (parsingHeaders) {
                    if (line.trim().isEmpty()) {
                        parsingHeaders = false;
                    } else if (line.contains(":")) {
                        int colonIdx = line.indexOf(':');
                        String headerKey = line.substring(0, colonIdx).trim();
                        String headerVal = line.substring(colonIdx + 1).trim();
                        headers.add(Map.of("key", headerKey, "value", headerVal));
                    } else {
                        // Not a header line, start body parsing
                        parsingHeaders = false;
                        bodyBuilder.append(line).append("\n");
                    }
                } else {
                    bodyBuilder.append(line).append("\n");
                }
            }
            body = bodyBuilder.toString().trim();

            Map<String, Object> apiRequest = new HashMap<>();
            apiRequest.put("method", method);
            apiRequest.put("url", url);
            apiRequest.put("headers", headers);
            apiRequest.put("body", body);

            metadata.put("apiRequest", apiRequest);
        }

        // 2. Extract database schema from ```sql code blocks
        Pattern sqlPattern = Pattern.compile("```sql\\s*\\n([\\s\\S]*?)```", Pattern.CASE_INSENSITIVE);
        Matcher sqlMatcher = sqlPattern.matcher(replyText);
        if (sqlMatcher.find()) {
            String sqlContent = sqlMatcher.group(1);
            Map<String, Object> dbSchema = parseSqlSchema(sqlContent);
            if (dbSchema != null) {
                metadata.put("databaseSchema", dbSchema);
            }
        }

        return metadata;
    }

    private Map<String, Object> parseSqlSchema(String sql) {
        try {
            Map<String, Object> schema = new HashMap<>();
            schema.put("dbType", "postgresql");

            List<Map<String, Object>> tables = new ArrayList<>();

            // Parse tables
            Pattern tablePattern = Pattern.compile("CREATE\\s+TABLE\\s+(\\w+)\\s*\\(([\\s\\S]*?)\\);", Pattern.CASE_INSENSITIVE);
            Matcher tableMatcher = tablePattern.matcher(sql);

            while (tableMatcher.find()) {
                String tableName = tableMatcher.group(1);
                String columnsText = tableMatcher.group(2);

                Map<String, Object> table = new HashMap<>();
                table.put("name", tableName);

                List<Map<String, Object>> columns = new ArrayList<>();
                String[] colLines = columnsText.split(",");

                for (String line : colLines) {
                    line = line.trim().replaceAll("\\s+", " ");
                    if (line.isEmpty() || line.toUpperCase().startsWith("CONSTRAINT") || line.toUpperCase().startsWith("FOREIGN KEY") || line.toUpperCase().startsWith("PRIMARY KEY (")) {
                        continue;
                    }

                    String[] parts = line.split(" ");
                    if (parts.length >= 2) {
                        String colName = parts[0].replaceAll("`|\"", "");
                        String colType = parts[1].toUpperCase();

                        // Clean types like VARCHAR(255)
                        boolean primaryKey = line.toUpperCase().contains("PRIMARY KEY");
                        boolean nullable = !line.toUpperCase().contains("NOT NULL");
                        boolean unique = line.toUpperCase().contains("UNIQUE");

                        Map<String, Object> column = new HashMap<>();
                        column.put("name", colName);
                        column.put("type", colType);
                        column.put("primaryKey", primaryKey);
                        column.put("nullable", nullable);
                        column.put("unique", unique);

                        columns.add(column);
                    }
                }
                table.put("columns", columns);
                tables.add(table);
            }

            if (!tables.isEmpty()) {
                schema.put("tables", tables);
                return schema;
            }
        } catch (Exception e) {
            log.warn("Failed to parse SQL schema: ", e);
        }
        return null;
    }
}
