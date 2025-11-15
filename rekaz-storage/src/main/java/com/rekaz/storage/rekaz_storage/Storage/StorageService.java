package com.rekaz.storage.rekaz_storage.Storage;

import com.rekaz.storage.rekaz_storage.Storage.Dto.RetrieveBlobDto;
import com.rekaz.storage.rekaz_storage.Storage.Dto.StoreBlobDto;

public interface StorageService {
    void store(StoreBlobDto storeBlobDto);
    RetrieveBlobDto retrieve(String id);
}
