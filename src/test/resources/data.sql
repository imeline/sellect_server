-- ✅ Category 데이터
MERGE INTO category (id, created_at, updated_at, depth, name, parent_id)
    KEY (id)
    VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 'Category1', NULL);

-- ✅ Brand 데이터
MERGE INTO brand (id, created_at, updated_at, name)
    KEY (id)
    VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Brand1');

-- ✅ User 데이터 (H2 예약어 문제 해결: "user" 사용)
MERGE INTO "user" (id, created_at, updated_at, nickname, role, uuid)
    KEY (id)
    VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Seller1', 'SELLER', 'uuid-seller1');

-- ✅ Product 데이터
MERGE INTO product (id, created_at, updated_at, name, description, price, brand_id, category_id, seller_id)
    KEY (id)
    VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Product1', 'Description1', 55000.00, 1, 1, 1);

-- ✅ ProductImage 데이터
MERGE INTO product_image (id, created_at, updated_at, image_url, representative, sequence, product_id)
    KEY (id)
    VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'http://example.com/image.jpg', TRUE, 1, 1);

-- ✅ inventory 데이터
MERGE INTO inventory (id, created_at, updated_at, product_id, stock, version)
    KEY (id)
    VALUES (1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, 10, 0);