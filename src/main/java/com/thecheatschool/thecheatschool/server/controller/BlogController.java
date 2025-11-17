package com.thecheatschool.thecheatschool.server.controller;

import com.thecheatschool.thecheatschool.server.model.ApiResponse;
import com.thecheatschool.thecheatschool.server.model.Blog;
import com.thecheatschool.thecheatschool.server.service.SanityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Pattern;
import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin(origins = {"http://localhost:5173", "https://*.vercel.app"})
@RequiredArgsConstructor
@Slf4j
@Validated
public class BlogController {

    private final SanityService sanityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Blog>>> getAllBlogs() {
        log.info("GET /api/blogs - Fetching all blogs");
        List<Blog> blogs = sanityService.fetchAllBlogs();
        return ResponseEntity.ok(new ApiResponse<>("success", blogs));
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<Blog>> getBlogBySlug(
            @PathVariable
            @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must contain only lowercase letters, numbers, and hyphens")
            String slug) {

        log.info("GET /api/blogs/{} - Fetching blog by slug", slug);
        Blog blog = sanityService.fetchBlogBySlug(slug);
        return ResponseEntity.ok(new ApiResponse<>("success", blog));
    }
}