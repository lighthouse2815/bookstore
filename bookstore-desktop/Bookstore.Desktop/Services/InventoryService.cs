using Bookstore.Desktop.Models;

namespace Bookstore.Desktop.Services;

public class InventoryService
{
    private readonly BookService bookService;

    public InventoryService(BookService bookService)
    {
        this.bookService = bookService;
    }

    public Task<IReadOnlyList<BookModel>> SearchAsync(string? keyword)
    {
        return bookService.SearchAsync(keyword);
    }

    public Task<PagedResult<BookModel>> SearchPageAsync(
        string? keyword,
        int page,
        int size)
    {
        return bookService.SearchPageAsync(keyword, page, size);
    }
}
