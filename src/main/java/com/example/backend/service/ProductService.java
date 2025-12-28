package com.example.backend.service;

import com.example.backend.dto.ProductDto;
import com.example.backend.entity.Product;
import com.example.backend.entity.User;
import com.example.backend.exception.ResourceNotFoundException;
import com.example.backend.repository.ProductRepository;
import com.example.backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductService {

    @Autowired
    private RapidApiEmbeddingService embeddingService;

    private static final Logger logger = LoggerFactory.getLogger(ProductService.class);

    @Value("${app.search.similarity-threshold:0.25}")
    private double similarityThreshold;

    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductService(ProductRepository productRepository, UserRepository userRepository) {
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAllProducts() {
        logger.debug("Fetching all products");

        return productRepository.findAll().stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsPaginated(Pageable pageable) {
        logger.debug("Fetching products with pagination: {}", pageable);

        return productRepository.findAll(pageable)
                .map(this::convertToProductDto);
    }

    @Transactional(readOnly = true)
    public ProductDto getProductById(Long id) {
        logger.debug("Fetching product by id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        return convertToProductDto(product);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getActiveProducts() {
        logger.debug("Fetching active products");

        return productRepository.findByIsActiveTrue().stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByCategory(String category) {
        logger.debug("Fetching products by category: {}", category);

        return productRepository.findByCategory(category).stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchProductsByName(String name) {
        logger.debug("Searching products by name: {}", name);

        return productRepository.findByNameContainingIgnoreCase(name).stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        logger.debug("Fetching products by price range: {} - {}", minPrice, maxPrice);

        return productRepository.findByPriceRange(minPrice, maxPrice).stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductDto> getAvailableProducts(Integer minStock) {
        logger.debug("Fetching available products with stock > {}", minStock);

        return productRepository.findAvailableProductsWithStock(minStock).stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<String> getDistinctCategories() {
        logger.debug("Fetching distinct product categories");

        return productRepository.findDistinctActiveCategories();
    }

    @Transactional(readOnly = true)
    public Page<ProductDto> getProductsWithFilters(String category, BigDecimal minPrice, 
                                                  BigDecimal maxPrice, Pageable pageable) {
        logger.debug("Fetching products with filters - category: {}, minPrice: {}, maxPrice: {}", 
                    category, minPrice, maxPrice);

        return productRepository.findProductsWithFilters(category, minPrice, maxPrice, pageable)
                .map(this::convertToProductDto);
    }

    public ProductDto createProduct(ProductDto productDto) throws ExecutionException, InterruptedException {
        logger.debug("Creating new product: {}", productDto.getName());

        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(productDto.getCategory());
        product.setStockQuantity(productDto.getStockQuantity());
        product.setIsActive(true);

        String textToEmbed = productDto.getName() + " " + productDto.getDescription();

        // Generate embedding using RapidAPI
        List<Double> embedding = embeddingService.generateEmbedding(textToEmbed).get(); // blocks for result

        product.setEmbedding(embedding);

        // Set created by current user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            User currentUser = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
            product.setCreatedBy(currentUser);
        }

        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully: {}", savedProduct.getName());

        return convertToProductDto(savedProduct);
    }

    @Transactional
    public List<ProductDto> createProducts(List<ProductDto> productDtos) throws ExecutionException, InterruptedException {
        logger.debug("Starting bulk creation for {} products", productDtos.size());

        // 1. Get the current user once to reuse for all products
        User currentUser = null;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            currentUser = userRepository.findByUsername(auth.getName())
                    .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));
        }

        List<Product> productsToSave = new ArrayList<>();

        for (ProductDto dto : productDtos) {
            Product product = new Product();
            product.setName(dto.getName());
            product.setDescription(dto.getDescription());
            product.setPrice(dto.getPrice());
            product.setCategory(dto.getCategory());
            product.setStockQuantity(dto.getStockQuantity());
            product.setIsActive(true);
            product.setCreatedBy(currentUser);

            // 2. Generate embedding
            String textToEmbed = dto.getName() + " " + dto.getDescription();
            List<Double> embedding = embeddingService.generateEmbedding(textToEmbed).get();
            product.setEmbedding(embedding);

            productsToSave.add(product);
        }

        // 3. Save all at once
        List<Product> savedProducts = productRepository.saveAll(productsToSave);

        return savedProducts.stream()
                .map(this::convertToProductDto)
                .collect(Collectors.toList());
    }

    public ProductDto updateProduct(Long id, ProductDto productDto) {
        logger.debug("Updating product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setCategory(productDto.getCategory());
        product.setStockQuantity(productDto.getStockQuantity());

        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated successfully: {}", updatedProduct.getName());

        return convertToProductDto(updatedProduct);
    }

    public void deleteProduct(Long id) {
        logger.debug("Deleting product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        //product.setIsActive(false);
        //productRepository.save(product);
        //logger.info("Product deactivated successfully: {}", product.getName());

        productRepository.delete(product);
        logger.info("Product deleted successfully: {}", product.getName());
    }

    public void restoreProduct(Long id) {
        logger.debug("Restoring product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        product.setIsActive(true);
        productRepository.save(product);

        logger.info("Product restored successfully: {}", product.getName());
    }

    public List<ProductDto> semanticSearch(String query) throws ExecutionException, InterruptedException {
        // Get embedding for the search query
        List<Double> queryEmbedding = embeddingService.generateEmbedding(query).get();

        // Get all products with embeddings
        List<Product> productsWithEmbeddings = productRepository.findByEmbeddingIsNotNull();

//        for (Product p : productsWithEmbeddings) {
//            double sim = cosineSimilarity(queryEmbedding, p.getEmbedding());
//            System.out.printf("%s -> %.4f%n", p.getName(), sim);
//        }

        // Compute cosine similarity (simple, in-memory version for demo)
        // For large datasets, use pgvector extension or a vector DB for fast search!
        //double SIMILARITY_THRESHOLD = 0.25;
        return productsWithEmbeddings.stream()
                .map(p -> new AbstractMap.SimpleEntry<>(p, cosineSimilarity(queryEmbedding, p.getEmbedding())))
                .filter(entry -> entry.getValue() >= similarityThreshold) // Only keep high matches
                .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .map(e -> convertToProductDto(e.getKey()))
                .collect(Collectors.toList());
    }

    // Computes cosine similarity between two vectors
    private double cosineSimilarity(List<Double> v1, List<Double> v2) {
        double dot = 0, norm1 = 0, norm2 = 0;
        for (int i = 0; i < v1.size(); i++) {
            dot += v1.get(i) * v2.get(i);
            norm1 += v1.get(i) * v1.get(i);
            norm2 += v2.get(i) * v2.get(i);
        }
        return dot / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }


    private ProductDto convertToProductDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getStockQuantity(),
                product.getIsActive(),
                product.getCreatedAt(),
                product.getUpdatedAt(),
                product.getCreatedBy() != null ? product.getCreatedBy().getUsername() : null
        );
    }
}