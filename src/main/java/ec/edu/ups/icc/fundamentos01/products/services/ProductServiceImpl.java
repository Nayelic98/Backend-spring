package ec.edu.ups.icc.fundamentos01.products.services;

import ec.edu.ups.icc.fundamentos01.categories.entity.CategoryEntity;
import ec.edu.ups.icc.fundamentos01.categories.repository.CategoryRepository;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;
import ec.edu.ups.icc.fundamentos01.users.entities.UserEntity;
import ec.edu.ups.icc.fundamentos01.products.entities.Product;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.repositories.ProductRepository;
import ec.edu.ups.icc.fundamentos01.products.dtos.*;
import ec.edu.ups.icc.fundamentos01.exception.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.exception.domain.NotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;

    public ProductServiceImpl(ProductRepository productRepo, UserRepository userRepo, CategoryRepository categoryRepo) {
        this.productRepo = productRepo;
        this.userRepo = userRepo;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<ProductResponseDto> findAll() {
        return productRepo.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto findById(Long id) {
        return productRepo.findById(id)
                .map(this::toResponseDto)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado con ID: " + id));
    }

    @Override
    public ProductResponseDto create(CreateProductDto dto) {
        UserEntity owner = userRepo.findById(dto.userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + dto.userId));
                Set<CategoryEntity> categories = validarCategorias(dto.categoryIds);

        // 3. CREAR DOMINIO
        Product product = Product.fromDto(dto);

        // 4. CREAR ENTIDAD CON RELACIONES N:N
        ProductEntity entity = product.toEntity(owner);
        entity.setCategories(categories);

        // 5. PERSISTIR
        ProductEntity saved = productRepo.save(entity);

        return toResponseDto(saved);
    }

        // ============== MÉTODOS HELPER ==============

private Set<CategoryEntity> validarCategorias(Set<Long> categoryIds) {
    Set<CategoryEntity> categories = new HashSet<>();
    
    for (Long categoryId : categoryIds) {
        CategoryEntity category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Categoría no encontrada: " + categoryId));
        categories.add(category);
    }
    
    return categories;
}
@Override
public List<ProductResponseDto> findByUserWithFilter(
        Long userId,
        String name,
        Double minPrice,
        Double maxPrice,
        Long categoryId
) {
    if (!userRepo.existsById(userId)) {
        throw new NotFoundException("Usuario no encontrado con ID: " + userId);
    }

    return productRepo.findByOwnerWithFilter(
                    userId,
                    name,
                    categoryId,
                    minPrice,
                    maxPrice
            ).stream()
            .map(this::toResponseDto)
            .toList();
}

     @Override
    public ProductResponseDto update(Long id, UpdateProductDto dto) {
        
        ProductEntity existing = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        // Validar nuevas categorías
        Set<CategoryEntity> newCategories = validarCategorias(dto.categoryIds);

        // Actualizar usando dominio
        Product product = Product.fromEntity(existing);
        product.update(dto);

      // 3. ACTUALIZAR USANDO Instancia de entidad
        existing.setDescription(dto.description != null ? dto.description : existing.getDescription());
        existing.setName(dto.name != null ? dto.name : existing.getName());

        // IMPORTANTE: Limpiar categorías existentes y asignar nuevas
        existing.clearCategories();
        existing.setCategories(newCategories);

        ProductEntity saved = productRepo.save(existing);
        return toResponseDto(saved);
    }

    @Override
    public ProductResponseDto partialUpdate(Long id, PartialUpdateProductDto dto) {
        ProductEntity existing = productRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Producto no encontrado"));

        Product product = Product.fromEntity(existing);
        product.partialUpdate(dto);

        ProductEntity updated = product.toEntity(existing.getOwner());
updated.setId(id);

updated.setCategories(existing.getCategories());

        ProductEntity saved = productRepo.save(updated);
        return toResponseDto(saved);
    }

    @Override
    public void delete(Long id) {
        if (!productRepo.existsById(id)) {
            throw new NotFoundException("Producto no encontrado");
        }
        productRepo.deleteById(id);
    }

    

    @Override
    public List<ProductResponseDto> findByUserId(Long userId) {
        if (!userRepo.existsById(userId)) {
            throw new NotFoundException("Usuario no encontrado con ID: " + userId);
        }
        return productRepo.findByOwnerId(userId).stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
public List<ProductResponseDto> findByCategoryId(Long categoryId) {
    if (!categoryRepo.existsById(categoryId)) {
        throw new NotFoundException("Categoría no encontrada con ID: " + categoryId);
    }

    return productRepo.findByCategories_Id(categoryId).stream()
            .map(this::toResponseDto)
            .toList();
}


    // === HELPER ===

    private ProductResponseDto toResponseDto(ProductEntity entity) {
        ProductResponseDto dto = new ProductResponseDto();
        dto.id = entity.getId();
        dto.name = entity.getName();
        dto.price = entity.getPrice();
        dto.description = entity.getDescription();
        dto.createdAt = entity.getCreatedAt();
        dto.updatedAt = entity.getUpdatedAt();

        // Mapeo User
        if (entity.getOwner() != null) {
            ProductResponseDto.UserSummaryDto userDto = new ProductResponseDto.UserSummaryDto();
            userDto.id = entity.getOwner().getId();
            userDto.name = entity.getOwner().getName();
            userDto.email = entity.getOwner().getEmail();
            dto.user = userDto; 
        }

        // Mapeo Category
       if (entity.getCategories() != null && !entity.getCategories().isEmpty()) {
    List<ProductResponseDto.CategoryResponseDto> categoriesDto = new ArrayList<>();

    for (CategoryEntity category : entity.getCategories()) {
        ProductResponseDto.CategoryResponseDto catDto =
                new ProductResponseDto.CategoryResponseDto();
        catDto.id = category.getId();
        catDto.name = category.getName();
        catDto.description = category.getDescription();
        categoriesDto.add(catDto);
    }

    dto.categories = categoriesDto;
}
        return dto;
    }
}