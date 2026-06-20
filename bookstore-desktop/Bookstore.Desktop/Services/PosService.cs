using System.Text.Json;
using Bookstore.Desktop.Dtos;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;

namespace Bookstore.Desktop.Services;

public class PosService
{
    private readonly ApiClient apiClient;

    public PosService(ApiClient apiClient)
    {
        this.apiClient = apiClient;
    }

    public async Task<OrderResponse> CreateOrderAsync(IReadOnlyList<PosCartItemModel> items, string paymentMethod, string? couponCode)
    {
        var request = new CreatePosOrderRequest
        {
            CustomerName = "Khách lẻ",
            CustomerPhone = null,
            PaymentMethod = paymentMethod,
            CouponCode = string.IsNullOrWhiteSpace(couponCode) ? null : couponCode.Trim(),
            Items = items.Select(item => new CreatePosOrderItemRequest
            {
                BookId = item.BookId,
                Quantity = item.Quantity
            }).ToArray()
        };

        var element = await apiClient.PostAsync("/api/staff/pos/orders", request);
        return element.Deserialize<OrderResponse>(JsonHelper.Options)
            ?? throw new InvalidOperationException("Backend không trả thông tin đơn POS.");
    }
}
