package com.spiriteye.studiousparakeet.dtos;

import java.util.List;

public class ProductSearchFacets {

    private final List<String> brands;

    private final List<String> categories;

    private final Float minPrice;

    private final Float maxPrice;

    public ProductSearchFacets(List<String> brands, List<String> categories, Float minPrice, Float maxPrice) {
        this.brands = brands;
        this.categories = categories;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    public static ProductSearchFacets empty() {
        return new ProductSearchFacets(List.of(), List.of(), null, null);
    }

    public List<String> getBrands() {
        return brands;
    }

    public List<String> getCategories() {
        return categories;
    }

    public Float getMinPrice() {
        return minPrice;
    }

    public Float getMaxPrice() {
        return maxPrice;
    }
}
