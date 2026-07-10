using System.Globalization;

namespace Bookstore.Desktop.Services;

public static class PosCheckoutRules
{
    public static bool CanIncreaseQuantity(int currentQuantity, int? stockQuantity)
    {
        return !stockQuantity.HasValue || currentQuantity < stockQuantity.Value;
    }

    public static bool IsValidQuantity(int quantity)
    {
        return quantity >= 1;
    }

    public static bool CanStartCheckout(bool isCreatingOrder, bool isCartEmpty)
    {
        return !isCreatingOrder && !isCartEmpty;
    }

    public static decimal CalculateChange(decimal cashReceived, decimal totalAmount)
    {
        return cashReceived - totalAmount;
    }

    public static bool HasEnoughCash(decimal cashReceived, decimal totalAmount)
    {
        return cashReceived >= totalAmount;
    }

    public static bool TryParseCashReceived(string? value, out decimal amount)
    {
        amount = 0;
        return !string.IsNullOrWhiteSpace(value)
               && (decimal.TryParse(value, NumberStyles.Number, CultureInfo.CurrentCulture, out amount)
                   || decimal.TryParse(value, NumberStyles.Number, CultureInfo.InvariantCulture, out amount))
               && amount >= 0;
    }
}
