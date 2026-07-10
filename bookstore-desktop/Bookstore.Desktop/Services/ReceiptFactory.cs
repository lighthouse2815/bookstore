using Bookstore.Desktop.Dtos;
using Bookstore.Desktop.Models;

namespace Bookstore.Desktop.Services;

public class ReceiptFactory
{
    public ReceiptModel CreateFromPosOrder(
        IReadOnlyList<PosCartItemModel> items,
        OrderResponse response,
        StaffUserModel? staff,
        string? customerName,
        string? customerPhone,
        decimal? cashReceived)
    {
        var isCash = string.Equals(response.PaymentMethod, "CASH", StringComparison.OrdinalIgnoreCase);

        return new ReceiptModel
        {
            OrderId = response.OrderId,
            OrderCode = string.IsNullOrWhiteSpace(response.OrderCode) ? response.OrderId : response.OrderCode,
            CreatedAt = response.CreatedAt ?? DateTimeOffset.Now,
            StaffName = staff?.DisplayName ?? "",
            CustomerName = NormalizeOptional(customerName),
            CustomerPhone = NormalizeOptional(customerPhone),
            PaymentMethod = response.PaymentMethod,
            Items = items.Select(item => new OrderItemModel
            {
                BookId = item.BookId,
                BookTitle = item.Title,
                Quantity = item.Quantity,
                UnitPrice = item.UnitPrice,
                LineTotal = item.LineTotal
            }).ToArray(),
            TotalAmount = items.Sum(item => item.LineTotal),
            DiscountAmount = response.DiscountAmount,
            FinalAmount = response.FinalAmount,
            CashReceived = isCash ? cashReceived : null,
            ChangeAmount = isCash && cashReceived.HasValue ? cashReceived.Value - response.FinalAmount : null
        };
    }

    public ReceiptModel CreateFromOrder(OrderModel order)
    {
        return new ReceiptModel
        {
            OrderId = order.OrderId,
            OrderCode = order.CodeOrId,
            CreatedAt = order.CreatedAt ?? DateTimeOffset.Now,
            CustomerName = NormalizeOptional(order.CustomerName),
            CustomerPhone = NormalizeOptional(order.CustomerPhone),
            PaymentMethod = order.PaymentMethod,
            Items = order.Items,
            TotalAmount = order.ProductTotal,
            DiscountAmount = order.DiscountAmount,
            FinalAmount = order.FinalAmount
        };
    }

    private static string? NormalizeOptional(string? value)
    {
        return string.IsNullOrWhiteSpace(value) ? null : value.Trim();
    }
}
