using System.Collections.ObjectModel;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class OrderLookupViewModel : ObservableObject
{
    private readonly OrderService orderService;

    public OrderLookupViewModel(OrderService orderService)
    {
        this.orderService = orderService;
    }

    public ObservableCollection<OrderModel> RecentOrders { get; } = new();

    [ObservableProperty]
    private string query = "";

    [ObservableProperty]
    private OrderModel? selectedOrder;

    [ObservableProperty]
    private string message = "";

    [ObservableProperty]
    private bool isLoading;

    [RelayCommand]
    private async Task SearchAsync()
    {
        if (string.IsNullOrWhiteSpace(Query))
        {
            Message = "Nhập order id hoặc order code.";
            return;
        }

        try
        {
            IsLoading = true;
            Message = "";
            SelectedOrder = await orderService.FindAsync(Query);
            Message = SelectedOrder == null ? "Không tìm thấy đơn hàng." : "Đã tìm thấy đơn hàng.";
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

    [RelayCommand]
    private async Task RefreshRecentAsync()
    {
        try
        {
            IsLoading = true;
            Message = "";
            RecentOrders.Clear();
            var orders = await orderService.GetRecentAsync();
            foreach (var order in orders)
            {
                RecentOrders.Add(order);
            }
            Message = $"Đã tải {orders.Count} đơn gần nhất.";
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
