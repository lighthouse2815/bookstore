[CmdletBinding()]
param(
    [string]$DatabaseName = "bookstore_db",
    [string]$DatabaseUser = "bookstore_user",
    [string]$DatabasePassword = "123456",
    [string]$RootPassword = "root",
    [ValidateRange(6, 16)]
    [int]$CategoryCount = 12,
    [ValidateRange(15, 50)]
    [int]$SeedSize = 50
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if ($DatabaseName -ne "bookstore_db") {
    throw "Safety check failed: this script may only reset bookstore_db."
}

if ($DatabaseUser -notmatch '^[a-zA-Z0-9_]+$') {
    throw "DatabaseUser contains unsupported characters."
}

Push-Location (Split-Path -Parent $PSScriptRoot)

$oldDbName = $env:DB_NAME
$oldDbUser = $env:DB_USER
$oldDbPassword = $env:DB_PASSWORD
$oldSeedSize = $env:APP_SEED_SIZE
$oldCategoryCount = $env:APP_SEED_CATEGORY_COUNT

try {
    docker compose up -d mysql | Out-Host

    $ready = $false
    for ($attempt = 1; $attempt -le 30; $attempt++) {
        docker exec -e "MYSQL_PWD=$RootPassword" bookstore-mysql mysqladmin ping -uroot --silent 2>$null
        if ($LASTEXITCODE -eq 0) {
            $ready = $true
            break
        }
        Start-Sleep -Seconds 1
    }

    if (-not $ready) {
        throw "MySQL did not become ready within 30 seconds."
    }

    $escapedPassword = $DatabasePassword.Replace("'", "''")
    $resetSql = @"
DROP DATABASE IF EXISTS ``$DatabaseName``;
CREATE DATABASE ``$DatabaseName`` CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS '$DatabaseUser'@'%' IDENTIFIED BY '$escapedPassword';
ALTER USER '$DatabaseUser'@'%' IDENTIFIED BY '$escapedPassword';
GRANT ALL PRIVILEGES ON ``$DatabaseName``.* TO '$DatabaseUser'@'%';
FLUSH PRIVILEGES;
"@

    $resetSql | docker exec -i -e "MYSQL_PWD=$RootPassword" bookstore-mysql mysql -uroot
    if ($LASTEXITCODE -ne 0) {
        throw "Could not recreate database $DatabaseName."
    }

    $env:DB_NAME = $DatabaseName
    $env:DB_USER = $DatabaseUser
    $env:DB_PASSWORD = $DatabasePassword
    $env:APP_SEED_SIZE = $SeedSize
    $env:APP_SEED_CATEGORY_COUNT = $CategoryCount

    & .\mvnw.cmd -q -DskipTests "-Dspring-boot.run.profiles=dev,seed" spring-boot:run
    if ($LASTEXITCODE -ne 0) {
        throw "Schema creation or data seeding failed."
    }

    docker exec -e "MYSQL_PWD=$DatabasePassword" bookstore-mysql mysql "-u$DatabaseUser" "-D$DatabaseName" -e `
        "SELECT TABLE_NAME, TABLE_ROWS FROM information_schema.TABLES WHERE TABLE_SCHEMA='$DatabaseName' ORDER BY TABLE_NAME;"
    if ($LASTEXITCODE -ne 0) {
        throw "Database verification query failed."
    }

    $bookCount = docker exec -e "MYSQL_PWD=$DatabasePassword" bookstore-mysql mysql `
        "-u$DatabaseUser" "-D$DatabaseName" -N -e "SELECT COUNT(*) FROM books;"
    if ($LASTEXITCODE -ne 0) {
        throw "Seed verification query failed."
    }
    $bookCountValue = [int](($bookCount | Select-Object -Last 1).Trim())
    if ($bookCountValue -ne $SeedSize) {
        throw "Seed verification failed: books must contain exactly $SeedSize rows."
    }

    $categoryCountResult = docker exec -e "MYSQL_PWD=$DatabasePassword" bookstore-mysql mysql `
        "-u$DatabaseUser" "-D$DatabaseName" -N -e "SELECT COUNT(*) FROM categories;"
    $categoryCountValue = [int](($categoryCountResult | Select-Object -Last 1).Trim())
    if ($categoryCountValue -ne $CategoryCount) {
        throw "Seed verification failed: categories must contain exactly $CategoryCount rows."
    }
}
finally {
    $env:DB_NAME = $oldDbName
    $env:DB_USER = $oldDbUser
    $env:DB_PASSWORD = $oldDbPassword
    $env:APP_SEED_SIZE = $oldSeedSize
    $env:APP_SEED_CATEGORY_COUNT = $oldCategoryCount
    Pop-Location
}
