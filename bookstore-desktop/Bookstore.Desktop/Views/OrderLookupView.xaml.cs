using System.Windows;
using System.Windows.Controls;

namespace Bookstore.Desktop.Views;

public partial class OrderLookupView : UserControl
{
    public OrderLookupView()
    {
        InitializeComponent();
        Loaded += (_, _) => ApplyResponsiveLayout();
    }

    private void OrderLookupView_OnSizeChanged(object sender, SizeChangedEventArgs e)
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
        ApplySearchLayout(isNarrow || ActualWidth < 900);

        if (isNarrow)
        {
            ListColumn.Width = new GridLength(1, GridUnitType.Star);
            DetailColumn.Width = new GridLength(0);
            ListRow.Height = GridLength.Auto;
            DetailRow.Height = GridLength.Auto;

            Grid.SetRow(OrdersListCard, 0);
            Grid.SetColumn(OrdersListCard, 0);
            Grid.SetRow(OrderDetailCard, 1);
            Grid.SetColumn(OrderDetailCard, 0);

            OrdersListCard.Height = 390;
            OrdersListCard.Margin = new Thickness(0, 0, 0, 16);
            OrderDetailCard.Margin = new Thickness(0);
            WorkspaceScrollViewer.VerticalScrollBarVisibility = ScrollBarVisibility.Auto;
            return;
        }

        ListColumn.Width = new GridLength(0.95, GridUnitType.Star);
        DetailColumn.Width = new GridLength(1.55, GridUnitType.Star);
        ListRow.Height = new GridLength(1, GridUnitType.Star);
        DetailRow.Height = new GridLength(0);

        Grid.SetRow(OrdersListCard, 0);
        Grid.SetColumn(OrdersListCard, 0);
        Grid.SetRow(OrderDetailCard, 0);
        Grid.SetColumn(OrderDetailCard, 1);

        OrdersListCard.Height = double.NaN;
        OrdersListCard.Margin = new Thickness(0, 0, 18, 0);
        OrderDetailCard.Margin = new Thickness(0);
        WorkspaceScrollViewer.VerticalScrollBarVisibility = ScrollBarVisibility.Disabled;
    }

    private void ApplySearchLayout(bool isNarrow)
    {
        if (isNarrow)
        {
            SearchInputColumn.Width = new GridLength(1, GridUnitType.Star);
            SearchButtonColumn.Width = new GridLength(1, GridUnitType.Star);
            RecentButtonColumn.Width = new GridLength(0);
            SearchSecondaryRow.Height = GridLength.Auto;

            Grid.SetRow(SearchInputHost, 0);
            Grid.SetColumn(SearchInputHost, 0);
            Grid.SetColumnSpan(SearchInputHost, 2);
            Grid.SetRow(SearchButton, 1);
            Grid.SetColumn(SearchButton, 0);
            Grid.SetRow(RecentButton, 1);
            Grid.SetColumn(RecentButton, 1);
            SearchButton.Margin = new Thickness(0, 12, 6, 0);
            RecentButton.Margin = new Thickness(6, 12, 0, 0);
            return;
        }

        SearchInputColumn.Width = new GridLength(1, GridUnitType.Star);
        SearchButtonColumn.Width = new GridLength(150);
        RecentButtonColumn.Width = new GridLength(200);
        SearchSecondaryRow.Height = new GridLength(0);

        Grid.SetRow(SearchInputHost, 0);
        Grid.SetColumn(SearchInputHost, 0);
        Grid.SetColumnSpan(SearchInputHost, 1);
        Grid.SetRow(SearchButton, 0);
        Grid.SetColumn(SearchButton, 1);
        Grid.SetRow(RecentButton, 0);
        Grid.SetColumn(RecentButton, 2);
        SearchButton.Margin = new Thickness(16, 0, 0, 0);
        RecentButton.Margin = new Thickness(18, 0, 0, 0);
    }
}
