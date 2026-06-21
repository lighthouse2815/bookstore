using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Models;

public class BookModel
{
    public string Id { get; init; } = "";
    public string Title { get; init; } = "";
    public string? Isbn { get; init; }
    public decimal Price { get; init; }
    public int? StockQuantity { get; init; }
    public string? AuthorName { get; init; }
    public string? CategoryName { get; init; }
    public string? ImageUrl { get; init; }

    public string PriceText => CurrencyHelper.Format(Price);
    public string StockText => StockQuantity.HasValue ? StockQuantity.Value.ToString() : "Khong co du lieu ton kho tu API hien tai";
    public string AuthorText => string.IsNullOrWhiteSpace(AuthorName) ? "Chua co tac gia" : AuthorName!;
    public string InventoryStatusText => StockQuantity switch
    {
        null => "Khong ro",
        <= 0 => "Het hang",
        <= 3 => "Rat it",
        <= 8 => "Sap het",
        _ => "Con nhieu"
    };

    public string InventoryStatusVariant => StockQuantity switch
    {
        null => "neutral",
        <= 0 => "danger",
        <= 3 => "danger",
        <= 8 => "warning",
        _ => "success"
    };
}
