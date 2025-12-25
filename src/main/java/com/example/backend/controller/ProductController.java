package com.example.backend.controller;

import com.example.backend.dto.ProductDto;
import com.example.backend.service.ProductService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts() {
        logger.debug("Fetching all products");

        List<ProductDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<ProductDto>> getProductsPaginated(Pageable pageable) {
        logger.debug("Fetching products with pagination");

        Page<ProductDto> products = productService.getProductsPaginated(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Long id) {
        logger.debug("Fetching product by id: {}", id);

        ProductDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/active")
    public ResponseEntity<List<ProductDto>> getActiveProducts() {
        logger.debug("Fetching active products");

        List<ProductDto> products = productService.getActiveProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductDto>> getProductsByCategory(@PathVariable String category) {
        logger.debug("Fetching products by category: {}", category);

        List<ProductDto> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductDto>> searchProductsByName(@RequestParam String name) {
        logger.debug("Searching products by name: {}", name);

        List<ProductDto> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDto>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        logger.debug("Fetching products by price range: {} - {}", minPrice, maxPrice);

        List<ProductDto> products = productService.getProductsByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProductDto>> getAvailableProducts(@RequestParam(defaultValue = "0") Integer minStock) {
        logger.debug("Fetching available products with stock > {}", minStock);

        List<ProductDto> products = productService.getAvailableProducts(minStock);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getDistinctCategories() {
        logger.debug("Fetching distinct product categories");

        List<String> categories = productService.getDistinctCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<ProductDto>> getProductsWithFilters(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {

        logger.debug("Fetching products with filters - category: {}, minPrice: {}, maxPrice: {}", 
                    category, minPrice, maxPrice);

        Page<ProductDto> products = productService.getProductsWithFilters(category, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(products);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<ProductDto> createProduct(@Valid @RequestBody ProductDto productDto) throws ExecutionException, InterruptedException {
        logger.debug("Creating new product: {}", productDto.getName());

        ProductDto createdProduct = productService.createProduct(productDto);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<ProductDto>> createProducts(@Valid @RequestBody List<ProductDto> productDtos) throws ExecutionException, InterruptedException {
        logger.debug("Creating {} new products in bulk", productDtos.size());

        List<ProductDto> createdProducts = productService.createProducts(productDtos);
        return new ResponseEntity<>(createdProducts, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @productService.getProductById(#id).createdByUsername == authentication.name")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {
        logger.debug("Updating product with id: {}", id);

        ProductDto updatedProduct = productService.updateProduct(id, productDto);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @productService.getProductById(#id).createdByUsername == authentication.name")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        logger.debug("Deleting product with id: {}", id);

        productService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> restoreProduct(@PathVariable Long id) {
        logger.debug("Restoring product with id: {}", id);

        productService.restoreProduct(id);
        return ResponseEntity.ok(Map.of("message", "Product restored successfully"));
    }

    @PostMapping("/semantic-search")
    public List<ProductDto> semanticSearch(@RequestBody Map<String, String> body) throws ExecutionException, InterruptedException {
        String query = body.get("query");
        return productService.semanticSearch(query);
    }

}