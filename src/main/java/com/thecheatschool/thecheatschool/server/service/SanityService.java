package com.thecheatschool.thecheatschool.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thecheatschool.thecheatschool.server.exception.BlogNotFoundException;
import com.thecheatschool.thecheatschool.server.model.Blog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
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

    @Cacheable(value = "blogs", key = "'all-blogs'")
    public List<Blog> fetchAllBlogs() {
        try {
            String query = "*[_type==\"post\"]|order(publishedAt desc){_id,title,slug,author->{name},mainImage,publishedAt,body}";

            log.info("Fetching all blogs from Sanity (cache miss)");

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

            List<Blog> blogs = Arrays.asList(objectMapper.treeToValue(result, Blog[].class));
            log.info("Successfully fetched {} blogs", blogs.size());

            return blogs;
        } catch (Exception e) {
            log.error("Error fetching blogs from Sanity", e);
            throw new RuntimeException("Failed to fetch blogs from CMS", e);
        }
    }

    @Cacheable(value = "blogs", key = "#slug")
    public Blog fetchBlogBySlug(String slug) {
        try {
            String query = "*[_type==\"post\"&&slug.current==\"" + slug + "\"][0]{_id,title,slug,author->{name},mainImage,publishedAt,body}";

            log.info("Fetching blog with slug: {} (cache miss)", slug);

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
                log.warn("No blog found with slug: {}", slug);
                throw new BlogNotFoundException(slug);
            }

            Blog blog = objectMapper.treeToValue(result, Blog.class);
            log.info("Successfully fetched blog: {}", blog.getTitle());

            return blog;
        } catch (BlogNotFoundException e) {
            throw e; // Re-throw to be handled by GlobalExceptionHandler
        } catch (Exception e) {
            log.error("Error fetching blog by slug: {}", slug, e);
            throw new RuntimeException("Failed to fetch blog from CMS", e);
        }
    }
}