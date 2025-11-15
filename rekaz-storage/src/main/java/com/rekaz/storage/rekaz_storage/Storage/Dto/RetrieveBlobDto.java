package com.rekaz.storage.rekaz_storage.Storage.Dto;

import java.time.Instant;
import jakarta.validation.constraints.NotNull;

public class RetrieveBlobDto {
    
    @NotNull(message = "ID cannot be null")
    private String id;

    @NotNull(message = "Data cannot be null")
    private String encodedData;

    @NotNull(message = "Size cannot be null")
    private Long size;

    @NotNull(message = "Creation time cannot be null")
    private Instant createdAt;

    public void setId(String id) {
        this.id = id;
        
    }

    public void setEncodedData(String encodedData) {
        this.encodedData = encodedData;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getId() {
        return id;
    }

    public String getEncodedData() {
        return encodedData;
    }

    public Long getSize() {
        return size;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }


    
}
