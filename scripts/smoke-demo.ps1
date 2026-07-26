param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$UserUsername = "minhanh.nguyen",
    [string]$UserPassword = $env:APP_DEMO_USER_PASSWORD,
    [string]$AdminUsername = $(if ($env:ADMIN_USERNAME) { $env:ADMIN_USERNAME } else { "admin_demo" }),
    [string]$AdminPassword = $env:ADMIN_PASSWORD
)

$ErrorActionPreference = "Stop"

function Resolve-RequiredValue {
    param(
        [string]$Value,
        [string]$Name
    )

    if ([string]::IsNullOrWhiteSpace($Value)) {
        throw "Missing required value: $Name"
    }

    return $Value.Trim()
}

function Invoke-JsonRequest {
    param(
        [ValidateSet("GET", "POST", "PUT", "DELETE")]
        [string]$Method,
        [string]$Url,
        [hashtable]$Headers = @{},
        [object]$Body = $null
    )

    $params = @{
        Method  = $Method
        Uri     = $Url
        Headers = $Headers
    }

    if ($null -ne $Body) {
        $params.ContentType = "application/json"
        $params.Body = ($Body | ConvertTo-Json -Depth 10)
    }

    return Invoke-RestMethod @params
}

function Assert-ApiSuccess {
    param(
        [object]$Response,
        [string]$CheckName
    )

    if ($null -eq $Response -or -not $Response.success) {
        throw "$CheckName failed"
    }

    Write-Host "PASS - $CheckName"
}

function New-BearerHeader {
    param([string]$Token)
    return @{ Authorization = "Bearer $Token" }
}

function Join-Url {
    param(
        [string]$Root,
        [string]$Path
    )

    $normalizedRoot = $Root.TrimEnd("/")
    if ($Path.StartsWith("/")) {
        return "$normalizedRoot$Path"
    }

    return "$normalizedRoot/$Path"
}

$BaseUrl = $BaseUrl.TrimEnd("/")
$UserPassword = Resolve-RequiredValue -Value $UserPassword -Name "APP_DEMO_USER_PASSWORD or -UserPassword"
$AdminPassword = Resolve-RequiredValue -Value $AdminPassword -Name "ADMIN_PASSWORD or -AdminPassword"

Write-Host "Smoke target: $BaseUrl"

$health = Invoke-RestMethod -Method Get -Uri (Join-Url -Root $BaseUrl -Path "/actuator/health")
if ($health.status -ne "UP") {
    throw "Health check failed: expected UP, got $($health.status)"
}
Write-Host "PASS - backend health"

$userLogin = Invoke-JsonRequest -Method POST -Url (Join-Url -Root $BaseUrl -Path "/api/auth/login") -Body @{
    username = $UserUsername
    password = $UserPassword
}
Assert-ApiSuccess -Response $userLogin -CheckName "user login"
$userToken = Resolve-RequiredValue -Value $userLogin.data.accessToken -Name "user access token"

$adminLogin = Invoke-JsonRequest -Method POST -Url (Join-Url -Root $BaseUrl -Path "/api/auth/login") -Body @{
    username = $AdminUsername
    password = $AdminPassword
}
Assert-ApiSuccess -Response $adminLogin -CheckName "admin login"
$adminToken = Resolve-RequiredValue -Value $adminLogin.data.accessToken -Name "admin access token"

$books = Invoke-JsonRequest -Method GET -Url (Join-Url -Root $BaseUrl -Path "/api/books?page=0&size=3")
Assert-ApiSuccess -Response $books -CheckName "books list"
if (-not $books.data -or $books.data.Count -lt 1) {
    throw "Books list is empty"
}

$book = $books.data[0]
$bookId = Resolve-RequiredValue -Value ([string]$book.id) -Name "book id"

$categories = Invoke-JsonRequest -Method GET -Url (Join-Url -Root $BaseUrl -Path "/api/categories")
Assert-ApiSuccess -Response $categories -CheckName "categories list"

$pageDetail = Invoke-JsonRequest -Method GET -Url (Join-Url -Root $BaseUrl -Path "/api/books/$bookId/page-detail")
Assert-ApiSuccess -Response $pageDetail -CheckName "book page detail"

$activeCoupons = Invoke-JsonRequest -Method GET -Url (Join-Url -Root $BaseUrl -Path "/api/coupons/active")
Assert-ApiSuccess -Response $activeCoupons -CheckName "active coupons"
if (-not $activeCoupons.data -or $activeCoupons.data.Count -lt 1) {
    throw "Active coupon list is empty"
}

$userHeaders = New-BearerHeader -Token $userToken
$cart = Invoke-JsonRequest -Method GET -Url (Join-Url -Root $BaseUrl -Path "/api/cart") -Headers $userHeaders
Assert-ApiSuccess -Response $cart -CheckName "cart fetch"

if (-not $cart.data.items -or $cart.data.items.Count -lt 1) {
    $cart = Invoke-JsonRequest -Method POST -Url (Join-Url -Root $BaseUrl -Path "/api/cart/items") -Headers $userHeaders -Body @{
        bookId   = $bookId
        quantity = 1
    }
    Assert-ApiSuccess -Response $cart -CheckName "cart add fallback"
}

$cartItemIds = @($cart.data.items | ForEach-Object { $_.id } | Where-Object { $_ })
if ($cartItemIds.Count -lt 1) {
    throw "Cart has no item IDs for best-coupon smoke"
}

$bestCouponQuery = ($cartItemIds | ForEach-Object { "itemIds=$_" }) -join "&"
$bestCouponUrl = Join-Url -Root $BaseUrl -Path "/api/cart/best-coupon?$bestCouponQuery&shippingMethod=DELIVERY"
$bestCoupon = Invoke-JsonRequest -Method GET -Url $bestCouponUrl -Headers $userHeaders
Assert-ApiSuccess -Response $bestCoupon -CheckName "best coupon"
if (-not $bestCoupon.data.available -or [string]::IsNullOrWhiteSpace([string]$bestCoupon.data.couponCode)) {
    throw "Best coupon response did not return an applicable coupon"
}

$wishlistBefore = Invoke-JsonRequest -Method GET -Url (Join-Url -Root $BaseUrl -Path "/api/wishlist") -Headers $userHeaders
Assert-ApiSuccess -Response $wishlistBefore -CheckName "wishlist fetch"
$wishlistBeforeIds = @($wishlistBefore.data | ForEach-Object { [string]$_.id })

$candidateBook = $books.data | Where-Object { $wishlistBeforeIds -notcontains [string]$_.id } | Select-Object -First 1
if ($null -ne $candidateBook) {
    $wishlistBookId = [string]$candidateBook.id
    $restoreMode = $false
} elseif ($wishlistBeforeIds.Count -gt 0) {
    $wishlistBookId = $wishlistBeforeIds[0]
    $restoreMode = $true
} else {
    throw "Could not find a book for wishlist smoke"
}

if ($restoreMode) {
    $wishlistRemoved = Invoke-JsonRequest -Method DELETE -Url (Join-Url -Root $BaseUrl -Path "/api/wishlist/items/$wishlistBookId") -Headers $userHeaders
    Assert-ApiSuccess -Response $wishlistRemoved -CheckName "wishlist remove"
    $wishlistRestored = Invoke-JsonRequest -Method POST -Url (Join-Url -Root $BaseUrl -Path "/api/wishlist/items/$wishlistBookId") -Headers $userHeaders
    Assert-ApiSuccess -Response $wishlistRestored -CheckName "wishlist restore"
} else {
    $wishlistAdded = Invoke-JsonRequest -Method POST -Url (Join-Url -Root $BaseUrl -Path "/api/wishlist/items/$wishlistBookId") -Headers $userHeaders
    Assert-ApiSuccess -Response $wishlistAdded -CheckName "wishlist add"
    $wishlistRemoved = Invoke-JsonRequest -Method DELETE -Url (Join-Url -Root $BaseUrl -Path "/api/wishlist/items/$wishlistBookId") -Headers $userHeaders
    Assert-ApiSuccess -Response $wishlistRemoved -CheckName "wishlist remove"
}

$adminHeaders = New-BearerHeader -Token $adminToken
$dashboard = Invoke-JsonRequest -Method GET -Url (Join-Url -Root $BaseUrl -Path "/api/admin/dashboard/summary") -Headers $adminHeaders
Assert-ApiSuccess -Response $dashboard -CheckName "admin dashboard summary"
if ($dashboard.data.totalOrders -lt 1) {
    throw "Dashboard summary has no orders"
}
if ($dashboard.data.lowStockBooks -lt 1) {
    throw "Dashboard summary has no low-stock books"
}

$adminOrders = Invoke-JsonRequest -Method GET -Url (Join-Url -Root $BaseUrl -Path "/api/admin/orders?page=0&size=1") -Headers $adminHeaders
Assert-ApiSuccess -Response $adminOrders -CheckName "admin order list"

Write-Host ""
Write-Host "Smoke demo checks completed successfully."
