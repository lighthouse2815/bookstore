using System.Collections.ObjectModel;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class InventoryViewModel : ObservableObject
{
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

    [RelayCommand]
    private async Task SearchAsync()
    {
        try
        {
            IsLoading = true;
            Message = "";
            Books.Clear();
            var books = await inventoryService.SearchAsync(Keyword);
            foreach (var book in books)
            {
                Books.Add(book);
            }
            Message = books.Count == 0 ? "Không có dữ liệu tồn kho phù hợp." : $"Đã tải {books.Count} sách.";
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
}
