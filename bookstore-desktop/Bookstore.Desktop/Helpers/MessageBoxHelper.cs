using System.Windows;

namespace Bookstore.Desktop.Helpers;

public static class MessageBoxHelper
{
    public static void Info(string message)
    {
        MessageBox.Show(message, "Bookstore POS", MessageBoxButton.OK, MessageBoxImage.Information);
    }

    public static void Error(string message)
    {
        MessageBox.Show(message, "Bookstore POS", MessageBoxButton.OK, MessageBoxImage.Error);
    }
}
