package com.sellect.server.product.application;

import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class FakeMultipartFile implements MultipartFile {

    private final String name;           // 필드 이름 (폼 데이터에서의 이름)
    private final String originalFilename; // 원본 파일 이름
    private final String contentType;    // 콘텐츠 타입 (예: "image/jpeg")
    private final byte[] content;        // 파일 내용 (바이트 배열)

    // 기본 생성자: 파일 이름만 지정
    public FakeMultipartFile(String originalFilename) {
        this("file", originalFilename, "application/octet-stream", new byte[0]);
    }

    // 전체 속성을 지정하는 생성자
    public FakeMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = content != null ? content.clone() : new byte[0];
    }

    @Override
    public String getName() {
        // 폼 필드 이름 반환
        return name;
    }

    @Override
    public String getOriginalFilename() {
        // 원본 파일 이름 반환
        return originalFilename;
    }

    @Override
    public String getContentType() {
        // 콘텐츠 타입 반환
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        // 내용이 없으면 true
        return content.length == 0;
    }

    @Override
    public long getSize() {
        // 파일 크기 반환
        return content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        // 내용의 복사본 반환
        return content.clone();
    }

    @Override
    public InputStream getInputStream() throws IOException {
        // 내용의 InputStream 반환
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        // 파일로 내용 전송
        Files.write(dest.toPath(), content);
    }

    public void transferTo(Path dest) throws IOException, IllegalStateException {
        Files.write(dest, content);
    }
}