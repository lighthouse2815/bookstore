namespace Bookstore.Desktop.Helpers;

public static class VietnameseCurrencyTextHelper
{
    private static readonly string[] DigitWords =
    {
        "không",
        "một",
        "hai",
        "ba",
        "bốn",
        "năm",
        "sáu",
        "bảy",
        "tám",
        "chín"
    };

    public static string ToWords(decimal amount)
    {
        var wholeAmount = decimal.Truncate(amount);
        if (wholeAmount < 0)
        {
            return "âm " + ToWords(decimal.Negate(wholeAmount));
        }

        if (wholeAmount == 0)
        {
            return "không đồng";
        }

        var groups = new List<int>();
        while (wholeAmount > 0)
        {
            groups.Add((int)(wholeAmount % 1_000));
            wholeAmount = decimal.Truncate(wholeAmount / 1_000);
        }

        var parts = new List<string>();
        var highestGroupIndex = groups.Count - 1;
        for (var groupIndex = highestGroupIndex; groupIndex >= 0; groupIndex--)
        {
            var groupValue = groups[groupIndex];
            if (groupValue == 0)
            {
                continue;
            }

            parts.Add(ReadThreeDigits(groupValue, groupIndex < highestGroupIndex));
            var scale = GetScale(groupIndex);
            if (scale.Length > 0)
            {
                parts.Add(scale);
            }
        }

        return string.Join(" ", parts) + " đồng";
    }

    private static string ReadThreeDigits(int value, bool includeLeadingHundreds)
    {
        var parts = new List<string>();
        var hundreds = value / 100;
        var tens = value % 100 / 10;
        var ones = value % 10;

        if (hundreds > 0 || includeLeadingHundreds)
        {
            parts.Add(DigitWords[hundreds]);
            parts.Add("trăm");
        }

        if (tens > 1)
        {
            parts.Add(DigitWords[tens]);
            parts.Add("mươi");
        }
        else if (tens == 1)
        {
            parts.Add("mười");
        }
        else if (ones > 0 && (hundreds > 0 || includeLeadingHundreds))
        {
            parts.Add("lẻ");
        }

        if (ones > 0)
        {
            parts.Add(ones switch
            {
                1 when tens > 1 => "mốt",
                4 when tens > 1 => "tư",
                5 when tens >= 1 => "lăm",
                _ => DigitWords[ones]
            });
        }

        return string.Join(" ", parts);
    }

    private static string GetScale(int groupIndex)
    {
        if (groupIndex == 0)
        {
            return "";
        }

        var scaleParts = new List<string>();
        switch (groupIndex % 3)
        {
            case 1:
                scaleParts.Add("ngàn");
                break;
            case 2:
                scaleParts.Add("triệu");
                break;
        }

        for (var index = 0; index < groupIndex / 3; index++)
        {
            scaleParts.Add("tỷ");
        }

        return string.Join(" ", scaleParts);
    }
}
