using System.Collections.ObjectModel;
using System.Net;
using System.Net.Http;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class ReportsViewModel : ObservableObject
{
    private readonly ReportService reportService;

    public ReportsViewModel(ReportService reportService)
    {
        this.reportService = reportService;
    }

    public ObservableCollection<RevenuePointModel> Revenue { get; } = new();
    public ObservableCollection<TopBookReportModel> TopBooks { get; } = new();
    public ObservableCollection<OrderStatusReportModel> OrderStatuses { get; } = new();
    public ObservableCollection<LowStockBookReportModel> LowStockBooks { get; } = new();
    public ObservableCollection<RecentOrderReportModel> RecentOrders { get; } = new();

    [ObservableProperty]
    private DashboardSummaryModel summary = new();

    [ObservableProperty]
    private DateTime? fromDate = DateTime.Today.AddDays(-29);

    [ObservableProperty]
    private DateTime? toDate = DateTime.Today;

    [ObservableProperty]
    private string message = "Chọn khoảng thời gian và tải báo cáo.";

    [ObservableProperty]
    [NotifyCanExecuteChangedFor(nameof(LoadCommand))]
    private bool isLoading;

    private bool CanLoad => !IsLoading;

    [RelayCommand(CanExecute = nameof(CanLoad))]
    public async Task LoadAsync()
    {
        if (!FromDate.HasValue || !ToDate.HasValue)
        {
            Message = "Vui lòng chọn đầy đủ ngày bắt đầu và ngày kết thúc.";
            return;
        }

        if (FromDate.Value.Date > ToDate.Value.Date)
        {
            Message = "Ngày bắt đầu không được sau ngày kết thúc.";
            return;
        }

        try
        {
            IsLoading = true;
            Message = "Đang tải dữ liệu báo cáo...";

            var snapshot = await reportService.GetAsync(FromDate.Value.Date, ToDate.Value.Date);
            Summary = snapshot.Summary;
            Replace(Revenue, snapshot.Revenue);
            Replace(TopBooks, snapshot.TopBooks);
            Replace(OrderStatuses, snapshot.OrderStatuses);
            Replace(LowStockBooks, snapshot.LowStockBooks);
            Replace(RecentOrders, snapshot.RecentOrders);

            Message = $"Đã cập nhật báo cáo lúc {DateTime.Now:HH:mm:ss}.";
        }
        catch (ApiClientException exception) when (exception.StatusCode == HttpStatusCode.Forbidden)
        {
            Message = "Chỉ tài khoản ADMIN mới được xem báo cáo tổng hợp.";
        }
        catch (HttpRequestException)
        {
            Message = "Không thể kết nối backend khi tải báo cáo.";
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

    private static void Replace<T>(ObservableCollection<T> target, IEnumerable<T> source)
    {
        target.Clear();
        foreach (var item in source)
        {
            target.Add(item);
        }
    }
}
