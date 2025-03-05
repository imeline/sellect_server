package com.sellect.server.product.util;

public class StorageUtil {

    public static String generateFileName(String originalFilename) {
        String identifier = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return identifier + "_" + System.currentTimeMillis() + fileExtension;
    }

}
