package com.thecheatschool.thecheatschool.server.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Blog {
    @JsonProperty("_id")
    private String id;

    private String title;
    private Slug slug;
    private Author author;
    private MainImage mainImage;
    private String publishedAt;
    private Object[] body;

    @Data
    public static class Slug {
        private String current;
    }

    @Data
    public static class Author {
        private String name;
    }

    @Data
    public static class MainImage {
        private Asset asset;

        @Data
        public static class Asset {
            @JsonProperty("_ref")
            private String ref;
        }
    }
}