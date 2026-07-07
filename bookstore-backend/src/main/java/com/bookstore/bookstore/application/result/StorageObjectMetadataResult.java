package com.bookstore.bookstore.application.result;

public record StorageObjectMetadataResult(
        String contentType,
        Long contentLength,
        String eTag
) {
}
