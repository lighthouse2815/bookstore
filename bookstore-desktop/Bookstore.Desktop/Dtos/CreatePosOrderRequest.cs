namespace Bookstore.Desktop.Dtos;

public class CreatePosOrderRequest
{
    public string? CustomerName { get; init; }
    public string? CustomerPhone { get; init; }
    public string PaymentMethod { get; init; } = "";
    public string? CouponCode { get; init; }
    public IReadOnlyList<CreatePosOrderItemRequest> Items { get; init; } = Array.Empty<CreatePosOrderItemRequest>();
}
