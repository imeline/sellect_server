package com.sellect.server.product.application;

import com.sellect.server.auth.domain.User;
import com.sellect.server.brand.domain.Brand;
import com.sellect.server.brand.repository.BrandRepository;
import com.sellect.server.category.domain.Category;
import com.sellect.server.category.repository.CategoryRepository;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import com.sellect.server.order.repository.OrderItemRepository;
import com.sellect.server.product.controller.request.ProductModifyRequest;
import com.sellect.server.product.controller.request.ProductRegisterRequest;
import com.sellect.server.product.controller.response.ProductDetailRetrieveResponse;
import com.sellect.server.product.controller.response.ProductDetailRetrieveBySellerResponse;
import com.sellect.server.product.controller.response.ProductModifyResponse;
import com.sellect.server.product.controller.response.ProductRegisterResponse;
import com.sellect.server.product.controller.response.SellerStatsRetrieveResponse;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import com.sellect.server.product.repository.ProductImageRepository;
import com.sellect.server.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductImageService productImageService;
    private final ProductRepository productRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;
    private final ProductImageRepository productImageRepository;
    private final OrderItemRepository orderItemRepository;
    private final StorageService storageService;

    /**
     * 상품 단건 등록
     */
    @Transactional
    public ProductRegisterResponse register(
        User seller,
        ProductRegisterRequest request,
        List<MultipartFile> images) {

        // 존재하지 않는 카테고리 체크
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "category"));

        // 존재하지 않는 브랜드 체크
        Brand brand = brandRepository.findById(request.brandId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "brand"));

        // 등록된 상품 기준 중복 검사 (sellerId, productName 기준)
        if (productRepository.isDuplicateProduct(seller.getId(), request.name())) {
            throw new CommonException(BError.EXIST, "product name");
        }

        Product product = productRepository.save(Product.register(
            seller,
            category,
            brand,
            request.getPriceAsBigDecimal(),
            request.name(),
            request.description(),
            request.stock()
        ));

        // todo: service 에서 service??? 추후 체크
        // 이미지 저장 (이미지 이름에는 식별자(uuid)가 포함되어 있고, 이를 통해 image context 와 매핑)
        Map<String, MultipartFile> imageMap = new HashMap<>();
        images.forEach(image -> {
            String filename = Objects.requireNonNull(image.getOriginalFilename());
            imageMap.put(filename.substring(0, filename.lastIndexOf(".")), image);
        });
        request.imageContexts().forEach(imageContext -> {
            MultipartFile imageFile = imageMap.get(imageContext.uuid());
            if (imageFile == null) {
                throw new CommonException(BError.NOT_MATCHES, "uuid in image context", "uuid in image file name");
            }
            productImageService.registerProductImage(product, imageContext, imageFile);
        });

        return ProductRegisterResponse.from(product);
    }

    /**
     * 상품 단건 등록 (이미지 별도)
     */
    @Transactional
    public ProductRegisterResponse register(
        User seller,
        ProductRegisterRequest request) {

        // 존재하지 않는 카테고리 체크
        Category category = categoryRepository.findById(request.categoryId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "category"));

        // 존재하지 않는 브랜드 체크
        Brand brand = brandRepository.findById(request.brandId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "brand"));

        // 등록된 상품 기준 중복 검사 (sellerId, productName 기준)
        if (productRepository.isDuplicateProduct(seller.getId(), request.name())) {
            throw new CommonException(BError.EXIST, "product name");
        }

        Product product = productRepository.save(Product.register(
            seller,
            category,
            brand,
            request.getPriceAsBigDecimal(),
            request.name(),
            request.description(),
            request.stock()
        ));

        if (request.imageContexts().isEmpty()) {
            throw new CommonException(BError.REQUIRED, "image context");
        }
        request.imageContexts().forEach(imageContext -> {
            String imageUrl = storageService.loadAsPath(imageContext.filename());
            ProductImage productImage = ProductImage.register(product, imageUrl, imageContext);
            productImageRepository.save(productImage, product);
        });

        return ProductRegisterResponse.from(product);
    }

    /**
     * 상품 다건 등록
     * (보류)
     */
//    @Transactional
//    public ProductMultipleRegisterResponse registerMultiple(
//        User seller,
//        List<ProductRegisterRequest> requests,
//        List<MultipartFile> images) {
//
//        List<Product> successProducts = new ArrayList<>();
//        List<ProductRegisterFailureResponse> failedProducts = new ArrayList<>();
//
//        // 요청 내 상품명 중복 검증을 위한 Set
//        Set<String> requestProductNames = new HashSet<>();
//
//        for (ProductRegisterRequest request : requests) {
//            // 요청 내 상품 기준 중복 검사
//            if (!requestProductNames.add(request.name())) {
//                failedProducts.add(
//                    ProductRegisterFailureResponse.from(request.name(), "요청 내 중복된 상품명")
//                );
//                continue;
//            }
//
//            Optional<Category> optionalCategory = categoryRepository.findById(request.categoryId());
//            // 존재하지 않는 카테고리 체크
//            if (optionalCategory.isEmpty()) {
//                failedProducts.add(
//                    ProductRegisterFailureResponse.from(request.name(), "존재하지 않는 카테고리"));
//                continue;
//            }
//
//            // 존재하지 않는 브랜드 체크
//            Optional<Brand> optionalBrand = brandRepository.findById(request.brandId());
//            if (optionalBrand.isEmpty()) {
//                failedProducts.add(
//                    ProductRegisterFailureResponse.from(request.name(), "존재하지 않는 브랜드"));
//                continue;
//            }
//
//            // 등록된 상품 기준 중복 검사 (sellerId, productName 기준)
//            if (productRepository.isDuplicateProduct(seller.getId(), request.name())) {
//                failedProducts.add(
//                    ProductRegisterFailureResponse.from(request.name(), "중복 상품"));
//                continue;
//            }
//
//            successProducts.add(Product.register(
//                seller,
//                optionalCategory.get(),
//                optionalBrand.get(),
//                request.getPriceAsBigDecimal(), // String -> BigDecimal 변환
//                request.name(),
//                request.description(),
//                request.stock()
//            ));
//        }
//
//        // 기획 : 실패한 게 하나도 없을 때에만 등록이 가능
//        if (failedProducts.isEmpty()) {
//            List<Product> products = productRepository.saveAll(successProducts);
//        }
//
//        // 성공 및 실패 리스트 반환
//        return ProductMultipleRegisterResponse.from(successProducts, failedProducts);
//    }

    /**
     * 상품 수정
     */
    @Transactional
    public ProductModifyResponse modify(Long sellerId, Long productId,
        ProductModifyRequest request) {

        // 수정할 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));

        // 유저의 상품이 맞는지 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new CommonException(BError.ACCESS_DENIED, "modify product");
        }

        // 수정할 값이 존재할 경우만 수정
        Product modifiedProduct = product.modify(
            Optional.ofNullable(request.getPriceAsBigDecimal()).orElse(product.getPrice()),
            Optional.ofNullable(request.name()).orElse(product.getName()),
            Optional.ofNullable(request.description()).orElse(product.getDescription()),
            Optional.ofNullable(request.stock()).orElse(product.getStock())
        );

        productRepository.save(modifiedProduct);

        return ProductModifyResponse.from(modifiedProduct);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void remove(Long sellerId, Long productId) {

        // 삭제할 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));

        // 유저의 상품이 맞는지 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new CommonException(BError.ACCESS_DENIED, "remove product");
        }

        productRepository.save(product.remove());
    }

    /**
     * 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductDetailRetrieveResponse retrieveDetail(Long productId) {
        // 상품 정보 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));

        Category smallCategory = categoryRepository.findById(product.getCategory().getId())
            .orElseThrow(() ->
                new CommonException(BError.NOT_EXIST, "category"));
        Category mediumCategory = categoryRepository.findById(smallCategory.getParentId())
            .orElseThrow(() ->
                new CommonException(BError.NOT_EXIST, "category"));
        Category largeCategory = categoryRepository.findById(mediumCategory.getParentId())
            .orElseThrow(() ->
                new CommonException(BError.NOT_EXIST, "category"));

        // 브랜드명 조회
        Brand brand = brandRepository.findById(product.getBrand().getId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "brand"));

        // 이미지들 조회
        List<ProductImage> productImages = productImageRepository.findByProductId(productId);

        // todo: JPA가 알아서 조회
        return ProductDetailRetrieveResponse.from(
            product,
            smallCategory,
            mediumCategory,
            largeCategory,
            product.getSeller(),
            brand,
            productImages);
    }

    //========================= Seller 전용 =========================//

    /**
     * 판매자의 상품 목록 조회
     */
    @Transactional(readOnly = true)
    public Page<ProductDetailRetrieveResponse> retrieveAllBySeller(User seller, int page, int size) {

        Page<Product> products = productRepository.findBySellerId(seller.getId(),
            PageRequest.of(page, size));

        return products.map(product -> {
            Category smallCategory = categoryRepository.findById(product.getCategory().getId())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "category"));
            Category mediumCategory = categoryRepository.findById(smallCategory.getParentId())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "category"));
            Category largeCategory = categoryRepository.findById(mediumCategory.getParentId())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "category"));
            Brand brand = brandRepository.findById(product.getBrand().getId())
                .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "brand"));

            List<ProductImage> productImages = productImageRepository.findByProductId(product.getId());
            return ProductDetailRetrieveResponse.from(
                product,
                smallCategory, mediumCategory, largeCategory,
                product.getSeller(), brand,
                productImages);
        });
    }

    /**
     * 판매자의 상품 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductDetailRetrieveBySellerResponse retrieveDetailBySeller(User seller,
        Long productId) {
        // 상품 정보 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "product"));

        // 유저의 상품이 맞는지 확인
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new CommonException(BError.ACCESS_DENIED, "product");
        }

        Category smallCategory = categoryRepository.findById(product.getCategory().getId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "category"));
        Category mediumCategory = categoryRepository.findById(smallCategory.getParentId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "category"));
        Category largeCategory = categoryRepository.findById(mediumCategory.getParentId())
            .orElseThrow(() -> new CommonException(BError.NOT_EXIST, "category"));

        List<ProductImage> productImages = productImageRepository.findByProductId(productId);
        Integer totalOrders = orderItemRepository.countCompleteOrdersByProductId(productId).orElse(0);
        BigDecimal totalSales = orderItemRepository.calculateSalesByProductId(productId).orElse(BigDecimal.ZERO);

        return ProductDetailRetrieveBySellerResponse.from(
            product, productImages,
            smallCategory, mediumCategory, largeCategory,
            totalOrders, totalSales);
    }

    /**
     * 판매자의 상품 통계 조회
     */
    @Transactional(readOnly = true)
    public SellerStatsRetrieveResponse retrieveStats(User seller) {
        // 판매 중인 상품 체크
        List<Product> products = productRepository.findAllBySellerId(seller.getId());

        BigDecimal totalSales = orderItemRepository.calculateTotalSalesByProductIds(
            products.stream()
                .map(Product::getId)
                .toList())
            .orElse(BigDecimal.ZERO);

        return SellerStatsRetrieveResponse.from(totalSales, products.size());
    }
}
