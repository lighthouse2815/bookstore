using System.Globalization;

namespace Bookstore.Desktop.Services;

public static class PosCheckoutRules
{
    private static readonly CultureInfo ViCulture = CultureInfo.GetCultureInfo("vi-VN");

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
        if (string.IsNullOrWhiteSpace(value))
        {
            return false;
        }

        var normalized = value.Trim();
        if (normalized.EndsWith('\u0111') || normalized.EndsWith('\u20ab'))
        {
            normalized = normalized[..^1].TrimEnd();
        }

        var digits = new string(normalized
            .Where(character => char.IsAsciiDigit(character))
            .ToArray());
        if (digits.Length == 0 || normalized.Any(character =>
                !char.IsAsciiDigit(character)
                && character is not '.' and not ','
                && !char.IsWhiteSpace(character)))
        {
            return false;
        }

        return decimal.TryParse(digits, NumberStyles.None, CultureInfo.InvariantCulture, out amount);
    }

    public static string FormatCashReceivedInput(string? value)
    {
        return TryParseCashReceived(value, out var amount)
            ? string.Format(ViCulture, "{0:N0} \u0111", amount)
            : "";
    }
}
