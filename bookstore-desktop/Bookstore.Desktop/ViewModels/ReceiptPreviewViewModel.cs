using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class ReceiptPreviewViewModel : ObservableObject
{
    private readonly ReceiptPrinterService receiptPrinterService;

    public ReceiptPreviewViewModel(ReceiptPrinterService receiptPrinterService)
    {
        this.receiptPrinterService = receiptPrinterService;
    }

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(HasReceipt))]
    private ReceiptModel? receipt;

    [ObservableProperty]
    private string message = "";

    public bool HasReceipt => Receipt != null;

    public void SetReceipt(ReceiptModel receipt)
    {
        Receipt = receipt;
        Message = "";
    }

    [RelayCommand]
    private async Task PrintDemoAsync()
    {
        if (Receipt == null)
        {
            Message = "Chưa có hóa đơn để in.";
            return;
        }

        try
        {
            var path = await receiptPrinterService.ExportTextAsync(Receipt);
            Message = "Đã xuất hóa đơn demo: " + path;
            MessageBoxHelper.Info(Message);
        }
        catch (Exception exception)
        {
            Message = exception.Message;
        }
    }
}
