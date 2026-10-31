# Descarga Mojarra 4.0 (JSF) y Weld 5.1 (CDI) en lib\tomcat (necesarias para correr el proyecto en Tomcat 10.1)
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'
try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 } catch {}
$here = Split-Path -Parent $MyInvocation.MyCommand.Path
$lib  = Join-Path (Split-Path -Parent $here) 'lib\tomcat'
$log  = Join-Path $here 'descargar_libs_tomcat.log'
Set-Content -Path $log -Value "=== descargar_libs_tomcat $(Get-Date -Format s) ===" -Encoding UTF8
function Log([string]$m) { Write-Host $m; Add-Content -Path $log -Value $m -Encoding UTF8 }
New-Item -ItemType Directory -Force -Path $lib | Out-Null
function Bajar([string]$group, [string]$artifact, [string]$pattern, [string]$fallback) {
  $base = "https://repo1.maven.org/maven2/$($group -replace '\.','/')/$artifact"
  $ver = $fallback
  try {
    [xml]$m = (Invoke-WebRequest -Uri "$base/maven-metadata.xml" -UseBasicParsing -TimeoutSec 30).Content
    $v = @($m.metadata.versioning.versions.version) | Where-Object { $_ -match $pattern } |
         Sort-Object { [version]($_ -replace '\.Final$','') } | Select-Object -Last 1
    if ($v) { $ver = $v }
  } catch { Log "  (metadata: $($_.Exception.Message))" }
  $name = "$artifact-$ver.jar"; $dest = Join-Path $lib $name
  if (Test-Path -LiteralPath $dest) { Log "ok (ya existia): $name"; return }
  try {
    Invoke-WebRequest -Uri "$base/$ver/$name" -OutFile "$dest.part" -UseBasicParsing -TimeoutSec 300
    Move-Item -LiteralPath "$dest.part" -Destination $dest -Force
    Log "ok: $name ($((Get-Item -LiteralPath $dest).Length) bytes)"
  } catch { Log "ERROR $name : $($_.Exception.Message)"; Remove-Item -LiteralPath "$dest.part" -Force -ErrorAction SilentlyContinue }
}
Bajar 'org.glassfish' 'jakarta.faces' '^4\.0\.\d+$' '4.0.5'
Bajar 'org.jboss.weld.servlet' 'weld-servlet-shaded' '^5\.1\.\d+\.Final$' '5.1.2.Final'
Log "=== FIN ==="
