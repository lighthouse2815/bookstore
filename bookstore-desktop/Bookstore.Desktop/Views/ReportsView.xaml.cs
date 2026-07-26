using System.Windows;
using System.Windows.Controls;

namespace Bookstore.Desktop.Views;

public partial class ReportsView : UserControl
{
    public ReportsView()
    {
        InitializeComponent();
        Loaded += (_, _) => ApplyResponsiveLayout();
    }

    private void ReportsView_OnSizeChanged(object sender, SizeChangedEventArgs e)
    {
        ApplyResponsiveLayout();
    }

    private void ApplyResponsiveLayout()
    {
        if (!IsLoaded)
        {
            return;
        }

        var isNarrow = ActualWidth < 980;
        ApplyHeaderLayout(isNarrow);
        ApplySummaryLayout(isNarrow);
        ApplyPairLayout(
            isNarrow,
            RevenueColumn,
            StatusColumn,
            RevenueStatusBottomRow,
            RevenueCard,
            StatusCard,
            1.4);
        ApplyPairLayout(
            isNarrow,
            TopBooksColumn,
            LowStockColumn,
            CatalogBottomRow,
            TopBooksCard,
            LowStockCard,
            1.25);
    }

    private void ApplyHeaderLayout(bool isNarrow)
    {
        if (isNarrow)
        {
            HeaderTitleColumn.Width = new GridLength(1, GridUnitType.Star);
            HeaderFromColumn.Width = new GridLength(1, GridUnitType.Star);
            HeaderToColumn.Width = GridLength.Auto;
            HeaderLoadColumn.Width = new GridLength(0);
            HeaderFiltersRow.Height = GridLength.Auto;

            Grid.SetRow(HeaderTitlePanel, 0);
            Grid.SetColumn(HeaderTitlePanel, 0);
            Grid.SetColumnSpan(HeaderTitlePanel, 3);
            Grid.SetRow(FromDatePanel, 1);
            Grid.SetColumn(FromDatePanel, 0);
            Grid.SetRow(ToDatePanel, 1);
            Grid.SetColumn(ToDatePanel, 1);
            Grid.SetRow(LoadReportButton, 1);
            Grid.SetColumn(LoadReportButton, 2);

            FromDatePanel.Margin = new Thickness(0, 14, 0, 0);
            ToDatePanel.Margin = new Thickness(12, 14, 0, 0);
            LoadReportButton.Margin = new Thickness(12, 36, 0, 0);
            return;
        }

        HeaderTitleColumn.Width = new GridLength(1, GridUnitType.Star);
        HeaderFromColumn.Width = GridLength.Auto;
        HeaderToColumn.Width = GridLength.Auto;
        HeaderLoadColumn.Width = GridLength.Auto;
        HeaderFiltersRow.Height = new GridLength(0);

        Grid.SetRow(HeaderTitlePanel, 0);
        Grid.SetColumn(HeaderTitlePanel, 0);
        Grid.SetColumnSpan(HeaderTitlePanel, 1);
        Grid.SetRow(FromDatePanel, 0);
        Grid.SetColumn(FromDatePanel, 1);
        Grid.SetRow(ToDatePanel, 0);
        Grid.SetColumn(ToDatePanel, 2);
        Grid.SetRow(LoadReportButton, 0);
        Grid.SetColumn(LoadReportButton, 3);

        FromDatePanel.Margin = new Thickness(18, 0, 0, 0);
        ToDatePanel.Margin = new Thickness(12, 0, 0, 0);
        LoadReportButton.Margin = new Thickness(12, 22, 0, 0);
    }

    private void ApplySummaryLayout(bool isNarrow)
    {
        var cards = new[] { SummaryCard0, SummaryCard1, SummaryCard2, SummaryCard3 };
        if (isNarrow)
        {
            SummaryColumn0.Width = new GridLength(1, GridUnitType.Star);
            SummaryColumn1.Width = new GridLength(1, GridUnitType.Star);
            SummaryColumn2.Width = new GridLength(0);
            SummaryColumn3.Width = new GridLength(0);
            SummaryBottomRow.Height = GridLength.Auto;

            for (var index = 0; index < cards.Length; index++)
            {
                Grid.SetRow(cards[index], index / 2);
                Grid.SetColumn(cards[index], index % 2);
            }

            SummaryCard0.Margin = new Thickness(0, 0, 6, 6);
            SummaryCard1.Margin = new Thickness(6, 0, 0, 6);
            SummaryCard2.Margin = new Thickness(0, 6, 6, 0);
            SummaryCard3.Margin = new Thickness(6, 6, 0, 0);
            return;
        }

        SummaryColumn0.Width = new GridLength(1, GridUnitType.Star);
        SummaryColumn1.Width = new GridLength(1, GridUnitType.Star);
        SummaryColumn2.Width = new GridLength(1, GridUnitType.Star);
        SummaryColumn3.Width = new GridLength(1, GridUnitType.Star);
        SummaryBottomRow.Height = new GridLength(0);

        for (var index = 0; index < cards.Length; index++)
        {
            Grid.SetRow(cards[index], 0);
            Grid.SetColumn(cards[index], index);
            cards[index].Margin = new Thickness(0, 0, index == cards.Length - 1 ? 0 : 12, 0);
        }
    }

    private static void ApplyPairLayout(
        bool isNarrow,
        ColumnDefinition primaryColumn,
        ColumnDefinition secondaryColumn,
        RowDefinition secondaryRow,
        FrameworkElement primaryCard,
        FrameworkElement secondaryCard,
        double primaryWeight)
    {
        if (isNarrow)
        {
            primaryColumn.Width = new GridLength(1, GridUnitType.Star);
            secondaryColumn.Width = new GridLength(0);
            secondaryRow.Height = GridLength.Auto;
            Grid.SetRow(primaryCard, 0);
            Grid.SetColumn(primaryCard, 0);
            Grid.SetRow(secondaryCard, 1);
            Grid.SetColumn(secondaryCard, 0);
            primaryCard.Margin = new Thickness(0, 0, 0, 12);
            secondaryCard.Margin = new Thickness(0);
            return;
        }

        primaryColumn.Width = new GridLength(primaryWeight, GridUnitType.Star);
        secondaryColumn.Width = new GridLength(1, GridUnitType.Star);
        secondaryRow.Height = new GridLength(0);
        Grid.SetRow(primaryCard, 0);
        Grid.SetColumn(primaryCard, 0);
        Grid.SetRow(secondaryCard, 0);
        Grid.SetColumn(secondaryCard, 1);
        primaryCard.Margin = new Thickness(0, 0, 12, 0);
        secondaryCard.Margin = new Thickness(0);
    }
}
