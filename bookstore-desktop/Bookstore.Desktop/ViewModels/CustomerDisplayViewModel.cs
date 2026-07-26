using System.Collections.ObjectModel;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Stores;
using CommunityToolkit.Mvvm.ComponentModel;

namespace Bookstore.Desktop.ViewModels;

public enum CustomerDisplayMode
{
    Empty,
    Draft,
    Paid
}

public partial class CustomerDisplayViewModel : ObservableObject
{
    private readonly PosCartStore cartStore;
    private bool showingPaidReceipt;

    public CustomerDisplayViewModel(PosCartStore cartStore)
    {
        this.cartStore = cartStore;
        cartStore.PropertyChanged += (_, args) =>
        {
            if (args.PropertyName == nameof(PosCartStore.TotalAmount))
            {
                RefreshFromCart();
            }
        };
        RefreshFromCart();
    }

    public ObservableCollection<CustomerDisplayLineModel> Items { get; } = new();

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(IsEmpty))]
    [NotifyPropertyChangedFor(nameof(IsDraft))]
    [NotifyPropertyChangedFor(nameof(IsPaid))]
    [NotifyPropertyChangedFor(nameof(StatusText))]
    [NotifyPropertyChangedFor(nameof(HeadlineText))]
    [NotifyPropertyChangedFor(nameof(GuidanceText))]
    private CustomerDisplayMode mode;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(SubtotalText))]
    private decimal subtotal;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(DiscountText))]
    private decimal discountAmount;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(FinalAmountText))]
    [NotifyPropertyChangedFor(nameof(FinalAmountInWordsText))]
    private decimal finalAmount;

    [ObservableProperty]
    private string orderCode = "";

    [ObservableProperty]
    private string paymentMethodText = "";

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(HasCashReceived))]
    [NotifyPropertyChangedFor(nameof(CashReceivedText))]
    private decimal? cashReceived;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(ChangeAmountText))]
    private decimal? changeAmount;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(UpdatedAtText))]
    private DateTimeOffset updatedAt = DateTimeOffset.Now;

    public bool IsEmpty => Mode == CustomerDisplayMode.Empty;
    public bool IsDraft => Mode == CustomerDisplayMode.Draft;
    public bool IsPaid => Mode == CustomerDisplayMode.Paid;
    public bool HasCashReceived => CashReceived.HasValue;

    public string StatusText => Mode switch
    {
        CustomerDisplayMode.Paid => "ĐÃ THANH TOÁN",
        CustomerDisplayMode.Draft => "CHƯA THANH TOÁN",
        _ => "SẴN SÀNG"
    };

    public string HeadlineText => Mode switch
    {
        CustomerDisplayMode.Paid => "Thanh toán thành công",
        CustomerDisplayMode.Draft => "Hóa đơn tạm tính",
        _ => "Xin chào quý khách"
    };

    public string GuidanceText => Mode switch
    {
        CustomerDisplayMode.Paid => "Cảm ơn quý khách đã mua sắm tại Bookstore.",
        CustomerDisplayMode.Draft => "Quý khách vui lòng kiểm tra sản phẩm và tổng tiền trước khi thanh toán.",
        _ => "Sản phẩm được nhân viên thêm vào sẽ hiển thị tại đây."
    };

    public string SubtotalText => CurrencyHelper.Format(Subtotal);
    public string DiscountText => CurrencyHelper.Format(DiscountAmount);
    public string FinalAmountText => CurrencyHelper.Format(FinalAmount);
    public string FinalAmountInWordsText => FinalAmount > 0
        ? $"({VietnameseCurrencyTextHelper.ToWords(FinalAmount)})"
        : "";
    public string CashReceivedText => CashReceived.HasValue
        ? CurrencyHelper.Format(CashReceived.Value)
        : "";
    public string ChangeAmountText => ChangeAmount.HasValue
        ? CurrencyHelper.Format(ChangeAmount.Value)
        : "";
    public string UpdatedAtText => $"Cập nhật lúc {UpdatedAt.LocalDateTime:HH:mm:ss}";

    public void ShowPaidReceipt(ReceiptModel receipt)
    {
        ArgumentNullException.ThrowIfNull(receipt);

        showingPaidReceipt = true;
        ReplaceItems(receipt.Items.Select(CustomerDisplayLineModel.FromReceiptItem));
        Subtotal = receipt.TotalAmount;
        DiscountAmount = receipt.DiscountAmount;
        FinalAmount = receipt.FinalAmount;
        OrderCode = receipt.OrderCode;
        PaymentMethodText = receipt.PaymentMethodText;
        CashReceived = receipt.CashReceived;
        ChangeAmount = receipt.ChangeAmount;
        UpdatedAt = DateTimeOffset.Now;
        Mode = CustomerDisplayMode.Paid;
    }

    public void Reset()
    {
        showingPaidReceipt = false;
        RefreshFromCart();
    }

    private void RefreshFromCart()
    {
        if (showingPaidReceipt && cartStore.IsEmpty)
        {
            return;
        }

        showingPaidReceipt = false;
        ReplaceItems(cartStore.Items.Select(CustomerDisplayLineModel.FromCartItem));
        Subtotal = cartStore.TotalAmount;
        DiscountAmount = 0;
        FinalAmount = cartStore.TotalAmount;
        OrderCode = "";
        PaymentMethodText = "";
        CashReceived = null;
        ChangeAmount = null;
        UpdatedAt = DateTimeOffset.Now;
        Mode = cartStore.IsEmpty ? CustomerDisplayMode.Empty : CustomerDisplayMode.Draft;
    }

    private void ReplaceItems(IEnumerable<CustomerDisplayLineModel> items)
    {
        Items.Clear();
        foreach (var item in items)
        {
            Items.Add(item);
        }
    }
}
