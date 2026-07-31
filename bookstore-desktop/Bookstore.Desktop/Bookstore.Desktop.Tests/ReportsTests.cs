using System.Net;
using System.Net.Http;
using System.Text;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using Bookstore.Desktop.Stores;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace Bookstore.Desktop.Tests;

[TestClass]
public class ReportsTests
{
    [TestMethod]
    public async Task Report_service_maps_all_dashboard_payloads()
    {
        var handler = new DashboardHandler();
        var settings = new AppSettingsStore("http://example.test");
        var auth = new AuthStore();
        auth.SetSession(
            "access-token",
            "refresh-token",
            new StaffUserModel { Username = "admin", Roles = new[] { "ADMIN" } });
        var service = new ReportService(new ApiClient(settings, auth, handler));

        var snapshot = await service.GetAsync(
            new DateTime(2026, 7, 1),
            new DateTime(2026, 7, 24));

        Assert.AreEqual(9_500_000m, snapshot.Summary.TotalRevenue);
        Assert.AreEqual(125, snapshot.Summary.TotalOrders);
        Assert.AreEqual(1, snapshot.Revenue.Count);
        Assert.AreEqual("24/07", snapshot.Revenue[0].Label);
        Assert.AreEqual(2, snapshot.TopBooks[0].SoldQuantity);
        Assert.AreEqual("PENDING", snapshot.OrderStatuses[0].Status);
        Assert.AreEqual(4, snapshot.LowStockBooks[0].StockQuantity);
        Assert.AreEqual("POS-001", snapshot.RecentOrders[0].OrderCode);
        Assert.IsTrue(handler.RequestPaths.Any(path =>
            path.Contains("from=2026-07-01", StringComparison.Ordinal)
            && path.Contains("to=2026-07-24", StringComparison.Ordinal)));
    }

    [TestMethod]
    public void Staff_user_exposes_admin_capability_from_roles()
    {
        Assert.IsTrue(new StaffUserModel { Roles = new[] { "ADMIN" } }.IsAdmin);
        Assert.IsTrue(new StaffUserModel { Roles = new[] { "admin" } }.IsAdmin);
        Assert.IsFalse(new StaffUserModel { Roles = new[] { "STAFF" } }.IsAdmin);
    }

    private sealed class DashboardHandler : HttpMessageHandler
    {
        private readonly object sync = new();

        public List<string> RequestPaths { get; } = new();

        protected override Task<HttpResponseMessage> SendAsync(
            HttpRequestMessage request,
            CancellationToken cancellationToken)
        {
            var path = request.RequestUri?.PathAndQuery ?? "";
            lock (sync)
            {
                RequestPaths.Add(path);
            }

            var payload = path switch
            {
                var value when value.StartsWith("/api/admin/dashboard/summary", StringComparison.Ordinal) => """
                    {"success":true,"data":{"totalRevenue":9500000,"todayRevenue":500000,"monthRevenue":4200000,"totalOrders":125,"todayOrders":4,"pendingOrders":3,"deliveredOrders":100,"cancelledOrders":5,"totalUsers":70,"totalBooks":48,"lowStockBooks":2,"newCustomers":1,"newReviews":2,"activeCoupons":3}}
                    """,
                var value when value.StartsWith("/api/admin/dashboard/revenue", StringComparison.Ordinal) => """
                    {"success":true,"data":[{"label":"24/07","revenue":500000,"orders":4}]}
                    """,
                var value when value.StartsWith("/api/admin/dashboard/top-books", StringComparison.Ordinal) => """
                    {"success":true,"data":[{"bookId":"74bd5c64-0df8-4db5-8d78-6d91d76efcd5","title":"Dế Mèn phiêu lưu ký","soldQuantity":2,"revenue":180000}]}
                    """,
                var value when value.StartsWith("/api/admin/dashboard/orders/status", StringComparison.Ordinal) => """
                    {"success":true,"data":[{"status":"PENDING","count":3}]}
                    """,
                var value when value.StartsWith("/api/admin/dashboard/low-stock", StringComparison.Ordinal) => """
                    {"success":true,"data":[{"bookId":"ff9a60a5-b7ae-4a48-8fa3-08fc8dfdb0dc","title":"Nhà giả kim","stockQuantity":4}]}
                    """,
                var value when value.StartsWith("/api/admin/dashboard/recent-orders", StringComparison.Ordinal) => """
                    {"success":true,"data":[{"orderId":"2df417d5-ef08-4ee9-a89f-1cc681185393","orderCode":"POS-001","customerName":"Nguyễn An","finalAmount":250000,"status":"CONFIRMED","createdAt":"2026-07-24T01:30:00Z"}]}
                    """,
                _ => """{"success":false,"message":"not found"}"""
            };

            return Task.FromResult(new HttpResponseMessage(HttpStatusCode.OK)
            {
                Content = new StringContent(payload, Encoding.UTF8, "application/json")
            });
        }
    }
}
