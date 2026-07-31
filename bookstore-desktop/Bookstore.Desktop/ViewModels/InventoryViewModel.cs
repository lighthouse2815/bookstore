using System.Collections.ObjectModel;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class InventoryViewModel : ObservableObject
{
    private const int DefaultPageSize = 10;
    private readonly InventoryService inventoryService;

    public InventoryViewModel(InventoryService inventoryService)
    {
        this.inventoryService = inventoryService;
    }

    public ObservableCollection<BookModel> Books { get; } = new();

    [ObservableProperty]
    private string keyword = "";

    [ObservableProperty]
    private string message = "";

    [ObservableProperty]
    private bool isLoading;

    [ObservableProperty]
    private int currentPage;

    [ObservableProperty]
    private int pageSize = DefaultPageSize;

    [ObservableProperty]
    private int totalCount;

    [ObservableProperty]
    private bool hasNextPage;

    public int CurrentPageNumber => TotalCount == 0 ? 0 : CurrentPage + 1;
    public int TotalPages => TotalCount == 0 ? 0 : (int)Math.Ceiling((double)TotalCount / PageSize);
    public bool HasPreviousPage => CurrentPage > 0;
    public string PageRangeText
    {
        get
        {
            if (TotalCount == 0)
            {
                return "0 của 0";
            }

            var firstItem = CurrentPage * PageSize + 1;
            var lastItem = Math.Min(firstItem + Books.Count - 1, TotalCount);
            return $"{firstItem} - {lastItem} của {TotalCount}";
        }
    }

    [RelayCommand]
    private Task SearchAsync()
    {
        return LoadPageAsync(0);
    }

    public Task LoadAsync()
    {
        return LoadPageAsync(0);
    }

    [RelayCommand]
    private Task PreviousPageAsync()
    {
        return HasPreviousPage
            ? LoadPageAsync(CurrentPage - 1)
            : Task.CompletedTask;
    }

    [RelayCommand]
    private Task NextPageAsync()
    {
        return HasNextPage
            ? LoadPageAsync(CurrentPage + 1)
            : Task.CompletedTask;
    }

    private async Task LoadPageAsync(int page)
    {
        try
        {
            IsLoading = true;
            Message = "";
            Books.Clear();
            var result = await inventoryService.SearchPageAsync(Keyword, page, PageSize);
            foreach (var book in result.Items)
            {
                Books.Add(book);
            }

            CurrentPage = result.Page;
            PageSize = result.Size;
            TotalCount = result.TotalCount;
            HasNextPage = result.HasNext;
            NotifyPaginationChanged();
            Message = result.TotalCount == 0
                ? "Không có dữ liệu tồn kho phù hợp."
                : $"Đang hiển thị {result.Items.Count} trong tổng số {result.TotalCount} sách.";
        }
        catch (Exception exception)
        {
            Message = exception.Message;
        }
        finally
        {
            IsLoading = false;
        }
    }

    private void NotifyPaginationChanged()
    {
        OnPropertyChanged(nameof(CurrentPageNumber));
        OnPropertyChanged(nameof(TotalPages));
        OnPropertyChanged(nameof(HasPreviousPage));
        OnPropertyChanged(nameof(PageRangeText));
    }
}
