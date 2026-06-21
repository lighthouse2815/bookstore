namespace Bookstore.Desktop.Models;

public class PosCartModel
{
    public IReadOnlyList<PosCartItemModel> Items { get; init; } = Array.Empty<PosCartItemModel>();
    public decimal TotalAmount => Items.Sum(item => item.LineTotal);
}
