param(
    [ValidateSet('Debug', 'Release')]
    [string]$Configuration = 'Debug',
    [string]$Runtime = 'win-x64'
)

$ErrorActionPreference = 'Stop'

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$projectFile = Join-Path $projectDir 'Bookstore.Desktop.csproj'
$publishDir = Join-Path $projectDir "artifacts\publish\$Configuration\$Runtime"

dotnet publish $projectFile -f net8.0-windows -c $Configuration -r $Runtime `
    -p:PublishSingleFile=true `
    -p:SelfContained=true `
    -p:IncludeNativeLibrariesForSelfExtract=true `
    -p:EnableCompressionInSingleFile=true `
    -o $publishDir

Start-Process -FilePath (Join-Path $publishDir 'Bookstore.Desktop.exe')
