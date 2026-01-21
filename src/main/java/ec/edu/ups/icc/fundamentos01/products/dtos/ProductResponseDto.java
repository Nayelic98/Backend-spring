package ec.edu.ups.icc.fundamentos01.products.dtos;

import java.time.LocalDateTime;
import java.util.List;

public class ProductResponseDto {
    public Long id;
    public String name;
    public String description;
    public double price;

    public CategoryResponseDto category;
    public UserSummaryDto user;

    public LocalDateTime createdAt;
    public LocalDateTime updatedAt;

    public static class UserSummaryDto {
        public Long id;
        public String name;
        public String email;
    }

    public static class CategoryResponseDto {
        public Long id;
        public String name;
        public String description;
    }
    
    public List<CategoryResponseDto> categories;}
