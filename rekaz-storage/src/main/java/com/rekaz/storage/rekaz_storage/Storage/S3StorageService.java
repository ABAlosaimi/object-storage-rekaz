package com.rekaz.storage.rekaz_storage.Storage;

import com.rekaz.storage.rekaz_storage.Exception.BlobNotFoundException;
import com.rekaz.storage.rekaz_storage.Exception.S3StorageException;
import com.rekaz.storage.rekaz_storage.Registry.StorageRegistry;
import com.rekaz.storage.rekaz_storage.Storage.Configs.S3Configs;
import com.rekaz.storage.rekaz_storage.Storage.Dto.RetrieveBlobDto;
import com.rekaz.storage.rekaz_storage.Storage.Dto.StoreBlobDto;
import com.rekaz.storage.rekaz_storage.Storage.Utils.AwsSignatureV4;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

@Service
public class S3StorageService implements StorageService {

    private final WebClient webClient;
    private final S3Configs s3Configs;
    private final BlobMetaRepo blobMetaRepo;
    private final StorageRegistry storageRegistry;

    public S3StorageService(WebClient s3WebClient, S3Configs s3Configs, BlobMetaRepo blobMetaRepo, StorageRegistry storageRegistry) {
        this.webClient = s3WebClient;
        this.s3Configs = s3Configs;
        this.blobMetaRepo = blobMetaRepo;
        this.storageRegistry = storageRegistry;
    }

    @Override
    public void store(StoreBlobDto storeBlobDto) {
        try {
            byte[] fileData = Base64.getDecoder().decode(storeBlobDto.getEncodedData());

            // building path in bucket
            String objectKey = "/" + s3Configs.getBucketName() + "/" + storeBlobDto.getId();

            //  SigV4 headers
            Map<String, String> awsHeaders = AwsSignatureV4.generateHeaders(
                    "PUT",
                    objectKey,
                    fileData,
                    s3Configs.getHost(),
                    s3Configs.getRegion(),
                    s3Configs.getAccessKey(),
                    s3Configs.getSecretKey(),
                    "application/octet-stream"
            );

            webClient.put()
                    .uri(s3Configs.getEndpoint() + objectKey)
                    .headers(headers -> awsHeaders.forEach(headers::set))
                    .bodyValue(fileData)
                    .retrieve()
                    .onStatus( // handle errors from S3 
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(
                                            new S3StorageException("S3 upload failed with status " +
                                                    response.statusCode() + ": " + errorBody)
                                    ))
                    )
                    .toBodilessEntity()
                    .block();

            
            BlobMeta blobMeta = new BlobMeta();
            blobMeta.setBlobId(storeBlobDto.getId());
            blobMeta.setSize((long) fileData.length);
            blobMeta.setCreatedAt(Instant.now());
            blobMeta.setStorageType(storageRegistry.getStorageTpye());

            blobMetaRepo.save(blobMeta);

        } catch (S3StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new S3StorageException("Failed to store blob in S3: " + e.getMessage(), e);
        }
    }

    @Override
    public RetrieveBlobDto retrieve(String id) {
        try {
            // S3 object key
            String objectKey = "/" + s3Configs.getBucketName() + "/" + id;

            // Empty payload for GET request
            byte[] emptyPayload = new byte[0];

            // Generate AWS Signature V4 headers for GET request
            Map<String, String> awsHeaders = AwsSignatureV4.generateHeaders(
                    "GET",
                    objectKey,
                    emptyPayload,
                    s3Configs.getHost(),
                    s3Configs.getRegion(),
                    s3Configs.getAccessKey(),
                    s3Configs.getSecretKey(),
                    null
            );

            // blob from S3 
            byte[] fileData = webClient.get()
                    .uri(s3Configs.getEndpoint() + objectKey)
                    .headers(headers -> awsHeaders.forEach(headers::set))
                    .retrieve()
                    .onStatus(
                            HttpStatus.NOT_FOUND::equals,
                            response -> Mono.error(new BlobNotFoundException("Blob not found with id: " + id))
                    )
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .flatMap(errorBody -> Mono.error(
                                            new S3StorageException("S3 download failed with status " +
                                                    response.statusCode() + ": " + errorBody)
                                    ))
                    )
                    .bodyToFlux(DataBuffer.class)
                    .collectList()
                    .map(dataBuffers -> {
                        // Calculate total size of the file returned
                        int totalSize = dataBuffers.stream()
                                .mapToInt(DataBuffer::readableByteCount)
                                .sum();

                        // Combine all buffers into single byte array
                        byte[] combinedData = new byte[totalSize];
                        int position = 0;
                        for (DataBuffer buffer : dataBuffers) {
                            int bufferSize = buffer.readableByteCount();
                            buffer.read(combinedData, position, bufferSize);
                            position += bufferSize;
                            DataBufferUtils.release(buffer);
                        }
                        return combinedData;
                    })
                    .block();

            if (fileData == null) {
                throw new BlobNotFoundException("Blob not found with id: " + id);
            }

            // Encode to base64
            String encodedData = Base64.getEncoder().encodeToString(fileData);

            // Get metadata from database
            BlobMeta blobMeta = blobMetaRepo.findByBlobId(id)
                    .orElseThrow(() -> new BlobNotFoundException("Blob metadata not found with id: " + id));

            RetrieveBlobDto retrieveBlobDto = new RetrieveBlobDto();
            retrieveBlobDto.setId(id);
            retrieveBlobDto.setEncodedData(encodedData);
            retrieveBlobDto.setSize(blobMeta.getSize());
            retrieveBlobDto.setCreatedAt(blobMeta.getCreatedAt());

            return retrieveBlobDto;

        } catch (BlobNotFoundException e) {
            throw e;
        } catch (S3StorageException e) {
            throw e;
        } catch (Exception e) {
            throw new S3StorageException("Failed to retrieve blob from S3: " + e.getMessage(), e);
        }
    }
}