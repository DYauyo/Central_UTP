# Levanta RestoControl en Apache Tomcat 10.1:
#   MySQL -> librerias JSF/CDI -> compila -> empaqueta WAR -> deploy en Tomcat -> abre el navegador
# Tomcat no trae JSF ni CDI (GlassFish si), por eso se agregan Mojarra 4.0 + Weld 5.1 solo al WAR de Tomcat
# (se guardan en lib\tomcat y NO afectan el build de NetBeans/GlassFish).
$ErrorActionPreference = 'Continue'
$ProgressPreference = 'SilentlyContinue'
try { [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12 } catch {}

$here      = Split-Path -Parent $MyInvocation.MyCommand.Path
$proj      = Split-Path -Parent $here
$ctx       = 'RestoControl'
$log       = Join-Path $here 'levantar_tomcat.log'
$libTomcat = Join-Path $proj 'lib\tomcat'
$buildRoot = Join-Path $proj 'build\tomcat'
$stage     = Join-Path $buildRoot $ctx
$classes   = Join-Path $stage 'WEB-INF\classes'
$distDir   = Join-Path $proj 'dist'
$warOut    = Join-Path $distDir ($ctx + '_tomcat.war')

Set-Content -Path $log -Value "=== levantar_tomcat $(Get-Date -Format s) ===" -Encoding UTF8
function Log([string]$m) { Write-Host $m; Add-Content -Path $log -Value $m -Encoding UTF8 }
function Run($exe, [string[]]$a) { Log ("> " + $exe + " " + ($a -join ' ')); & $exe @a 2>&1 | ForEach-Object { Log ("  " + "$_") }; Log ("  exit=" + $LASTEXITCODE) }
function Fail([string]$m, [int]$code) { Log "`nERROR: $m"; Log "Revisa el log: $log"; exit $code }
function PortOpen([int]$p) { try { $c = New-Object Net.Sockets.TcpClient; $ok = $c.ConnectAsync('127.0.0.1', $p).Wait(1500); $c.Close(); return $ok } catch { return $false } }
function PortOwner([int]$p) {
  try {
    $c = Get-NetTCPConnection -LocalPort $p -State Listen -ErrorAction Stop | Select-Object -First 1
    $pr = Get-Process -Id $c.OwningProcess -ErrorAction SilentlyContinue
    return "$($pr.ProcessName) (PID $($c.OwningProcess))"
  } catch { return 'desconocido' }
}
function ShortPath([string]$p) {
  try { $s = (New-Object -ComObject Scripting.FileSystemObject).GetFolder($p).ShortPath; if ($s) { return $s } } catch {}
  return $p
}

Log "Proyecto: $proj"

# ---------- JAVA (JDK con javac y jar) ----------
Log "`n=== JAVA ==="
$cands = @()
$fastJdk = "$env:USERPROFILE\Downloads\jdk-17 (2) (1)\jdk-17"
if (Test-Path "$fastJdk\bin\javac.exe") { $cands += $fastJdk } else {
  if ($env:JAVA_HOME) { $cands += $env:JAVA_HOME }
  foreach ($p in 'C:\Program Files\Java','C:\Program Files\Eclipse Adoptium','C:\Program Files\Microsoft','C:\Program Files\Zulu','C:\Program Files\BellSoft','C:\Program Files\Amazon Corretto','C:\Program Files\OpenJDK',"$env:USERPROFILE\.jdks") {
    Get-ChildItem $p -Directory -ErrorAction SilentlyContinue | ForEach-Object { $cands += $_.FullName } }
  foreach ($d in "$env:USERPROFILE\Downloads","$env:USERPROFILE\Desktop",'C:\','C:\Program Files') {
    Get-ChildItem $d -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -match 'jdk|java' } | ForEach-Object {
      $cands += $_.FullName; Get-ChildItem $_.FullName -Directory -ErrorAction SilentlyContinue | ForEach-Object { $cands += $_.FullName } } }
  foreach ($w in (where.exe javac 2>$null)) { $cands += (Split-Path -Parent (Split-Path -Parent $w)) }
}
$jdk = $null; $best = 0
foreach ($c in ($cands | Select-Object -Unique)) {
  $j = Join-Path $c 'bin\java.exe'
  if (-not (Test-Path (Join-Path $c 'bin\javac.exe')) -or -not (Test-Path (Join-Path $c 'bin\jar.exe'))) { continue }
  $v = (& $j -version 2>&1 | ForEach-Object { "$_" }) -join ' '
  $n = 0; if ($v -match 'version "(\d+)') { $n = [int]$matches[1] }
  Log "  $c -> $n"
  $score = if ($n -eq 17) { 100 } elseif ($n -ge 17 -and $n -le 21) { 90 - $n } elseif ($n -gt 21) { 50 - $n } else { 0 }
  if ($score -gt $best) { $best = $score; $jdk = $c }
}
if (-not $jdk) { Fail 'no se encontro un JDK 17+ (con javac y jar) instalado.' 1 }
Log "JDK elegido: $jdk"
$javac  = Join-Path $jdk 'bin\javac.exe'
$jarExe = Join-Path $jdk 'bin\jar.exe'

# ---------- TOMCAT ----------
Log "`n=== TOMCAT ==="
Add-Type -AssemblyName System.IO.Compression.FileSystem -ErrorAction SilentlyContinue
function TomcatVersion([string]$dir) {
  $jarPath = Join-Path $dir 'lib\catalina.jar'
  try {
    $z = [IO.Compression.ZipFile]::OpenRead($jarPath)
    try {
      $e = $z.GetEntry('org/apache/catalina/util/ServerInfo.properties')
      if ($e) {
        $sr = New-Object IO.StreamReader($e.Open()); $t = $sr.ReadToEnd(); $sr.Close()
        if ($t -match 'Apache Tomcat/(\d+\.\d+\.\d+)') { return [version]$matches[1] }
      }
    } finally { $z.Dispose() }
  } catch {}
  if ((Split-Path $dir -Leaf) -match '(\d+\.\d+\.\d+)') { return [version]$matches[1] }
  return $null
}
$tcands = @()
if ($env:CATALINA_HOME) { $tcands += $env:CATALINA_HOME }
$roots = @([Environment]::GetFolderPath('Desktop'),"$env:USERPROFILE\Desktop","$env:USERPROFILE\Downloads","$env:USERPROFILE\Documents",$env:USERPROFILE,'C:\','D:\','C:\Program Files\Apache Software Foundation','C:\Program Files (x86)\Apache Software Foundation')
foreach ($r in $roots) {
  Get-ChildItem $r -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -match 'tomcat' } | ForEach-Object {
    $tcands += $_.FullName
    Get-ChildItem $_.FullName -Directory -ErrorAction SilentlyContinue | Where-Object { $_.Name -match 'tomcat' } | ForEach-Object { $tcands += $_.FullName } } }
$tomcat = $null; $tver = $null; $tbest = 0
foreach ($t in ($tcands | Select-Object -Unique)) {
  if (-not (Test-Path (Join-Path $t 'bin\startup.bat')) -or -not (Test-Path (Join-Path $t 'lib\catalina.jar'))) { continue }
  $v = TomcatVersion $t
  Log "  $t -> $v"
  if (-not $v) { continue }
  $s = 0
  if ($v.Major -eq 10 -and $v.Minor -ge 1) { $s = 1000 + $v.Build } elseif ($v.Major -eq 11) { $s = 500 + $v.Build }
  if ($s -gt $tbest) { $tbest = $s; $tomcat = $t; $tver = $v }
}
if (-not $tomcat) { Fail 'no se encontro Apache Tomcat 10.1 (Tomcat 9 o el de XAMPP no sirven: el proyecto usa Jakarta EE 10).' 2 }
Log "Tomcat elegido: $tomcat (version $tver)"
if ($tver.Major -ne 10) { Log "  AVISO: el proyecto esta hecho para Tomcat 10.1 (Jakarta EE 10)." }

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
if (-not (PortOpen 3306)) { Log "  AVISO: MySQL no responde en 3306; la app abrira pero no podra iniciar sesion." }
$mysql = $null
foreach ($p in 'C:\xampp\mysql','C:\Program Files\MySQL','C:\Program Files (x86)\MySQL','C:\laragon\bin\mysql') {
  if (-not $mysql) { $mysql = Get-ChildItem $p -Recurse -Depth 3 -Filter 'mysql.exe' -ErrorAction SilentlyContinue | Select-Object -First 1 -ExpandProperty FullName } }
if ($mysql -and (PortOpen 3306)) {
  Log "  cliente: $mysql"
  Run $mysql @('-uroot','-pAdmin123!','-e','SELECT COUNT(*) AS usuarios FROM sistema_restaurante2.usuario;')
}

# ---------- LIBRERIAS JSF + CDI PARA TOMCAT ----------
Log "`n=== LIBRERIAS JSF/CDI (lib\tomcat) ==="
New-Item -ItemType Directory -Force -Path $libTomcat | Out-Null
function LatestVersion([string]$group, [string]$artifact, [string]$pattern, [string]$fallback) {
  $u = "https://repo1.maven.org/maven2/$($group -replace '\.','/')/$artifact/maven-metadata.xml"
  try {
    [xml]$m = (Invoke-WebRequest -Uri $u -UseBasicParsing -TimeoutSec 30).Content
    $v = @($m.metadata.versioning.versions.version) | Where-Object { $_ -match $pattern } |
         Sort-Object { [version]($_ -replace '\.Final$','') } | Select-Object -Last 1
    if ($v) { return $v }
  } catch { Log "  (no se pudo leer $u : $($_.Exception.Message))" }
  return $fallback
}
function EnsureJar([string]$group, [string]$artifact, [string]$filter, [string]$pattern, [string]$fallback) {
  $have = Get-ChildItem -LiteralPath $libTomcat -Filter $filter -ErrorAction SilentlyContinue | Select-Object -First 1
  if ($have) { Log "  ok: $($have.Name)"; return $true }
  $ver  = LatestVersion $group $artifact $pattern $fallback
  $name = "$artifact-$ver.jar"
  $url  = "https://repo1.maven.org/maven2/$($group -replace '\.','/')/$artifact/$ver/$name"
  $dest = Join-Path $libTomcat $name
  Log "  descargando $url"
  try {
    Invoke-WebRequest -Uri $url -OutFile "$dest.part" -UseBasicParsing -TimeoutSec 300
    Move-Item -LiteralPath "$dest.part" -Destination $dest -Force
    Log "  ok: $name ($([math]::Round((Get-Item -LiteralPath $dest).Length / 1MB, 1)) MB)"
    return $true
  } catch {
    Log "  ERROR al descargar: $($_.Exception.Message)"
    Remove-Item -LiteralPath "$dest.part" -Force -ErrorAction SilentlyContinue
    return $false
  }
}
$okFaces = EnsureJar 'org.glassfish' 'jakarta.faces' 'jakarta.faces-4.0*.jar' '^4\.0\.\d+$' '4.0.5'
$okWeld  = EnsureJar 'org.jboss.weld.servlet' 'weld-servlet-shaded' 'weld-servlet-shaded-5.1*.jar' '^5\.1\.\d+\.Final$' '5.1.2.Final'
if (-not ($okFaces -and $okWeld)) {
  Fail ("faltan librerias. Descarga manualmente de https://repo1.maven.org/maven2/ " +
        "org/glassfish/jakarta.faces (4.0.x) y org/jboss/weld/servlet/weld-servlet-shaded (5.1.x.Final) " +
        "y copia los .jar en: $libTomcat") 3
}

# ---------- COMPILAR Y EMPAQUETAR ----------
Log "`n=== BUILD PARA TOMCAT ==="
if (Test-Path -LiteralPath $stage) { Remove-Item -LiteralPath $stage -Recurse -Force }
New-Item -ItemType Directory -Force -Path $stage, $distDir | Out-Null

# 1) contenido web (JSP, JSF, assets, web.xml)
Copy-Item -Path (Join-Path $proj 'web\*') -Destination $stage -Recurse -Force
Remove-Item -LiteralPath (Join-Path $stage 'WEB-INF\glassfish-web.xml') -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $classes, (Join-Path $stage 'WEB-INF\lib'), (Join-Path $stage 'META-INF') | Out-Null

# 2) context.xml (BeanManager de Weld) y beans.xml (CDI)
$contextXml = @'
<?xml version="1.0" encoding="UTF-8"?>
<Context>
    <!-- BeanManager de CDI (Weld) para Tomcat -->
    <Resource name="BeanManager" auth="Container"
              type="jakarta.enterprise.inject.spi.BeanManager"
              factory="org.jboss.weld.resources.ManagerObjectFactory"/>
</Context>
'@
$beansXml = @'
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="https://jakarta.ee/xml/ns/jakartaee"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xsi:schemaLocation="https://jakarta.ee/xml/ns/jakartaee https://jakarta.ee/xml/ns/jakartaee/beans_4_0.xsd"
       version="4.0" bean-discovery-mode="annotated">
</beans>
'@
[IO.File]::WriteAllText((Join-Path $stage 'META-INF\context.xml'), $contextXml)
if (-not (Test-Path (Join-Path $stage 'WEB-INF\beans.xml'))) { [IO.File]::WriteAllText((Join-Path $stage 'WEB-INF\beans.xml'), $beansXml) }

# 3) librerias del proyecto + JSF/CDI
Get-ChildItem -LiteralPath (Join-Path $proj 'lib') -Filter '*.jar' | ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $stage 'WEB-INF\lib') -Force }
Get-ChildItem -LiteralPath $libTomcat -Filter '*.jar' | ForEach-Object { Copy-Item -LiteralPath $_.FullName -Destination (Join-Path $stage 'WEB-INF\lib') -Force }

# 4) compilar src\java
$srcDir = Join-Path $proj 'src\java'
$srcs = Get-ChildItem -LiteralPath $srcDir -Recurse -Filter '*.java' | ForEach-Object { '"' + ($_.FullName -replace '\\','/') + '"' }
Log "  fuentes .java: $(@($srcs).Count)"
$argf = Join-Path $buildRoot 'fuentes.txt'
[IO.File]::WriteAllLines($argf, [string[]]$srcs, [Text.Encoding]::Default)
$cp = (Join-Path $tomcat 'lib\*') + ';' + (Join-Path $proj 'lib\*') + ';' + (Join-Path $libTomcat '*')
Run $javac @('-encoding','UTF-8','--release','17','-proc:none','-nowarn','-d',$classes,'-cp',$cp,"@$argf")
if ($LASTEXITCODE -ne 0) { Fail 'la compilacion fallo (ver errores arriba).' 4 }

# 5) recursos (i18n\*.properties, etc.)
Get-ChildItem -LiteralPath $srcDir -Recurse -File | Where-Object { $_.Extension -ne '.java' } | ForEach-Object {
  $rel = $_.FullName.Substring($srcDir.Length).TrimStart('\')
  $dst = Join-Path $classes $rel
  New-Item -ItemType Directory -Force -Path (Split-Path -Parent $dst) | Out-Null
  Copy-Item -LiteralPath $_.FullName -Destination $dst -Force
}

# 6) WAR
if (Test-Path -LiteralPath $warOut) { Remove-Item -LiteralPath $warOut -Force }
Run $jarExe @('--create','--file',$warOut,'-C',$stage,'.')
if ($LASTEXITCODE -ne 0 -or -not (Test-Path -LiteralPath $warOut)) { Fail 'no se pudo crear el WAR.' 5 }
Log "  WAR: $warOut ($([math]::Round((Get-Item -LiteralPath $warOut).Length / 1MB, 1)) MB)"

# ---------- PUERTOS ----------
Log "`n=== TOMCAT: puertos ==="
$serverXml = Join-Path $tomcat 'conf\server.xml'
$sx = [IO.File]::ReadAllText($serverXml)
$sxNc = [regex]::Replace($sx, '(?s)<!--.*?-->', '')
$port = 8080
foreach ($m in [regex]::Matches($sxNc, '<Connector\b[^>]*>')) {
  $tag = $m.Value
  if ($tag -match 'protocol="AJP') { continue }
  if ($tag -match 'SSLEnabled="true"') { continue }
  if ($tag -match '\sport="(\d+)"') { $port = [int]$matches[1]; break }
}
$shutPort = 8005; if ($sxNc -match '<Server\b[^>]*\sport="(-?\d+)"') { $shutPort = [int]$matches[1] }
Log "  HTTP: $port | shutdown: $shutPort"

# si ya hay un Tomcat corriendo, se detiene para redesplegar limpio
if ($shutPort -gt 0 -and (PortOpen $shutPort)) {
  Log "  Tomcat ya esta corriendo: deteniendolo..."
  $env:JAVA_HOME = ShortPath $jdk; $env:JRE_HOME = $env:JAVA_HOME
  $env:CATALINA_HOME = $tomcat; $env:CATALINA_BASE = $tomcat
  Run (Join-Path $tomcat 'bin\shutdown.bat') @()
  for ($i = 0; $i -lt 40 -and (PortOpen $port); $i++) { Start-Sleep 1 }
}
# si el puerto HTTP lo ocupa otro programa, se usa otro libre (se respalda server.xml)
if (PortOpen $port) {
  $owner = PortOwner $port
  $nuevo = $null
  foreach ($p in 8082..8099) { if (-not (PortOpen $p)) { $nuevo = $p; break } }
  if (-not $nuevo) { Fail "el puerto $port esta ocupado por $owner y no hay otro libre (8082-8099)." 6 }
  Log "  El puerto $port esta ocupado por $owner -> Tomcat usara el puerto $nuevo"
  $bak = "$serverXml.bak_restocontrol"
  if (-not (Test-Path $bak)) { Copy-Item -LiteralPath $serverXml -Destination $bak; Log "  respaldo: $bak" }
  $sx = [regex]::Replace($sx, '(\s)port="' + $port + '"', '${1}port="' + $nuevo + '"')
  [IO.File]::WriteAllText($serverXml, $sx)
  $port = $nuevo
}

# ---------- DEPLOY ----------
Log "`n=== DEPLOY ==="
$webapps = Join-Path $tomcat 'webapps'
foreach ($p in @((Join-Path $webapps "$ctx.war"), (Join-Path $webapps $ctx), (Join-Path $tomcat "work\Catalina\localhost\$ctx"))) {
  if (Test-Path -LiteralPath $p) { Remove-Item -LiteralPath $p -Recurse -Force -ErrorAction SilentlyContinue }
}
$ctxDesc = Join-Path $tomcat "conf\Catalina\localhost\$ctx.xml"
if (Test-Path -LiteralPath $ctxDesc) { Log "  AVISO: existe $ctxDesc y podria apuntar a otra carpeta; si la app no carga, borralo." }
Copy-Item -LiteralPath $warOut -Destination (Join-Path $webapps "$ctx.war") -Force
Log "  copiado a $webapps\$ctx.war"

# ---------- INICIAR TOMCAT ----------
Log "`n=== INICIAR TOMCAT ==="
$hoy = Get-Date -Format 'yyyy-MM-dd'
$catLog = Join-Path $tomcat "logs\catalina.$hoy.log"
$locLog = Join-Path $tomcat "logs\localhost.$hoy.log"
$catIni = 0; if (Test-Path $catLog) { $catIni = @(Get-Content $catLog).Count }
$locIni = 0; if (Test-Path $locLog) { $locIni = @(Get-Content $locLog).Count }

$env:JAVA_HOME = ShortPath $jdk; $env:JRE_HOME = $env:JAVA_HOME
$env:CATALINA_HOME = $tomcat; $env:CATALINA_BASE = $tomcat
$env:CATALINA_OPTS = '-Dfile.encoding=UTF-8'
Log "  JAVA_HOME=$env:JAVA_HOME"
Start-Process -FilePath (Join-Path $tomcat 'bin\startup.bat') -WorkingDirectory (Join-Path $tomcat 'bin')
for ($i = 0; $i -lt 90 -and -not (PortOpen $port); $i++) { Start-Sleep 1 }
Log "  puerto $port abierto: $(PortOpen $port)"

$url = "http://localhost:$port/$ctx/login.jsp"
$ok = $false
for ($i = 0; $i -lt 60 -and -not $ok; $i++) {
  try { $r = Invoke-WebRequest -Uri $url -UseBasicParsing -TimeoutSec 10; if ($r.StatusCode -eq 200) { $ok = $true } } catch { Start-Sleep 2 }
}

# ---------- DIAGNOSTICO (logs de Tomcat) ----------
$cat = @(); if (Test-Path $catLog) { $cat = @(Get-Content $catLog | Select-Object -Skip $catIni) }
$loc = @(); if (Test-Path $locLog) { $loc = @(Get-Content $locLog | Select-Object -Skip $locIni) }
$moj  = $cat | Select-String -Pattern 'Mojarra' | Select-Object -Last 1
$weld = $cat | Select-String -Pattern 'WELD-000900|WELD-ENV-0011' | Select-Object -Last 1
Log "`n  JSF (Mojarra): $(if ($moj) { $moj.Line.Trim() } else { 'no aparece en el log' })"
Log "  CDI (Weld):    $(if ($weld) { $weld.Line.Trim() } else { 'no aparece en el log' })"
$errs = ($cat + $loc) | Select-String -Pattern 'SEVERE|GRAVE|Exception' | Select-Object -Last 25
if ($errs) { Log "  --- errores en logs de Tomcat ---"; $errs | ForEach-Object { Log ("  " + $_.Line) } }

if ($ok) {
  Log "`nOK: HTTP 200 en $url"
  Log "Para detener Tomcat cierra su ventana (o ejecuta $tomcat\bin\shutdown.bat)."
  Log "=== FIN ==="
  Start-Process $url
} else {
  Log "`nNo respondio $url"
  Log "Revisa la ventana de Tomcat y los logs en $tomcat\logs"
  Log "=== FIN ==="
  exit 7
}
