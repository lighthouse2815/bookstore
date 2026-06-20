package com.bookstore.mobile.feature.book.data

import com.bookstore.mobile.core.network.ApiClient
import com.bookstore.mobile.core.storage.AppSettingsDataStore
import com.bookstore.mobile.core.util.ResultState
import com.bookstore.mobile.feature.book.data.dto.BookDetailDto
import com.bookstore.mobile.feature.book.data.dto.BookDto
import com.bookstore.mobile.feature.book.data.dto.BookPageDetailBookDto
import com.bookstore.mobile.feature.book.data.dto.BookPageDetailDto
import com.bookstore.mobile.shared.model.Book
import com.bookstore.mobile.shared.model.BookDetail
import com.bookstore.mobile.shared.model.Category

class BookRepository(
    private val apiClient: ApiClient,
    private val settingsDataStore: AppSettingsDataStore,
) {
    suspend fun getBooks(keyword: String = ""): ResultState<List<Book>> = call("Khong lay duoc danh sach sach") {
        val service = apiClient.service()
        val response = if (keyword.isBlank()) service.getBooks() else service.searchBooks(keyword.trim())
        val refs = loadReferences()
        val rootUrl = apiClient.rootBaseUrl()
        response.data.orEmpty().map { it.toModel(refs, rootUrl) }
    }

    suspend fun getCategories(): ResultState<List<Category>> = call("Khong lay duoc danh muc") {
        apiClient.service().getCategories().data.orEmpty().map { Category(it.id, it.name) }
    }

    suspend fun getBookDetail(id: String): ResultState<Book> = call("Khong lay duoc chi tiet sach") {
        val rootUrl = apiClient.rootBaseUrl()
        val pageDetail = runCatching { apiClient.service().getBookPageDetail(id).data }.getOrNull()
        if (pageDetail != null) {
            pageDetail.toModel(rootUrl)
        } else {
            val refs = loadReferences()
            val book = apiClient.service().getBook(id).data ?: error("Khong tim thay sach")
            book.toModel(refs, rootUrl)
        }
    }

    private suspend fun loadReferences(): ReferenceMaps {
        val service = apiClient.service()
        val categories = runCatching { service.getCategories().data.orEmpty() }.getOrDefault(emptyList())
        val authors = runCatching { service.getAuthors().data.orEmpty() }.getOrDefault(emptyList())
        val publishers = runCatching { service.getPublishers().data.orEmpty() }.getOrDefault(emptyList())
        return ReferenceMaps(
            categories.associate { it.id to it.name },
            authors.associate { it.id to it.name },
            publishers.associate { it.id to it.name },
        )
    }

    private suspend fun currentRootUrl(): String = settingsDataStore.getBaseUrl().trimEnd('/')

    private fun BookDto.toModel(refs: ReferenceMaps, rootUrl: String): Book {
        val imageUrls = images.sortedBy { it.sortOrder }.mapNotNull { absoluteUrl(it.imageUrl, rootUrl) }
        val cover = images.firstOrNull { it.primaryImage }?.imageUrl ?: images.firstOrNull()?.imageUrl ?: imageUrl
        return Book(
            id = id,
            title = title,
            isbn = isbn,
            description = description,
            price = price,
            stockQuantity = stockQuantity,
            soldCount = soldCount,
            rating = averageRating,
            reviewCount = reviewCount,
            coverUrl = absoluteUrl(cover, rootUrl),
            author = refs.authors[authorId].orEmpty(),
            category = refs.categories[categoryId].orEmpty(),
            publisher = refs.publishers[publisherId].orEmpty(),
            images = imageUrls,
            detail = detail?.toModel(),
        )
    }

    private fun BookPageDetailDto.toModel(rootUrl: String): Book {
        val category = categoryTrail.lastOrNull()?.name.orEmpty()
        return book.toModel(author.name, category, publisher.name, rootUrl)
    }

    private fun BookPageDetailBookDto.toModel(
        authorName: String,
        categoryName: String,
        publisherName: String,
        rootUrl: String,
    ): Book {
        val imageUrls = images.sortedBy { it.sortOrder }.mapNotNull { absoluteUrl(it.imageUrl, rootUrl) }
        val cover = images.firstOrNull { it.primaryImage }?.imageUrl ?: images.firstOrNull()?.imageUrl
        return Book(
            id = id,
            title = title,
            isbn = isbn,
            description = description,
            price = price,
            stockQuantity = stockQuantity,
            soldCount = soldCount,
            rating = averageRating,
            reviewCount = reviewCount,
            coverUrl = absoluteUrl(cover, rootUrl),
            author = authorName,
            category = categoryName,
            publisher = publisherName,
            images = imageUrls,
            detail = detail?.toModel(),
        )
    }

    private fun BookDetailDto.toModel(): BookDetail = BookDetail(
        pageCount = pageCount,
        publicationYear = publicationYear,
        language = language,
        coverType = coverType,
        dimensions = dimensions,
        weight = weight,
        translator = translator,
        edition = edition,
    )

    private fun absoluteUrl(value: String?, rootUrl: String): String? {
        val normalized = value?.trim().orEmpty()
        if (normalized.isBlank()) return null
        if (normalized.startsWith("http://") || normalized.startsWith("https://")) return normalized
        if (normalized.startsWith("/")) return rootUrl.trimEnd('/') + normalized
        return rootUrl.trimEnd('/') + "/" + normalized
    }

    private suspend fun <T> call(
        fallback: String,
        block: suspend () -> T,
    ): ResultState<T> = runCatching { block() }
        .fold(
            onSuccess = { ResultState.Success(it) },
            onFailure = { ResultState.Error(apiClient.errorMessage(it, fallback)) },
        )

    private data class ReferenceMaps(
        val categories: Map<String, String>,
        val authors: Map<String, String>,
        val publishers: Map<String, String>,
    )
}
