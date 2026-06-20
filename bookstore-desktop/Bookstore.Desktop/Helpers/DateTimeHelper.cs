using System.Globalization;

namespace Bookstore.Desktop.Helpers;

public static class DateTimeHelper
{
    private static readonly CultureInfo ViCulture = CultureInfo.GetCultureInfo("vi-VN");

    public static string Format(DateTimeOffset value)
    {
        return value.ToLocalTime().ToString("dd/MM/yyyy HH:mm", ViCulture);
    }
}
