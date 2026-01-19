package com.example.search;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Tag(
        name = "Documents API",
        description = "APIs for managing and searching documents stored in Elasticsearch"
)
@RestController
public class SearchController {

    @Operation(
            summary = "Get all documents",
            description = "Returns a list of all uploaded documents (id and filename)"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of documents returned successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/documents")
    public List<Map<String, String>> getDocuments() {
        try {
            return ElasticService.getAllDocuments();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Operation(
            summary = "Get document content by ID",
            description = "Returns the full extracted content of a document using its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document content returned successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/documents/{id}")
    public String getDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable String id
    ) {
        try {
            return ElasticService.getDocumentContent(id);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Operation(
            summary = "Delete document by ID",
            description = "Deletes a document from Elasticsearch using its ID"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Document deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Document not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @DeleteMapping("/documents/{id}")
    public String deleteDocument(
            @Parameter(description = "Document ID", required = true)
            @PathVariable String id
    ) {
        try {
            return ElasticService.deleteDocument(id);
        } catch (Exception e) {
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }
    }

    @Operation(
            summary = "Search in document content",
            description = "Searches for one or multiple terms inside all documents and returns highlighted fragments"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results returned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid search term"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @GetMapping("/search")
    public List<Map<String, String>> search(
            @Parameter(description = "Search term(s), can be multiple words", required = true, example = "implementation testing")
            @RequestParam String term
    ) {
        try {
            return ElasticService.searchWithHighlight(term);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
