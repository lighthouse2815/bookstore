using Bookstore.Desktop.Dtos;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using Bookstore.Desktop.Stores;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using System.Net;
using System.Net.Http;
using System.Text;

namespace Bookstore.Desktop.Tests;

[TestClass]
public class PosCheckoutTests
{
    [TestMethod]
    public void Add_same_book_increases_quantity_without_a_duplicate_row()
    {
        var cart = new PosCartStore();
        var book = Book("book-1", 100_000m, stockQuantity: 5);

        cart.Add(book);
        cart.Add(book);

        Assert.AreEqual(1, cart.Items.Count);
        Assert.AreEqual(2, cart.Items[0].Quantity);
    }

    [TestMethod]
    public void Cart_quantity_stays_positive_and_does_not_exceed_stock()
    {
        var cart = new PosCartStore();
        var item = new PosCartItemModel(Book("book-1", 50_000m, stockQuantity: 2), quantity: 1);
        cart.Items.Add(item);

        cart.Decrease(item);

        Assert.AreEqual(1, item.Quantity);
        Assert.IsTrue(PosCheckoutRules.IsValidQuantity(item.Quantity));
        Assert.IsTrue(PosCheckoutRules.CanIncreaseQuantity(1, 2));
        Assert.IsFalse(PosCheckoutRules.CanIncreaseQuantity(2, 2));
        Assert.IsFalse(PosCheckoutRules.CanIncreaseQuantity(0, 0));
    }

    [TestMethod]
    public void Cart_subtotal_and_total_are_calculated_from_item_lines()
    {
        var cart = new PosCartStore();
        cart.Add(Book("book-1", 100_000m, stockQuantity: 5));
        cart.Add(Book("book-1", 100_000m, stockQuantity: 5));
        cart.Add(Book("book-2", 75_000m, stockQuantity: 5));

        Assert.AreEqual(3, cart.TotalQuantity);
        Assert.AreEqual(275_000m, cart.TotalAmount);
        Assert.AreEqual(200_000m, cart.Items.Single(item => item.BookId == "book-1").LineTotal);
    }

    [TestMethod]
    public void Cash_received_and_change_are_calculated_correctly()
    {
        Assert.IsTrue(PosCheckoutRules.TryParseCashReceived("300000", out var cashReceived));
        Assert.IsTrue(PosCheckoutRules.HasEnoughCash(cashReceived, 275_000m));
        Assert.AreEqual(25_000m, PosCheckoutRules.CalculateChange(cashReceived, 275_000m));
    }

    [TestMethod]
    public void Cash_smaller_than_total_is_blocked()
    {
        Assert.IsFalse(PosCheckoutRules.HasEnoughCash(200_000m, 275_000m));
    }

    [TestMethod]
    public void Checkout_guard_blocks_a_second_submission()
    {
        Assert.IsTrue(PosCheckoutRules.CanStartCheckout(isCreatingOrder: false, isCartEmpty: false));
        Assert.IsFalse(PosCheckoutRules.CanStartCheckout(isCreatingOrder: true, isCartEmpty: false));
        Assert.IsFalse(PosCheckoutRules.CanStartCheckout(isCreatingOrder: false, isCartEmpty: true));
    }

    [TestMethod]
    public void Receipt_factory_uses_order_code_items_total_and_customer_information()
    {
        var items = new[]
        {
            new PosCartItemModel(Book("book-1", 100_000m, stockQuantity: 5), 2),
            new PosCartItemModel(Book("book-2", 75_000m, stockQuantity: 5), 1),
        };
        var receipt = new ReceiptFactory().CreateFromPosOrder(
            items,
            new OrderResponse
            {
                OrderId = "order-id",
                OrderCode = "POS-001",
                PaymentMethod = "CASH",
                DiscountAmount = 25_000m,
                FinalAmount = 250_000m,
            },
            new StaffUserModel { Username = "cashier" },
            " Nguyen Van A ",
            " 0900000000 ",
            cashReceived: 300_000m);

        Assert.AreEqual("POS-001", receipt.OrderCode);
        Assert.AreEqual(2, receipt.Items.Count);
        Assert.AreEqual(275_000m, receipt.TotalAmount);
        Assert.AreEqual(250_000m, receipt.FinalAmount);
        Assert.AreEqual("Nguyen Van A", receipt.CustomerName);
        Assert.AreEqual("0900000000", receipt.CustomerPhone);
        Assert.AreEqual(50_000m, receipt.ChangeAmount);
    }

    [TestMethod]
    public void Historical_receipt_handles_missing_cash_and_cashier_safely()
    {
        var receipt = new ReceiptFactory().CreateFromOrder(new OrderModel
        {
            OrderId = "legacy-order",
            PaymentMethod = "BANK_TRANSFER",
            CustomerName = " ",
            CustomerPhone = null,
            ProductTotal = 100_000m,
            FinalAmount = 100_000m,
        });

        Assert.AreEqual("legacy-order", receipt.OrderCode);
        Assert.AreEqual("", receipt.StaffName);
        Assert.IsNull(receipt.CashReceived);
        Assert.IsNull(receipt.ChangeAmount);
        Assert.IsNull(receipt.CustomerName);
        Assert.IsFalse(receipt.HasCashReceived);
    }

    [TestMethod]
    public async Task Api_client_retries_only_once_after_an_unauthorized_response()
    {
        var handler = new RetryOnceHandler();
        var settings = new AppSettingsStore();
        settings.UpdateBaseUrl("http://example.test");
        var auth = new AuthStore();
        auth.SetSession("old-access", "refresh-token", new StaffUserModel { Id = "staff-1", Username = "cashier" });
        var client = new ApiClient(settings, auth, handler);

        await Assert.ThrowsExactlyAsync<ApiClientException>(() => client.GetAsync("/api/books"));

        Assert.AreEqual(2, handler.ProtectedRequestCount);
        Assert.AreEqual(1, handler.RefreshRequestCount);
    }

    private static BookModel Book(string id, decimal price, int? stockQuantity)
    {
        return new BookModel { Id = id, Title = id, Price = price, StockQuantity = stockQuantity };
    }

    private sealed class RetryOnceHandler : HttpMessageHandler
    {
        public int ProtectedRequestCount { get; private set; }
        public int RefreshRequestCount { get; private set; }

        protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
        {
            if (request.RequestUri?.AbsolutePath == "/api/auth/refresh")
            {
                RefreshRequestCount++;
                return Task.FromResult(JsonResponse(HttpStatusCode.OK, """
                    {"success":true,"data":{"accessToken":"new-access","refreshToken":"new-refresh"}}
                    """));
            }

            ProtectedRequestCount++;
            return Task.FromResult(JsonResponse(HttpStatusCode.Unauthorized, "{\"success\":false,\"message\":\"expired\"}"));
        }

        private static HttpResponseMessage JsonResponse(HttpStatusCode statusCode, string content)
        {
            return new HttpResponseMessage(statusCode)
            {
                Content = new StringContent(content, Encoding.UTF8, "application/json"),
            };
        }
    }
}
