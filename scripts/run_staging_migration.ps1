<#
# PowerShell script: run_staging_migration.ps1
# Purpose: backup staging DB, run Flyway migrations (V1 baseline → V2 fix users → V3 clean indexes), verify and provide rollback guidance.
# Note: Run this on a machine that can reach the staging DB (or on the staging host).
# Usage:
#   Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
#   .\run_staging_migration.ps1
# The script:
# - prompts for DB connection info (or use defaults)
# - creates a mysqldump backup to BackupDir
# - runs migration via Flyway (maven) or executes the SQL file directly
# - verifies important checks and writes a log
# - prints rollback command using the generated backup file
# Security: do not put plaintext passwords in shared terminals. Use secrets store for CI.
#>

param(
    [string]$DbHost = "localhost",
    [int]$DbPort = 3306,
    [string]$DbName = "devnote",
    [string]$DbUser = "root",
    [string]$DbPassword = "",
    [string]$BackupDir = "C:\backups",
    [string]$ProjectRoot = "",
    [string]$MigrationFile = "src/main/resources/db/migration/V2__fix_users.sql",
    [switch]$UseFlyway = $true
)

# 如果未传入 ProjectRoot，基于脚本位置解析（支持从任意工作目录运行）
if ([string]::IsNullOrEmpty($ProjectRoot)) {
    if ($PSScriptRoot) { $scriptDir = $PSScriptRoot }
    elseif ($MyInvocation -and $MyInvocation.MyCommand -and $MyInvocation.MyCommand.Definition) { $scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition }
    else { $scriptDir = (Get-Location).Path }
    try { $ProjectRoot = (Resolve-Path (Join-Path $scriptDir "..")).Path } catch { $ProjectRoot = (Join-Path $scriptDir "..") }
}

# 交互式提示（如果未设置密码则要求输入）
if ([string]::IsNullOrEmpty($DbPassword)) {
    Write-Host "Enter database password (input will be hidden):" -NoNewline
    $DbPasswordSecure = Read-Host -AsSecureString
    $bstr = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($DbPasswordSecure)
    try { $DbPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto($bstr) } finally { [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($bstr) }
}

# Confirm
Write-Host ("About to migrate database {0}@{1}:{2}/{3}" -f $DbUser, $DbHost, $DbPort, $DbName) -ForegroundColor Yellow
$confirm = Read-Host "Type YES to continue"
if ($confirm -ne 'YES') { Write-Host "Operation canceled" -ForegroundColor Cyan; exit 0 }

# 创建备份目录
if (-not (Test-Path $BackupDir)) { New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null }
$ts = Get-Date -Format yyyyMMddHHmmss
$backupFile = Join-Path $BackupDir "devnote_backup_$ts.sql"
$logFile = Join-Path $BackupDir "devnote_migrate_log_$ts.txt"

# 1) 备份数据库
$WriteHostPrefix = "[INFO]"
Write-Host "Starting database backup to $backupFile ..." -ForegroundColor Green
$dumpCmd = "mysqldump -h $DbHost -P $DbPort -u $DbUser -p`"$DbPassword`" $DbName --single-transaction --quick --routines --events > `"$backupFile`""
Write-Host "Running backup command (via cmd.exe) ..." -ForegroundColor Gray
$dumpExit = cmd.exe /c $dumpCmd
if ($LASTEXITCODE -ne 0) {
    Write-Host "Backup failed (exit code $LASTEXITCODE). Check mysqldump and permissions." -ForegroundColor Red
    exit 1
}
Write-Host "Backup completed: $backupFile" -ForegroundColor Green
Add-Content -Path $logFile -Value "[$(Get-Date)] Backup created: $backupFile"

# 2) 预检查（查询重复 username，email NULL 计数）
Write-Host "Running pre-checks..." -ForegroundColor Green
$checkCmd1 = "mysql -h $DbHost -P $DbPort -u $DbUser -p`"$DbPassword`" -e `"SELECT username, COUNT(*) AS c FROM users GROUP BY username HAVING c>1;`" $DbName"
$checkCmd2 = "mysql -h $DbHost -P $DbPort -u $DbUser -p`"$DbPassword`" -e `"SELECT COUNT(*) FROM users WHERE email IS NULL OR TRIM(email) = '';`" $DbName"
Write-Host "Checking for duplicate usernames (if any results, duplicates exist)..." -ForegroundColor Gray
cmd.exe /c $checkCmd1 | Tee-Object -FilePath $logFile -Append
Write-Host "Counting empty/null emails..." -ForegroundColor Gray
cmd.exe /c $checkCmd2 | Tee-Object -FilePath $logFile -Append

Write-Host "If duplicate usernames exist, handle them before proceeding (or confirm manual merge strategy)." -ForegroundColor Yellow

# 3) 执行迁移（根据选择）
if ($UseFlyway) {
    Write-Host "Running Flyway migration via Maven..." -ForegroundColor Green
    # Prefer project mvnw (mvnw.cmd on Windows) then mvn
    $mavenCmd = $null
    $candidates = @("mvnw.cmd","mvnw")
    foreach ($c in $candidates) { $p = Join-Path $ProjectRoot $c; if (Test-Path $p) { $mavenCmd = $p; break } }
    if (-not $mavenCmd) { $mavenCmd = "mvn" }
    $flywayUrl = "jdbc:mysql://$($DbHost):$($DbPort)/$($DbName)"
    $flywayCmd = "& `"$mavenCmd`" -DskipTests=true -Dflyway.url=`"$flywayUrl`" -Dflyway.user=$DbUser -Dflyway.password=$DbPassword flyway:migrate"
    Write-Host "Running: $flywayCmd" -ForegroundColor Gray
    Invoke-Expression $flywayCmd 2>&1 | Tee-Object -FilePath $logFile -Append
    if ($LASTEXITCODE -ne 0) { Write-Host "Flyway migrate failed (exit $LASTEXITCODE). Check log." -ForegroundColor Red; exit 1 }
} else {
    Write-Host "Using mysql CLI to execute migration SQL file: $MigrationFile" -ForegroundColor Green
    $fullMigrationPath = Join-Path $ProjectRoot $MigrationFile
    if (Test-Path $fullMigrationPath) { $fullMigrationPath = (Resolve-Path $fullMigrationPath).Path }
    if (-not (Test-Path $fullMigrationPath)) { Write-Host "Migration file not found: $fullMigrationPath" -ForegroundColor Red; exit 1 }
    $sqlCmd = "mysql -h $DbHost -P $DbPort -u $DbUser -p`"$DbPassword`" $DbName < `"$fullMigrationPath`""
    Write-Host "Running: $sqlCmd" -ForegroundColor Gray
    cmd.exe /c $sqlCmd 2>&1 | Tee-Object -FilePath $logFile -Append
    if ($LASTEXITCODE -ne 0) { Write-Host "SQL file execution failed (exit $LASTEXITCODE). Check log and consider rollback." -ForegroundColor Red; exit 1 }
}

Add-Content -Path $logFile -Value "[$(Get-Date)] Migration executed."

# 4) 迁移后验证
Write-Host "Post-migration verification..." -ForegroundColor Green
$verify1 = "mysql -h $DbHost -P $DbPort -u $DbUser -p`"$DbPassword`" -e `"SHOW FULL COLUMNS FROM users LIKE 'password';`" $DbName"
$verify2 = "mysql -h $DbHost -P $DbPort -u $DbUser -p`"$DbPassword`" -e `"SELECT COUNT(*) FROM users WHERE email IS NULL OR TRIM(email) = '';`" $DbName"
$verify3 = "mysql -h $DbHost -P $DbPort -u $DbUser -p`"$DbPassword`" -e `"SELECT id, username, email FROM users WHERE email LIKE 'user%@example.invalid' LIMIT 10;`" $DbName"
$verify4 = "mysql -h $DbHost -P $DbPort -u $DbUser -p`"$DbPassword`" -e `"SHOW INDEX FROM users;`" $DbName
cmd.exe /c $verify1 | Tee-Object -FilePath $logFile -Append
cmd.exe /c $verify2 | Tee-Object -FilePath $logFile -Append
cmd.exe /c $verify3 | Tee-Object -FilePath $logFile -Append
cmd.exe /c $verify4 | Tee-Object -FilePath $logFile -Append

Write-Host "V3 Index check completed - verify no 'uc_users_username' index exists" -ForegroundColor Cyan

Write-Host "Verification complete. Check log: $logFile" -ForegroundColor Cyan

# 5) 回滚提示（如需恢复使用备份文件）
Write-Host "To rollback (example): mysql -u <user> -p <database> < $backupFile" -ForegroundColor Yellow
Write-Host "Script finished. Decide whether to promote migration to production based on verification." -ForegroundColor Green
# record
Add-Content -Path $logFile -Value "[$(Get-Date)] Script finished. Backup: $backupFile"
exit 0
