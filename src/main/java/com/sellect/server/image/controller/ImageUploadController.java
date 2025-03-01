package com.sellect.server.image.controller;

import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.HEAD;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import com.sellect.server.image.config.properties.TusProperties;
import com.sellect.server.product.application.StorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.desair.tus.server.TusFileUploadService;
import me.desair.tus.server.exception.TusException;
import me.desair.tus.server.upload.UploadInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/images")
public class ImageUploadController {

    private final TusFileUploadService fileUploadService;
    private final StorageService storageService;
    private final TusProperties tusProperties;

    // TODO: 서비스 로직으로 분리
    @RequestMapping(value = { "/upload", "/upload/**" },
        method = { POST, PATCH, HEAD, DELETE, GET })
    public void upload(HttpServletRequest servletRequest, HttpServletResponse servletResponse) throws IOException {

        servletResponse.setHeader("Access-Control-Expose-Headers", "Location, Upload-Offset, Upload-Length");
        servletResponse.setHeader("Access-Control-Allow-Methods", "*");

        fileUploadService.process(servletRequest, servletResponse);

        String uploadURI = servletRequest.getRequestURI();
        UploadInfo uploadInfo = null;

        try {
            uploadInfo = fileUploadService.getUploadInfo(uploadURI);
        } catch (IOException | TusException e) {
            log.error("get upload info", e);
            // TODO: 예외 처리
        }

        // 마지막 청크가 업로드된 이후 이미지 저장소에 이미지 저장
        if (uploadInfo != null && !uploadInfo.isUploadInProgress()) {
            try (InputStream inputStream = fileUploadService.getUploadedBytes(uploadURI)) {
                storageService.store(inputStream, uploadInfo.getFileName());
            } catch (IOException | TusException e) {
                log.error("get uploaded bytes", e);
                // TODO: 예외 처리
            }

            try {
                fileUploadService.deleteUpload(uploadURI);
            } catch (IOException | TusException e) {
                log.error("delete upload", e);
                // TODO: 예외 처리
            }
        }
    }

    @Scheduled(fixedDelayString = "PT24H")
    private void cleanup() {
        Path path = Paths.get(tusProperties.getStoragePath());
        Path locksDir = path.resolve("locks");
        if (Files.exists(locksDir)) {
            try {
                fileUploadService.cleanup();
            }
            catch (IOException e) {
                log.error("cleanup", e);
            }
        }
    }

}
