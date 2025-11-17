package com.thecheatschool.thecheatschool.server.controller;

import com.thecheatschool.thecheatschool.server.model.Blog;
import com.thecheatschool.thecheatschool.server.model.ApiResponse;
import com.thecheatschool.thecheatschool.server.service.SanityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blogs")
@CrossOrigin(origins = {"http://localhost:5173", "https://*.vercel.app"})
@RequiredArgsConstructor
@Slf4j
public class BlogController {

    private final SanityService sanityService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Blog>>> getAllBlogs() {
        try {
            log.info("GET /api/blogs - Fetching all blogs");
            List<Blog> blogs = sanityService.fetchAllBlogs();
            return ResponseEntity.ok(new ApiResponse<>("success", blogs));
        } catch (Exception e) {
            log.error("Error in getAllBlogs", e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>("error", null, "Failed to fetch blogs"));
        }
    }

    @GetMapping("/{slug}")
    public ResponseEntity<ApiResponse<Blog>> getBlogBySlug(@PathVariable String slug) {
        try {
            log.info("GET /api/blogs/{} - Fetching blog by slug", slug);
            Blog blog = sanityService.fetchBlogBySlug(slug);

            if (blog == null) {
                return ResponseEntity.status(404)
                        .body(new ApiResponse<>("error", null, "Blog not found"));
            }

            return ResponseEntity.ok(new ApiResponse<>("success", blog));
        } catch (Exception e) {
            log.error("Error in getBlogBySlug: {}", slug, e);
            return ResponseEntity.status(500)
                    .body(new ApiResponse<>("error", null, "Failed to fetch blog"));
        }
    }
}