using System.Collections.ObjectModel;
using Bookstore.Desktop.Dtos;
using Bookstore.Desktop.Helpers;
using Bookstore.Desktop.Models;
using Bookstore.Desktop.Services;
using Bookstore.Desktop.Stores;
using CommunityToolkit.Mvvm.ComponentModel;
using CommunityToolkit.Mvvm.Input;

namespace Bookstore.Desktop.ViewModels;

public partial class PosViewModel : ObservableObject
{
    private readonly BookService bookService;
    private readonly PosService posService;
    private readonly PosCartStore cartStore;
    private readonly NavigationService navigationService;
    private readonly ReceiptPreviewViewModel receiptPreviewViewModel;
    private readonly AuthStore authStore;

    public PosViewModel(
        BookService bookService,
        PosService posService,
        PosCartStore cartStore,
        NavigationService navigationService,
        ReceiptPreviewViewModel receiptPreviewViewModel,
        AuthStore authStore)
    {
        this.bookService = bookService;
        this.posService = posService;
        this.cartStore = cartStore;
        this.navigationService = navigationService;
        this.receiptPreviewViewModel = receiptPreviewViewModel;
        this.authStore = authStore;
        cartStore.PropertyChanged += (_, _) => RefreshCartTotals();
    }

    public ObservableCollection<BookModel> SearchResults { get; } = new();
    public ObservableCollection<PosCartItemModel> CartItems => cartStore.Items;
    public IReadOnlyList<string> PaymentMethods { get; } = new[] { "CASH", "BANK_TRANSFER", "COD" };

    [ObservableProperty]
    private string searchKeyword = "";

    [ObservableProperty]
    private string selectedPaymentMethod = "CASH";

    [ObservableProperty]
    private string? couponCode;

    [ObservableProperty]
    private string message = "";

    [ObservableProperty]
    private bool isLoading;

    public string TotalAmountText => CurrencyHelper.Format(cartStore.TotalAmount);
    public string TotalQuantityText => cartStore.TotalQuantity.ToString();
    public bool CanCheckout => !cartStore.IsEmpty && !IsLoading;

    [RelayCommand]
    private async Task SearchAsync()
    {
        try
        {
            IsLoading = true;
            Message = "";
            SearchResults.Clear();
            var books = await bookService.SearchAsync(SearchKeyword);
            foreach (var book in books)
            {
                SearchResults.Add(book);
            }
            Message = books.Count == 0 ? "Không tìm thấy sách phù hợp." : $"Tìm thấy {books.Count} sách.";
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

    [RelayCommand]
    private void AddToCart(BookModel book)
    {
        if (book.StockQuantity.HasValue && book.StockQuantity.Value <= 0)
        {
            Message = "Sách này đã hết tồn kho.";
            return;
        }

        var currentQuantity = CartItems.FirstOrDefault(item => item.BookId == book.Id)?.Quantity ?? 0;
        if (book.StockQuantity.HasValue && currentQuantity >= book.StockQuantity.Value)
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
        if (item.Book.StockQuantity.HasValue && item.Quantity >= item.Book.StockQuantity.Value)
        {
            Message = "Không thể tăng quá tồn kho hiện tại.";
            return;
        }

        cartStore.Increase(item);
    }

    [RelayCommand]
    private void Decrease(PosCartItemModel item)
    {
        cartStore.Decrease(item);
    }

    [RelayCommand]
    private void Remove(PosCartItemModel item)
    {
        cartStore.Remove(item);
    }

    [RelayCommand]
    private void ClearCart()
    {
        cartStore.Clear();
        Message = "Đã xóa giỏ hàng.";
    }

    [RelayCommand]
    private async Task CheckoutAsync()
    {
        if (cartStore.IsEmpty)
        {
            Message = "Giỏ hàng đang trống.";
            return;
        }

        try
        {
            IsLoading = true;
            Message = "";
            var snapshot = CartItems.Select(item => new PosCartItemModel(item.Book, item.Quantity)).ToArray();
            var response = await posService.CreateOrderAsync(snapshot, SelectedPaymentMethod, CouponCode);
            var receipt = BuildReceipt(snapshot, response);
            receiptPreviewViewModel.SetReceipt(receipt);
            cartStore.Clear();
            Message = $"Thanh toán thành công. Mã đơn: {receipt.OrderCode}";
            navigationService.NavigateTo(receiptPreviewViewModel);
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

    partial void OnIsLoadingChanged(bool value)
    {
        OnPropertyChanged(nameof(CanCheckout));
    }

    private void RefreshCartTotals()
    {
        OnPropertyChanged(nameof(TotalAmountText));
        OnPropertyChanged(nameof(TotalQuantityText));
        OnPropertyChanged(nameof(CanCheckout));
    }

    private ReceiptModel BuildReceipt(IReadOnlyList<PosCartItemModel> items, OrderResponse response)
    {
        var total = response.TotalAmount > 0 ? response.TotalAmount : items.Sum(item => item.LineTotal);
        var final = response.FinalAmount > 0 ? response.FinalAmount : total;
        return new ReceiptModel
        {
            OrderId = response.OrderId,
            OrderCode = string.IsNullOrWhiteSpace(response.OrderCode) ? response.OrderId : response.OrderCode!,
            CreatedAt = DateTimeOffset.Now,
            StaffName = authStore.CurrentUser?.DisplayName ?? "",
            PaymentMethod = response.PaymentMethod,
            Items = items.Select(item => new OrderItemModel
            {
                BookId = item.BookId,
                BookTitle = item.Title,
                Quantity = item.Quantity,
                UnitPrice = item.UnitPrice,
                LineTotal = item.LineTotal
            }).ToArray(),
            TotalAmount = total,
            DiscountAmount = response.DiscountAmount,
            FinalAmount = final
        };
    }
}
