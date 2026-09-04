# ============================================================
#  One-shot dummy data setup. This is the only seed script you need
#  to run -- no separate SQL files to remember.
#
#  1) Runs backend/src/main/resources/sql/dummy_data1.sql +
#     long_description.sql via the mysql CLI: 25 users / 15 rooms
#     (10 world-heritage-themed with 6 photos each, 5 without) /
#     20 bookings / 12 reviews / 15 bookmarks. These accounts CANNOT
#     log in (dummy password hashes) -- they're screen/demo data
#     only. This step TRUNCATEs and reloads those tables every run,
#     so it always converges to the same state no matter what was
#     there before.
#  2) Creates ONE real, working login account by calling the
#     backend's own REST API (signup/login), so you have a login
#     that actually works to test auth-gated screens with.
#
#  Safe to re-run no matter the current DB state: step 1 always
#  resets to the same dataset, and step 2's demo account is reused
#  (signup failure is caught) if step 1 was skipped and it survived.
#
#  Prerequisite: backend must already be running (default http://localhost:8080).
#  If backend/uploads/seed/ doesn't have the seed photos yet, this
#  script runs copy-seed-images.ps1 for you first (source: ..\숙소 사진) --
#  step 1's rooms need those files.
#  If mysql.exe can't be found, step 1 is skipped with a warning --
#  step 2 still runs, so you always end up with a working login.
#
#  Run from the backend folder:
#    powershell -ExecutionPolicy Bypass -File .\seed-dummy-data.ps1
#
#  NOTE: this file MUST stay saved as UTF-8 *with BOM*.
#  Windows PowerShell 5.1 reads a BOM-less file as the ANSI codepage (cp949)
#  and the Korean literals below turn into garbage.
# ============================================================

param(
    [string]$BaseUrl = "http://localhost:8080"
)

$ErrorActionPreference = "Stop"

$backendDir = $PSScriptRoot
$seedDir = Join-Path $backendDir "uploads\seed"

$demoEmail = "demo@nubi.local"
$demoPassword = "password1234"

function Invoke-Json {
    # Invoke-RestMethod on Windows PowerShell 5.1 mis-decodes non-ASCII response
    # bodies when the server's Content-Type omits "charset=utf-8" (Spring's default
    # for application/json) -- Korean text comes back as mojibake. Invoke-WebRequest's
    # RawContentStream gives us the raw bytes so we can decode as UTF-8 ourselves.
    param(
        [string]$Uri,
        [string]$Method = "Get",
        [hashtable]$Body = $null,
        [hashtable]$Headers = @{}
    )
    $params = @{ Uri = $Uri; Method = $Method; Headers = $Headers; UseBasicParsing = $true }
    if ($Body) {
        $json = $Body | ConvertTo-Json -Depth 5
        $params["Body"] = [System.Text.Encoding]::UTF8.GetBytes($json)
        $params["ContentType"] = "application/json; charset=utf-8"
    }
    $response = Invoke-WebRequest @params
    $bytes = $response.RawContentStream.ToArray()
    if ($bytes.Length -eq 0) { return $null }
    $text = [System.Text.Encoding]::UTF8.GetString($bytes)
    try {
        return $text | ConvertFrom-Json
    } catch {
        return $text
    }
}

function Find-MySqlExe {
    $cmd = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $found = Get-ChildItem "C:\Program Files\MySQL\*\bin\mysql.exe", "C:\Program Files (x86)\MySQL\*\bin\mysql.exe" -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($found) { return $found.FullName }
    return $null
}

function Get-EnvValue {
    param([string]$Path, [string]$Key, [string]$Default)
    if (Test-Path -LiteralPath $Path) {
        $line = Get-Content -LiteralPath $Path | Where-Object { $_ -match "^\s*$Key\s*=" } | Select-Object -First 1
        if ($line) { return ($line -split '=', 2)[1].Trim() }
    }
    return $Default
}

# ------------------------------------------------------------
# 0) 시드 이미지 준비 (없으면 copy-seed-images.ps1 자동 실행)
#    1번 단계의 SQL이 이 파일들을 참조하므로 SQL 실행 전에 준비합니다.
# ------------------------------------------------------------
$probeFile = Join-Path $seedDir "jongmyo1.png"
if (-not (Test-Path -LiteralPath $probeFile)) {
    Write-Host ""
    Write-Host "0) 시드 이미지가 없어 copy-seed-images.ps1 을 먼저 실행합니다."
    & (Join-Path $backendDir "copy-seed-images.ps1")
}

# ------------------------------------------------------------
# 1) 리치 더미 데이터 (세계 유적 테마 숙소 15개 - 10개는 사진 포함,
#    예약/리뷰/북마크 포함) - mysql CLI로 SQL 실행
#    이 계정들은 로그인이 안 되는 화면용 데이터입니다. 매번 TRUNCATE 후
#    다시 채우므로, DB에 뭐가 있었든 항상 같은 상태로 수렴합니다.
# ------------------------------------------------------------
Write-Host ""
Write-Host "1) 리치 더미 데이터 (users/rooms/bookings/reviews/bookmarks)"

$mysqlExe = Find-MySqlExe
if (-not $mysqlExe) {
    Write-Host "   mysql.exe를 찾을 수 없어 건너뜁니다 (PATH에 추가하거나 MySQL을 설치하세요)." -ForegroundColor Yellow
} else {
    $envPath = Join-Path $backendDir ".env"
    $dbUrl = Get-EnvValue -Path $envPath -Key "DB_URL" -Default "jdbc:mysql://localhost:3306/accommodation_db"
    $dbUser = Get-EnvValue -Path $envPath -Key "DB_USERNAME" -Default "root"
    $dbPassword = Get-EnvValue -Path $envPath -Key "DB_PASSWORD" -Default "1234"

    if ($dbUrl -match 'jdbc:mysql://([^:/]+):(\d+)/([^?]+)') {
        $dbHost = $Matches[1]; $dbPort = $Matches[2]; $dbName = $Matches[3]
    } else {
        $dbHost = "localhost"; $dbPort = "3306"; $dbName = "accommodation_db"
    }

    $sqlFiles = @(
        (Join-Path $backendDir "src\main\resources\sql\dummy_data1.sql"),
        (Join-Path $backendDir "src\main\resources\sql\long_description.sql")
    )
    foreach ($sqlFile in $sqlFiles) {
        if (-not (Test-Path -LiteralPath $sqlFile)) {
            Write-Host "   파일 없음 (건너뜀): $sqlFile" -ForegroundColor Yellow
            continue
        }
        # mysql의 `source` 명령은 백슬래시를 이스케이프로 해석하므로 경로는 슬래시로 바꿔서 전달합니다.
        $sourcePath = $sqlFile.Replace('\', '/')
        & $mysqlExe "--default-character-set=utf8mb4" "-h" $dbHost "-P" $dbPort "-u" $dbUser "-p$dbPassword" "-e" "source $sourcePath;" $dbName
        if ($LASTEXITCODE -ne 0) {
            Write-Host "   실패: $(Split-Path $sqlFile -Leaf) (exit $LASTEXITCODE)" -ForegroundColor Yellow
        } else {
            Write-Host "   완료: $(Split-Path $sqlFile -Leaf)"
        }
    }
}

# ------------------------------------------------------------
# 2) 데모 계정 준비 (실제 로그인 가능한 계정)
#    주의: 1번 단계가 매번 users 테이블을 TRUNCATE하므로, 이 계정도
#    매번 새로 만들어집니다 (이전 실행에서 만든 계정은 사라집니다).
#    숙소는 따로 만들지 않습니다 - 1번 단계의 rooms 11~15가 이미
#    "사진 없는 일반 숙소" 역할을 하고 있어서 더 만들면 중복입니다.
# ------------------------------------------------------------
Write-Host ""
Write-Host "2) 데모 계정 준비 ($demoEmail)"

try {
    Invoke-Json -Method Post -Uri "$BaseUrl/api/accounts/signup" -Body @{
        email    = $demoEmail
        password = $demoPassword
        name     = "데모호스트"
        phone    = "010-1234-5678"
    } | Out-Null
    Write-Host "   새로 가입했습니다."
} catch {
    Write-Host "   이미 가입된 계정으로 보입니다. 로그인만 진행합니다."
}

Write-Host ""
Write-Host "완료. 테스트 계정: $demoEmail / $demoPassword"
Write-Host ""
