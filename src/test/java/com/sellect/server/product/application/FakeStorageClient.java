package com.sellect.server.product.application;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public class FakeStorageClient implements StorageClient {

    public static final String FAKE_IMAGE_STORAGE_URL = "http://fake-url.com/";

    private final Map<String, String> storage = new HashMap<>();

    @Override
    public void init() {}

    @Override
    public void store(MultipartFile file, String filename) {
        storage.put(filename, "http://fake-url.com/" + filename);
    }

    @Override
    public void store(InputStream inputStream, String filename) {
        storage.put(filename, "http://fake-url.com/" + filename);
    }

    @Override
    public String loadAsPath(String filename) {
        return storage.getOrDefault(filename, null);
    }

    @Override
    public Resource loadAsResource(String filename) {
        return null; // 테스트에 필요하면 추가 구현 가능
    }

    @Override
    public void deleteAll() {
        storage.clear();
    }
}

