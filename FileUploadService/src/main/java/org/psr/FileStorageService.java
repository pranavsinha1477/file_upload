package org.psr;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FileStorageService {

    private final Path storageLocation = Paths.get("uploads");

    private Connection dbConnection;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storageLocation);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage", e);
        }
        try {
            dbConnection = DriverManager.getConnection("jdbc:mysql://localhost:3306/dropbox");
            Statement stmt = dbConnection.createStatement();
            stmt.execute("CREATE TABLE IF NOT EXISTS file_metadata (id INT AUTO_INCREMENT PRIMARY KEY, filename VARCHAR(255), upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        } catch (SQLException e) {
            dbConnection = null;
            System.err.println("Database connection failed. Falling back to file system only.");
        }
    }

    public String storeFile(MultipartFile file) {
        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
        try {
            Path targetLocation = storageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            if (dbConnection != null) {
                PreparedStatement pstmt = dbConnection.prepareStatement("INSERT INTO file_metadata (filename) VALUES (?)");
                pstmt.setString(1, fileName);
                pstmt.executeUpdate();
            }
            return fileName;
        } catch (IOException | SQLException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    public List<String> listFiles() {
        if (dbConnection != null) {
            try {
                Statement stmt = dbConnection.createStatement();
                ResultSet rs = stmt.executeQuery("SELECT filename FROM file_metadata");
                List<String> filenames = new ArrayList<>();
                while (rs.next()) {
                    filenames.add(rs.getString("filename"));
                }
                return filenames;
            } catch (SQLException e) {
                System.err.println("Failed to retrieve file list from DB, falling back to file system");
            }
        }
        try (Stream<Path> stream = Files.walk(this.storageLocation, 1)) {
            return stream.filter(path -> !path.equals(this.storageLocation))
                    .map(storageLocation::relativize)
                    .map(Path::toString)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Failed to read stored files", e);
        }
    }

    public Resource loadFile(String filename) {
        try {
            Path file = storageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("File not found");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("File not found", e);
        }
    }
}
