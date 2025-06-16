package com.ixi_U.chatbot.extractor;

public interface MetadataExtractor {

    FilterConditions extractFilters(String query);

    String generateCleanQuery(String originalQuery, FilterConditions filters);
}