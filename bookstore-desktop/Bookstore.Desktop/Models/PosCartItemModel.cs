using Bookstore.Desktop.Helpers;
using CommunityToolkit.Mvvm.ComponentModel;

namespace Bookstore.Desktop.Models;

public partial class PosCartItemModel : ObservableObject
{
    public PosCartItemModel(BookModel book, int quantity)
    {
        Book = book;
        Quantity = quantity;
    }

    public BookModel Book { get; }
    public string BookId => Book.Id;
    public string Title => Book.Title;
    public string? Isbn => Book.Isbn;
    public decimal UnitPrice => Book.Price;

    [ObservableProperty]
    [NotifyPropertyChangedFor(nameof(LineTotal))]
    [NotifyPropertyChangedFor(nameof(UnitPriceText))]
    [NotifyPropertyChangedFor(nameof(LineTotalText))]
    private int quantity;

    public decimal LineTotal => UnitPrice * Quantity;
    public string UnitPriceText => CurrencyHelper.Format(UnitPrice);
    public string LineTotalText => CurrencyHelper.Format(LineTotal);
}
