using System.Windows;
using System.Windows.Controls;
using System.Windows.Media;
using System.Windows.Shapes;

namespace Bookstore.Desktop.Helpers;

public static class ThemeResourceAdapter
{
    private static readonly HashSet<Color> CardBackgrounds = Colors(
        "#FFFFFF",
        "#FCFDFE",
        "#FAFBFD");

    private static readonly HashSet<Color> AlternateBackgrounds = Colors(
        "#F2F7FE",
        "#F4F7FD",
        "#F5F7FB",
        "#F6F8FB",
        "#F8FAFC",
        "#F8FAFE",
        "#F9FBFF",
        "#FAFBFE",
        "#EEF3FD",
        "#EEF4FF",
        "#FFF7F7");

    private static readonly HashSet<Color> PrimaryTextColors = Colors(
        "#111827",
        "#152A4D",
        "#16325F",
        "#17356E",
        "#1E2B45",
        "#1F2B44",
        "#243753",
        "#2C3E63",
        "#37465C",
        "#37465F",
        "#3C4F6B");

    private static readonly HashSet<Color> SecondaryTextColors = Colors(
        "#4B5563",
        "#52657F",
        "#5F6D86",
        "#64748B",
        "#677792",
        "#6B7C95",
        "#6E7FA5",
        "#7A8CAA",
        "#7C8798",
        "#94A3C3",
        "#9BAACC",
        "#A6B4CF");

    private static readonly HashSet<Color> BorderColors = Colors(
        "#CBD5E1",
        "#CBD7EE",
        "#CDD7E4",
        "#D5DDE8",
        "#D6DFEE",
        "#D9E1EC",
        "#D9E3F2",
        "#DCE5F4",
        "#DDE3EA",
        "#DDE6F4",
        "#E0E6EF",
        "#E4EAF1",
        "#E5EDF8",
        "#E6ECF7",
        "#E9EEF8",
        "#EEF1F6",
        "#EEF2FA");

    private static bool isInitialized;

    public static void Initialize()
    {
        if (isInitialized)
        {
            return;
        }

        isInitialized = true;
        EventManager.RegisterClassHandler(
            typeof(FrameworkElement),
            FrameworkElement.LoadedEvent,
            new RoutedEventHandler(OnElementLoaded));
    }

    private static void OnElementLoaded(object sender, RoutedEventArgs e)
    {
        if (sender is FrameworkElement element)
        {
            Apply(element);
        }
    }

    private static void Apply(FrameworkElement element)
    {
        if (element is TextBox or PasswordBox or ComboBox or DatePicker)
        {
            Remap(element, Control.BackgroundProperty, CardBackgrounds, "TextBoxBackgroundBrush");
            Remap(element, Control.BackgroundProperty, AlternateBackgrounds, "TextBoxBackgroundBrush");
            Remap(element, Control.ForegroundProperty, PrimaryTextColors, "TextBoxForegroundBrush");
            Remap(element, Control.ForegroundProperty, SecondaryTextColors, "SecondaryTextBrush");
            Remap(element, Control.BorderBrushProperty, BorderColors, "TextBoxBorderBrush");
            return;
        }

        if (element is Control control)
        {
            Remap(control, Control.BackgroundProperty, CardBackgrounds, "CardBrush");
            Remap(control, Control.BackgroundProperty, AlternateBackgrounds, "SurfaceAltBrush");
            Remap(control, Control.ForegroundProperty, PrimaryTextColors, "PrimaryTextBrush");
            Remap(control, Control.ForegroundProperty, SecondaryTextColors, "SecondaryTextBrush");
            Remap(control, Control.BorderBrushProperty, BorderColors, "BorderBrushLight");
        }

        if (element is TextBlock textBlock)
        {
            Remap(textBlock, TextBlock.ForegroundProperty, PrimaryTextColors, "PrimaryTextBrush");
            Remap(textBlock, TextBlock.ForegroundProperty, SecondaryTextColors, "SecondaryTextBrush");
        }

        if (element is Border border)
        {
            Remap(border, Border.BackgroundProperty, CardBackgrounds, "CardBrush");
            Remap(border, Border.BackgroundProperty, AlternateBackgrounds, "SurfaceAltBrush");
            Remap(border, Border.BorderBrushProperty, BorderColors, "BorderBrushLight");
        }

        if (element is Panel panel)
        {
            Remap(panel, Panel.BackgroundProperty, CardBackgrounds, "CardBrush");
            Remap(panel, Panel.BackgroundProperty, AlternateBackgrounds, "SurfaceAltBrush");
        }

        if (element is Shape shape)
        {
            Remap(shape, Shape.FillProperty, CardBackgrounds, "CardBrush");
            Remap(shape, Shape.FillProperty, AlternateBackgrounds, "SurfaceAltBrush");
            Remap(shape, Shape.StrokeProperty, BorderColors, "BorderBrushLight");
        }

        if (element is DataGrid dataGrid)
        {
            Remap(dataGrid, DataGrid.HorizontalGridLinesBrushProperty, BorderColors, "BorderBrushLight");
            Remap(dataGrid, DataGrid.VerticalGridLinesBrushProperty, BorderColors, "BorderBrushLight");
        }
    }

    private static void Remap(
        FrameworkElement element,
        DependencyProperty property,
        IReadOnlySet<Color> colors,
        string resourceKey)
    {
        if (element.GetValue(property) is SolidColorBrush brush && colors.Contains(brush.Color))
        {
            element.SetResourceReference(property, resourceKey);
        }
    }

    private static HashSet<Color> Colors(params string[] values)
    {
        return values
            .Select(value => (Color)ColorConverter.ConvertFromString(value))
            .ToHashSet();
    }
}
