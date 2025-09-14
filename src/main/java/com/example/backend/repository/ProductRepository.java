package com.example.backend.repository;

import com.example.backend.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Default JPA methods
    List<Product> findByIsActiveTrue();
    List<Product> findByCategory(String category);
    List<Product> findByNameContainingIgnoreCase(String name);

    // Custom JPQL queries
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :minPrice AND :maxPrice AND p.isActive = true")
    List<Product> findByPriceRange(@Param("minPrice") BigDecimal minPrice, 
                                   @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity > :quantity AND p.isActive = true ORDER BY p.stockQuantity DESC")
    List<Product> findAvailableProductsWithStock(@Param("quantity") Integer quantity);

    @Query("SELECT p FROM Product p WHERE p.category = :category AND p.price <= :maxPrice ORDER BY p.price ASC")
    List<Product> findByCategoryAndMaxPrice(@Param("category") String category, 
                                            @Param("maxPrice") BigDecimal maxPrice);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true ORDER BY p.category")
    List<String> findDistinctActiveCategories();

    // Custom native SQL queries
    @Query(value = """
        SELECT p.*, AVG(p.price) OVER (PARTITION BY p.category) as avg_category_price
        FROM products p 
        WHERE p.is_active = true 
        ORDER BY p.category, p.price
        """, nativeQuery = true)
    List<Product> findProductsWithCategoryAveragePrice();

    @Query(value = """
        SELECT p.category, COUNT(*) as product_count, 
               AVG(p.price) as avg_price, 
               SUM(p.stock_quantity) as total_stock
        FROM products p 
        WHERE p.is_active = true 
        GROUP BY p.category 
        ORDER BY product_count DESC
        """, nativeQuery = true)
    List<Object[]> getCategoryStatistics();

    // Pageable queries
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(:category IS NULL OR p.category = :category) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findProductsWithFilters(@Param("category") String category,
                                          @Param("minPrice") BigDecimal minPrice,
                                          @Param("maxPrice") BigDecimal maxPrice,
                                          Pageable pageable);

    List<Product> findByEmbeddingIsNotNull();
}