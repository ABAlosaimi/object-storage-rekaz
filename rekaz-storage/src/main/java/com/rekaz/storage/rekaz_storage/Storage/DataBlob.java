package com.rekaz.storage.rekaz_storage.Storage;

import java.time.Instant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "data_blobs")
public class DataBlob {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "blob_id", nullable = false, unique = true)
    private String blobId;

    @Column(name = "encoded_data", nullable = false)
    private String Data;

    @Column(name = "size", nullable = false)
    private Long size;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public Long getId() {
        return id;
    }

    public String getBlobId() {
        return blobId;
    }

    public String getData() {
        return Data;
    }

    public Long getSize() {
        return size;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setBlobId(String blobId) {
        this.blobId = blobId;
    }

    public void setData(String encodedData) {
        this.Data = encodedData;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    
}