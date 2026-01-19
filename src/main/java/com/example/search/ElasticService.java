package com.example.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.*;

public class ElasticService {

    private static final String ELASTIC_URL = "http://localhost:9200/documents/_doc/";

    public static void indexDocument(String filename, String content, String fileType, int wordCount) throws Exception {
        Map<String, Object> doc = new HashMap<>();
        doc.put("filename", filename);
        doc.put("content", content);
        doc.put("file_type", fileType);
        doc.put("word_count", wordCount);
        doc.put("upload_date", Instant.now().toString());

        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(doc, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(ELASTIC_URL + UUID.randomUUID(), entity, String.class);
        System.out.println("Elastic response: " + response.getBody());
    }

    public static List<Map<String, String>> searchWithHighlight(String term) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String[] terms = term.trim().split("\\s+");
        StringBuilder shouldClauses = new StringBuilder();
        for(String t : terms){
            shouldClauses.append("{\"wildcard\":{\"content\":{\"value\":\"*")
                    .append(t.toLowerCase())
                    .append("*\",\"case_insensitive\":true}}},");
        }
        if(shouldClauses.length() > 0) shouldClauses.setLength(shouldClauses.length() - 1);

        String searchJson = "{\n" +
                "  \"query\": {\n" +
                "    \"bool\": {\n" +
                "      \"should\": [" + shouldClauses.toString() + "]\n" +
                "    }\n" +
                "  },\n" +
                "  \"highlight\": {\n" +
                "    \"pre_tags\": [\"<mark>\"],\n" +
                "    \"post_tags\": [\"</mark>\"],\n" +
                "    \"fields\": { \"content\": {} },\n" +
                "    \"number_of_fragments\": 1\n" +
                "  }\n" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(searchJson, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(
                "http://localhost:9200/documents/_search",
                entity,
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resp.getBody());
        List<Map<String, String>> results = new ArrayList<>();

        for(JsonNode hit : root.path("hits").path("hits")){
            JsonNode hl = hit.path("highlight").path("content");
            if(hl.isArray() && hl.size() > 0){
                Map<String,String> map = new HashMap<>();
                map.put("id", hit.path("_id").asText());
                map.put("highlight", hl.get(0).asText());
                results.add(map);
            }
        }
        return results;
    }


    public static List<Map<String, String>> getAllDocuments() throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        String searchJson = "{\n" +
                "  \"query\": { \"match_all\": {} },\n" +
                "  \"_source\": [\"filename\"]\n" +
                "}";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(searchJson, headers);

        ResponseEntity<String> resp = restTemplate.postForEntity(
                "http://localhost:9200/documents/_search",
                entity,
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resp.getBody());
        List<Map<String, String>> docs = new ArrayList<>();

        for (JsonNode hit : root.path("hits").path("hits")) {
            Map<String, String> doc = new HashMap<>();
            doc.put("id", hit.path("_id").asText());
            doc.put("filename", hit.path("_source").path("filename").asText());
            docs.add(doc);
        }
        return docs;
    }

    public static String getDocumentContent(String id) throws Exception {
        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> resp = restTemplate.getForEntity(
                "http://localhost:9200/documents/_doc/" + id,
                String.class
        );

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(resp.getBody());

        return root.path("_source").path("content").asText();
    }

    public static String deleteDocument(String id) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> resp = restTemplate.exchange(
                "http://localhost:9200/documents/_doc/" + id,
                HttpMethod.DELETE,
                null,
                String.class
        );
        return resp.getBody();
    }
}
