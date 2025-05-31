import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.psr.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private final Path testDir = Paths.get("uploads");

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        fileStorageService.init();
    }

    @AfterEach
    void tearDown() throws IOException {
        FileSystemUtils.deleteRecursively(testDir);
    }

    @Test
    void testStoreFileAndListFiles() {
        MockMultipartFile file = new MockMultipartFile("file", "test.txt", "text/plain", "Hello".getBytes());
        String fileName = fileStorageService.storeFile(file);
        assertEquals("test.txt", fileName);

        List<String> files = fileStorageService.listFiles();
        assertTrue(files.contains("test.txt"));
    }

    @Test
    void testLoadFile() {
        MockMultipartFile file = new MockMultipartFile("file", "sample.txt", "text/plain", "Sample".getBytes());
        fileStorageService.storeFile(file);

        Resource resource = fileStorageService.loadFile("sample.txt");
        assertTrue(resource.exists());
        assertTrue(resource.isReadable());
    }

    @Test
    void testLoadFileNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileStorageService.loadFile("notfound.txt");
        });
        assertTrue(exception.getMessage().contains("File not found"));
    }
}