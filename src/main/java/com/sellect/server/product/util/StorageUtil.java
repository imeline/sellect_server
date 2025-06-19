package com.sellect.server.product.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StorageUtil {

    private static final Set<String> VALID_IMAGE_EXTENSIONS = new HashSet<>(
        List.of(
            ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".webp"
        )
    );

    public static String generateFileName(String originalFilename) {
        String identifier = originalFilename.substring(0, originalFilename.lastIndexOf("."));
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return identifier + "_" + System.currentTimeMillis() + fileExtension;
    }

    public static boolean isImageFile(String filename) {
        // 입력값이 null 이거나 빈 문자열이면 false 반환
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        // 파일명을 소문자로 변환하여 처리
        String lowerCaseFilename = filename.toLowerCase();

        // 마지막 점(.)의 위치를 찾음
        int dotIndex = lowerCaseFilename.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == 0 || dotIndex == lowerCaseFilename.length() - 1) {
            return false; // 점이 없거나, 점으로 시작하거나, 점으로 끝나면 false
        }

        // 확장자 추출
        String extension = lowerCaseFilename.substring(dotIndex);

        // 유효한 이미지 확장자인지 확인
        return VALID_IMAGE_EXTENSIONS.contains(extension);
    }

}
