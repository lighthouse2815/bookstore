using System.Windows;
using System.Windows.Controls;
using Bookstore.Desktop.Services;
using Bookstore.Desktop.ViewModels;

namespace Bookstore.Desktop.Views;

public partial class PosView : UserControl
{
    private bool isFormattingCashReceived;

    public PosView()
    {
        InitializeComponent();
        Loaded += PosView_OnLoaded;
    }

    private async void PosView_OnLoaded(object sender, RoutedEventArgs e)
    {
        ApplyResponsiveLayout();
        if (DataContext is PosViewModel viewModel)
        {
            await viewModel.LoadCouponsAsync();
        }
    }

    private void PosView_OnSizeChanged(object sender, SizeChangedEventArgs e)
    {
        ApplyResponsiveLayout();
    }

    private void CashReceivedTextBox_OnTextChanged(object sender, TextChangedEventArgs e)
    {
        if (isFormattingCashReceived || sender is not TextBox textBox)
        {
            return;
        }

        var currentText = textBox.Text;
        var digitsToRightOfCaret = currentText
            .Skip(textBox.CaretIndex)
            .Count(char.IsAsciiDigit);
        var formattedText = PosCheckoutRules.FormatCashReceivedInput(currentText);
        if (string.Equals(currentText, formattedText, StringComparison.Ordinal))
        {
            return;
        }

        isFormattingCashReceived = true;
        try
        {
            textBox.SetCurrentValue(TextBox.TextProperty, formattedText);
            textBox.CaretIndex = FindCashCaretIndex(formattedText, digitsToRightOfCaret);
            textBox.GetBindingExpression(TextBox.TextProperty)?.UpdateSource();
        }
        finally
        {
            isFormattingCashReceived = false;
        }
    }

    private static int FindCashCaretIndex(string formattedText, int digitsToRight)
    {
        var suffixStart = formattedText.EndsWith(" \u0111", StringComparison.Ordinal)
            ? formattedText.Length - 2
            : formattedText.Length;
        if (digitsToRight <= 0)
        {
            return suffixStart;
        }

        var seenDigits = 0;
        for (var index = suffixStart - 1; index >= 0; index--)
        {
            if (!char.IsAsciiDigit(formattedText[index]))
            {
                continue;
            }

            seenDigits++;
            if (seenDigits == digitsToRight)
            {
                return index;
            }
        }

        return 0;
    }

    private void ApplyResponsiveLayout()
    {
        if (!IsLoaded)
        {
            return;
        }

        var isNarrow = ActualWidth < 1040;
        if (isNarrow)
        {
            ResultsColumn.Width = new GridLength(1, GridUnitType.Star);
            CartColumn.Width = new GridLength(0);
            ResultsRow.Height = GridLength.Auto;
            CartRow.Height = GridLength.Auto;

            Grid.SetRow(ResultsCard, 0);
            Grid.SetColumn(ResultsCard, 0);
            Grid.SetRow(CartCard, 1);
            Grid.SetColumn(CartCard, 0);

            ResultsCard.Height = 430;
            ResultsCard.Margin = new Thickness(0, 0, 0, 16);
            CartCard.Margin = new Thickness(0);
            WorkspaceScrollViewer.VerticalScrollBarVisibility = ScrollBarVisibility.Auto;
            CartFormScrollViewer.VerticalScrollBarVisibility = ScrollBarVisibility.Disabled;
            return;
        }

        ResultsColumn.Width = new GridLength(1.95, GridUnitType.Star);
        CartColumn.Width = new GridLength(1.55, GridUnitType.Star);
        ResultsRow.Height = new GridLength(1, GridUnitType.Star);
        CartRow.Height = new GridLength(0);

        Grid.SetRow(ResultsCard, 0);
        Grid.SetColumn(ResultsCard, 0);
        Grid.SetRow(CartCard, 0);
        Grid.SetColumn(CartCard, 1);

        ResultsCard.Height = double.NaN;
        ResultsCard.Margin = new Thickness(0, 0, 18, 0);
        CartCard.Margin = new Thickness(0);
        WorkspaceScrollViewer.VerticalScrollBarVisibility = ScrollBarVisibility.Disabled;
        CartFormScrollViewer.VerticalScrollBarVisibility = ScrollBarVisibility.Auto;
    }
}
