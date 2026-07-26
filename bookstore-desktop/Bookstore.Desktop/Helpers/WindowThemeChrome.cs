using System.Runtime.InteropServices;
using System.Windows;
using System.Windows.Interop;
using System.Windows.Media;

namespace Bookstore.Desktop.Helpers;

public static class WindowThemeChrome
{
    private const int DwmUseImmersiveDarkMode = 20;
    private const int DwmBorderColor = 34;
    private const int DwmCaptionColor = 35;
    private const int DwmTextColor = 36;

    public static void Apply(Window window, bool isDarkMode)
    {
        if (!OperatingSystem.IsWindowsVersionAtLeast(10))
        {
            return;
        }

        var handle = new WindowInteropHelper(window).Handle;
        if (handle == IntPtr.Zero)
        {
            return;
        }

        var darkModeValue = isDarkMode ? 1 : 0;
        _ = DwmSetWindowAttribute(handle, DwmUseImmersiveDarkMode, ref darkModeValue, sizeof(int));

        var borderColor = ToColorRef(isDarkMode ? Color.FromRgb(51, 65, 85) : Color.FromRgb(221, 227, 234));
        var captionColor = ToColorRef(isDarkMode ? Color.FromRgb(15, 23, 42) : Color.FromRgb(246, 248, 251));
        var textColor = ToColorRef(isDarkMode ? Color.FromRgb(229, 231, 235) : Color.FromRgb(17, 24, 39));

        _ = DwmSetWindowAttributeColor(handle, DwmBorderColor, ref borderColor, sizeof(uint));
        _ = DwmSetWindowAttributeColor(handle, DwmCaptionColor, ref captionColor, sizeof(uint));
        _ = DwmSetWindowAttributeColor(handle, DwmTextColor, ref textColor, sizeof(uint));
    }

    private static uint ToColorRef(Color color)
    {
        return color.R | ((uint)color.G << 8) | ((uint)color.B << 16);
    }

    [DllImport("dwmapi.dll")]
    private static extern int DwmSetWindowAttribute(
        IntPtr windowHandle,
        int attribute,
        ref int attributeValue,
        int attributeSize);

    [DllImport("dwmapi.dll", EntryPoint = "DwmSetWindowAttribute")]
    private static extern int DwmSetWindowAttributeColor(
        IntPtr windowHandle,
        int attribute,
        ref uint attributeValue,
        int attributeSize);
}
