using System.ComponentModel;
using System.Windows;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.ViewModels;

namespace Bookstore.Desktop;

public partial class MainWindow : Window
{
    private INotifyPropertyChanged? observedViewModel;

    public MainWindow()
    {
        InitializeComponent();
        SourceInitialized += (_, _) => ApplyWindowChrome();
        Loaded += (_, _) => ApplyResponsiveLayout();
        DataContextChanged += OnDataContextChanged;
        Closed += (_, _) => ObserveViewModel(null);
    }

    private void MainWindow_OnSizeChanged(object sender, SizeChangedEventArgs e)
    {
        ApplyResponsiveLayout();
    }

    private void ApplyResponsiveLayout()
    {
        if (!IsLoaded)
        {
            return;
        }

        var isCompact = ActualWidth < 1180;
        SidebarColumn.Width = new GridLength(isCompact ? 156 : 210);
        MainContentHost.Margin = new Thickness(isCompact ? 12 : 18);
        HeaderPanel.Margin = new Thickness(isCompact ? 14 : 20, 0, isCompact ? 14 : 20, 0);
        CurrentUserTextBlock.MaxWidth = isCompact ? 220 : 360;
    }

    private void OnDataContextChanged(object sender, DependencyPropertyChangedEventArgs e)
    {
        ObserveViewModel(e.NewValue as INotifyPropertyChanged);
        ApplyWindowChrome();
    }

    private void ObserveViewModel(INotifyPropertyChanged? viewModel)
    {
        if (observedViewModel != null)
        {
            observedViewModel.PropertyChanged -= OnViewModelPropertyChanged;
        }

        observedViewModel = viewModel;
        if (observedViewModel != null)
        {
            observedViewModel.PropertyChanged += OnViewModelPropertyChanged;
        }
    }

    private void OnViewModelPropertyChanged(object? sender, PropertyChangedEventArgs e)
    {
        if (e.PropertyName == nameof(MainViewModel.IsDarkMode))
        {
            ApplyWindowChrome();
        }
    }

    private void ApplyWindowChrome()
    {
        var isDarkMode = (DataContext as MainViewModel)?.IsDarkMode == true;
        WindowThemeChrome.Apply(this, isDarkMode);
    }
}
