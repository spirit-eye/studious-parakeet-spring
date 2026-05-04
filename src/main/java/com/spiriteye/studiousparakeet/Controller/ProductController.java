package com.spiriteye.studiousparakeet.Controller;

import com.spiriteye.studiousparakeet.dtos.ProductSearchRequest;
import com.spiriteye.studiousparakeet.dtos.ProductSearchResponse;
import com.spiriteye.studiousparakeet.entitys.Product;
import com.spiriteye.studiousparakeet.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productService.saveProduct(product);
    }

    @GetMapping("/search")
    public List<Product> searchProducts(@RequestParam String name) {
        return productService.searchByName(name);
    }

    @GetMapping("/search/price")
    public List<Product> searchProductsByPriceRange(@RequestParam float minPrice, @RequestParam float maxPrice) {
        return productService.searchByPriceRange(minPrice, maxPrice);
    }

    @GetMapping("/es/search")
    public ProductSearchResponse<Product> searchProductsByEs(@ModelAttribute ProductSearchRequest request) {
        return productService.search(request);
    }

    @GetMapping("/es/search/hot")
    public List<String> getHotSearches(@RequestParam(defaultValue = "10") int size) {
        return productService.getHotSearches(size);
    }

    @GetMapping("/es/search/background")
    public List<String> getBackgroundSearches(@RequestParam(defaultValue = "5") int size) {
        return productService.getBackgroundSearches(size);
    }

    @GetMapping("/es/search/recommend")
    public List<String> getRecommendedSearches(@RequestParam(required = false) String keyword,
                                               @RequestParam(defaultValue = "10") int size) {
        return productService.getRecommendedSearches(keyword, size);
    }

    @GetMapping("/es/search/suggestions")
    public List<String> getSearchSuggestions(@RequestParam String keyword,
                                             @RequestParam(defaultValue = "10") int size) {
        return productService.getSearchSuggestions(keyword, size);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> handleInvalidRequest(IllegalArgumentException exception) {
        return Map.of("message", exception.getMessage());
    }
}
