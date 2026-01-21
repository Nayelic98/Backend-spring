package ec.edu.ups.icc.fundamentos01.users.services;

import ec.edu.ups.icc.fundamentos01.exception.domain.ConflictException;
import ec.edu.ups.icc.fundamentos01.exception.domain.NotFoundException;
import ec.edu.ups.icc.fundamentos01.products.dtos.ProductResponseDto;
import ec.edu.ups.icc.fundamentos01.products.entities.Product;
import ec.edu.ups.icc.fundamentos01.products.entities.ProductEntity;
import ec.edu.ups.icc.fundamentos01.products.services.ProductService;
import ec.edu.ups.icc.fundamentos01.users.dtos.*;
import ec.edu.ups.icc.fundamentos01.users.entities.User;
import ec.edu.ups.icc.fundamentos01.users.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    
    private final ProductService productService;

    public UserServiceImpl(UserRepository userRepository,
                           ProductService productService) {
        this.repository = userRepository;
        this.productService = productService;
    }
    @Override
    public List<UserResponseDto> findAll() {
        return repository.findAll().stream()
                .map(User::fromEntity)
                .map(User::toResponseDto)
                .toList();
    }
   @Override
public List<ProductResponseDto> findProductsByUser(Long userId) {
    if (!repository.existsById(userId)) {
        throw new NotFoundException("Usuario no encontrado con ID: " + userId);
    }

    return productService.findByUserId(userId);
}


    @Override
    public Object findOne(int id) {
        return repository.findById((long) id)
                .map(User::fromEntity)
                .map(User::toResponseDto)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado con ID: " + id));
    }
    @Override
public List<ProductResponseDto> getProductsByUserIdWithFilters(
        Long userId,
        String name,
        Double minPrice,
        Double maxPrice,
        Long categoryId
) {
    if (!repository.existsById(userId)) {
        throw new NotFoundException("Usuario no encontrado con ID: " + userId);
    }

    return productService.findByUserWithFilter(
            userId,
            name,
            minPrice,
            maxPrice,
            categoryId
    );
}


    @Override
    public UserResponseDto create(CreateUserDto dto) {
        if (repository.findByEmail(dto.email).isPresent()) {
            throw new ConflictException("El email ya está registrado: " + dto.email);
        }

        User user = User.fromDto(dto);
        var savedEntity = repository.save(user.toEntity());
        return User.fromEntity(savedEntity).toResponseDto();
    }

    @Override
    public Object update(int id, UpdateUserDto dto) {
        return repository.findById((long) id)
                .map(User::fromEntity)
                .map(u -> u.update(dto))
                .map(User::toEntity)
                .map(repository::save)
                .map(User::fromEntity)
                .map(User::toResponseDto)
                .orElseThrow(
                        () -> new NotFoundException("No se puede actualizar. Usuario no encontrado con ID: " + id));
    }


    @Override
    public Object partialUpdate(int id, PartialUpdateUserDto dto) {
        return repository.findById((long) id)
                .map(User::fromEntity)
                .map(u -> u.partialUpdate(dto))
                .map(User::toEntity)
                .map(repository::save)
                .map(User::fromEntity)
                .map(User::toResponseDto)
                .orElseThrow(
                        () -> new NotFoundException("No se puede actualizar. Usuario no encontrado con ID: " + id));
    }

    @Override
    public Object delete(int id) {
        if (!repository.existsById((long) id)) {
            throw new NotFoundException("No se puede eliminar. Usuario no encontrado con ID: " + id);
        }
        repository.deleteById((long) id);
        return "Deleted successfully";
    }
}