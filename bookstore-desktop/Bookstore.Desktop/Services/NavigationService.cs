using CommunityToolkit.Mvvm.ComponentModel;

namespace Bookstore.Desktop.Services;

public partial class NavigationService : ObservableObject
{
    [ObservableProperty]
    private object? currentViewModel;

    public void NavigateTo(object viewModel)
    {
        CurrentViewModel = viewModel;
    }
}
