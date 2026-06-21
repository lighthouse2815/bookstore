using System.IO;
using System.Text;
using Bookstore.Desktop.Models;

namespace Bookstore.Desktop.Services;

public class ReceiptPrinterService
{
    public async Task<string> ExportTextAsync(ReceiptModel receipt)
    {
        var directory = Path.Combine(
            Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),
            "BookstorePOS",
            "Receipts");
        Directory.CreateDirectory(directory);

        var fileName = $"{SanitizeFileName(receipt.OrderCode)}-{DateTime.Now:yyyyMMddHHmmss}.txt";
        var path = Path.Combine(directory, fileName);
        await File.WriteAllTextAsync(path, BuildReceiptText(receipt), Encoding.UTF8);
        return path;
    }

    private static string BuildReceiptText(ReceiptModel receipt)
    {
        var builder = new StringBuilder();
        builder.AppendLine(receipt.StoreName);
        builder.AppendLine($"Mã đơn: {receipt.OrderCode}");
        builder.AppendLine($"Ngày: {receipt.CreatedAtText}");
        builder.AppendLine($"Nhân viên: {receipt.StaffName}");
        builder.AppendLine($"Thanh toán: {receipt.PaymentMethod}");
        builder.AppendLine(new string('-', 42));
        foreach (var item in receipt.Items)
        {
            builder.AppendLine(item.BookTitle);
            builder.AppendLine($"  {item.Quantity} x {item.UnitPriceText} = {item.LineTotalText}");
        }
        builder.AppendLine(new string('-', 42));
        builder.AppendLine($"Tạm tính: {receipt.TotalText}");
        builder.AppendLine($"Giảm giá: {receipt.DiscountText}");
        builder.AppendLine($"Tổng cộng: {receipt.FinalText}");
        return builder.ToString();
    }

    private static string SanitizeFileName(string value)
    {
        var invalidChars = Path.GetInvalidFileNameChars();
        return string.Join("_", value.Split(invalidChars, StringSplitOptions.RemoveEmptyEntries));
    }
}
