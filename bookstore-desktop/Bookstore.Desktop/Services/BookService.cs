using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;

namespace Bookstore.Desktop.Services;

public class BookService
{
    private readonly ApiClient apiClient;

    public BookService(ApiClient apiClient)
    {
        this.apiClient = apiClient;
    }

    public async Task<IReadOnlyList<BookModel>> SearchAsync(string? keyword)
    {
        var normalizedKeyword = keyword?.Trim();
        var path = string.IsNullOrWhiteSpace(normalizedKeyword)
            ? "/api/books"
            : "/api/books/search?keyword=" + Uri.EscapeDataString(normalizedKeyword);
        var element = await apiClient.GetAsync(path);

        if (element.ValueKind != System.Text.Json.JsonValueKind.Array)
        {
            return Array.Empty<BookModel>();
        }

        return element.EnumerateArray()
            .Select(MapBook)
            .Where(book => !string.IsNullOrWhiteSpace(book.Id))
            .ToArray();
    }

    private static BookModel MapBook(System.Text.Json.JsonElement element)
    {
        var author = JsonHelper.GetString(element, "authorName");
        if (author == null && JsonHelper.TryGetProperty(element, out var authorElement, "author"))
        {
            author = JsonHelper.GetString(authorElement, "name") ?? authorElement.ToString();
        }

        var category = JsonHelper.GetString(element, "categoryName");
        if (category == null && JsonHelper.TryGetProperty(element, out var categoryElement, "category"))
        {
            category = JsonHelper.GetString(categoryElement, "name") ?? categoryElement.ToString();
        }

        return new BookModel
        {
            Id = JsonHelper.GetString(element, "id", "bookId") ?? "",
            Title = JsonHelper.GetString(element, "title", "name", "bookTitle") ?? "",
            Isbn = JsonHelper.GetString(element, "isbn"),
            Price = JsonHelper.GetDecimal(element, "price", "unitPrice"),
            StockQuantity = JsonHelper.GetIntNullable(element, "stockQuantity", "quantity", "stock"),
            AuthorName = author,
            CategoryName = category,
            ImageUrl = JsonHelper.GetString(element, "imageUrl", "thumbnailUrl", "cover")
        };
    }
}
