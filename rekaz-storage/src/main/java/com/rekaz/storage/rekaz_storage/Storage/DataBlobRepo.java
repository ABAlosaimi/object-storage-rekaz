package com.rekaz.storage.rekaz_storage.Storage;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DataBlobRepo extends JpaRepository<DataBlob, Long> {
     Optional<DataBlob> findByBlobId(String blobId);
}
