package com.rekaz.storage.rekaz_storage.Storage;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rekaz.storage.rekaz_storage.Auth.JWTService;
import com.rekaz.storage.rekaz_storage.Registry.StorageRegistry;
import com.rekaz.storage.rekaz_storage.Storage.Dto.RetrieveBlobDto;
import com.rekaz.storage.rekaz_storage.Storage.Dto.StoreBlobDto;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
public class StorageController {

    private final StorageRegistry storageRegistry;
    private final LocalStorageService localStorageService;
    private final DatabaseStorageService databaseStorageService;
    private final S3StorageService s3StorageService;
    private final JWTService jwtService;

    public StorageController(StorageRegistry storageRegistry, LocalStorageService localStorageService,
                             DatabaseStorageService databaseStorageService, S3StorageService s3StorageService , JWTService jwtService) {
        this.storageRegistry = storageRegistry;
        this.localStorageService = localStorageService;
        this.databaseStorageService = databaseStorageService;
        this.s3StorageService = s3StorageService;
        this.jwtService = jwtService;
    }

    @PostMapping("/v1/storage")
    public ResponseEntity<String> configStorageType(@Valid @RequestBody StorageRegistry storageRegistry) {
       this.storageRegistry.setStorageTpye(storageRegistry.getStorageTpye());
       this.storageRegistry.setStoragePath(storageRegistry.getStoragePath());
       String accessToken = jwtService.generateToken(this.storageRegistry); // for thr seek of testing only 
        
       return ResponseEntity.ok("configuration set successfully.\n Your Access Token: " + accessToken);

    }

    @PostMapping("/v1/blobs")
    public ResponseEntity<Object> storeBlob(@Valid @RequestBody StoreBlobDto storeBlobDto) {
        storeCheckStorageType(storeBlobDto);
        return ResponseEntity.ok("Blob stored successfully in " + storageRegistry.getStorageTpye() + " storage.");
    }


    @GetMapping("/v1/blobs/{id}")
    public ResponseEntity<Object> retrieveBlob(@PathVariable String id) {
        RetrieveBlobDto data = retrieveCheckStorageType(id);
        return ResponseEntity.ok("Blob retrieved successfully from " + storageRegistry.getStorageTpye() + " storage.\n Data: " 
        + data.getEncodedData() + "\n Size: " + data.getSize() + "\n Created At: " + data.getCreatedAt());
    }   


    private void storeCheckStorageType(StoreBlobDto storeBlobDto) {

        switch (storageRegistry.getStorageTpye()) {
            case "local-file":
                localStorageService.store(storeBlobDto);
                break;
            
            case "s3":
                s3StorageService.store(storeBlobDto);
                break;

            case "DB":
                databaseStorageService.store(storeBlobDto);
                break;
            default:
            throw new IllegalArgumentException("Unsupported storage type check the docs for matchig the same storage types names: " + storageRegistry.getStorageTpye());
        }

    }


     private RetrieveBlobDto retrieveCheckStorageType(String id) {

        switch (storageRegistry.getStorageTpye()) {
            case "local-file":
                return localStorageService.retrieve(id);
               
            case "s3":
                return s3StorageService.retrieve(id);
                
            case "DB":
                return databaseStorageService.retrieve(id);

            default:
            throw new IllegalArgumentException("Unsupported storage type check the docs for matchig the same storage types names: " + storageRegistry.getStorageTpye());
        }

    }
  
}
