package com.bitreiver.app_server.domain.community.converter;

import com.bitreiver.app_server.domain.community.enums.Category;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class CategoryConverter implements AttributeConverter<Category, String> {
    
    @Override
    public String convertToDatabaseColumn(Category category) {
        if (category == null) {
            return null;
        }
        return category.getCode();
    }
    
    @Override
    public Category convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }
        return Category.fromCode(code);
    }
}
