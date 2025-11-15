package com.rekaz.storage.rekaz_storage.Registry;

import org.springframework.stereotype.Component;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

@Component
public class StorageRegistry {

    @NotBlank(message = "Strorage type cannot be blank")
    @NotNull(message = "Storage type cannot be null")
    private String storageTpye;

    @Pattern(
        regexp = "^(?!.*\\.\\.)[a-zA-Z0-9._/-]+",
        message = "Storage path not valid"
    )
    private String storagePath;


    public void setStorageTpye(String storageTpye) {
        this.storageTpye = storageTpye;
    }

    public void setStoragePath(String storagePath) {
        this.storagePath = storagePath;
    }

    public String getStorageTpye() {
        return storageTpye;
    }

    public String getStoragePath() {
        return storagePath;
    }

}
