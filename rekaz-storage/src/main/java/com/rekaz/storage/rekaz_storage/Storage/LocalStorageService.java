package com.rekaz.storage.rekaz_storage.Storage;

import org.springframework.stereotype.Service;
import com.rekaz.storage.rekaz_storage.Exception.BlobNotFoundException;
import com.rekaz.storage.rekaz_storage.Exception.FailLocalStorageException;
import com.rekaz.storage.rekaz_storage.Registry.StorageRegistry;
import com.rekaz.storage.rekaz_storage.Storage.Dto.RetrieveBlobDto;
import com.rekaz.storage.rekaz_storage.Storage.Dto.StoreBlobDto;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.Base64;

@Service
public class LocalStorageService implements StorageService {

    private final StorageRegistry storageRegistry;
    private final BlobMetaRepo blobMetaRepo;

    public LocalStorageService(StorageRegistry storageRegistry, BlobMetaRepo blobMetaRepo) {
        this.storageRegistry = storageRegistry;
        this.blobMetaRepo = blobMetaRepo;
    }


    @Override
    public void store(StoreBlobDto storeBlobDto) {
        try {

            String basePath = storageRegistry.getStoragePath();

            Path filePath = Paths.get(basePath, storeBlobDto.getId());

            // Create parent directories if they don't exist
            Files.createDirectories(filePath.getParent());

            byte[] decodedData = Base64.getDecoder().decode(storeBlobDto.getEncodedData()); 

            Files.write(filePath, decodedData);

            Long size = Files.size(filePath);

            BlobMeta blobMeta = new BlobMeta();

            blobMeta.setBlobId(storeBlobDto.getId()); 
            blobMeta.setSize(size);
            blobMeta.setCreatedAt(Instant.now());
            blobMeta.setStorageType(storageRegistry.getStorageTpye());

            blobMetaRepo.save(blobMeta);

        } catch (IOException e) {
            throw new FailLocalStorageException("Failed to store blob in local storage: " + e.getMessage());
        }
    }

    @Override
    public RetrieveBlobDto retrieve(String id) {
        try {
        
            String basePath = storageRegistry.getStoragePath();

            Path filePath = Paths.get(basePath, id);

            if (!Files.exists(filePath)) {
                throw new BlobNotFoundException("Blob not found with id: " + id);
            }

            byte[] fileData = Files.readAllBytes(filePath);

            String encodedData = Base64.getEncoder().encodeToString(fileData);

            long sizeInBytes = Files.size(filePath);

            BasicFileAttributes attributes = Files.readAttributes(filePath, BasicFileAttributes.class);
            Instant createdAt = attributes.creationTime().toInstant();

            RetrieveBlobDto retrieveBlobDto = new RetrieveBlobDto();
            retrieveBlobDto.setId(id);
            retrieveBlobDto.setEncodedData(encodedData);
            retrieveBlobDto.setSize(sizeInBytes);
            retrieveBlobDto.setCreatedAt(createdAt);

            return retrieveBlobDto;

        } catch (BlobNotFoundException e) {
            throw e;
        } catch (IOException e) {
            throw new FailLocalStorageException("Failed to retrieve blob: " + e.getMessage());
        }
    }
}