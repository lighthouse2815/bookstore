namespace Bookstore.Desktop.Dtos;

public class OrderItemResponse
{
    public string Id { get; init; } = "";
    public string BookId { get; init; } = "";
    public string BookTitle { get; init; } = "";
    public decimal UnitPrice { get; init; }
    public int Quantity { get; init; }
    public decimal LineTotal { get; init; }
}
