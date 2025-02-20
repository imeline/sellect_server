CREATE TABLE `cart_item`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL,
    `product_id` BIGINT   NOT NULL,
    `quantity`      INT      NOT NULL,
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME NOT NULL,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `user_received_coupon`
(
    `id`         BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT   NOT NULL,
    `coupon_id`  BIGINT   NOT NULL,
    `created_at` DATETIME NOT NULL,
    `updated_at` DATETIME NOT NULL,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `payment`
(
    `id`         BIGINT         NOT NULL AUTO_INCREMENT,
    `orders_id`  BIGINT         NOT NULL,
    `price`      DECIMAL(10, 2) NOT NULL,
    `created_at` DATETIME NULL,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `category`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(255) NOT NULL,
    `parent_id`  BIGINT NULL,
    `depth`      TINYINT NULL,
    `created_at` DATETIME NULL,
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `user_auth`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT       NOT NULL,
    `email`      VARCHAR(255) NOT NULL,
    `password`   VARCHAR(255) NOT NULL,
    `created_at` DATETIME     NOT NULL,
    `updated_at` DATETIME     NOT NULL,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `product_image`
(
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `product_id`     BIGINT       NOT NULL,
    `image_url`      VARCHAR(255) NOT NULL,
    `representative` TINYINT(1) NOT NULL,
    `uuid`           VARCHAR(255) NOT NULL,
    `prev`           VARCHAR(255) NULL,
    `next`           VARCHAR(255) NULL,
    `created_at`     DATETIME     NOT NULL,
    `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `delete_at`      DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `preferred_brand`
(
    `id`         BIGINT NOT NULL AUTO_INCREMENT,
    `user_id`    BIGINT NOT NULL,
    `brand_id`   BIGINT NOT NULL,
    `created_at` DATETIME NULL,
    `updated_at` DATETIME NULL,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `brand`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(255) NOT NULL,
    `created_at` DATETIME NULL,
    `updated_at` DATETIME NULL DEFAULT CURRENT_TIMESTAMP,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `orders`
(
    `id`                      BIGINT         NOT NULL AUTO_INCREMENT,
    `user_id`                 BIGINT         NOT NULL,
    `user_received_coupon_id` BIGINT NULL,
    `price`                   DECIMAL(10, 2) NOT NULL,
    `order_number`            VARCHAR(50)    NOT NULL,
    `created_at`              DATETIME       NOT NULL,
    `delete_at`               DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `coupon`
(
    `id`            BIGINT   NOT NULL AUTO_INCREMENT,
    `seller_id`     BIGINT   NOT NULL,
    `discount_cost` INT      NOT NULL,
    `quantity`      INT      NOT NULL,
    `expired_at`    DATETIME NOT NULL,
    `created_at`    DATETIME NOT NULL,
    `updated_at`    DATETIME NOT NULL,
    `delete_at`     DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `order_item`
(
    `id`         BIGINT NOT NULL AUTO_INCREMENT,
    `orders_id`  BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity`   INT UNSIGNED NOT NULL,
    `created_at` DATETIME NULL,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `product`
(
    `id`          BIGINT         NOT NULL AUTO_INCREMENT,
    `seller_id`   BIGINT         NOT NULL,
    `category_id` BIGINT         NOT NULL,
    `brand_id`    BIGINT         NOT NULL,
    `price`       DECIMAL(10, 2) NOT NULL,
    `name`        VARCHAR(255)   NOT NULL,
    `description` VARCHAR(5000)  NOT NULL,
    `stock`       INT UNSIGNED   NOT NULL,
    `created_at`  DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  DATETIME       NOT NULL,
    `delete_at`   DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `user`
(
    `id`         BIGINT                  NOT NULL AUTO_INCREMENT,
    `uuid`       VARCHAR(50)             NOT NULL,
    `nickname`   VARCHAR(50)             NOT NULL,
    `role`       ENUM('USER', 'SELLER')  NOT NULL,
    `created_at` DATETIME                NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME                NOT NULL,
    `delete_at`  DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `review`
(
    `id`                 BIGINT   NOT NULL AUTO_INCREMENT,
    `user_id`            BIGINT   NOT NULL,
    `product_id`         BIGINT   NOT NULL,
    `rating`             FLOAT    NULL,
    `text`               TEXT     NULL,
    `helpful_vote_count` INT      NULL,
    `created_at`         DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`         DATETIME NOT NULL,
    `delete_at`          DATETIME NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `auto_complete_keyword`
(
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `keyword`    VARCHAR(255) NOT NULL,
    `frequency`  BIGINT       NOT NULL,
    `created_at` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME     NOT NULL,
    `delete_at`  DATETIME     NULL,
    PRIMARY KEY (`id`)
);

CREATE TABLE `search_log`
(
    `id`               BIGINT         NOT NULL AUTO_INCREMENT,
    `keyword`          VARCHAR(255)   NOT NULL,
    `user_identifier`       VARCHAR(255)   NOT NULL,
    `result_count`     INTEGER        NULL,
    `category_id`      BIGINT         NULL,
    `brand_id`         BIGINT         NULL,
    `filter_applied`   TINYINT(1)     NULL,
    `timestamp`        DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`)
);
