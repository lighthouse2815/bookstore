using System.Collections.ObjectModel;
using System.Net;
using System.Net.Http;
using System.Text.RegularExpressions;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using Bookstore.Desktop.Stores;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class PosViewModel : ObservableObject
{
    private static readonly Regex PhoneInputPattern = new(@"^[0-9+\-\s()]{8,20}$", RegexOptions.Compiled);

    private readonly BookService bookService;
    private readonly PosService posService;
    private readonly PosCartStore cartStore;
    private readonly NavigationService navigationService;
    private readonly ReceiptPreviewViewModel receiptPreviewViewModel;
    private readonly OrderLookupViewModel orderLookupViewModel;
    private readonly ReceiptFactory receiptFactory;
    private readonly AuthStore authStore;
    private readonly CustomerDisplayViewModel customerDisplayViewModel;

    public PosViewModel(
        BookService bookService,
        PosService posService,
        PosCartStore cartStore,
        NavigationService navigationService,
        ReceiptPreviewViewModel receiptPreviewViewModel,
        OrderLookupViewModel orderLookupViewModel,
        ReceiptFactory receiptFactory,
        AuthStore authStore,
        CustomerDisplayViewModel customerDisplayViewModel)
    {
        this.bookService = bookService;
        this.posService = posService;
        this.cartStore = cartStore;
        this.navigationService = navigationService;
        this.receiptPreviewViewModel = receiptPreviewViewModel;
        this.orderLookupViewModel = orderLookupViewModel;
        this.receiptFactory = receiptFactory;
        this.authStore = authStore;
        this.customerDisplayViewModel = customerDisplayViewModel;
        PaymentMethods = new[]
        {
            new PaymentMethodOption("CASH", "Tiền mặt"),
            new PaymentMethodOption("BANK_TRANSFER", "Chuyển khoản"),
            new PaymentMethodOption("BANK_TRANSFER_QR", "Chuyển khoản QR"),
            new PaymentMethodOption("COD", "COD")
        };
        SelectedPaymentMethod = PaymentMethods[0];
        cartStore.PropertyChanged += (_, _) => RefreshCartTotals();
    }

    public ObservableCollection<BookModel> SearchResults { get; } = new();
    public ObservableCollection<PosCartItemModel> CartItems => cartStore.Items;
    public ObservableCollection<CouponOptionModel> AvailableCoupons { get; } = new();
    public IReadOnlyList<PaymentMethodOption> PaymentMethods { get; }

    [ObservableProperty]
    private string searchKeyword = "";

    [ObservableProperty]
    private PaymentMethodOption? selectedPaymentMethod;

    [ObservableProperty]
    private string? couponCode;

    [ObservableProperty]
    private string couponHelpText = "Chọn mã đang hoạt động hoặc nhập mã thủ công.";

    [ObservableProperty]
    private bool isLoadingCoupons;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(IsCustomerDetailsEnabled))]
    private bool useWalkInCustomer = true;

    [ObservableProperty]
    private string? customerName;

    [ObservableProperty]
    private string? customerPhone;

    [ObservableProperty]
    private string cashReceivedText = "";

    [ObservableProperty]
    private string message = "";

    [ObservableProperty]
    private bool isLoading;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(CanCheckout))]
    [NotifyPropertyChangedFor(nameof(CanEditCart))]
    [NotifyPropertyChangedFor(nameof(CheckoutButtonText))]
    private bool isCreatingOrder;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(HasLastReceipt))]
    [NotifyPropertyChangedFor(nameof(LastOrderSummary))]
    private ReceiptModel? lastReceipt;

    public string TotalAmountText => CurrencyHelper.Format(cartStore.TotalAmount);
    public string TotalQuantityText => cartStore.TotalQuantity.ToString();
    public bool CanCheckout => PosCheckoutRules.CanStartCheckout(IsCreatingOrder, cartStore.IsEmpty);
    public bool CanEditCart => !IsCreatingOrder;
    public bool IsCustomerDetailsEnabled => !UseWalkInCustomer;
    public bool IsCashPayment => string.Equals(SelectedPaymentMethod?.Code, "CASH", StringComparison.OrdinalIgnoreCase);
    public bool HasCashReceived => TryParseCashReceived(out _);
    public string CashReceivedInWordsText => TryParseCashReceived(out var amount)
        ? $"({VietnameseCurrencyTextHelper.ToWords(amount)})"
        : "";
    public string CashChangeText => TryParseCashReceived(out var amount)
        ? CurrencyHelper.Format(PosCheckoutRules.CalculateChange(amount, cartStore.TotalAmount))
        : "--";
    public string CheckoutButtonText => IsCreatingOrder ? "Đang tạo đơn..." : "Tạo đơn / Thanh toán";
    public bool HasLastReceipt => LastReceipt != null;
    public string LastOrderSummary => LastReceipt == null
        ? ""
        : $"Đơn {LastReceipt.OrderCode} · {LastReceipt.FinalText}";

    [RelayCommand]
    private async Task SearchAsync()
    {
        if (IsCreatingOrder)
        {
            return;
        }

        try
        {
            IsLoading = true;
            Message = "Đang tìm sách...";
            SearchResults.Clear();
            var books = await bookService.SearchAsync(SearchKeyword);
            foreach (var book in books)
            {
                SearchResults.Add(book);
            }
            Message = books.Count == 0 ? "Không tìm thấy sách phù hợp." : $"Tìm thấy {books.Count} sách.";
        }
        catch (ApiClientException exception)
        {
            Message = FormatApiError(exception, "tìm sách");
        }
        catch (HttpRequestException)
        {
            Message = "Không thể kết nối backend khi tìm sách. Kiểm tra URL API và mạng.";
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

    public async Task LoadCouponsAsync()
    {
        if (IsLoadingCoupons)
        {
            return;
        }

        try
        {
            IsLoadingCoupons = true;
            var coupons = await posService.GetActiveBookCouponsAsync();
            AvailableCoupons.Clear();
            foreach (var coupon in coupons)
            {
                AvailableCoupons.Add(coupon);
            }

            UpdateCouponHelpText();
        }
        catch
        {
            CouponHelpText = "Không tải được danh sách mã; bạn vẫn có thể nhập mã thủ công.";
        }
        finally
        {
            IsLoadingCoupons = false;
        }
    }

    [RelayCommand]
    private void AddToCart(BookModel book)
    {
        if (!CanEditCart)
        {
            return;
        }

        if (book.StockQuantity.HasValue && book.StockQuantity.Value <= 0)
        {
            Message = "Sách này đã hết tồn kho.";
            return;
        }

        var currentQuantity = CartItems.FirstOrDefault(item => item.BookId == book.Id)?.Quantity ?? 0;
        if (!PosCheckoutRules.CanIncreaseQuantity(currentQuantity, book.StockQuantity))
        {
            Message = "Số lượng trong giỏ đã bằng tồn kho hiện tại.";
            return;
        }

        cartStore.Add(book);
        Message = $"Đã thêm \"{book.Title}\" vào giỏ.";
    }

    [RelayCommand]
    private void Increase(PosCartItemModel item)
    {
        if (!CanEditCart)
        {
            return;
        }

        if (!PosCheckoutRules.CanIncreaseQuantity(item.Quantity, item.Book.StockQuantity))
        {
            Message = "Không thể tăng quá tồn kho hiện tại.";
            return;
        }

        cartStore.Increase(item);
    }

    [RelayCommand]
    private void Decrease(PosCartItemModel item)
    {
        if (CanEditCart)
        {
            cartStore.Decrease(item);
        }
    }

    [RelayCommand]
    private void Remove(PosCartItemModel item)
    {
        if (CanEditCart)
        {
            cartStore.Remove(item);
        }
    }

    [RelayCommand]
    private void ClearCart()
    {
        if (!CanEditCart)
        {
            return;
        }

        cartStore.Clear();
        Message = "Đã xóa giỏ hàng.";
    }

    [RelayCommand]
    private async Task CheckoutAsync()
    {
        if (!PosCheckoutRules.CanStartCheckout(IsCreatingOrder, cartStore.IsEmpty))
        {
            if (cartStore.IsEmpty)
            {
                Message = "Giỏ hàng đang trống.";
            }
            return;
        }

        if (SelectedPaymentMethod == null)
        {
            Message = "Chọn phương thức thanh toán trước khi tạo đơn.";
            return;
        }

        if (!TryResolveCustomer(out var customerName, out var customerPhone))
        {
            return;
        }

        if (!TryValidateCashReceived(out var cashReceived))
        {
            return;
        }

        try
        {
            IsCreatingOrder = true;
            Message = "Đang tạo đơn POS...";
            var snapshot = CartItems.Select(item => new PosCartItemModel(item.Book, item.Quantity)).ToArray();
            var response = await posService.CreateOrderAsync(
                snapshot,
                SelectedPaymentMethod.Code,
                CouponCode,
                customerName,
                customerPhone);
            var receipt = receiptFactory.CreateFromPosOrder(
                snapshot,
                response,
                authStore.CurrentUser,
                customerName,
                customerPhone,
                cashReceived);

            customerDisplayViewModel.ShowPaidReceipt(receipt);
            LastReceipt = receipt;
            receiptPreviewViewModel.SetReceipt(receipt);
            cartStore.Clear();
            ResetSaleInputs();
            Message = $"Thanh toán thành công. Mã đơn: {receipt.OrderCode}. Tổng tiền: {receipt.FinalText}.";
        }
        catch (ApiClientException exception)
        {
            Message = FormatApiError(exception, "tạo đơn");
        }
        catch (HttpRequestException)
        {
            Message = "Không thể kết nối backend khi tạo đơn. Kiểm tra URL API và mạng rồi thử lại.";
        }
        catch (Exception exception)
        {
            Message = exception.Message;
        }
        finally
        {
            IsCreatingOrder = false;
        }
    }

    [RelayCommand]
    private void OpenLastReceipt()
    {
        if (LastReceipt == null)
        {
            Message = "Chưa có hóa đơn vừa tạo để xuất.";
            return;
        }

        receiptPreviewViewModel.SetReceipt(LastReceipt);
        navigationService.NavigateTo(receiptPreviewViewModel);
    }

    [RelayCommand]
    private async Task OpenLastOrderDetailAsync()
    {
        if (LastReceipt == null)
        {
            Message = "Chưa có đơn vừa tạo để mở chi tiết.";
            return;
        }

        if (await orderLookupViewModel.LoadOrderAsync(LastReceipt.OrderId, "Đã mở chi tiết đơn vừa tạo."))
        {
            navigationService.NavigateTo(orderLookupViewModel);
        }
    }

    partial void OnSelectedPaymentMethodChanged(PaymentMethodOption? value)
    {
        OnPropertyChanged(nameof(IsCashPayment));
        OnPropertyChanged(nameof(CashChangeText));
    }

    partial void OnCashReceivedTextChanged(string value)
    {
        OnPropertyChanged(nameof(HasCashReceived));
        OnPropertyChanged(nameof(CashReceivedInWordsText));
        OnPropertyChanged(nameof(CashChangeText));
    }

    partial void OnCouponCodeChanged(string? value)
    {
        UpdateCouponHelpText();
    }

    private void UpdateCouponHelpText()
    {
        var selectedCoupon = AvailableCoupons.FirstOrDefault(coupon =>
            string.Equals(coupon.Code, CouponCode?.Trim(), StringComparison.OrdinalIgnoreCase));
        if (selectedCoupon != null)
        {
            CouponHelpText = selectedCoupon.DetailText;
            return;
        }

        CouponHelpText = AvailableCoupons.Count == 0
            ? "Chưa có mã giảm giá sách đang hoạt động; vẫn có thể nhập mã thủ công."
            : $"Có {AvailableCoupons.Count} mã đang hoạt động; chọn trong danh sách hoặc nhập mã khác.";
    }

    private void RefreshCartTotals()
    {
        OnPropertyChanged(nameof(TotalAmountText));
        OnPropertyChanged(nameof(TotalQuantityText));
        OnPropertyChanged(nameof(CanCheckout));
        OnPropertyChanged(nameof(CashChangeText));
    }

    private bool TryResolveCustomer(out string? resolvedCustomerName, out string? resolvedCustomerPhone)
    {
        resolvedCustomerName = null;
        resolvedCustomerPhone = null;

        if (UseWalkInCustomer)
        {
            return true;
        }

        resolvedCustomerName = NormalizeOptional(CustomerName);
        resolvedCustomerPhone = NormalizeOptional(CustomerPhone);
        if (resolvedCustomerPhone == null)
        {
            return true;
        }

        var digitCount = resolvedCustomerPhone.Count(char.IsDigit);
        if (!PhoneInputPattern.IsMatch(resolvedCustomerPhone) || digitCount is < 8 or > 15)
        {
            Message = "Số điện thoại chỉ nên gồm 8-15 chữ số; có thể dùng dấu +, khoảng trắng hoặc gạch nối.";
            return false;
        }

        return true;
    }

    private bool TryValidateCashReceived(out decimal? cashReceived)
    {
        cashReceived = null;
        if (!IsCashPayment || string.IsNullOrWhiteSpace(CashReceivedText))
        {
            return true;
        }

        if (!TryParseCashReceived(out var parsedAmount))
        {
            Message = "Tiền khách đưa phải là một số hợp lệ.";
            return false;
        }

        if (!PosCheckoutRules.HasEnoughCash(parsedAmount, cartStore.TotalAmount))
        {
            Message = "Tiền khách đưa chưa đủ để hoàn tất thanh toán tiền mặt.";
            return false;
        }

        cashReceived = parsedAmount;
        return true;
    }

    private bool TryParseCashReceived(out decimal amount)
    {
        return PosCheckoutRules.TryParseCashReceived(CashReceivedText, out amount);
    }

    private void ResetSaleInputs()
    {
        CouponCode = null;
        CustomerName = null;
        CustomerPhone = null;
        CashReceivedText = "";
        UseWalkInCustomer = true;
        SelectedPaymentMethod = PaymentMethods[0];
    }

    private static string? NormalizeOptional(string? value)
    {
        return string.IsNullOrWhiteSpace(value) ? null : value.Trim();
    }

    private static string FormatApiError(ApiClientException exception, string action)
    {
        return exception.StatusCode switch
        {
            HttpStatusCode.BadRequest => $"Không thể {action}. {exception.Message}",
            HttpStatusCode.Unauthorized => "Phiên đăng nhập đã hết hạn hoặc không hợp lệ. Vui lòng đăng nhập lại.",
            HttpStatusCode.Forbidden => "Bạn không có quyền STAFF/ADMIN để thực hiện thao tác POS.",
            _ => $"Không thể {action}. {exception.Message}"
        };
    }
}
