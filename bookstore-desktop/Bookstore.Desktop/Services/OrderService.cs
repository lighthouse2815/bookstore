using System.Text.Json;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;

namespace Bookstore.Desktop.Services;

public class OrderService
{
    private readonly ApiClient apiClient;

    public OrderService(ApiClient apiClient)
    {
        this.apiClient = apiClient;
    }

    public async Task<OrderModel?> FindAsync(string query)
    {
        var normalized = query.Trim();
        if (Guid.TryParse(normalized, out _))
        {
            var element = await apiClient.GetAsync("/api/staff/pos/orders/" + Uri.EscapeDataString(normalized));
            return MapOrder(element);
        }

        var all = await GetRecentAsync();
        return all.FirstOrDefault(order => string.Equals(order.OrderCode, normalized, StringComparison.OrdinalIgnoreCase));
    }

    public async Task<IReadOnlyList<OrderModel>> GetRecentAsync()
    {
        var element = await apiClient.GetAsync("/api/staff/pos/orders");
        if (element.ValueKind != JsonValueKind.Array)
        {
            return Array.Empty<OrderModel>();
        }

        return element.EnumerateArray()
            .Select(MapOrder)
            .Where(order => order != null)
            .Select(order => order!)
            .OrderByDescending(order => order.CreatedAt)
            .Take(30)
            .ToArray();
    }

    private static OrderModel? MapOrder(JsonElement element)
    {
        var orderId = JsonHelper.GetString(element, "orderId", "id");
        if (string.IsNullOrWhiteSpace(orderId))
        {
            return null;
        }

        var items = Array.Empty<OrderItemModel>();
        if (JsonHelper.TryGetProperty(element, out var itemsElement, "items") && itemsElement.ValueKind == JsonValueKind.Array)
        {
            items = itemsElement.EnumerateArray()
                .Select(MapItem)
                .ToArray();
        }

        var createdAtText = JsonHelper.GetString(element, "createdAt");
        DateTimeOffset? createdAt = DateTimeOffset.TryParse(createdAtText, out var parsedDate) ? parsedDate : null;
        var total = JsonHelper.GetDecimal(element, "totalAmount");
        var final = JsonHelper.GetDecimal(element, "finalAmount");

        return new OrderModel
        {
            OrderId = orderId,
            OrderCode = JsonHelper.GetString(element, "orderCode", "code"),
            Status = JsonHelper.GetString(element, "status", "orderStatus") ?? "",
            PaymentMethod = JsonHelper.GetString(element, "paymentMethod") ?? "",
            PaymentStatus = JsonHelper.GetString(element, "paymentStatus") ?? "",
            TotalAmount = total,
            FinalAmount = final == 0 ? total : final,
            CreatedAt = createdAt,
            Items = items
        };
    }

    private static OrderItemModel MapItem(JsonElement element)
    {
        return new OrderItemModel
        {
            Id = JsonHelper.GetString(element, "id", "orderItemId") ?? "",
            BookId = JsonHelper.GetString(element, "bookId") ?? "",
            BookTitle = JsonHelper.GetString(element, "bookTitle", "title", "name") ?? "",
            UnitPrice = JsonHelper.GetDecimal(element, "unitPrice", "price"),
            Quantity = JsonHelper.GetIntNullable(element, "quantity") ?? 0,
            LineTotal = JsonHelper.GetDecimal(element, "lineTotal", "total")
        };
    }
}
