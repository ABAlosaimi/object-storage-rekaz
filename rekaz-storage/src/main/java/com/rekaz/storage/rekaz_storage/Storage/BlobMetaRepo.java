package com.rekaz.storage.rekaz_storage.Storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BlobMetaRepo extends JpaRepository<BlobMeta, Long> {
    Optional<BlobMeta> findByBlobId(String blobId);
}
