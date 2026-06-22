package my.documind.service;

import lombok.extern.log4j.Log4j2;
import my.documind.exception.ErrorMessage;
import my.documind.exception.FileException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Log4j2
@Service
public class LocalFileStorageService implements FileStorageService {
    @Value("${document.upload-dir}")
    private String uploadDir;

    @Override
    public String store(MultipartFile file) {
        String storedFilename = UUID.randomUUID() + "_" + file.getOriginalFilename(); // 파일명 충돌 방지를 위해 UUID 사용
        Path uploadPath = Paths.get(uploadDir);
        Path path = Paths.get(uploadDir, storedFilename);
        try {
            // 업로드 디렉터리가 없으면 생성
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            file.transferTo(path);
            log.info("파일 저장 완료. path={}", path);
            return storedFilename;
        } catch (IOException e) {
            throw new FileException(ErrorMessage.FILE_SAVE_FAILED, e);
        }
    }

    @Override
    public void delete(String storedFilename) {
        try {
            Files.deleteIfExists(Paths.get(uploadDir, storedFilename));
        } catch (IOException e) {
            throw new FileException(ErrorMessage.FILE_DELETE_FAILED, e);
        }
    }
}