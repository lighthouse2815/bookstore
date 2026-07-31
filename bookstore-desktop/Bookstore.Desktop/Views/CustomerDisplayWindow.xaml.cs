using System.Runtime.InteropServices;
using System.Windows;
using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Views;

public partial class CustomerDisplayWindow : Window
{
    public CustomerDisplayWindow()
    {
        InitializeComponent();
        Loaded += CustomerDisplayWindow_OnLoaded;
    }

    private void CustomerDisplayWindow_OnLoaded(object sender, RoutedEventArgs e)
    {
        var customerMonitor = FindSecondaryMonitor();
        if (customerMonitor.HasValue)
        {
            var bounds = customerMonitor.Value;
            WindowStartupLocation = WindowStartupLocation.Manual;
            WindowState = WindowState.Normal;
            WindowStyle = WindowStyle.None;
            ResizeMode = ResizeMode.NoResize;
            Left = bounds.Left;
            Top = bounds.Top;
            Width = bounds.Width;
            Height = bounds.Height;
            return;
        }

        WindowThemeChrome.Apply(this, isDarkMode: true);
    }

    private static MonitorBounds? FindSecondaryMonitor()
    {
        MonitorBounds? secondaryMonitor = null;
        _ = EnumDisplayMonitors(
            IntPtr.Zero,
            IntPtr.Zero,
            (monitor, _, _, _) =>
            {
                var monitorInfo = new MonitorInfo
                {
                    Size = Marshal.SizeOf<MonitorInfo>()
                };
                if (GetMonitorInfo(monitor, ref monitorInfo)
                    && (monitorInfo.Flags & MonitorInfoPrimary) == 0)
                {
                    secondaryMonitor = new MonitorBounds(
                        monitorInfo.Monitor.Left,
                        monitorInfo.Monitor.Top,
                        monitorInfo.Monitor.Right - monitorInfo.Monitor.Left,
                        monitorInfo.Monitor.Bottom - monitorInfo.Monitor.Top);
                    return false;
                }

                return true;
            },
            IntPtr.Zero);

        return secondaryMonitor;
    }

    private const uint MonitorInfoPrimary = 1;

    private delegate bool MonitorEnumProcedure(
        IntPtr monitor,
        IntPtr monitorDeviceContext,
        IntPtr monitorRectangle,
        IntPtr data);

    [DllImport("user32.dll")]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool EnumDisplayMonitors(
        IntPtr deviceContext,
        IntPtr clipRectangle,
        MonitorEnumProcedure callback,
        IntPtr data);

    [DllImport("user32.dll", CharSet = CharSet.Auto)]
    [return: MarshalAs(UnmanagedType.Bool)]
    private static extern bool GetMonitorInfo(IntPtr monitor, ref MonitorInfo monitorInfo);

    [StructLayout(LayoutKind.Sequential)]
    private struct NativeRectangle
    {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }

    [StructLayout(LayoutKind.Sequential, CharSet = CharSet.Auto)]
    private struct MonitorInfo
    {
        public int Size;
        public NativeRectangle Monitor;
        public NativeRectangle WorkArea;
        public uint Flags;
    }

    private readonly record struct MonitorBounds(int Left, int Top, int Width, int Height);
}
