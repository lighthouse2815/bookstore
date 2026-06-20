using System.Collections.ObjectModel;
using Bookstore.Desktop.Models;
using CommunityToolkit.Mvvm.ComponentModel;

namespace Bookstore.Desktop.Stores;

public partial class PosCartStore : ObservableObject
{
    public ObservableCollection<PosCartItemModel> Items { get; } = new();

    public decimal TotalAmount => Items.Sum(item => item.LineTotal);
    public int TotalQuantity => Items.Sum(item => item.Quantity);
    public bool IsEmpty => Items.Count == 0;

    public void Add(BookModel book)
    {
        var existing = Items.FirstOrDefault(item => item.BookId == book.Id);
        if (existing != null)
        {
            existing.Quantity += 1;
            RefreshTotals();
            return;
        }

        var item = new PosCartItemModel(book, 1);
        item.PropertyChanged += (_, _) => RefreshTotals();
        Items.Add(item);
        RefreshTotals();
    }

    public void Increase(PosCartItemModel item)
    {
        item.Quantity += 1;
        RefreshTotals();
    }

    public void Decrease(PosCartItemModel item)
    {
        if (item.Quantity <= 1)
        {
            return;
        }

        item.Quantity -= 1;
        RefreshTotals();
    }

    public void Remove(PosCartItemModel item)
    {
        Items.Remove(item);
        RefreshTotals();
    }

    public void Clear()
    {
        Items.Clear();
        RefreshTotals();
    }

    private void RefreshTotals()
    {
        OnPropertyChanged(nameof(TotalAmount));
        OnPropertyChanged(nameof(TotalQuantity));
        OnPropertyChanged(nameof(IsEmpty));
    }
}
