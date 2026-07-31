package com.bookstore.bookstore.presentation.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.bookstore.bookstore.domain.enums.CategoryLocale;
import com.bookstore.bookstore.domain.exception.DomainErrorCode;
import com.bookstore.bookstore.domain.exception.DomainException;
import com.bookstore.bookstore.domain.model.Category;
import com.bookstore.bookstore.domain.model.CategoryTranslation;
import com.bookstore.bookstore.presentation.request.CategoryTranslationRequest;
import com.bookstore.bookstore.presentation.request.CreateCategoryRequest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class CategoryWebMapperTest {

    private final CategoryWebMapper categoryWebMapper = new CategoryWebMapper();

    @Test
    void toCreateCommand_mapsLocaleCodeToDomainEnum() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                "LITERATURE",
                List.of(
                        new CategoryTranslationRequest("vi", "Văn học", "Sách văn học"),
                        new CategoryTranslationRequest("en", "Literature", "Literary books")
                ),
                null,
                null
        );

        var command = categoryWebMapper.toCreateCommand(request);

        assertEquals(CategoryLocale.VI, command.translations().get(0).locale());
        assertEquals(CategoryLocale.EN, command.translations().get(1).locale());
    }

    @Test
    void toCreateCommand_whenLocaleIsUnsupported_rejects() {
        CreateCategoryRequest request = new CreateCategoryRequest(
                "LITERATURE",
                List.of(new CategoryTranslationRequest("fr", "Littérature", null)),
                null,
                null
        );

        DomainException exception = assertThrows(
                DomainException.class,
                () -> categoryWebMapper.toCreateCommand(request)
        );

        assertEquals(DomainErrorCode.INVALID_CATEGORY_LOCALE, exception.getErrorCode());
    }

    @Test
    void toCategoryResponse_keepsLowercaseStringContract() {
        Category category = new Category(
                UUID.randomUUID(),
                "LITERATURE",
                "Văn học",
                "Sách văn học",
                Map.of(
                        CategoryLocale.VI,
                        new CategoryTranslation(CategoryLocale.VI, "Văn học", "Sách văn học"),
                        CategoryLocale.EN,
                        new CategoryTranslation(CategoryLocale.EN, "Literature", "Literary books")
                ),
                null,
                null,
                Instant.EPOCH,
                Instant.EPOCH,
                null
        );

        var response = categoryWebMapper.toCategoryResponse(category);

        assertEquals("vi", response.translations().get("vi").locale());
        assertEquals("Văn học", response.translations().get("vi").name());
        assertEquals("en", response.translations().get("en").locale());
        assertEquals("Literature", response.translations().get("en").name());
    }
}
