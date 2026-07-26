namespace Bookstore.Desktop.Models;

public sealed record PagedResult<T>(
    IReadOnlyList<T> Items,
    int TotalCount,
    int Page,
    int Size,
    bool HasNext)
{
    public int TotalPages => TotalCount == 0
        ? 0
        : (int)Math.Ceiling((double)TotalCount / Size);
}
