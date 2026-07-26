using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Models;

public class ReceiptModel
{
    public string StoreName { get; init; } = "Bookstore POS";
    public string OrderId { get; init; } = "";
    public string OrderCode { get; init; } = "";
    public DateTimeOffset CreatedAt { get; init; } = DateTimeOffset.Now;
    public string StaffName { get; init; } = "";
    public string? CustomerName { get; init; }
    public string? CustomerPhone { get; init; }
    public string PaymentMethod { get; init; } = "";
    public decimal? CashReceived { get; init; }
    public decimal? ChangeAmount { get; init; }
    public IReadOnlyList<OrderItemModel> Items { get; init; } = Array.Empty<OrderItemModel>();
    public decimal TotalAmount { get; init; }
    public decimal DiscountAmount { get; init; }
    public decimal FinalAmount { get; init; }

    public string TotalText => CurrencyHelper.Format(TotalAmount);
    public string DiscountText => CurrencyHelper.Format(DiscountAmount);
    public string FinalText => CurrencyHelper.Format(FinalAmount);
    public string CreatedAtText => DateTimeHelper.Format(CreatedAt);
    public bool HasStaffName => !string.IsNullOrWhiteSpace(StaffName);
    public bool HasCustomerName => !string.IsNullOrWhiteSpace(CustomerName);
    public bool HasCustomerPhone => !string.IsNullOrWhiteSpace(CustomerPhone);
    public bool HasCashReceived => CashReceived.HasValue;
    public string CashReceivedText => CashReceived.HasValue ? CurrencyHelper.Format(CashReceived.Value) : "";
    public string ChangeAmountText => ChangeAmount.HasValue ? CurrencyHelper.Format(ChangeAmount.Value) : "";
    public string PaymentMethodText => PaymentMethod.Trim().ToUpperInvariant() switch
    {
        "CASH" => "Tiền mặt",
        "BANK_TRANSFER" => "Chuyển khoản",
        "BANK_TRANSFER_QR" => "Chuyển khoản QR",
        "COD" => "COD",
        _ => PaymentMethod
    };
}
