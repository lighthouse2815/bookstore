using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Models;

public sealed class CouponOptionModel
{
    public string Code { get; init; } = "";
    public string? Description { get; init; }
    public string CouponType { get; init; } = "";
    public string DiscountType { get; init; } = "";
    public decimal DiscountValue { get; init; }
    public decimal MinOrderAmount { get; init; }
    public decimal? MaxDiscountAmount { get; init; }

    public string DetailText
    {
        get
        {
            var discountText = string.Equals(DiscountType, "PERCENTAGE", StringComparison.OrdinalIgnoreCase)
                ? $"Giảm {DiscountValue:0.##}%"
                : $"Giảm {CurrencyHelper.Format(DiscountValue)}";
            var minimumText = MinOrderAmount > 0
                ? $"Đơn từ {CurrencyHelper.Format(MinOrderAmount)}"
                : "Không yêu cầu giá trị đơn tối thiểu";

            return string.IsNullOrWhiteSpace(Description)
                ? $"{discountText} · {minimumText}"
                : $"{Description.Trim()} · {discountText} · {minimumText}";
        }
    }
}
