package com.thecheatschool.thecheatschool.server.exception;

public class BlogNotFoundException extends RuntimeException {
    public BlogNotFoundException(String slug) {
        super("Blog not found with slug: " + slug);
    }
}