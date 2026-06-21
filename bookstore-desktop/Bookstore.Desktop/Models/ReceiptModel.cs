using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Models;

public class ReceiptModel
{
    public string StoreName { get; init; } = "Bookstore POS";
    public string OrderId { get; init; } = "";
    public string OrderCode { get; init; } = "";
    public DateTimeOffset CreatedAt { get; init; } = DateTimeOffset.Now;
    public string StaffName { get; init; } = "";
    public string PaymentMethod { get; init; } = "";
    public IReadOnlyList<OrderItemModel> Items { get; init; } = Array.Empty<OrderItemModel>();
    public decimal TotalAmount { get; init; }
    public decimal DiscountAmount { get; init; }
    public decimal FinalAmount { get; init; }

    public string TotalText => CurrencyHelper.Format(TotalAmount);
    public string DiscountText => CurrencyHelper.Format(DiscountAmount);
    public string FinalText => CurrencyHelper.Format(FinalAmount);
    public string CreatedAtText => DateTimeHelper.Format(CreatedAt);
}
