package com.foodexpiry.demo;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PostMapping
    public Product addProduct(@RequestBody Product product) {
        return productRepository.save(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(
            @PathVariable Long id,
            @RequestBody Product updatedProduct) {

        Product product = productRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Product not found: " + id));

        product.setName(updatedProduct.getName());
        product.setExpiryDate(updatedProduct.getExpiryDate());
        product.setPrice(updatedProduct.getPrice());
        product.setImageName(updatedProduct.getImageName());

        // FIX: Save the selected unit
        product.setUnit(updatedProduct.getUnit());

        return productRepository.save(product);
    }

    @DeleteMapping("/{id}") 
    public void deleteProduct(@PathVariable Long id) {
        productRepository.deleteById(id);
    }
}