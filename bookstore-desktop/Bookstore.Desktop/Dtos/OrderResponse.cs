namespace Bookstore.Desktop.Dtos;

public class OrderResponse
{
    public string OrderId { get; init; } = "";
    public string? OrderCode { get; init; }
    public decimal TotalAmount { get; init; }
    public decimal DiscountAmount { get; init; }
    public decimal FinalAmount { get; init; }
    public string PaymentMethod { get; init; } = "";
    public string PaymentStatus { get; init; } = "";
    public string? Status { get; init; }
    public string? OrderStatus { get; init; }
    public DateTimeOffset? CreatedAt { get; init; }
    public IReadOnlyList<OrderItemResponse> Items { get; init; } = Array.Empty<OrderItemResponse>();
}
