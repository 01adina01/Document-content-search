package com.example.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Tag(
        name = "Upload API",
        description = "API for uploading PDF and Word documents"
)
@RestController
public class UploadController {

    @Operation(
            summary = "Upload a document",
            description = "Uploads a PDF or Word document, extracts text using Apache Tika, and indexes it into Elasticsearch"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document uploaded and indexed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file type"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public Map<String, Object> upload(
            @Parameter(
                    description = "PDF or Word file to upload",
                    required = true
            )
            @RequestPart("file") MultipartFile file
    ) {
        Map<String, Object> response = new HashMap<>();
        try {
            String filename = file.getOriginalFilename();
            if (filename == null || !(filename.endsWith(".pdf") || filename.endsWith(".doc") || filename.endsWith(".docx"))) {
                response.put("error", "Only PDF or Word files are allowed");
                return response;
            }

            String content = TikaService.extractText(file.getInputStream());
            if (content.trim().isEmpty()) {
                response.put("error", "Unable to extract text from document");
                return response;
            }

            int wordCount = content.split("\\s+").length;
            String fileType = filename.substring(filename.lastIndexOf('.') + 1);

            ElasticService.indexDocument(filename, content, fileType, wordCount);

            response.put("filename", filename);
            response.put("message", "Upload successful");

        } catch (Exception e) {
            e.printStackTrace();
            response.put("error", e.getMessage());
        }
        return response;
    }
}
