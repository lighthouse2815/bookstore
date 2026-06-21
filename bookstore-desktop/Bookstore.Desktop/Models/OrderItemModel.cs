using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Models;

public class OrderItemModel
{
    public string Id { get; init; } = "";
    public string BookId { get; init; } = "";
    public string BookTitle { get; init; } = "";
    public decimal UnitPrice { get; init; }
    public int Quantity { get; init; }
    public decimal LineTotal { get; init; }

    public string UnitPriceText => CurrencyHelper.Format(UnitPrice);
    public string LineTotalText => CurrencyHelper.Format(LineTotal);
}
