-- 기존 테이블 삭제
DROP TABLE IF EXISTS category CASCADE;
DROP TABLE IF EXISTS brand CASCADE;
DROP TABLE IF EXISTS "user" CASCADE;
DROP TABLE IF EXISTS product CASCADE;
DROP TABLE IF EXISTS product_image CASCADE;
DROP TABLE IF EXISTS inventory CASCADE;
-- 새롭게 추가된 테이블

-- 카테고리 테이블
CREATE TABLE category
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delete_at  TIMESTAMP    NULL,
    depth      INT          NOT NULL,
    name       VARCHAR(255) NOT NULL,
    parent_id  BIGINT       NULL
);

-- 브랜드 테이블
CREATE TABLE brand
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delete_at  TIMESTAMP    NULL,
    name       VARCHAR(255) NOT NULL
);

-- 사용자 테이블
CREATE TABLE "user"
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delete_at  TIMESTAMP   NULL,
    nickname   VARCHAR(50) NOT NULL,
    role       VARCHAR(10) NOT NULL CHECK (role IN ('USER', 'SELLER')),
    uuid       VARCHAR(50) NOT NULL UNIQUE
);

-- 상품 테이블 (stock 제거)
CREATE TABLE product
(
    id          BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delete_at   TIMESTAMP      NULL,
    name        VARCHAR(255)   NOT NULL,
    description VARCHAR(5000),
    price       DECIMAL(19, 2) NOT NULL,
    brand_id    BIGINT,
    category_id BIGINT,
    seller_id   BIGINT
);

-- 재고 테이블 (새로 추가됨)
CREATE TABLE inventory
(
    id         BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delete_at  TIMESTAMP NULL,
    product_id BIGINT    NOT NULL UNIQUE, -- OneToOne 관계
    stock      INT       NOT NULL,
    version BIGINT DEFAULT 0 -- 낙관적 락을 위한 버전 관리
);

-- 상품 이미지 테이블
CREATE TABLE product_image
(
    id             BIGINT PRIMARY KEY AUTO_INCREMENT,
    created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delete_at      TIMESTAMP    NULL,
    image_url      VARCHAR(500) NOT NULL,
    representative BOOLEAN      NOT NULL,
    sequence       INT          NOT NULL,
    product_id     BIGINT       NOT NULL
);
