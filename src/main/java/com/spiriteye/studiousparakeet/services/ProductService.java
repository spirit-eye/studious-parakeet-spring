package com.spiriteye.studiousparakeet.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.spiriteye.studiousparakeet.Repositories.ProductRepository;
import com.spiriteye.studiousparakeet.dtos.ProductSearchFacets;
import com.spiriteye.studiousparakeet.dtos.ProductSearchRequest;
import com.spiriteye.studiousparakeet.dtos.ProductSearchResponse;
import com.spiriteye.studiousparakeet.entitys.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;


import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class ProductService {

    private static final Map<String, String> SORT_FIELDS = Map.of(
            "price", "price",
            "createdat", "createdAt",
            "brand", "brand",
            "category", "category"
    );

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @Autowired
    private ObjectMapper objectMapper;

    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> searchByName(String name) {
        return productRepository.findByNameContaining(name);
    }

    public List<Product> searchByPriceRange(float minPrice, float maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public ProductSearchResponse<Product> search(ProductSearchRequest request) {
        int page = request.getPage();
        int size = request.getSize();

        StringQuery query = new StringQuery(buildSearchQuery(request));
        query.setPageable(PageRequest.of(page, size, buildSort(request)));

        SearchHits<Product> searchHits = elasticsearchOperations.search(query, Product.class);
        List<Product> products = searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .toList();

        return new ProductSearchResponse<>(products, searchHits.getTotalHits(), page, size, buildFacets(products));
    }

    public List<String> getHotSearches(int size) {
        SearchHits<Product> searchHits = searchProducts(matchAllQuery(), normalizeSize(size));
        return extractSearchTerms(searchHits, size);
    }

    public List<String> getBackgroundSearches(int size) {
        SearchHits<Product> searchHits = searchProducts(matchAllQuery(), normalizeSize(size));
        return extractSearchTerms(searchHits, size);
    }

    public List<String> getRecommendedSearches(String keyword, int size) {
        String query = hasText(keyword) ? buildRecommendationQuery(keyword) : matchAllQuery();
        SearchHits<Product> searchHits = searchProducts(query, normalizeSize(size));
        return extractSearchTerms(searchHits, size);
    }

    public List<String> getSearchSuggestions(String keyword, int size) {
        if (!hasText(keyword)) {
            return List.of();
        }

        SearchHits<Product> searchHits = searchProducts(buildSuggestionQuery(keyword), normalizeSize(size));
        return extractSearchTerms(searchHits, size);
    }

    private String buildSearchQuery(ProductSearchRequest request) {
        validatePriceRange(request);

        boolean hasKeyword = hasText(request.getKeyword());
        boolean hasBrand = hasText(request.getBrand());
        boolean hasCategory = hasText(request.getCategory());
        boolean hasPriceRange = request.getMinPrice() != null || request.getMaxPrice() != null;

        if (!hasKeyword && !hasBrand && !hasCategory && !hasPriceRange) {
            ObjectNode matchAll = objectMapper.createObjectNode();
            matchAll.set("match_all", objectMapper.createObjectNode());
            return matchAll.toString();
        }

        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode bool = root.putObject("bool");
        ArrayNode must = bool.putArray("must");
        ArrayNode filter = bool.putArray("filter");

        if (hasKeyword) {
            ObjectNode multiMatch = must.addObject().putObject("multi_match");
            multiMatch.put("query", request.getKeyword().trim());
            multiMatch.put("type", "best_fields");
            multiMatch.put("operator", "and");
            multiMatch.putArray("fields")
                    .add("name^2")
                    .add("description")
                    .add("brand")
                    .add("category");
        }

        if (hasBrand) {
            filter.addObject().putObject("term").put("brand", request.getBrand().trim());
        }

        if (hasCategory) {
            filter.addObject().putObject("term").put("category", request.getCategory().trim());
        }

        if (hasPriceRange) {
            ObjectNode priceRange = filter.addObject().putObject("range").putObject("price");
            if (request.getMinPrice() != null) {
                priceRange.put("gte", request.getMinPrice());
            }
            if (request.getMaxPrice() != null) {
                priceRange.put("lte", request.getMaxPrice());
            }
        }

        return root.toString();
    }

    private Sort buildSort(ProductSearchRequest request) {
        String sortBy = request.getSortBy();
        if ("relevance".equalsIgnoreCase(sortBy)) {
            return Sort.unsorted();
        }

        String sortField = SORT_FIELDS.get(sortBy.toLowerCase());
        if (sortField == null) {
            throw new IllegalArgumentException("sortBy only supports: relevance, price, createdAt, brand, category");
        }

        Sort.Direction direction = Sort.Direction.fromOptionalString(request.getSortDirection())
                .orElseThrow(() -> new IllegalArgumentException("sortDirection only supports: asc, desc"));
        return Sort.by(direction, sortField);
    }

    private void validatePriceRange(ProductSearchRequest request) {
        if (request.getMinPrice() != null && request.getMaxPrice() != null
                && request.getMinPrice() > request.getMaxPrice()) {
            throw new IllegalArgumentException("minPrice must be less than or equal to maxPrice");
        }
    }

    private ProductSearchFacets buildFacets(List<Product> products) {
        List<String> brands = products.stream()
                .map(Product::getBrand)
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        List<String> categories = products.stream()
                .map(Product::getCategory)
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        Float minPrice = products.stream()
                .map(Product::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(null);

        Float maxPrice = products.stream()
                .map(Product::getPrice)
                .max(Comparator.naturalOrder())
                .orElse(null);

        return new ProductSearchFacets(brands, categories, minPrice, maxPrice);
    }

    private String buildRecommendationQuery(String keyword) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode multiMatch = root.putObject("multi_match");
        multiMatch.put("query", keyword.trim());
        multiMatch.put("type", "best_fields");
        multiMatch.put("operator", "or");
        multiMatch.putArray("fields")
                .add("name^3")
                .add("description")
                .add("brand^2")
                .add("category^2");
        return root.toString();
    }

    private String buildSuggestionQuery(String keyword) {
        ObjectNode root = objectMapper.createObjectNode();
        ObjectNode bool = root.putObject("bool");
        ArrayNode should = bool.putArray("should");

        should.addObject().putObject("match_phrase_prefix").putObject("name").put("query", keyword.trim());
        should.addObject().putObject("match_phrase_prefix").putObject("description").put("query", keyword.trim());
        should.addObject().putObject("prefix").put("brand", keyword.trim());
        should.addObject().putObject("prefix").put("category", keyword.trim());
        bool.put("minimum_should_match", 1);

        return root.toString();
    }

    private String matchAllQuery() {
        ObjectNode matchAll = objectMapper.createObjectNode();
        matchAll.set("match_all", objectMapper.createObjectNode());
        return matchAll.toString();
    }

    private SearchHits<Product> searchProducts(String queryJson, int size) {
        StringQuery query = new StringQuery(queryJson);
        query.setPageable(PageRequest.of(0, size));
        return elasticsearchOperations.search(query, Product.class);
    }

    private List<String> extractSearchTerms(SearchHits<Product> searchHits, int size) {
        return searchHits.getSearchHits()
                .stream()
                .map(SearchHit::getContent)
                .flatMap(product -> Stream.of(product.getName(), product.getBrand(), product.getCategory()))
                .filter(this::hasText)
                .map(String::trim)
                .distinct()
                .limit(normalizeSize(size))
                .toList();
    }

    private int normalizeSize(int size) {
        return Math.min(Math.max(size, 1), 100);
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
