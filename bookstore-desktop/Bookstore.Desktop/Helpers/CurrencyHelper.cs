using System.Globalization;

namespace Bookstore.Desktop.Helpers;

public static class CurrencyHelper
{
    private static readonly CultureInfo ViCulture = CultureInfo.GetCultureInfo("vi-VN");

    public static string Format(decimal value)
    {
        return string.Format(ViCulture, "{0:N0} đ", value);
    }
}
