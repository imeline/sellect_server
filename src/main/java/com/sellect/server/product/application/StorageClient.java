package com.sellect.server.product.application;

import java.io.InputStream;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageClient {

    void init() throws Exception;

    void store(MultipartFile file, String filename);

    void store(InputStream inputStream, String filename);

    String loadAsPath(String filename);

    Resource loadAsResource(String filename);

    void deleteAll();

}
