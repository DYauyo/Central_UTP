# Levanta RestoControl: MySQL + GlassFish 7 + deploy de dist\RestoControl.war
$ErrorActionPreference = 'Continue'
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$proj = Split-Path -Parent $here
$war  = Join-Path $proj 'dist\RestoControl.war'
$log  = Join-Path $here 'levantar.log'
$ctx  = 'RestoControl'
Set-Content -Path $log -Value "=== levantar $(Get-Date -Format s) ===" -Encoding UTF8
function Log([string]$m) { Write-Host $m; Add-Content -Path $log -Value $m -Encoding UTF8 }
function Run($exe, [string[]]$a) { Log ("> " + $exe + " " + ($a -join ' ')); & $exe @a 2>&1 | ForEach-Object { Log ("  " + "$_") }; Log ("  exit=" + $LASTEXITCODE) }
function PortOpen([int]$p) { try { $c = New-Object Net.Sockets.TcpClient; $ok = $c.ConnectAsync('127.0.0.1', $p).Wait(1500); $c.Close(); return $ok } catch { return $false } }

Log "Proyecto: $proj"
Log "WAR: $war  existe=$(Test-Path $war)"

# ---------- JAVA ----------
Log "`n=== JAVA ==="
$cands = @()
$fastJdk = "$env:USERPROFILE\Downloads\jdk-17 (2) (1)\jdk-17"
if (Test-Path "$fastJdk\bin\java.exe") { $cands += $fastJdk } else {
if ($env:JAVA_HOME) { $cands += $env:JAVA_HOME }
foreach ($p in 'C:\Program Files\Java','C:\Program Files\Eclipse Adoptium','C:\Program Files\Microsoft','C:\Program Files\Zulu','C:\Program Files\BellSoft','C:\Program Files\Amazon Corretto','C:\Program Files\OpenJDK',"$env:USERPROFILE\.jdks") {
  Get-ChildItem $p -Directory -ErrorAction SilentlyContinue | ForEach-Object { $cands += $_.FullName } }
# JDK de asenv.bat de GlassFish y de Descargas
$desk = [Environment]::GetFolderPath('Desktop')
foreach ($ae in @("$desk\glassfish7\glassfish\config\asenv.bat")) { if (Test-Path $ae) { foreach ($l in (Get-Content $ae)) { if ($l -match '^\s*set\s+"?AS_JAVA=([^"]+)"?') { $cands += $matches[1].Trim() } } } }
foreach ($d in "$env:USERPROFILE\Downloads","$env:USERPROFILE\Desktop",'C:\','C:\Program Files') { Get-ChildItem $d -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -match 'jdk|java|jre' } | ForEach-Object { $cands += $_.FullName; Get-ChildItem $_.FullName -Directory -ErrorAction SilentlyContinue | ForEach-Object { $cands += $_.FullName } } }
foreach ($w in (where.exe java 2>$null)) { $cands += (Split-Path -Parent (Split-Path -Parent $w)) }
foreach ($k in 'HKLM:\SOFTWARE\JavaSoft\JDK','HKLM:\SOFTWARE\Microsoft\JDK','HKLM:\SOFTWARE\Eclipse Adoptium\JDK') { Get-ChildItem $k -Recurse -ErrorAction SilentlyContinue | ForEach-Object { $v = (Get-ItemProperty $_.PSPath -ErrorAction SilentlyContinue); if ($v.JavaHome) { $cands += $v.JavaHome }; if ($v.Path) { $cands += $v.Path } } }
}
Log ("  candidatos: " + (($cands | Select-Object -Unique) -join ' | '))
$jdk = $null; $best = 0
foreach ($c in ($cands | Select-Object -Unique)) {
  $j = Join-Path $c 'bin\java.exe'
  if (-not (Test-Path $j)) { continue }
  $v = (& $j -version 2>&1 | ForEach-Object { "$_" }) -join ' '
  $n = 0; if ($v -match 'version "(\d+)') { $n = [int]$matches[1] }
  Log "  $c -> $n"
  $score = if ($n -eq 17) { 100 } elseif ($n -ge 17 -and $n -le 21) { 90 - $n } elseif ($n -gt 21) { 50 - $n } else { 0 }
  if ($score -gt $best) { $best = $score; $jdk = $c }
}
if (-not $jdk) { Log "ERROR: no se encontro un JDK 17+ instalado."; exit 1 }
Log "JDK elegido: $jdk"
$env:JAVA_HOME = $jdk; $env:AS_JAVA = $jdk; $env:Path = "$jdk\bin;$env:Path"

# ---------- GLASSFISH ----------
Log "`n=== GLASSFISH ==="
$asadmins = @()
$fastGf = Join-Path ([Environment]::GetFolderPath('Desktop')) 'glassfish7\bin\asadmin.bat'
if (Test-Path $fastGf) { $asadmins += $fastGf } else {
Get-ChildItem "$env:APPDATA\NetBeans" -Recurse -Filter '*.properties' -ErrorAction SilentlyContinue | Where-Object { $_.FullName -match 'GlassFish' } | ForEach-Object {
  foreach ($l in (Get-Content $_.FullName)) { if ($l -match '^homefolder=(.+)$') { $h = $matches[1] -replace '\\\\','\' -replace '\\:',':'; Log "  NetBeans registra GlassFish en: $h"; $asadmins += (Join-Path $h 'bin\asadmin.bat') } } }
$roots = @([Environment]::GetFolderPath('Desktop'),'C:\','D:\',$env:USERPROFILE,"$env:USERPROFILE\Downloads","$env:USERPROFILE\Desktop","$env:USERPROFILE\Documents",'C:\Program Files','C:\Program Files (x86)')
$skip = 'Windows','ProgramData','$Recycle.Bin','System Volume Information','Recovery','PerfLogs','AppData','OneDrive','Program Files','Program Files (x86)','Users'
foreach ($r in $roots) {
  Get-ChildItem $r -Directory -ErrorAction SilentlyContinue | Where-Object { $skip -notcontains $_.Name } | ForEach-Object {
    Get-ChildItem $_.FullName -Recurse -Depth 3 -Filter 'asadmin.bat' -ErrorAction SilentlyContinue | ForEach-Object { $asadmins += $_.FullName } } }
}
$asadmins = $asadmins | Where-Object { Test-Path $_ } | Select-Object -Unique
$asadmins | ForEach-Object { Log "  encontrado: $_" }
$asadmin = ($asadmins | Where-Object { $_ -match 'glassfish7' } | Select-Object -First 1)
if (-not $asadmin) { $asadmin = $asadmins | Select-Object -First 1 }
if (-not $asadmin) { Log "ERROR: no se encontro GlassFish (asadmin.bat)."; exit 2 }
Log "asadmin: $asadmin"
$gfroot = Split-Path -Parent (Split-Path -Parent $asadmin)
Get-ChildItem $gfroot -Recurse -Depth 2 -Filter 'asenv.bat' -ErrorAction SilentlyContinue | ForEach-Object { Get-Content $_.FullName | Where-Object { $_ -match 'AS_JAVA' } | ForEach-Object { Log "  asenv: $_" } }

# ---------- MYSQL ----------
Log "`n=== MYSQL ==="
$svcs = Get-Service | Where-Object { $_.Name -match 'mysql|maria' }
$svcs | ForEach-Object { Log ("  servicio " + $_.Name + " = " + $_.Status) }
if (-not (PortOpen 3306)) {
  foreach ($s in $svcs) {
    if ($s.Status -ne 'Running') {
      Log "  Iniciando servicio $($s.Name) (puede pedir permiso de administrador)..."
      try { Start-Service $s.Name -ErrorAction Stop } catch { Start-Process powershell -Verb RunAs -Wait -ArgumentList "-NoProfile -Command Start-Service '$($s.Name)'" }
    }
  }
  if (-not (PortOpen 3306) -and (Test-Path 'C:\xampp\mysql\bin\mysqld.exe')) { Log "  Iniciando MySQL de XAMPP..."; Start-Process 'C:\xampp\mysql\bin\mysqld.exe' -ArgumentList '--defaults-file=C:\xampp\mysql\bin\my.ini','--standalone' -WorkingDirectory 'C:\xampp' -WindowStyle Hidden }
  for ($i = 0; $i -lt 20 -and -not (PortOpen 3306); $i++) { Start-Sleep 1 }
}
Log "  puerto 3306 abierto: $(PortOpen 3306)"
$mysql = $null
foreach ($p in 'C:\Program Files\MySQL','C:\Program Files (x86)\MySQL','C:\xampp\mysql','C:\laragon\bin\mysql','C:\Program Files\MariaDB 11.4','C:\Program Files\MariaDB 10.11') {
  if (-not $mysql) { $mysql = Get-ChildItem $p -Recurse -Depth 3 -Filter 'mysql.exe' -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName } }
if ($mysql) {
  Log "  cliente: $mysql"
  Run $mysql @('-uroot','-pAdmin123!','-e','SELECT COUNT(*) AS usuarios FROM sistema_restaurante2.usuario; SHOW TABLES FROM sistema_restaurante2;')
} else { Log "  (no se encontro mysql.exe para verificar la BD)" }

# ---------- DOMINIO + DEPLOY ----------
$port = 8080
$dx = Join-Path $gfroot 'glassfish\domains\domain1\config\domain.xml'
if (-not (Test-Path $dx)) { $dx = Join-Path $gfroot 'domains\domain1\config\domain.xml' }
if ((Test-Path $dx) -and ((Get-Content $dx -Raw) -match 'port="(\d+)" name="http-listener-1"')) { if ($matches[1]) { $port = [int]$matches[1] } }
Log "`n=== GLASSFISH: dominio (HTTP $port) ==="
Log "  $port abierto: $(PortOpen $port) | 4848 abierto: $(PortOpen 4848)"
if (-not (PortOpen 4848)) {
  $gfout = Join-Path $here 'glassfish_out.log'
  Log "> start-domain --verbose (oculto, salida en $gfout)"
  Start-Process -FilePath $asadmin -ArgumentList '--interactive=false','start-domain','--verbose','domain1' -WindowStyle Hidden -RedirectStandardOutput $gfout -RedirectStandardError "$gfout.err"
  for ($i = 0; $i -lt 150 -and -not (PortOpen 4848); $i++) { Start-Sleep 1 }
  Start-Sleep 5
  Log "  4848 abierto: $(PortOpen 4848) | $port abierto: $(PortOpen $port)"
  if (-not (PortOpen 4848)) { Log "  --- salida GlassFish ---"; Get-Content $gfout,"$gfout.err" -ErrorAction SilentlyContinue | Select-Object -Last 60 | ForEach-Object { Log "  $_" } }
}
Run $asadmin @('--interactive=false','list-applications')
Log "`n=== DEPLOY ==="
Run $asadmin @('--interactive=false','deploy','--force=true','--name',$ctx,'--contextroot',$ctx,$war)
Run $asadmin @('--interactive=false','list-applications')

$url = "http://localhost:$port/$ctx/login.jsp"
try { $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 60; Log "`nHTTP $($r.StatusCode) en $url" } catch { Log "`nHTTP ERROR en ${url}: $($_.Exception.Message)" }
Log "=== FIN ==="
Start-Process $url
