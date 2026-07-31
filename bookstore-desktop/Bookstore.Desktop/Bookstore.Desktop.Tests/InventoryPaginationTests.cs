using System.Net;
using System.Net.Http;
using System.Text;
using Bookstore.Desktop.Services;
using Bookstore.Desktop.Stores;
using Microsoft.VisualStudio.TestTools.UnitTesting;

namespace Bookstore.Desktop.Tests;

[TestClass]
public class InventoryPaginationTests
{
    [TestMethod]
    public async Task Book_service_requests_only_one_page_and_maps_pagination_headers()
    {
        var handler = new PaginatedBooksHandler();
        var settings = new AppSettingsStore("http://example.test");
        var service = new BookService(new ApiClient(settings, new AuthStore(), handler));

        var result = await service.SearchPageAsync(null, page: 1, size: 10);

        Assert.AreEqual("/api/books?page=1&size=10", handler.RequestPath);
        Assert.AreEqual(25, result.TotalCount);
        Assert.AreEqual(1, result.Page);
        Assert.AreEqual(10, result.Size);
        Assert.IsTrue(result.HasNext);
        Assert.AreEqual(3, result.TotalPages);
        Assert.AreEqual(2, result.Items.Count);
        Assert.AreEqual("Sách trang hai", result.Items[0].Title);
    }

    private sealed class PaginatedBooksHandler : HttpMessageHandler
    {
        public string RequestPath { get; private set; } = "";

        protected override Task<HttpResponseMessage> SendAsync(
            HttpRequestMessage request,
            CancellationToken cancellationToken)
        {
            RequestPath = request.RequestUri?.PathAndQuery ?? "";
            var response = new HttpResponseMessage(HttpStatusCode.OK)
            {
                Content = new StringContent(
                    """
                    {
                      "success": true,
                      "data": [
                        {
                          "id": "book-11",
                          "title": "Sách trang hai",
                          "isbn": "9780000000011",
                          "price": 120000,
                          "stockQuantity": 8
                        },
                        {
                          "id": "book-12",
                          "title": "Sách kế tiếp",
                          "isbn": "9780000000012",
                          "price": 95000,
                          "stockQuantity": 4
                        }
                      ]
                    }
                    """,
                    Encoding.UTF8,
                    "application/json")
            };
            response.Headers.Add("X-Total-Count", "25");
            response.Headers.Add("X-Page", "1");
            response.Headers.Add("X-Size", "10");
            response.Headers.Add("X-Has-Next", "true");
            return Task.FromResult(response);
        }
    }
}
