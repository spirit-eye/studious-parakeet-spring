package com.spiriteye.studiousparakeet.dtos;

import java.util.List;

public class ProductSearchResponse<T> {

    private final List<T> content;

    private final long totalHits;

    private final int page;

    private final int size;

    private final int totalPages;

    private final boolean hasNext;

    private final boolean hasPrevious;

    private final ProductSearchFacets facets;

    public ProductSearchResponse(List<T> content, long totalHits, int page, int size) {
        this(content, totalHits, page, size, ProductSearchFacets.empty());
    }

    public ProductSearchResponse(List<T> content, long totalHits, int page, int size, ProductSearchFacets facets) {
        this.content = content;
        this.totalHits = totalHits;
        this.page = page;
        this.size = size;
        this.totalPages = size == 0 ? 0 : (int) Math.ceil((double) totalHits / size);
        this.hasNext = page + 1 < totalPages;
        this.hasPrevious = page > 0 && totalPages > 0;
        this.facets = facets == null ? ProductSearchFacets.empty() : facets;
    }

    public List<T> getContent() {
        return content;
    }

    public long getTotalHits() {
        return totalHits;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public boolean isHasPrevious() {
        return hasPrevious;
    }

    public ProductSearchFacets getFacets() {
        return facets;
    }
}
