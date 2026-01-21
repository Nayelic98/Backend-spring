package ec.edu.ups.icc.fundamentos01.products.entities;

import java.util.HashSet;
import java.util.Set;

import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.products.dtos.*;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;

public class Product {
    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer stock;
CategoryEntity category;
    public Product() {
    }

    public Product(String name, Double price, String description) {
        this.name = name;
        this.price = price;
        this.description = description;
        this.stock = 0;
    }

    public Product(Long id, String name, String description, Double price, Integer stock) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.stock = stock;
    }

    // Factory methods
    public static Product fromDto(CreateProductDto dto) {
        return new Product(dto.name, dto.price, dto.description);
    }

    public static Product fromEntity(ProductEntity entity) {
        return new Product(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice(),
                entity.getStock());
    }

    // Conversion methods
    public ProductEntity toEntity(UserEntity owner) {

    ProductEntity entity = new ProductEntity();

    if (this.id != null && this.id > 0) {
        entity.setId(this.id);
    }

    entity.setName(this.name);
    entity.setPrice(this.price);
    entity.setDescription(this.description);
    entity.setStock(this.stock);

    // Relación ManyToOne
    entity.setOwner(owner);

    // 🔥 CORRECCIÓN: convertir CategoryEntity → Set<CategoryEntity>
    Set<CategoryEntity> categories = new HashSet<>();
    categories.add(category);
    entity.setCategories(categories);

    return entity;
}


    public Product update(UpdateProductDto dto) {
        this.name = dto.name;
        this.description = dto.description;
        this.price = dto.price;
        // stock si aplica
        return this;
    }

    // ESTE ES EL MÉTODO QUE TE FALTABA
    public Product partialUpdate(PartialUpdateProductDto dto) {
        if (dto.name != null)
            this.name = dto.name;
        if (dto.description != null)
            this.description = dto.description;
        if (dto.price != null)
            this.price = dto.price;
        return this;
    }

    public Long getId() {
        return id;
    }
}