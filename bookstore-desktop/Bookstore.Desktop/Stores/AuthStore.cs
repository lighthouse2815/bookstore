using Bookstore.Desktop.Models;
using CommunityToolkit.Mvvm.ComponentModel;

namespace Bookstore.Desktop.Stores;

public partial class AuthStore : ObservableObject
{
    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(IsAuthenticated))]
    private string? accessToken;

    [ObservableProperty]
    private string? refreshToken;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(IsAuthenticated))]
    private StaffUserModel? currentUser;

    public bool IsAuthenticated => !string.IsNullOrWhiteSpace(AccessToken) && CurrentUser != null;

    public void SetSession(string accessToken, string refreshToken, StaffUserModel user)
    {
        AccessToken = accessToken;
        RefreshToken = refreshToken;
        CurrentUser = user;
    }

    public void Clear()
    {
        AccessToken = null;
        RefreshToken = null;
        CurrentUser = null;
    }
}
