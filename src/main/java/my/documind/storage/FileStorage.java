package my.documind.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface FileStorage {
    String store(MultipartFile file);
    void delete(String storedFilename);
    Path getPath(String storedFilename);
}