# ============================================================
#  Copy room photos into the folder the app serves, renaming to ASCII.
#
#    ..\숙소 사진\고인돌1.png  ->  backend\uploads\seed\dolmen1.png
#
#  WebUploadConfig maps /uploads/** to the uploads folder, so after this
#  runs you can open http://localhost:8080/uploads/seed/dolmen1.png
#
#  Run from the backend folder:
#    powershell -ExecutionPolicy Bypass -File .\copy-seed-images.ps1
#
#  NOTE: this file MUST stay saved as UTF-8 *with BOM*.
#  Windows PowerShell 5.1 reads a BOM-less file as the ANSI codepage (cp949)
#  and the Korean filenames below turn into garbage.
# ============================================================

param(
    [string]$Src,
    [string]$Dst
)

$ErrorActionPreference = "Stop"

$backend = $PSScriptRoot
if (-not $Src) { $Src = Join-Path (Split-Path $backend -Parent) "숙소 사진" }
if (-not $Dst) { $Dst = Join-Path $backend "uploads\seed" }

# Korean source prefix -> ASCII slug. Must match the slug values in
# src\main\resources\sql\V3__seed_room_images.sql
$map = [ordered]@{
    "고인돌"   = "dolmen"
    "무령왕릉" = "muryeong"
    "선정릉"   = "seonjeongneung"
    "장군총"   = "janggunchong"
    "종묘"     = "jongmyo"
    "진시황릉" = "jinshihuang"
    "카타콤"   = "catacomb"
    "타지마할" = "tajmahal"
    "폼페이"   = "pompeii"
    "피라미드" = "pyramid"
}

# Guard against the encoding problem described above: if this file was read
# with the wrong codepage the Korean literals are mojibake and nothing is found.
# Probe a literal directly -- do NOT index into $map.Keys, an OrderedDictionary
# key collection is not indexable in PowerShell 5.1 and would return $null.
$probe = "고인돌"
if ($probe.Length -ne 3 -or [int][char]$probe[0] -ne 44256) {
    Write-Host ""
    Write-Host "This script was not read as UTF-8." -ForegroundColor Red
    Write-Host "Re-save copy-seed-images.ps1 as 'UTF-8 with BOM' and run it again." -ForegroundColor Red
    Write-Host ""
    exit 1
}

if (-not (Test-Path -LiteralPath $Src)) {
    Write-Host "Source folder not found: $Src" -ForegroundColor Red
    Write-Host "Pass it explicitly:  .\copy-seed-images.ps1 -Src 'C:\path\to\photos'" -ForegroundColor Yellow
    exit 1
}
New-Item -ItemType Directory -Force -Path $Dst | Out-Null

Write-Host ""
Write-Host "  from : $Src"
Write-Host "  to   : $Dst"
Write-Host ""

$copied  = 0
$missing = @()

foreach ($kw in $map.Keys) {
    $slug = $map[$kw]
    for ($i = 1; $i -le 6; $i++) {
        $from = Join-Path $Src "$kw$i.png"
        $to   = Join-Path $Dst "$slug$i.png"
        if (Test-Path -LiteralPath $from) {
            Copy-Item -LiteralPath $from -Destination $to -Force
            $copied++
        } else {
            $missing += "$kw$i.png"
        }
    }
}

Write-Host "Copied $copied file(s). (expected 60)"

if ($missing.Count -gt 0) {
    Write-Host ""
    Write-Host "Not found ($($missing.Count)):" -ForegroundColor Yellow
    $missing | ForEach-Object { Write-Host "  - $_" -ForegroundColor Yellow }
}

Write-Host ""
Write-Host "Next: run src\main\resources\sql\V3__seed_room_images.sql in MySQL."
Write-Host ""
