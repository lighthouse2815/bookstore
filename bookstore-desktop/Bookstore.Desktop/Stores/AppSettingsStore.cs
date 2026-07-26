using Bookstore.Desktop.Config;
using CommunityToolkit.Mvvm.ComponentModel;
using System.IO;
using System.Windows;
using System.Windows.Media;

namespace Bookstore.Desktop.Stores;

public partial class AppSettingsStore : ObservableObject
{
    private static readonly string ThemePreferencePath = Path.Combine(
        Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
        "BookstorePOS",
        "theme.txt");

    private static readonly IReadOnlyDictionary<string, string> LightThemeBrushes = new Dictionary<string, string>
    {
        ["AppBackgroundBrush"] = "#F6F8FB",
        ["SurfaceBrush"] = "#FFFFFF",
        ["SurfaceAltBrush"] = "#F8FAFC",
        ["CardBrush"] = "#FFFFFF",
        ["SidebarBrush"] = "#1F2937",
        ["SidebarForegroundBrush"] = "#FFFFFF",
        ["SidebarDividerBrush"] = "#4B5563",
        ["PrimaryBrush"] = "#2563EB",
        ["BorderBrushLight"] = "#DDE3EA",
        ["PrimaryTextBrush"] = "#111827",
        ["SecondaryTextBrush"] = "#64748B",
        ["TextBoxBackgroundBrush"] = "#FFFFFF",
        ["TextBoxForegroundBrush"] = "#111827",
        ["TextBoxBorderBrush"] = "#CBD5E1",
        ["InfoBrush"] = "#2563EB",
        ["GridLineBrush"] = "#E5EAF2",
        ["HoverBrush"] = "#F3F6FC",
        ["SelectionBrush"] = "#E4EDFF",
        ["SelectionTextBrush"] = "#17356E",
        ["AccentSoftBrush"] = "#EEF4FF",
        ["AccentSoftTextBrush"] = "#3568D4",
        ["NeutralStatusBrush"] = "#64748B",
        ["NeutralStatusSurfaceBrush"] = "#F1F3F7",
        ["SuccessBrush"] = "#15803D",
        ["SuccessSurfaceBrush"] = "#E9F8EF",
        ["WarningBrush"] = "#A16207",
        ["WarningSurfaceBrush"] = "#FFF5D6",
        ["DangerBrush"] = "#C24141",
        ["DangerSurfaceBrush"] = "#FDECEC"
    };

    private static readonly IReadOnlyDictionary<string, string> DarkThemeBrushes = new Dictionary<string, string>
    {
        ["AppBackgroundBrush"] = "#0F172A",
        ["SurfaceBrush"] = "#111827",
        ["SurfaceAltBrush"] = "#1E293B",
        ["CardBrush"] = "#111827",
        ["SidebarBrush"] = "#020617",
        ["SidebarForegroundBrush"] = "#E5E7EB",
        ["SidebarDividerBrush"] = "#334155",
        ["PrimaryBrush"] = "#60A5FA",
        ["BorderBrushLight"] = "#334155",
        ["PrimaryTextBrush"] = "#E5E7EB",
        ["SecondaryTextBrush"] = "#94A3B8",
        ["TextBoxBackgroundBrush"] = "#0B1220",
        ["TextBoxForegroundBrush"] = "#E5E7EB",
        ["TextBoxBorderBrush"] = "#475569",
        ["InfoBrush"] = "#93C5FD",
        ["GridLineBrush"] = "#273449",
        ["HoverBrush"] = "#182236",
        ["SelectionBrush"] = "#1E3A5F",
        ["SelectionTextBrush"] = "#F8FAFC",
        ["AccentSoftBrush"] = "#172B4D",
        ["AccentSoftTextBrush"] = "#9CC3FF",
        ["NeutralStatusBrush"] = "#B1BDCF",
        ["NeutralStatusSurfaceBrush"] = "#1C2739",
        ["SuccessBrush"] = "#6EE7A0",
        ["SuccessSurfaceBrush"] = "#123322",
        ["WarningBrush"] = "#F2C86B",
        ["WarningSurfaceBrush"] = "#3A2A10",
        ["DangerBrush"] = "#FCA5A5",
        ["DangerSurfaceBrush"] = "#3B1D24"
    };

    public AppSettingsStore(string? apiBaseUrlOverride = null)
    {
        ApiBaseUrl = string.IsNullOrWhiteSpace(apiBaseUrlOverride)
            ? AppConfig.DefaultApiBaseUrl
            : AppConfig.NormalizeApiBaseUrl(apiBaseUrlOverride);
        ApplyTheme(IsDarkMode);
    }

    public string ApiBaseUrl { get; }

    [ObservableProperty]
    private string googleClientId = AppConfig.DefaultGoogleClientId;

    [ObservableProperty]
    private bool isDarkMode = LoadThemePreference();

    public void UpdateGoogleClientId(string value)
    {
        GoogleClientId = string.IsNullOrWhiteSpace(value)
            ? AppConfig.DefaultGoogleClientId
            : value.Trim();
    }

    public void UpdateTheme(bool value)
    {
        IsDarkMode = value;
        ApplyTheme(value);
        SaveThemePreference(value);
    }

    private static bool LoadThemePreference()
    {
        try
        {
            return !File.Exists(ThemePreferencePath)
                || !bool.TryParse(File.ReadAllText(ThemePreferencePath), out var value)
                || value;
        }
        catch (IOException)
        {
            return true;
        }
        catch (UnauthorizedAccessException)
        {
            return true;
        }
    }

    private static void SaveThemePreference(bool value)
    {
        try
        {
            var directory = Path.GetDirectoryName(ThemePreferencePath);
            if (!string.IsNullOrWhiteSpace(directory))
            {
                Directory.CreateDirectory(directory);
            }

            File.WriteAllText(ThemePreferencePath, value.ToString());
        }
        catch (IOException)
        {
            // Theme changes still apply for the current session.
        }
        catch (UnauthorizedAccessException)
        {
            // Theme changes still apply for the current session.
        }
    }

    private static void ApplyTheme(bool isDarkMode)
    {
        if (Application.Current is null)
        {
            return;
        }

        var palette = isDarkMode ? DarkThemeBrushes : LightThemeBrushes;

        foreach (var (key, hexColor) in palette)
        {
            if (ColorConverter.ConvertFromString(hexColor) is not Color color)
            {
                continue;
            }
            Application.Current.Resources[key] = new SolidColorBrush(color);
        }
    }
}
