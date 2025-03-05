package com.sellect.server.product.application;

import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.product.config.properties.FileSystemStorageProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

@Component
@Qualifier("fileSystemStorageClient")
public class FileSystemStorageClient implements StorageClient {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageClient(FileSystemStorageProperties properties) throws Exception {

        if (properties.getLocation() == null || properties.getLocation().trim().isEmpty()) {
            throw new Exception("file system storage location is null or empty");
        }
        this.rootLocation = Paths.get(properties.getLocation());
    }

    @Override
    @PostConstruct
    public void init() throws Exception {
        try {
            Files.createDirectories(rootLocation);
        } catch (IOException e) {
            throw new Exception("Could not initialize file system storage", e);
        }
    }

    @Override
    public void store(MultipartFile file, String filename) {
        try {
            validateFilename(filename);
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new CommonException(BError.FAIL_FOR_REASON,
                    "store file",
                    "cannot store file outside current directory.");
            }

            // InputStream 자원을 사용한 후에는 반드시 닫아주어야 함
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new CommonException(BError.FAIL_FOR_REASON,
                "store file",
                e.getMessage());
        }
    }

    @Override
    public void store(InputStream inputStream, String filename) {
        try {
            validateFilename(filename);
            Path destinationFile = this.rootLocation.resolve(Paths.get(filename))
                .normalize().toAbsolutePath();
            if (!destinationFile.getParent().equals(this.rootLocation.toAbsolutePath())) {
                throw new CommonException(BError.FAIL_FOR_REASON,
                    "store file",
                    "cannot store file outside current directory.");
            }

            try (inputStream) {
                Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new CommonException(BError.FAIL_FOR_REASON,
                "store file",
                e.getMessage());
        }
    }


    @Override
    public String loadAsPath(String filename) {
        Path filePath = rootLocation.resolve(filename);
        if (!Files.exists(filePath)) {
            throw new CommonException(BError.NOT_EXIST, "file");
        }
        return filePath.toString();
    }

    @Override
    public Resource loadAsResource(String filename) {
        try {
            Path file = rootLocation.resolve(filename).normalize().toAbsolutePath();
            Resource resource = new UrlResource(file.toUri());
            if (resource.getFile().isDirectory()) {
                throw new CommonException(BError.FAIL_FOR_REASON, "load file", "file is directory");
            }
            if (!resource.exists()) {
                throw new CommonException(BError.NOT_EXIST, "file");
            }
            return resource;
        } catch (IOException e) {
            throw new CommonException(BError.FAIL_FOR_REASON, "load file", e.getMessage());
        }
    }

    @Override
    public void deleteAll() {
        try (Stream<Path> files = Files.list(rootLocation)) {
            files.forEach(path -> FileSystemUtils.deleteRecursively(path.toFile()));
        } catch (IOException e) {
            throw new CommonException(BError.FAIL_FOR_REASON, "delete files", e.getMessage());
        }
    }

    private static void validateFilename(String filename) {
        if (Objects.isNull(filename) || filename.isBlank()) {
            throw new CommonException(BError.NOT_EXIST, "file name");
        }
    }
}
