namespace Bookstore.Desktop.Dtos;

public class CreatePosOrderItemRequest
{
    public string BookId { get; init; } = "";
    public int Quantity { get; init; }
}
