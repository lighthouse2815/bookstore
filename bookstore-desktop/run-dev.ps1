[CmdletBinding()]
param(
    [ValidateSet('win-x64', 'win-arm64')]
    [string]$Runtime = 'win-x64'
)

$ErrorActionPreference = 'Stop'

$repoDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectFile = Join-Path $repoDir 'Bookstore.Desktop\Bookstore.Desktop.csproj'
$runId = Get-Date -Format 'yyyyMMdd-HHmmss'
$publishDir = Join-Path ([System.IO.Path]::GetTempPath()) "bookstore-desktop-dev\$runId\$Runtime"
$executable = Join-Path $publishDir 'Bookstore.Desktop.exe'

Write-Host 'Dang dong goi ban Debug single-file moi nhat...'

& dotnet publish $projectFile `
    -f net10.0-windows `
    -c Debug `
    -r $Runtime `
    -p:PublishSingleFile=true `
    -p:SelfContained=true `
    -p:IncludeNativeLibrariesForSelfExtract=true `
    -p:EnableCompressionInSingleFile=true `
    -o $publishDir

if ($LASTEXITCODE -ne 0)
{
    throw "Dong goi Debug that bai voi ma loi $LASTEXITCODE."
}

if (-not (Test-Path -LiteralPath $executable))
{
    throw "Khong tim thay file chay sau khi dong goi: $executable"
}

$process = Start-Process `
    -FilePath $executable `
    -WorkingDirectory $repoDir `
    -PassThru

Write-Host "Da mo Bookstore POS (PID $($process.Id))."
Write-Host "Thu muc Debug tam: $publishDir"
