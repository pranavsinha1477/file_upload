import org.junit.jupiter.api.Test;
import org.psr.FileUploadApplication;
import org.springframework.boot.test.context.SpringBootTest;

class FileUploadApplicationTest {
    @Test
    void contextLoads() {
        // Verifies that the Spring context loads successfully
    }

    @Test
    void mainMethodRuns() {
        FileUploadApplication.main(new String[]{});
    }
}