package com.bookstore.bookstore.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.bookstore.bookstore.application.command.CreateCategoryCommand;
import com.bookstore.bookstore.application.command.CategoryTranslationCommand;
import com.bookstore.bookstore.application.command.CreatePublisherCommand;
import com.bookstore.bookstore.application.port.out.ICategoryRepository;
import com.bookstore.bookstore.application.port.out.IPublisherRepository;
import com.bookstore.bookstore.domain.enums.FileProvider;
import com.bookstore.bookstore.domain.enums.FilePurpose;
import com.bookstore.bookstore.domain.enums.FileStatus;
import com.bookstore.bookstore.domain.enums.FileVisibility;
import com.bookstore.bookstore.domain.model.FileAsset;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReferenceImageServiceTest {

    @Mock
    private ICategoryRepository categoryRepository;

    @Mock
    private IPublisherRepository publisherRepository;

    @Mock
    private FileAssetPolicyService fileAssetPolicyService;

    @InjectMocks
    private CategoryService categoryService;

    @InjectMocks
    private PublisherService publisherService;

    @Test
    void createCategory_assignsActivePublicCategoryImage() {
        UUID fileAssetId = UUID.randomUUID();
        FileAsset imageAsset = publicImage(fileAssetId, FilePurpose.CATEGORY_IMAGE);
        when(fileAssetPolicyService.requireActiveAsset(
                fileAssetId, FilePurpose.CATEGORY_IMAGE, FileVisibility.PUBLIC
        )).thenReturn(imageAsset);
        when(categoryRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var category = categoryService.create(new CreateCategoryCommand(
                "LITERATURE",
                java.util.List.of(
                        new CategoryTranslationCommand("vi", "Văn học", "Sách văn học"),
                        new CategoryTranslationCommand("en", "Literature", "Literary books")
                ),
                null,
                fileAssetId
        ));

        assertEquals(fileAssetId, category.getImageFileAssetId());
        assertEquals(imageAsset.getPublicUrl(), category.getImageUrl());
        assertEquals("LITERATURE", category.getCode());
        assertEquals("Literature", category.getTranslations().get("en").name());
        verify(fileAssetPolicyService).requireActiveAsset(
                fileAssetId, FilePurpose.CATEGORY_IMAGE, FileVisibility.PUBLIC
        );
    }

    @Test
    void createPublisher_assignsActivePublicPublisherLogo() {
        UUID fileAssetId = UUID.randomUUID();
        FileAsset logoAsset = publicImage(fileAssetId, FilePurpose.PUBLISHER_LOGO);
        when(fileAssetPolicyService.requireActiveAsset(
                fileAssetId, FilePurpose.PUBLISHER_LOGO, FileVisibility.PUBLIC
        )).thenReturn(logoAsset);
        when(publisherRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var publisher = publisherService.create(new CreatePublisherCommand(
                "Nhà xuất bản", "Mô tả", fileAssetId
        ));

        assertEquals(fileAssetId, publisher.getLogoFileAssetId());
        assertEquals(logoAsset.getPublicUrl(), publisher.getLogoUrl());
        verify(fileAssetPolicyService).requireActiveAsset(
                fileAssetId, FilePurpose.PUBLISHER_LOGO, FileVisibility.PUBLIC
        );
    }

    private static FileAsset publicImage(UUID id, FilePurpose purpose) {
        Instant now = Instant.EPOCH;
        return new FileAsset(
                id,
                FileProvider.R2,
                purpose,
                "bookstore-assets",
                "public/references/image.jpg",
                "https://cdn.example.com/public/references/image.jpg",
                "image.jpg",
                "image/jpeg",
                1_024L,
                "checksum",
                FileVisibility.PUBLIC,
                FileStatus.ACTIVE,
                UUID.randomUUID(),
                now,
                now,
                null
        );
    }
}
