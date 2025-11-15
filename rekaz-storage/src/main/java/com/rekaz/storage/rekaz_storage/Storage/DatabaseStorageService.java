package com.rekaz.storage.rekaz_storage.Storage;

import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import org.springframework.stereotype.Service;
import com.rekaz.storage.rekaz_storage.Registry.StorageRegistry;
import com.rekaz.storage.rekaz_storage.Storage.Dto.RetrieveBlobDto;
import com.rekaz.storage.rekaz_storage.Storage.Dto.StoreBlobDto;

@Service
public class DatabaseStorageService implements StorageService {
    
    private final BlobMetaRepo blobMetaRepo;
    private final DataBlobRepo dataBlobRepo;
    private final StorageRegistry storageRegistry;

    public DatabaseStorageService(BlobMetaRepo blobMetaRepo, DataBlobRepo dataBlobRepo, StorageRegistry storageRegistry) {
        this.blobMetaRepo = blobMetaRepo;
        this.dataBlobRepo = dataBlobRepo;
        this.storageRegistry = storageRegistry;
    }

    @Override
    public void store(StoreBlobDto storeBlobDto) {
        try{
            byte[] decodedData = Base64.getDecoder().decode(storeBlobDto.getEncodedData()); 
            Long size = (long) decodedData.length;
            Instant time = Instant.now();
            String id = storeBlobDto.getId();

            DataBlob dataBlob = new DataBlob();
            dataBlob.setBlobId(id);
            dataBlob.setData(decodedData.toString());
            dataBlob.setCreatedAt(time);
            dataBlob.setSize(size);
            
            dataBlobRepo.save(dataBlob);

            BlobMeta blobMeta = new BlobMeta();
            blobMeta.setBlobId(id);
            blobMeta.setSize(size);
            blobMeta.setCreatedAt(time);
            blobMeta.setStorageType(storageRegistry.getStorageTpye());

            blobMetaRepo.save(blobMeta);

        }catch(IllegalArgumentException e){
            throw new IllegalArgumentException("Invalid Base64 encoded data or Database issue: " + e.getMessage());
        }
    }

    @Override
    public RetrieveBlobDto retrieve(String id) {
        Optional<DataBlob> isDataBlob = dataBlobRepo.findByBlobId(id);

        if(!isDataBlob.isPresent()){
            throw new IllegalArgumentException("Blob with ID " + id + " not found");
        }

        DataBlob dataBlob = isDataBlob.get();

        String encodedData = Base64.getEncoder().encodeToString(dataBlob.getData().getBytes());


        RetrieveBlobDto retrieveBlobDto = new RetrieveBlobDto();
        retrieveBlobDto.setId(dataBlob.getBlobId());
        retrieveBlobDto.setEncodedData(encodedData);
        retrieveBlobDto.setCreatedAt(dataBlob.getCreatedAt());
        retrieveBlobDto.setSize(dataBlob.getSize());

        return retrieveBlobDto;

    }
}