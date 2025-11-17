package com.thecheatschool.thecheatschool.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecheatschool.thecheatschool.server.model.Blog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SanityService {

    @Value("${sanity.project-id}")
    private String projectId;

    @Value("${sanity.dataset}")
    private String dataset;

    @Value("${sanity.api-version}")
    private String apiVersion;

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public List<Blog> fetchAllBlogs() {
        try {
            String baseUrl = String.format("https://%s.apicdn.sanity.io/v%s/data/query/%s",
                    projectId, apiVersion, dataset);

            String query = "*[_type==\"post\"]|order(publishedAt desc){_id,title,slug,author->{name},mainImage,publishedAt,body}";

            log.info("Fetching all blogs from Sanity");

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(projectId + ".apicdn.sanity.io")
                            .path("/v" + apiVersion + "/data/query/" + dataset)
                            .queryParam("query", query)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.get("result");

            return Arrays.asList(objectMapper.treeToValue(result, Blog[].class));
        } catch (Exception e) {
            log.error("Error fetching blogs from Sanity", e);
            throw new RuntimeException("Failed to fetch blogs", e);
        }
    }

    public Blog fetchBlogBySlug(String slug) {
        try {
            String query = "*[_type==\"post\"&&slug.current==\"" + slug + "\"][0]{_id,title,slug,author->{name},mainImage,publishedAt,body}";

            log.info("Fetching blog with slug: {}", slug);

            String response = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host(projectId + ".apicdn.sanity.io")
                            .path("/v" + apiVersion + "/data/query/" + dataset)
                            .queryParam("query", query)
                            .build())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            JsonNode result = root.get("result");

            if (result == null || result.isNull()) {
                return null;
            }

            return objectMapper.treeToValue(result, Blog.class);
        } catch (Exception e) {
            log.error("Error fetching blog by slug: {}", slug, e);
            throw new RuntimeException("Failed to fetch blog", e);
        }
    }

}