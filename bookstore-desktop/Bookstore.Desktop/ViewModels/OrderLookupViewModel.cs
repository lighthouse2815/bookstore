using System.Collections.ObjectModel;
using System.Net;
using System.Net.Http;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class OrderLookupViewModel : ObservableObject
{
    private readonly OrderService orderService;
    private readonly ReceiptFactory receiptFactory;
    private readonly ReceiptPreviewViewModel receiptPreviewViewModel;
    private readonly NavigationService navigationService;

    public OrderLookupViewModel(
        OrderService orderService,
        ReceiptFactory receiptFactory,
        ReceiptPreviewViewModel receiptPreviewViewModel,
        NavigationService navigationService)
    {
        this.orderService = orderService;
        this.receiptFactory = receiptFactory;
        this.receiptPreviewViewModel = receiptPreviewViewModel;
        this.navigationService = navigationService;
    }

    public ObservableCollection<OrderModel> RecentOrders { get; } = new();

    [ObservableProperty]
    private string query = "";

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(CanReprint))]
    private OrderModel? selectedOrder;

    [ObservableProperty]
    private string message = "";

    [ObservableProperty]
    private bool isLoading;

    public bool CanReprint => SelectedOrder != null && !IsLoading;

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
            Message = "Đang tìm đơn hàng...";
            SelectedOrder = await orderService.FindAsync(Query);
            Message = SelectedOrder == null ? "Không tìm thấy đơn hàng." : "Đã tìm thấy đơn hàng.";
        }
        catch (ApiClientException exception)
        {
            Message = FormatApiError(exception);
        }
        catch (HttpRequestException)
        {
            Message = "Không thể kết nối backend khi tra cứu đơn hàng.";
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
            Message = "Đang tải đơn gần đây...";
            RecentOrders.Clear();
            var orders = await orderService.GetRecentAsync();
            foreach (var order in orders)
            {
                RecentOrders.Add(order);
            }
            Message = orders.Count == 0 ? "Chưa có đơn hàng để hiển thị." : $"Đã tải {orders.Count} đơn gần nhất.";
        }
        catch (ApiClientException exception)
        {
            Message = FormatApiError(exception);
        }
        catch (HttpRequestException)
        {
            Message = "Không thể kết nối backend khi tải đơn gần đây.";
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
    private void Reprint()
    {
        if (SelectedOrder == null)
        {
            Message = "Chọn một đơn hàng trước khi xuất lại hóa đơn.";
            return;
        }

        receiptPreviewViewModel.SetReceipt(receiptFactory.CreateFromOrder(SelectedOrder));
        navigationService.NavigateTo(receiptPreviewViewModel);
    }

    public async Task<bool> LoadOrderAsync(string orderId, string? successMessage = null)
    {
        try
        {
            IsLoading = true;
            Message = "Đang tải chi tiết đơn hàng...";
            SelectedOrder = await orderService.GetByIdAsync(orderId);
            if (SelectedOrder == null)
            {
                Message = "Không tìm thấy chi tiết đơn hàng.";
                return false;
            }

            Message = successMessage ?? "Đã tải chi tiết đơn hàng.";
            return true;
        }
        catch (ApiClientException exception)
        {
            Message = FormatApiError(exception);
            return false;
        }
        catch (HttpRequestException)
        {
            Message = "Không thể kết nối backend khi tải chi tiết đơn hàng.";
            return false;
        }
        catch (Exception exception)
        {
            Message = exception.Message;
            return false;
        }
        finally
        {
            IsLoading = false;
        }
    }

    partial void OnIsLoadingChanged(bool value)
    {
        OnPropertyChanged(nameof(CanReprint));
    }

    private static string FormatApiError(ApiClientException exception)
    {
        return exception.StatusCode switch
        {
            HttpStatusCode.Unauthorized => "Phiên đăng nhập đã hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.",
            HttpStatusCode.Forbidden => "Bạn không có quyền STAFF/ADMIN để tra cứu đơn POS.",
            _ => exception.Message
        };
    }
}
