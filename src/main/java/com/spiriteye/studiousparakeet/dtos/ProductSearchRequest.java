package com.spiriteye.studiousparakeet.dtos;

public class ProductSearchRequest {

    private String keyword;

    private String brand;

    private String category;

    private Float minPrice;

    private Float maxPrice;

    private int page = 0;

    private int size = 10;

    private String sortBy = "relevance";

    private String sortDirection = "desc";

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Float getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Float minPrice) {
        this.minPrice = minPrice;
    }

    public Float getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Float maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getPage() {
        return Math.max(page, 0);
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return Math.min(Math.max(size, 1), 100);
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getSortBy() {
        if (sortBy == null || sortBy.trim().isEmpty()) {
            return "relevance";
        }

        return sortBy.trim();
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public String getSortDirection() {
        if (sortDirection == null || sortDirection.trim().isEmpty()) {
            return "desc";
        }

        return sortDirection.trim();
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }
}
