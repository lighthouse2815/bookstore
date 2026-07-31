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

    public async Task<OrderResponse> CreateOrderAsync(
        IReadOnlyList<PosCartItemModel> items,
        string paymentMethod,
        string? couponCode,
        string? customerName,
        string? customerPhone)
    {
        var request = new CreatePosOrderRequest
        {
            CustomerName = NormalizeOptional(customerName),
            CustomerPhone = NormalizeOptional(customerPhone),
            PaymentMethod = paymentMethod,
            CouponCode = NormalizeOptional(couponCode),
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

    public async Task<IReadOnlyList<CouponOptionModel>> GetActiveBookCouponsAsync()
    {
        var element = await apiClient.GetAsync("/api/coupons/active");
        if (element.ValueKind != JsonValueKind.Array)
        {
            return Array.Empty<CouponOptionModel>();
        }

        return element.Deserialize<CouponOptionModel[]>(JsonHelper.Options)?
            .Where(coupon =>
                !string.IsNullOrWhiteSpace(coupon.Code)
                && string.Equals(coupon.CouponType, "BOOK", StringComparison.OrdinalIgnoreCase))
            .OrderBy(coupon => coupon.Code, StringComparer.OrdinalIgnoreCase)
            .ToArray()
            ?? Array.Empty<CouponOptionModel>();
    }

    private static string? NormalizeOptional(string? value)
    {
        return string.IsNullOrWhiteSpace(value) ? null : value.Trim();
    }
}
