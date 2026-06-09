package my.documind.service;

import my.documind.domain.User;
import my.documind.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTests {
    @Mock
    private UserService userService;

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MultipartFile file;

    @InjectMocks
    private DocumentService documentService;

    @Test
    @DisplayName("업로드 성공")
    void upload_success() throws Exception {
        String email = "test@test.com";

        User user = User.builder()
                .id(1L)
                .email(email)
                .build();

        when(userService.getByEmail(email)).thenReturn(user);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        when(file.getContentType()).thenReturn("application/pdf");
        when(file.getSize()).thenReturn(100L);

        when(fileStorageService.store(file)).thenReturn("uuid_test.pdf");

        documentService.upload(List.of(file), email);

        verify(fileStorageService, times(1)).store(file);
        verify(documentRepository, times(1)).saveAll(anyList());
    }
}