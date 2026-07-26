param(
    [switch]$ForceDownload,
    [switch]$VerboseCandidates,
    [switch]$SkipDownload,
    [switch]$SkipUpload,
    [switch]$SkipDatabase
)

$ErrorActionPreference = "Stop"
$ProgressPreference = "SilentlyContinue"

Add-Type -AssemblyName System.Drawing
Add-Type -AssemblyName System.Web

$backendRoot = Split-Path -Parent $PSScriptRoot
$catalogPath = Join-Path $backendRoot "src/main/java/com/bookstore/bookstore/infrastructure/persistence/DevelopmentSeedCatalog.java"
$envPath = Join-Path $backendRoot ".env"
$coverDirectory = Join-Path $backendRoot "target/seed-covers"
$sourceManifestPath = Join-Path $coverDirectory "sources.csv"
$sqlPath = Join-Path $backendRoot "target/seed-cover-migration.sql"

$canonicalTitles = @{
    "9781542625029" = "Pride and Prejudice"
    "9782075094450" = "Harry Potter and the Philosopher's Stone"
    "9780142423295" = "Wuthering Heights"
    "9787111124849" = "How to Win Friends and Influence People"
    "9781652775980" = "Animal Farm"
    "9785882156922" = "The Little Prince"
    "9780439451932" = "Harry Potter and the Chamber of Secrets"
    "9780792443483" = "The Hobbit"
    "9788390423029" = "The Alchemist"
    "9789500301961" = "Romeo and Juliet"
    "9781781105665" = "Harry Potter and the Prisoner of Azkaban"
    "9798463590435" = "The Adventures of Huckleberry Finn"
    "9798849341927" = "A Tale of Two Cities"
    "9781481903219" = "The Hunger Games"
    "9781911060260" = "The Wonderful Wizard of Oz"
    "9781299091757" = "Fifty Shades of Grey"
    "9781856137690" = "Harry Potter and the Goblet of Fire"
    "9798846711082" = "The Stranger"
    "9789544467616" = "Harry Potter and the Order of the Phoenix"
    "9782021011968" = "Diary of a Wimpy Kid"
    "9780316014410" = "Twilight"
    "9783423240321" = "Jane Eyre"
    "9781368098168" = "The Lightning Thief"
    "9798464861350" = "Oliver Twist"
    "9783257250329" = "Charlotte's Web"
    "9788497870801" = "The Da Vinci Code"
    "9788183220743" = "Harry Potter and the Half-Blood Prince"
    "9789387669208" = "The Diary of a Young Girl"
    "9787540210700" = "The Count of Monte Cristo"
    "9780061824562" = "Don Quixote"
    "9780571371723" = "Lord of the Flies"
    "9798600502628" = "The Old Man and the Sea"
    "9780812416824" = "The Adventures of Tom Sawyer"
    "9781517444853" = "Of Mice and Men"
    "9798574450031" = "Persuasion"
    "9786287574632" = "And Then There Were None"
    "9780385365765" = "A Brief History of Time"
    "9781847496386" = "The Wind in the Willows"
    "9781407037219" = "The Book Thief"
    "9781547904266" = "Murder on the Orient Express"
    "9781984055767" = "Tao Te Ching"
    "9780141392608" = "Les Miserables"
    "9798589232837" = "Madame Bovary"
    "9788417031275" = "Angels and Demons"
    "9780749303273" = "The Grapes of Wrath"
    "9788804436775" = "The Lion, the Witch and the Wardrobe"
    "9788416126835" = "The Very Hungry Caterpillar"
    "9785403034128" = "Tuesdays with Morrie"
    "9785389061606" = "The Perks of Being a Wallflower"
    "9781568653501" = "The Color Purple"
}

$preferredCoverUrls = @{
    "9780142423295" = "https://cdn2.penguin.com.au/covers/original/9780141326696.jpg"
    "9787111124849" = "https://d28hgpri8am2if.cloudfront.net/book_images/onix/cvr9781982171452/how-to-win-friends-and-influence-people-9781982171452_hr.jpg"
    "9785882156922" = "https://m.media-amazon.com/images/I/71OZY035QKL.jpg"
    "9780439451932" = "https://m.media-amazon.com/images/I/915KEvGiX-L._SL1500_.jpg"
    "9780792443483" = "https://m.media-amazon.com/images/I/81uEDUfKBZL.jpg"
    "9788390423029" = "https://m.media-amazon.com/images/I/71pJIgY8ZuL._SL1500_.jpg"
    "9789500301961" = "https://cdn2.penguin.com.au/covers/original/9780451526861.jpg"
    "9781781105665" = "https://res.cloudinary.com/bloomsbury-atlas/image/upload/w_360,c_scale,dpr_1.5/jackets/9781408855676.jpg"
    "9781481903219" = "https://m.media-amazon.com/images/I/91DJoFCspLL._SL1500_.jpg"
    "9781911060260" = "https://www.readandcobooks.co.uk/wp-content/uploads/wonderful-wizard-of-oz-baum-9781528718660-cover.jpg"
    "9781299091757" = "https://m.media-amazon.com/images/I/810BkqRP+iL.jpg"
    "9781856137690" = "https://m.media-amazon.com/images/I/91eSc94YJ7L._SL1500_.jpg"
    "9798846711082" = "https://m.media-amazon.com/images/I/71sOPkj3V1L._SL1500_.jpg"
    "9789544467616" = "https://m.media-amazon.com/images/I/81Budsu1XBL.jpg"
    "9782021011968" = "https://3.bp.blogspot.com/-vWm8B0jTtGw/UNBMOU1px0I/AAAAAAAACBg/lr40GFfNm7w/s1600/Diary+of+a+Wimpy+Kid+Book+1_ABRAMS.JPG"
    "9783423240321" = "https://cdn2.penguin.com.au/covers/original/9780141973746.jpg"
    "9798464861350" = "https://cdn2.penguin.com.au/covers/original/9780241736142.jpg"
    "9783257250329" = "https://cdn2.penguin.com.au/covers/original/9780141354828.jpg"
    "9788497870801" = "https://cdn2.penguin.com.au/covers/original/9780552159715.jpg"
    "9788183220743" = "https://m.media-amazon.com/images/I/81DN1723hUL._SL1500_.jpg"
    "9789387669208" = "https://m.media-amazon.com/images/I/81BKhSB8mEL._SL1500_.jpg"
    "9787540210700" = "https://m.media-amazon.com/images/I/81JCe8+BS-L._SL1500_.jpg"
    "9780571371723" = "https://m.media-amazon.com/images/I/71-WP1T-bjL._SL1200_.jpg"
    "9798600502628" = "https://d28hgpri8am2if.cloudfront.net/book_images/onix/cvr9781476787848/the-old-man-and-the-sea-9781476787848_hr.jpg"
    "9780812416824" = "https://m.media-amazon.com/images/I/81rc49pvJkL._UF1000,1000_QL80_.jpg"
    "9781517444853" = "https://cdn2.penguin.com.au/covers/original/9780141185101.jpg"
    "9798574450031" = "https://m.media-amazon.com/images/I/51wy4ISVLYL._SL1200_.jpg"
    "9786287574632" = "https://m.media-amazon.com/images/I/81Ut8bY0z9L._SL1500_.jpg"
    "9781847496386" = "https://m.media-amazon.com/images/I/815GcCTiG0L.jpg"
    "9781407037219" = "https://images.thenile.io/r1000/9781760783693.jpg"
    "9781547904266" = "https://m.media-amazon.com/images/I/71ihbKf67RL._SL1500_.jpg"
    "9781984055767" = "https://cdn2.penguin.com.au/covers/original/9781611800777.jpg"
    "9780141392608" = "https://m.media-amazon.com/images/I/91teZUyg4QS.jpg"
    "9798589232837" = "https://cdn.penguin.co.in/wp-content/uploads/sites/2/2024/08/9789815162554.jpg"
    "9788417031275" = "https://images.thenile.io/r1000/9780743493468.jpg"
    "9780749303273" = "https://cdn2.penguin.com.au/covers/original/9780141394886.jpg"
    "9788416126835" = "https://images.thenile.io/r1000/9780582504714.jpg"
    "9785403034128" = "https://m.media-amazon.com/images/I/71cwRo9eVML._SL1500_.jpg"
    "9781568653501" = "https://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1703084419i/52892857.jpg"
}

function Read-DotEnv([string]$Path) {
    $values = @{}
    foreach ($line in [System.IO.File]::ReadAllLines($Path, [System.Text.Encoding]::UTF8)) {
        $trimmed = $line.Trim()
        if (-not $trimmed -or $trimmed.StartsWith("#") -or -not $trimmed.Contains("=")) {
            continue
        }

        $separatorIndex = $trimmed.IndexOf("=")
        $name = $trimmed.Substring(0, $separatorIndex).Trim()
        $value = $trimmed.Substring($separatorIndex + 1).Trim()
        if (($value.StartsWith('"') -and $value.EndsWith('"')) -or
            ($value.StartsWith("'") -and $value.EndsWith("'"))) {
            $value = $value.Substring(1, $value.Length - 2)
        }
        $values[$name] = $value
    }
    return $values
}

function Require-Value($Values, [string]$Name) {
    $value = $Values[$Name]
    if ([string]::IsNullOrWhiteSpace($value)) {
        throw "Missing $Name in .env"
    }
    return $value
}

function Get-SeedBooks([string]$Path) {
    $content = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    $pattern = 'book\("(?<title>[^\"]+)",\s*"(?<author>[^\"]+)",\s*\d+,\s*\d+,\s*"(?<isbn>\d+)",\s*(?<coverId>\d+),'
    $matches = [regex]::Matches($content, $pattern)
    if ($matches.Count -ne 50) {
        throw "Expected 50 books in DevelopmentSeedCatalog.java, found $($matches.Count)"
    }

    return $matches | ForEach-Object {
        [pscustomobject]@{
            Title = $_.Groups['title'].Value
            Author = $_.Groups['author'].Value
            Isbn = $_.Groups['isbn'].Value
            CoverId = $_.Groups['coverId'].Value
        }
    }
}

function Get-BingImageUrls([string]$Query) {
    $encodedQuery = [uri]::EscapeDataString($Query)
    for ($attempt = 1; $attempt -le 3; $attempt++) {
        Start-Sleep -Milliseconds 750
        $response = Invoke-WebRequest `
            -Uri "https://www.bing.com/images/search?q=$encodedQuery&form=HDRSC2" `
            -UseBasicParsing `
            -TimeoutSec 30 `
            -Headers @{ "User-Agent" = "Mozilla/5.0" }
        $urls = @([regex]::Matches($response.Content, 'murl&quot;:&quot;(?<url>.*?)&quot;') |
            ForEach-Object { [System.Web.HttpUtility]::HtmlDecode($_.Groups['url'].Value) } |
            Where-Object { $_ -match '^https?://' } |
            Select-Object -Unique)
        if ($urls.Count -gt 0) {
            return $urls
        }
    }
    return @()
}

function Try-SaveCover([string]$Url, [string]$DestinationPath) {
    $candidatePath = "$DestinationPath.candidate"
    Remove-Item -LiteralPath $candidatePath -Force -ErrorAction SilentlyContinue

    try {
        Invoke-WebRequest `
            -Uri $Url `
            -OutFile $candidatePath `
            -UseBasicParsing `
            -MaximumRedirection 5 `
            -TimeoutSec 8 `
            -Headers @{ "User-Agent" = "Mozilla/5.0" }

        $candidateFile = Get-Item -LiteralPath $candidatePath
        if ($candidateFile.Length -lt 10KB -or $candidateFile.Length -gt 8MB) {
            if ($VerboseCandidates) { Write-Warning "Rejected by size: $Url ($($candidateFile.Length) bytes)" }
            return $false
        }

        $stream = [System.IO.File]::OpenRead($candidatePath)
        try {
            $image = [System.Drawing.Image]::FromStream($stream, $true, $true)
            try {
                $ratio = $image.Height / [double]$image.Width
                if ($image.Width -lt 250 -or $image.Height -lt 350 -or $ratio -lt 1.08 -or $ratio -gt 2.2) {
                    if ($VerboseCandidates) { Write-Warning "Rejected by dimensions: $Url ($($image.Width)x$($image.Height))" }
                    return $false
                }

                $bitmap = New-Object System.Drawing.Bitmap($image.Width, $image.Height)
                try {
                    $graphics = [System.Drawing.Graphics]::FromImage($bitmap)
                    try {
                        $graphics.Clear([System.Drawing.Color]::White)
                        $graphics.DrawImage($image, 0, 0, $image.Width, $image.Height)
                    } finally {
                        $graphics.Dispose()
                    }
                    $bitmap.Save($DestinationPath, [System.Drawing.Imaging.ImageFormat]::Jpeg)
                } finally {
                    $bitmap.Dispose()
                }
            } finally {
                $image.Dispose()
            }
        } finally {
            $stream.Dispose()
        }

        return $true
    } catch {
        if ($VerboseCandidates) { Write-Warning "Rejected after error: $Url ($($_.Exception.Message))" }
        return $false
    } finally {
        Remove-Item -LiteralPath $candidatePath -Force -ErrorAction SilentlyContinue
    }
}

if (-not (Test-Path -LiteralPath $envPath)) {
    throw "Cannot find $envPath"
}

$envValues = Read-DotEnv $envPath
$books = Get-SeedBooks $catalogPath
New-Item -ItemType Directory -Path $coverDirectory -Force | Out-Null

if (-not $SkipDownload) {
    if ($ForceDownload) {
        Get-ChildItem -LiteralPath $coverDirectory -Filter "*.jpg" -File -ErrorAction SilentlyContinue |
            Remove-Item -Force
    }

    $sources = [System.Collections.Generic.List[object]]::new()
    $index = 0
    foreach ($book in $books) {
        $index++
        $destinationPath = Join-Path $coverDirectory "$($book.Isbn).jpg"
        if (Test-Path -LiteralPath $destinationPath) {
            Write-Host "[$index/50] Existing cover: $($book.Title)"
            continue
        }

        Write-Host "[$index/50] Finding cover: $($book.Title)"
        $canonicalTitle = $canonicalTitles[$book.Isbn]
        if ([string]::IsNullOrWhiteSpace($canonicalTitle)) {
            throw "Missing canonical title for ISBN $($book.Isbn)"
        }
        $queries = @(
            "$canonicalTitle $($book.Author) book cover"
            "$($book.Title) $($book.Author) book cover"
        )
        $saved = $false
        $preferredCoverUrl = $preferredCoverUrls[$book.Isbn]
        if ($preferredCoverUrl -and (Try-SaveCover $preferredCoverUrl $destinationPath)) {
            $sources.Add([pscustomobject]@{
                isbn = $book.Isbn
                title = $book.Title
                author = $book.Author
                sourceUrl = $preferredCoverUrl
            })
            $saved = $true
        }
        foreach ($query in $queries) {
            if ($saved) {
                break
            }
            if ($VerboseCandidates) { Write-Host "Query: $query" }
            $candidateUrls = @(Get-BingImageUrls $query | Select-Object -First 8)
            foreach ($candidateUrl in $candidateUrls) {
                if (Try-SaveCover $candidateUrl $destinationPath) {
                    $sources.Add([pscustomobject]@{
                        isbn = $book.Isbn
                        title = $book.Title
                        author = $book.Author
                        sourceUrl = $candidateUrl
                    })
                    $saved = $true
                    break
                }
            }
            if ($saved) {
                break
            }
        }

        if (-not $saved) {
            throw "Cannot find a valid cover for $($book.Title) ($($book.Isbn))"
        }
    }

    if ($sources.Count -gt 0) {
        $sources | Export-Csv -LiteralPath $sourceManifestPath -NoTypeInformation -Encoding UTF8
    }
}

$missingCovers = @($books | Where-Object { -not (Test-Path -LiteralPath (Join-Path $coverDirectory "$($_.Isbn).jpg")) })
if ($missingCovers.Count -gt 0) {
    throw "$($missingCovers.Count) covers are still missing in $coverDirectory"
}

$storageBucket = Require-Value $envValues "STORAGE_BUCKET"
$storageEndpoint = Require-Value $envValues "STORAGE_ENDPOINT"
$storageAccessKey = Require-Value $envValues "STORAGE_ACCESS_KEY"
$storageSecretKey = Require-Value $envValues "STORAGE_SECRET_KEY"
$storagePublicBaseUrl = (Require-Value $envValues "STORAGE_PUBLIC_BASE_URL").TrimEnd('/')
$storageRegion = if ($envValues["STORAGE_REGION"]) { $envValues["STORAGE_REGION"] } else { "auto" }

if (-not $SkipUpload) {
    Write-Host "Uploading 50 covers to R2..."
    & docker run --rm `
        -e "AWS_ACCESS_KEY_ID=$storageAccessKey" `
        -e "AWS_SECRET_ACCESS_KEY=$storageSecretKey" `
        -e "AWS_DEFAULT_REGION=$storageRegion" `
        -v "${coverDirectory}:/covers:ro" `
        amazon/aws-cli `
        s3 cp /covers "s3://$storageBucket/public/seed/books/" `
        --recursive `
        --exclude "*" `
        --include "*.jpg" `
        --content-type "image/jpeg" `
        --cache-control "public, max-age=31536000, immutable" `
        --endpoint-url $storageEndpoint `
        --no-progress
    if ($LASTEXITCODE -ne 0) {
        throw "R2 upload failed"
    }
}

if (-not $SkipDatabase) {
    $databaseName = Require-Value $envValues "DB_NAME"
    $databaseUser = Require-Value $envValues "DB_USER"
    $databasePassword = Require-Value $envValues "DB_PASSWORD"
    $mysqlContainer = if ($envValues["MYSQL_CONTAINER_NAME"]) { $envValues["MYSQL_CONTAINER_NAME"] } else { "bookstore-mysql" }
    $escapedBucket = $storageBucket.Replace("'", "''")
    $escapedBaseUrl = $storagePublicBaseUrl.Replace("'", "''")

    $statements = [System.Collections.Generic.List[string]]::new()
    $statements.Add("START TRANSACTION;")
    foreach ($book in $books) {
        $coverPath = Join-Path $coverDirectory "$($book.Isbn).jpg"
        $sizeBytes = (Get-Item -LiteralPath $coverPath).Length
        $checksum = (Get-FileHash -LiteralPath $coverPath -Algorithm SHA256).Hash.ToLowerInvariant()
        $storageKey = "public/seed/books/$($book.Isbn).jpg"
        $publicUrl = "$escapedBaseUrl/$storageKey"

        $statements.Add(@"
UPDATE file_assets fa
JOIN book_images bi ON bi.file_asset_id = fa.id
JOIN books b ON b.id = bi.book_id
SET fa.bucket = '$escapedBucket',
    fa.provider = 'R2',
    fa.public_url = '$publicUrl',
    fa.storage_key = '$storageKey',
    fa.original_name = '$($book.Isbn).jpg',
    fa.content_type = 'image/jpeg',
    fa.size_bytes = $sizeBytes,
    fa.checksum_sha256 = '$checksum',
    fa.status = 'ACTIVE',
    fa.updated_at = UTC_TIMESTAMP(6)
WHERE b.isbn = '$($book.Isbn)' AND bi.primary_image = TRUE;
"@)
        $statements.Add("UPDATE books SET image_url = '$publicUrl', updated_at = UTC_TIMESTAMP(6) WHERE isbn = '$($book.Isbn)';")
        $statements.Add("UPDATE book_images bi JOIN books b ON b.id = bi.book_id SET bi.image_url = '$publicUrl' WHERE b.isbn = '$($book.Isbn)' AND bi.primary_image = TRUE;")
    }
    $statements.Add("COMMIT;")
    [System.IO.File]::WriteAllLines($sqlPath, $statements, [System.Text.UTF8Encoding]::new($false))

    Write-Host "Updating current book data..."
    Get-Content -LiteralPath $sqlPath -Raw |
        & docker exec -i -e "MYSQL_PWD=$databasePassword" $mysqlContainer `
            mysql --default-character-set=utf8mb4 -u $databaseUser $databaseName
    if ($LASTEXITCODE -ne 0) {
        throw "Database update failed"
    }

    $updatedCount = & docker exec -e "MYSQL_PWD=$databasePassword" $mysqlContainer `
        mysql -N -u $databaseUser $databaseName `
        -e "SELECT COUNT(*) FROM file_assets WHERE purpose='BOOK_IMAGE' AND public_url LIKE '$escapedBaseUrl/public/seed/books/%';"
    if ([int]$updatedCount -ne 50) {
        throw "Only $updatedCount/50 database covers point to R2"
    }
}

$sampleUrl = "$storagePublicBaseUrl/public/seed/books/$($books[0].Isbn).jpg"
try {
    $sampleResponse = Invoke-WebRequest -Uri $sampleUrl -Method Head -UseBasicParsing -TimeoutSec 20
    if ($sampleResponse.StatusCode -ne 200) {
        throw "HTTP $($sampleResponse.StatusCode)"
    }
} catch {
    throw "The public R2 cover is not reachable: $sampleUrl"
}

if (-not $SkipUpload -and -not $SkipDatabase) {
    Write-Host "Done: 50 covers are stored in R2 and synchronized with the database."
} elseif ($SkipUpload -and $SkipDatabase) {
    Write-Host "Done: 50 local covers are ready for visual review."
} else {
    Write-Host "Done: the requested cover migration steps completed successfully."
}
