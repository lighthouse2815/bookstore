using Bookstore.Desktop.Helpers;

namespace Bookstore.Desktop.Models;

public sealed class DashboardSummaryModel
{
    public decimal TotalRevenue { get; init; }
    public decimal TodayRevenue { get; init; }
    public decimal MonthRevenue { get; init; }
    public long TotalOrders { get; init; }
    public long TodayOrders { get; init; }
    public long PendingOrders { get; init; }
    public long DeliveredOrders { get; init; }
    public long CancelledOrders { get; init; }
    public long TotalUsers { get; init; }
    public long TotalBooks { get; init; }
    public long LowStockBooks { get; init; }
    public long NewCustomers { get; init; }
    public long NewReviews { get; init; }
    public long ActiveCoupons { get; init; }

    public string TotalRevenueText => CurrencyHelper.Format(TotalRevenue);
    public string TodayRevenueText => CurrencyHelper.Format(TodayRevenue);
    public string MonthRevenueText => CurrencyHelper.Format(MonthRevenue);
}

public sealed class RevenuePointModel
{
    public string Label { get; init; } = "";
    public decimal Revenue { get; init; }
    public long Orders { get; init; }

    public string RevenueText => CurrencyHelper.Format(Revenue);
}

public sealed class TopBookReportModel
{
    public string BookId { get; init; } = "";
    public string Title { get; init; } = "";
    public long SoldQuantity { get; init; }
    public decimal Revenue { get; init; }

    public string RevenueText => CurrencyHelper.Format(Revenue);
}

public sealed class OrderStatusReportModel
{
    public string Status { get; init; } = "";
    public long Count { get; init; }

    public string StatusText => Status.Trim().ToUpperInvariant() switch
    {
        "PENDING" => "Chờ xác nhận",
        "CONFIRMED" => "Đã xác nhận",
        "PROCESSING" => "Đang xử lý",
        "SHIPPING" => "Đang giao",
        "DELIVERED" => "Đã giao",
        "CANCELLED" => "Đã hủy",
        _ => string.IsNullOrWhiteSpace(Status) ? "Chưa rõ" : Status
    };
}

public sealed class LowStockBookReportModel
{
    public string BookId { get; init; } = "";
    public string Title { get; init; } = "";
    public int StockQuantity { get; init; }
}

public sealed class RecentOrderReportModel
{
    public string OrderId { get; init; } = "";
    public string OrderCode { get; init; } = "";
    public string CustomerName { get; init; } = "";
    public decimal FinalAmount { get; init; }
    public string Status { get; init; } = "";
    public DateTimeOffset? CreatedAt { get; init; }

    public string CodeOrId => string.IsNullOrWhiteSpace(OrderCode) ? OrderId : OrderCode;
    public string FinalAmountText => CurrencyHelper.Format(FinalAmount);
    public string CreatedAtText => CreatedAt.HasValue ? DateTimeHelper.Format(CreatedAt.Value) : "";
}

public sealed record ReportSnapshot(
    DashboardSummaryModel Summary,
    IReadOnlyList<RevenuePointModel> Revenue,
    IReadOnlyList<TopBookReportModel> TopBooks,
    IReadOnlyList<OrderStatusReportModel> OrderStatuses,
    IReadOnlyList<LowStockBookReportModel> LowStockBooks,
    IReadOnlyList<RecentOrderReportModel> RecentOrders);
