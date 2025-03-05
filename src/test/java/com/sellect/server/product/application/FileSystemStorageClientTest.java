package com.sellect.server.product.application;

import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.product.config.properties.FileSystemStorageProperties;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;
import org.springframework.core.io.Resource;

class FileSystemStorageClientTest {

    private static Path tempDir;
    private FileSystemStorageClient storageClient;

    @BeforeAll
    static void setupTempDir() throws IOException {
        tempDir = Files.createTempDirectory("storage-test");
    }

    @BeforeEach
    void setUp() throws Exception {
        FileSystemStorageProperties properties = new FileSystemStorageProperties();
        properties.setLocation(tempDir.toString());
        storageClient = new FileSystemStorageClient(properties);
        storageClient.init();
    }

    @AfterEach
    void tearDown() {
        storageClient.deleteAll();
    }

    @AfterAll
    static void cleanupTempDir() throws IOException {
        Files.deleteIfExists(tempDir);
    }

    @Test
    @DisplayName("파일 저장소 초기화(init) 테스트 - 디렉토리 생성 호출 검증")
    void shouldInitializeStorageAndCallCreateDirectories() {
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
            mockedFiles.when(() -> Files.createDirectories(tempDir)).thenAnswer(invocation -> null);
            assertThatCode(() -> storageClient.init()).doesNotThrowAnyException();
            mockedFiles.verify(() -> Files.createDirectories(tempDir));
        }
    }

    @Nested
    @DisplayName("FileSystemStorageClient 빈(Bean) 초기화 테스트")
    class FileSystemStorageClientInitTests {

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("파일 저장소 경로가 비어 있으면 예외가 발생해야 한다.")
        void shouldThrowExceptionWhenLocationIsEmpty(String location) {
            FileSystemStorageProperties properties = new FileSystemStorageProperties();
            properties.setLocation(location);
            assertThatThrownBy(() -> new FileSystemStorageClient(properties))
                .isInstanceOf(Exception.class)
                .hasMessage("file system storage location is null or empty");
        }

        @Test
        @DisplayName("파일 저장소 초기화 실패 시 체크 예외가 발생해야 한다.")
        void shouldThrowExceptionWhenInitFailed() {
            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                IOException ioException = new IOException("Directory creation failed");
                mockedFiles.when(() -> Files.createDirectories(any(Path.class))).thenThrow(ioException);

                FileSystemStorageProperties properties = new FileSystemStorageProperties();
                properties.setLocation(tempDir.toString());
                FileSystemStorageClient newClient = new FileSystemStorageClient(properties);

                assertThatThrownBy(newClient::init)
                    .isInstanceOf(Exception.class)
                    .hasMessage("Could not initialize file system storage")
                    .hasCause(ioException);
            } catch (Exception e) {
                fail("Unexpected exception during setup: " + e.getMessage());
            }
        }
    }

    @Nested
    @DisplayName("파일 저장 후 새로운 파일 이름 반환(store) 테스트")
    class StoreTests {

        @Test
        @DisplayName("정상적인 MultipartFile 저장 시 파일이 경로에 존재해야 한다.")
        void shouldSaveMultipartFileSuccessfully() throws IOException {
            FakeMultipartFile file = new FakeMultipartFile("file", "test.txt", "text/plain", "test data".getBytes());
            storageClient.store(file, file.getName());
            Path filePath = tempDir.resolve(file.getName());
            assertThat(Files.exists(filePath)).isTrue();
        }

        @Test
        @DisplayName("정상적인 InputStream 저장 시 파일이 경로에 존재해야 한다.")
        void shouldSaveInputStreamSuccessfully() throws IOException {
            storageClient.store(new ByteArrayInputStream("test data".getBytes()), "test.txt");
            Path filePath = tempDir.resolve("test.txt");
            assertThat(Files.exists(filePath)).isTrue();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = " ")
        @DisplayName("파일 이름이 없거나 공백일 경우 예외가 발생해야 한다.")
        void shouldThrowExceptionWhenFilenameIsInvalid(String filename) {
            FakeMultipartFile file = new FakeMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
            assertThatThrownBy(() -> storageClient.store(file, filename))
                .isInstanceOf(CommonException.class)
                .hasMessage(BError.NOT_EXIST.getMessage("file name"));
        }

        @Test
        @DisplayName("파일 저장 경로가 rootLocation 밖이면 예외가 발생해야 한다.")
        void shouldThrowExceptionWhenPathOutsideRoot() {
            FakeMultipartFile file = new FakeMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
            String outsidePath = "../outside.txt";
            assertThatThrownBy(() -> storageClient.store(file, outsidePath))
                .isInstanceOf(CommonException.class)
                .hasMessage(BError.FAIL_FOR_REASON.getMessage("store file", "cannot store file outside current directory."));
        }

        @Test
        @DisplayName("MultipartFile 저장 시 IOException 발생하면 예외가 던져져야 한다.")
        void shouldThrowExceptionWhenMultipartFileStoreFails() {
            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                FakeMultipartFile file = new FakeMultipartFile("file", "test.txt", "text/plain", "data".getBytes());
                IOException ioException = new IOException("Copy failed");
                mockedFiles.when(() -> Files.copy((InputStream) any(), any(), any())).thenThrow(ioException);

                assertThatThrownBy(() -> storageClient.store(file, "test.txt"))
                    .isInstanceOf(CommonException.class)
                    .hasMessage(BError.FAIL_FOR_REASON.getMessage("store file", "Copy failed"));
            }
        }

        @Test
        @DisplayName("InputStream 저장 시 IOException 발생하면 예외가 던져져야 한다.")
        void shouldThrowExceptionWhenInputStreamStoreFails() {
            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                ByteArrayInputStream inputStream = new ByteArrayInputStream("data".getBytes());
                IOException ioException = new IOException("Copy failed");
                mockedFiles.when(() -> Files.copy((InputStream) any(), any(), any())).thenThrow(ioException);

                assertThatThrownBy(() -> storageClient.store(inputStream, "test.txt"))
                    .isInstanceOf(CommonException.class)
                    .hasMessage(BError.FAIL_FOR_REASON.getMessage("store file", "Copy failed"));
            }
        }
    }

    @Nested
    @DisplayName("파일 경로 조회(loadAsPath) 테스트")
    class LoadAsPathTests {

        @Test
        @DisplayName("파일 이름으로 해당 파일의 경로를 반환해야 한다.")
        void shouldReturnFilePath() {
            storageClient.store(new ByteArrayInputStream("test data".getBytes()), "test.txt");
            String path = storageClient.loadAsPath("test.txt");
            assertThat(path).isEqualTo(tempDir.resolve("test.txt").toString());
        }

        @Test
        @DisplayName("존재하지 않는 파일을 조회하면 예외가 발생해야 한다.")
        void shouldThrowExceptionWhenFileNotExists() {
            assertThatThrownBy(() -> storageClient.loadAsPath("not_exist.txt"))
                .isInstanceOf(CommonException.class)
                .hasMessage(BError.NOT_EXIST.getMessage("file"));
        }
    }

    @Nested
    @DisplayName("파일 리소스 조회(loadAsResource) 테스트")
    class LoadAsResourceTests {

        @Test
        @DisplayName("존재하는 파일을 리소스로 로드할 수 있어야 한다.")
        void shouldReturnValidResource() throws IOException {
            Path testFile = tempDir.resolve("sample.txt");
            Files.createFile(testFile);
            Resource resource = storageClient.loadAsResource("sample.txt");
            assertThat(resource.exists()).isTrue();
            assertThat(resource.isReadable()).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 파일을 리소스로 로드하면 예외가 발생해야 한다.")
        void shouldThrowExceptionWhenFileNotExists() {
            assertThatThrownBy(() -> storageClient.loadAsResource("not_exist.txt"))
                .isInstanceOf(CommonException.class)
                .hasMessage(BError.NOT_EXIST.getMessage("file"));
        }

        @Test
        @DisplayName("파일이 아닌 디렉토리를 리소스로 로드하면 예외가 발생해야 한다.")
        void shouldThrowExceptionWhenPathIsDirectory() throws IOException {
            Path dir = tempDir.resolve("subdir");
            Files.createDirectory(dir);
            assertThatThrownBy(() -> storageClient.loadAsResource("subdir"))
                .isInstanceOf(CommonException.class)
                .hasMessage(BError.FAIL_FOR_REASON.getMessage("load file", "file is directory"));
        }
    }

    @Nested
    @DisplayName("파일 삭제(deleteAll) 테스트")
    class DeleteAllTests {

        @Test
        @DisplayName("파일 삭제 후, 저장소가 비어 있어야 한다.")
        void shouldRemoveAllFiles() throws IOException {
            Files.createFile(tempDir.resolve("file1.txt"));
            Files.createFile(tempDir.resolve("file2.txt"));
            storageClient.deleteAll();
            assertThat(Files.list(tempDir).count()).isZero();
        }

        @Test
        @DisplayName("이미 비어 있는 경우에도 정상 동작해야 한다.")
        void shouldNotFailWhenNoFiles() {
            assertThatCode(() -> storageClient.deleteAll()).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("파일 삭제 시 IOException 발생하면 예외가 던져져야 한다.")
        void shouldThrowExceptionWhenDeleteFails() throws IOException {
            Files.createFile(tempDir.resolve("file1.txt"));
            try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {
                mockedFiles.when(() -> Files.list(tempDir)).thenThrow(new IOException("Delete failed"));

                assertThatThrownBy(() -> storageClient.deleteAll())
                    .isInstanceOf(CommonException.class)
                    .hasMessage(BError.FAIL_FOR_REASON.getMessage("delete files", "Delete failed"));
            }
        }
    }
}