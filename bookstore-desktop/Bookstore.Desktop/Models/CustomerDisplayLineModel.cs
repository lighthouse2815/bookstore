using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Models;

public sealed class CustomerDisplayLineModel
{
    public string Title { get; init; } = "";
    public int Quantity { get; init; }
    public decimal UnitPrice { get; init; }
    public decimal LineTotal { get; init; }

    public string UnitPriceText => CurrencyHelper.Format(UnitPrice);
    public string LineTotalText => CurrencyHelper.Format(LineTotal);

    public static CustomerDisplayLineModel FromCartItem(PosCartItemModel item)
    {
        return new CustomerDisplayLineModel
        {
            Title = item.Title,
            Quantity = item.Quantity,
            UnitPrice = item.UnitPrice,
            LineTotal = item.LineTotal
        };
    }

    public static CustomerDisplayLineModel FromReceiptItem(OrderItemModel item)
    {
        return new CustomerDisplayLineModel
        {
            Title = item.BookTitle,
            Quantity = item.Quantity,
            UnitPrice = item.UnitPrice,
            LineTotal = item.LineTotal
        };
    }
}
