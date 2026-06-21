using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Models;

public class OrderModel
{
    public string OrderId { get; init; } = "";
    public string? OrderCode { get; init; }
    public string Status { get; init; } = "";
    public string PaymentMethod { get; init; } = "";
    public string PaymentStatus { get; init; } = "";
    public decimal TotalAmount { get; init; }
    public decimal FinalAmount { get; init; }
    public DateTimeOffset? CreatedAt { get; init; }
    public IReadOnlyList<OrderItemModel> Items { get; init; } = Array.Empty<OrderItemModel>();

    public string CodeOrId => string.IsNullOrWhiteSpace(OrderCode) ? OrderId : OrderCode!;
    public string TotalText => CurrencyHelper.Format(FinalAmount > 0 ? FinalAmount : TotalAmount);
    public string CreatedAtText => CreatedAt.HasValue ? DateTimeHelper.Format(CreatedAt.Value) : "";
    public string PaymentStatusText => PaymentStatus?.Trim().ToUpperInvariant() switch
    {
        "PAID" => "Da thanh toan",
        "COMPLETED" => "Da thanh toan",
        "SUCCESS" => "Da thanh toan",
        "PENDING" => "Cho thanh toan",
        "FAILED" => "Thanh toan loi",
        _ => string.IsNullOrWhiteSpace(PaymentStatus) ? "Chua ro" : PaymentStatus
    };

    public string PaymentStatusVariant => PaymentStatus?.Trim().ToUpperInvariant() switch
    {
        "PAID" => "success",
        "COMPLETED" => "success",
        "SUCCESS" => "success",
        "PENDING" => "warning",
        "FAILED" => "danger",
        _ => "neutral"
    };
}
