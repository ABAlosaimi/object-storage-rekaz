package com.rekaz.storage.rekaz_storage.Storage.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StoreBlobDto {

    @NotBlank(message = "ID cannot be blank")
    @NotNull(message = "ID cannot be null")
    private String id;

    @NotNull(message = "Data cannot be null")
    @NotBlank(message = "Data cannot be blank")
    private String encodedData;

    public void setId(String id) {
        this.id = id;
    }

    public void setEncodedData(String encodedData) {
        this.encodedData = encodedData;
    }

    public String getId() {
        return id;
    }

    public String getEncodedData() {
        return encodedData;
    }

}
