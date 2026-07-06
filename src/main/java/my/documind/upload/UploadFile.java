package my.documind.upload;

import org.springframework.web.multipart.MultipartFile;

public record UploadFile(MultipartFile file, String storedFilename) {}
