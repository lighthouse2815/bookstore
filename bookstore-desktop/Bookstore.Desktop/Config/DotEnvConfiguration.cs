using System.IO;

namespace Bookstore.Desktop.Config;

internal static class DotEnvConfiguration
{
    private static readonly Lazy<IReadOnlyDictionary<string, string>> FileValues =
        new(LoadFileValues);

    public static string? GetFirst(params string[] names)
    {
        foreach (var name in names)
        {
            var processValue = Environment.GetEnvironmentVariable(name);
            if (!string.IsNullOrWhiteSpace(processValue))
            {
                return processValue.Trim();
            }
        }

        foreach (var name in names)
        {
            if (FileValues.Value.TryGetValue(name, out var fileValue)
                && !string.IsNullOrWhiteSpace(fileValue))
            {
                return fileValue.Trim();
            }
        }

        return null;
    }

    internal static IReadOnlyDictionary<string, string> ParseLines(IEnumerable<string> lines)
    {
        var values = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);

        foreach (var rawLine in lines)
        {
            var line = rawLine.Trim();
            if (line.Length == 0 || line.StartsWith('#'))
            {
                continue;
            }

            if (line.StartsWith("export ", StringComparison.OrdinalIgnoreCase))
            {
                line = line[7..].TrimStart();
            }

            var separatorIndex = line.IndexOf('=');
            if (separatorIndex <= 0)
            {
                continue;
            }

            var key = line[..separatorIndex].Trim();
            if (!IsValidKey(key))
            {
                continue;
            }

            var value = line[(separatorIndex + 1)..].Trim();
            if (value.Length >= 2
                && ((value[0] == '"' && value[^1] == '"')
                    || (value[0] == '\'' && value[^1] == '\'')))
            {
                value = value[1..^1];
            }

            values[key] = value;
        }

        return values;
    }

    private static IReadOnlyDictionary<string, string> LoadFileValues()
    {
        var values = new Dictionary<string, string>(StringComparer.OrdinalIgnoreCase);

        foreach (var path in EnumerateCandidateFiles())
        {
            if (!File.Exists(path))
            {
                continue;
            }

            try
            {
                foreach (var (key, value) in ParseLines(File.ReadLines(path)))
                {
                    values.TryAdd(key, value);
                }
            }
            catch (IOException)
            {
                // Keep looking for another local configuration file.
            }
            catch (UnauthorizedAccessException)
            {
                // Keep looking for another local configuration file.
            }
        }

        return values;
    }

    private static IEnumerable<string> EnumerateCandidateFiles()
    {
        var localFiles = new List<string>();
        var siblingFiles = new List<string>();
        var seenDirectories = new HashSet<string>(StringComparer.OrdinalIgnoreCase);

        foreach (var startPath in new[] { AppContext.BaseDirectory, Environment.CurrentDirectory })
        {
            var directory = new DirectoryInfo(startPath);
            while (directory is not null)
            {
                if (seenDirectories.Add(directory.FullName))
                {
                    localFiles.Add(Path.Combine(directory.FullName, ".env"));
                    siblingFiles.Add(Path.Combine(directory.FullName, "bookstore-website", ".env"));
                    siblingFiles.Add(Path.Combine(directory.FullName, "bookstore-backend", ".env"));
                }

                directory = directory.Parent;
            }
        }

        return localFiles.Concat(siblingFiles).Distinct(StringComparer.OrdinalIgnoreCase);
    }

    private static bool IsValidKey(string key)
    {
        if (key.Length == 0 || !(char.IsLetter(key[0]) || key[0] == '_'))
        {
            return false;
        }

        return key.All(character => char.IsLetterOrDigit(character) || character == '_');
    }
}
