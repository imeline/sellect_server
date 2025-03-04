package com.sellect.server.brand.application;

import com.sellect.server.brand.controller.response.BrandRetrieveResponse;
import com.sellect.server.brand.repository.BrandRepository;
import com.sellect.server.common.exception.CommonException;
import com.sellect.server.common.exception.enums.BError;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandRetrieveResponse> retrieveBrandsContainingName(String brandName) {
        return brandRepository.findByNameContaining(brandName).stream()
            .map(BrandRetrieveResponse::from)
            .toList();
    }

}
