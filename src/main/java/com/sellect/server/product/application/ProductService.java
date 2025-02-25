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
import com.sellect.server.product.controller.response.ProductDetailReadResponse;
import com.sellect.server.product.controller.response.ProductDetailRetrieveBySellerResponse;
import com.sellect.server.product.controller.response.ProductModifyResponse;
import com.sellect.server.product.controller.response.ProductMultipleRegisterResponse;
import com.sellect.server.product.controller.response.ProductRegisterFailureResponse;
import com.sellect.server.product.controller.response.ProductRegisterResponse;
import com.sellect.server.product.controller.response.SellerStatsRetrieveResponse;
import com.sellect.server.product.domain.Product;
import com.sellect.server.product.domain.ProductImage;
import com.sellect.server.product.repository.ProductImageRepository;
import com.sellect.server.product.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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
            request.getPriceAsBigDecimal(), // String -> BigDecimal 변환
            request.name(),
            request.description(),
            request.stock()
        ));

        // todo: service 에서 service??? 추후 체크
        // 이미지 저장
        Map<String, MultipartFile> imageMap = new HashMap<>();
        images.forEach(image -> {
            imageMap.put(Objects.requireNonNull(image.getOriginalFilename())
                .substring(0, image.getOriginalFilename().lastIndexOf(".")), image);
        });
        request.imageContexts().forEach(imageContext -> {
            productImageService.registerProductImage(product, imageContext, imageMap.get(imageContext.uuid()));
        });

        return ProductRegisterResponse.from(product);
    }

    @Transactional
    public ProductMultipleRegisterResponse registerMultiple(
        User seller,
        List<ProductRegisterRequest> requests,
        List<MultipartFile> images) {

        List<Product> successProducts = new ArrayList<>();
        List<ProductRegisterFailureResponse> failedProducts = new ArrayList<>();

        // 요청 내 상품명 중복 검증을 위한 Set
        Set<String> requestProductNames = new HashSet<>();

        for (ProductRegisterRequest request : requests) {
            // 요청 내 상품 기준 중복 검사
            if (!requestProductNames.add(request.name())) {
                failedProducts.add(
                    ProductRegisterFailureResponse.from(request.name(), "요청 내 중복된 상품명")
                );
                continue;
            }

            Optional<Category> optionalCategory = categoryRepository.findById(request.categoryId());
            // 존재하지 않는 카테고리 체크
            if (optionalCategory.isEmpty()) {
                failedProducts.add(
                    ProductRegisterFailureResponse.from(request.name(), "존재하지 않는 카테고리"));
                continue;
            }

            // 존재하지 않는 브랜드 체크
            Optional<Brand> optionalBrand = brandRepository.findById(request.brandId());
            if (optionalBrand.isEmpty()) {
                failedProducts.add(
                    ProductRegisterFailureResponse.from(request.name(), "존재하지 않는 브랜드"));
                continue;
            }

            // 등록된 상품 기준 중복 검사 (sellerId, productName 기준)
            if (productRepository.isDuplicateProduct(seller.getId(), request.name())) {
                failedProducts.add(
                    ProductRegisterFailureResponse.from(request.name(), "중복 상품"));
                continue;
            }

            successProducts.add(Product.register(
                seller,
                optionalCategory.get(),
                optionalBrand.get(),
                request.getPriceAsBigDecimal(), // String -> BigDecimal 변환
                request.name(),
                request.description(),
                request.stock()
            ));
        }

        // 기획 : 실패한 게 하나도 없을 때에만 등록이 가능
        if (failedProducts.isEmpty()) {
            List<Product> products = productRepository.saveAll(successProducts);
        }

        // TODO: 이미지 저장

        // 성공 및 실패 리스트 반환
        return ProductMultipleRegisterResponse.from(successProducts, failedProducts);
    }

    @Transactional
    public ProductModifyResponse modify(Long sellerId, Long productId,
        ProductModifyRequest request) {

        // 수정할 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

        // 유저의 상품이 맞는지 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("상품을 수정할 권한이 없습니다.");
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

    @Transactional
    public void remove(Long sellerId, Long productId) {

        // 삭제할 상품이 존재하는지 확인
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));

        // 유저의 상품이 맞는지 확인
        if (!product.getSeller().getId().equals(sellerId)) {
            throw new RuntimeException("상품을 수정할 권한이 없습니다.");
        }

        productRepository.save(product.remove());
    }

    @Transactional(readOnly = true)
    public ProductDetailReadResponse readDetail(Long productId) {
        // 상품 정보 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("존재하지 않는 상품입니다."));

        Category smallCategory = categoryRepository.findById(product.getCategory().getId())
            .orElseThrow(() ->
                new RuntimeException("존재하지 않는 카테고리입니다."));
        Category mediumCategory = categoryRepository.findById(smallCategory.getParentId())
            .orElseThrow(() ->
                new RuntimeException("존재하지 않는 카테고리입니다."));
        Category largeCategory = categoryRepository.findById(mediumCategory.getParentId())
            .orElseThrow(() ->
                new RuntimeException("존재하지 않는 카테고리입니다."));

        // 브랜드명 조회
        Brand brand = brandRepository.findById(product.getBrand().getId())
            .orElseThrow(() -> new RuntimeException("존재하지 않는 브랜드입니다."));

        // 이미지들 조회
        List<ProductImage> productImages = productImageRepository.findByProductId(productId);

        // todo: JPA가 알아서 조회
        return ProductDetailReadResponse.from(
            product,
            smallCategory,
            mediumCategory,
            largeCategory,
            product.getSeller(),
            brand,
            productImages);
    }


    //========================= Seller 전용 =========================//
    @Transactional(readOnly = true)
    public Page<ProductDetailReadResponse> retrieveAllBySeller(User seller, int page, int size) {

        Page<Product> products = productRepository.findBySellerId(seller.getId(), PageRequest.of(page, size));

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
            return ProductDetailReadResponse.from(
                product,
                smallCategory, mediumCategory, largeCategory,
                product.getSeller(), brand,
                productImages);
        });
    }

    @Transactional(readOnly = true)
    public ProductDetailRetrieveBySellerResponse retrieveDetailBySeller(User seller, Long productId) {
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
        Integer totalOrders = orderItemRepository.countCompleteOrdersByProductId(productId);
        BigDecimal totalSales = orderItemRepository.calculateSalesByProductId(productId);

        return ProductDetailRetrieveBySellerResponse.from(
            product, productImages,
            smallCategory, mediumCategory, largeCategory,
            totalOrders, totalSales);
    }

    @Transactional(readOnly = true)
    public SellerStatsRetrieveResponse retrieveStats(User seller) {
        // 판매 중인 상품 체크
        List<Product> products = productRepository.findAllBySellerId(seller.getId());

        System.out.println("products = " + products);
        // todo: products가 비어있을때는? stream이어서 상관없나?
        BigDecimal totalSales = orderItemRepository.calculateTotalSalesByProductIds(
            products.stream()
                .map(Product::getId).toList());

        totalSales = totalSales != null ? totalSales : BigDecimal.ZERO;
        return SellerStatsRetrieveResponse.from(totalSales, products.size());
    }
}
