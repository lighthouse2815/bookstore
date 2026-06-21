namespace Bookstore.Desktop.Dtos;

public class BookResponse
{
    public string Id { get; init; } = "";
    public string Title { get; init; } = "";
    public string? Isbn { get; init; }
    public decimal Price { get; init; }
    public int? StockQuantity { get; init; }
    public string? AuthorName { get; init; }
    public string? CategoryName { get; init; }
    public string? ImageUrl { get; init; }
}
