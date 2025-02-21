package com.sellect.server.brand.application;

import com.sellect.server.brand.controller.response.BrandReadResponse;
import com.sellect.server.brand.domain.Brand;
import com.sellect.server.brand.repository.BrandRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    @Transactional(readOnly = true)
    public List<BrandReadResponse> readAll() {
        List<Brand> brands = brandRepository.findAll();
        return brands.stream()
            .map(BrandReadResponse::from)
            .collect(Collectors.toList());
    }

}
