package com.banking.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for profile picture and document file storage.
 */
public interface FileStorageService {

    /**
     * Stores a file and returns the URL path to access it.
     *
     * @param file      the uploaded file
     * @param subfolder the target subfolder (e.g., "profiles", "kyc")
     * @return the relative URL path to the stored file
     */
    String storeFile(MultipartFile file, String subfolder);

    /**
     * Deletes a previously stored file.
     *
     * @param filePath the path of the file to delete
     */
    void deleteFile(String filePath);
}
