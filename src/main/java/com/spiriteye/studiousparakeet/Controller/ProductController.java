package com.spiriteye.studiousparakeet.Controller;

import com.spiriteye.studiousparakeet.entitys.Product;
import com.spiriteye.studiousparakeet.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
}