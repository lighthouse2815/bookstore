namespace Bookstore.Desktop.Dtos;

public class CreatePosOrderRequest
{
    public string CustomerName { get; init; } = "Khách lẻ";
    public string? CustomerPhone { get; init; }
    public string PaymentMethod { get; init; } = "CASH";
    public string? CouponCode { get; init; }
    public IReadOnlyList<CreatePosOrderItemRequest> Items { get; init; } = Array.Empty<CreatePosOrderItemRequest>();
}
