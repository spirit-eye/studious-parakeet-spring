package com.spiriteye.studiousparakeet.Repositories;

import com.spiriteye.studiousparakeet.entitys.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    List<Product> findByNameContaining(String name);

    List<Product> findByPriceBetween(float minPrice, float maxPrice);
}