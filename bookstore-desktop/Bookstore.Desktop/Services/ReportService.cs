using System.Globalization;
using System.Text.Json;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;

namespace Bookstore.Desktop.Services;

public sealed class ReportService
{
    private readonly ApiClient apiClient;

    public ReportService(ApiClient apiClient)
    {
        this.apiClient = apiClient;
    }

    public async Task<ReportSnapshot> GetAsync(DateTime from, DateTime to)
    {
        var fromValue = from.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);
        var toValue = to.ToString("yyyy-MM-dd", CultureInfo.InvariantCulture);

        var summaryTask = apiClient.GetAsync("/api/admin/dashboard/summary");
        var revenueTask = apiClient.GetAsync(
            $"/api/admin/dashboard/revenue?from={fromValue}&to={toValue}&groupBy=DAY");
        var topBooksTask = apiClient.GetAsync("/api/admin/dashboard/top-books?limit=10");
        var statusesTask = apiClient.GetAsync("/api/admin/dashboard/orders/status");
        var lowStockTask = apiClient.GetAsync("/api/admin/dashboard/low-stock?threshold=10");
        var recentOrdersTask = apiClient.GetAsync("/api/admin/dashboard/recent-orders?limit=10");

        await Task.WhenAll(
            summaryTask,
            revenueTask,
            topBooksTask,
            statusesTask,
            lowStockTask,
            recentOrdersTask);

        return new ReportSnapshot(
            DeserializeObject<DashboardSummaryModel>(await summaryTask),
            DeserializeArray<RevenuePointModel>(await revenueTask),
            DeserializeArray<TopBookReportModel>(await topBooksTask),
            DeserializeArray<OrderStatusReportModel>(await statusesTask),
            DeserializeArray<LowStockBookReportModel>(await lowStockTask),
            DeserializeArray<RecentOrderReportModel>(await recentOrdersTask));
    }

    private static T DeserializeObject<T>(JsonElement element) where T : class, new()
    {
        return element.ValueKind == JsonValueKind.Object
            ? element.Deserialize<T>(JsonHelper.Options) ?? new T()
            : new T();
    }

    private static IReadOnlyList<T> DeserializeArray<T>(JsonElement element)
    {
        return element.ValueKind == JsonValueKind.Array
            ? element.Deserialize<T[]>(JsonHelper.Options) ?? Array.Empty<T>()
            : Array.Empty<T>();
    }
}
