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
            return await GetByIdAsync(normalized);
        }

        var all = await GetAllAsync();
        return all.FirstOrDefault(order => string.Equals(order.OrderCode, normalized, StringComparison.OrdinalIgnoreCase));
    }

    public async Task<OrderModel?> GetByIdAsync(string orderId)
    {
        var element = await apiClient.GetAsync("/api/staff/pos/orders/" + Uri.EscapeDataString(orderId));
        return MapOrder(element);
    }

    public async Task<IReadOnlyList<OrderModel>> GetRecentAsync()
    {
        return (await GetAllAsync())
            .OrderByDescending(order => order.CreatedAt)
            .Take(30)
            .ToArray();
    }

    private async Task<IReadOnlyList<OrderModel>> GetAllAsync()
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
        var productTotal = JsonHelper.GetDecimal(element, "productTotal", "subtotal");
        var total = JsonHelper.GetDecimal(element, "totalAmount");
        var discount = JsonHelper.GetDecimal(element, "discountAmount", "couponDiscount");
        var final = JsonHelper.GetDecimal(element, "finalAmount");

        return new OrderModel
        {
            OrderId = orderId,
            OrderCode = JsonHelper.GetString(element, "orderCode", "code"),
            Status = JsonHelper.GetString(element, "status", "orderStatus") ?? "",
            PaymentMethod = JsonHelper.GetString(element, "paymentMethod") ?? "",
            PaymentStatus = JsonHelper.GetString(element, "paymentStatus") ?? "",
            CustomerName = JsonHelper.GetString(element, "receiverName", "customerName"),
            CustomerPhone = JsonHelper.GetString(element, "receiverPhone", "customerPhone"),
            ProductTotal = productTotal,
            TotalAmount = total,
            DiscountAmount = discount,
            FinalAmount = final,
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
