# Instalación reproducible de EchoCall Lab en Windows 11

> Esta guía documenta el entorno de referencia de EchoCall Lab sobre Windows 11 y PowerShell.
> Los comandos, rutas y procedimientos se han definido para este
> entorno. Linux y macOS no están cubiertos por este Getting Started y pueden
> requerir comandos, rutas y configuración diferentes.

Esta guía parte de un Windows 11 sin preparar y termina con EchoCall Patched
instalado y funcionando en un Android Emulator API 36. Al final enviarás un
paquete válido para comprobar el recorrido completo. También puedes probar,
solo contra Patched, el rechazo seguro de un paquete demasiado grande.

Los comandos no ejecutan `receiver_vuln` ni envían entradas malformadas a
EchoCall Vulnerable. Usa el laboratorio únicamente en sistemas propios o
expresamente autorizados.

## 1. Entorno de referencia y requisitos

### Equipo Windows

- Windows 11 de 64 bits sobre arquitectura x86-64.
- PowerShell 5.1 o PowerShell 7.
- Cuenta con permiso para instalar aplicaciones y aceptar elevaciones UAC.
- Conexión a Internet para `winget`, Git, Gradle y Android SDK Manager.
- Virtualización Intel VT-x o AMD-V habilitada en UEFI.
- Al menos 16 GB de RAM para Android Studio y un emulador; 32 GB recomendados.
- Al menos 16 GB libres en la unidad del sistema; 32 GB o más recomendados.

Comprueba el sistema desde PowerShell:

```powershell
if (-not [Environment]::Is64BitOperatingSystem) { throw "Se requiere Windows de 64 bits" }
Get-CimInstance Win32_OperatingSystem | Select-Object Caption, Version, OSArchitecture
Get-CimInstance Win32_Processor | Select-Object Name, VirtualizationFirmwareEnabled
Get-PSDrive -Name C | Select-Object Name, Used, Free
winget --version
```

`VirtualizationFirmwareEnabled` debe mostrar `True`. Si muestra `False`, activa
Intel VT-x o AMD-V en la configuración UEFI antes de crear el emulador. Si
`winget` no existe, instala o actualiza **App Installer** desde Microsoft Store
y abre una PowerShell nueva.

### Versiones requeridas por el repositorio

| Componente | Versión | Naturaleza del requisito |
|---|---|---|
| Python | 3.10 o posterior | Versión soportada por EchoCall Lab |
| CMake para Native Core | 3.20 o posterior | Mínimo declarado |
| Compilador C | MSVC con soporte C17 | Toolchain Windows de referencia |
| Gradle Wrapper | 8.13 | Versión fijada por el repositorio |
| Android Gradle Plugin | 8.12.2 | Versión fijada |
| Kotlin | 2.0.21 | Versión fijada |
| Android SDK | `compileSdk=36`, `targetSdk=36` | Configuración fijada |
| Android mínimo | `minSdk=28` | Mínimo admitido por la aplicación |
| Android SDK Build Tools | `35.0.0` | Versión mínima y predeterminada para AGP 8.12; no está fijada por el proyecto |
| Android NDK | `27.0.12077973` | Versión fijada |
| CMake para Android | `3.22.1` | Versión fijada |
| Target Java/Kotlin | 17 | `sourceCompatibility`, `targetCompatibility` y `jvmTarget` fijados por el proyecto |
| JDK para ejecutar Gradle/AGP | 17 o posterior | JDK 17 es el mínimo requerido por AGP 8.12; se usa el JBR de Android Studio |
| AVD de referencia | API 36, ABI `x86_64` | Configuración respaldada para EchoCall Lab |

`minSdk=28` no describe el emulador de referencia. El AVD utilizado en esta
guía es API 36 con ABI `x86_64`.

## 2. Instalar las herramientas de Windows

Abre una PowerShell normal. Los instaladores pueden solicitar elevación UAC.
Instala Git, Python 3.12, CMake, Visual Studio Build Tools 2022 con el workload
de C++ y Android Studio:

```powershell
winget install --id Git.Git --exact --source winget --no-upgrade --accept-source-agreements --accept-package-agreements
winget install --id Python.Python.3.12 --exact --source winget --no-upgrade --accept-source-agreements --accept-package-agreements
winget install --id Kitware.CMake --exact --source winget --no-upgrade --accept-source-agreements --accept-package-agreements
winget install --id Microsoft.VisualStudio.2022.BuildTools --exact --source winget --no-upgrade --accept-source-agreements --accept-package-agreements --override "--wait --passive --add Microsoft.VisualStudio.Workload.VCTools --includeRecommended"
winget install --id Google.AndroidStudio --exact --source winget --no-upgrade --accept-source-agreements --accept-package-agreements
```

`--no-upgrade` instala el paquete cuando falta, pero evita forzar la
actualización si ya existe una versión instalada. No fija una versión concreta.

Cuando terminen los instaladores, cierra PowerShell y abre una nueva para que
reciba el `PATH` actualizado. Verifica las herramientas de host:

```powershell
$requiredCommands = @("git", "python", "cmake", "ctest")
foreach ($command in $requiredCommands) {
    if (-not (Get-Command $command -ErrorAction SilentlyContinue)) {
        throw "No se encuentra $command en PATH. Reabre PowerShell o revisa su instalación."
    }
}

git --version
python --version
$cmakeVersionOutput = (& cmake --version) -join "`n"
$cmakeVersionOutput
ctest --version

$pythonVersion = [Version](& python -c "import platform; print(platform.python_version())")
if ($pythonVersion -lt [Version]"3.10") { throw "EchoCall Lab requiere Python 3.10 o posterior" }

if ($cmakeVersionOutput -notmatch 'cmake version (\d+\.\d+\.\d+)') { throw "No se pudo determinar la versión de CMake" }
$cmakeVersion = [Version]$Matches[1]
if ($cmakeVersion -lt [Version]"3.20.0") { throw "EchoCall Lab requiere CMake 3.20 o posterior" }

$vswhere = Join-Path ${env:ProgramFiles(x86)} "Microsoft Visual Studio\Installer\vswhere.exe"
if (-not (Test-Path -LiteralPath $vswhere)) { throw "No se encuentra vswhere.exe; revisa Visual Studio Build Tools" }
$vsInstall = & $vswhere -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath
if ([string]::IsNullOrWhiteSpace($vsInstall)) { throw "Falta el workload C++ de Visual Studio Build Tools" }
$vsInstall
```

No instales Gradle globalmente: el repositorio incluye el wrapper 8.13. Los
scripts Python solo usan la biblioteca estándar y no requieren `pip install`.

## 3. Clonar EchoCall Lab

La guía utiliza `$env:USERPROFILE\source\zero-click-lab` como ruta reproducible.
Ejecuta:

```powershell
$workspace = Join-Path $env:USERPROFILE "source"
$repo = Join-Path $workspace "zero-click-lab"
New-Item -ItemType Directory -Path $workspace -Force | Out-Null
if (Test-Path -LiteralPath $repo) { throw "La ruta $repo ya existe; utiliza una carpeta vacía para esta instalación" }
Set-Location $workspace
git clone https://github.com/gumbita/zero-click-lab.git
Set-Location $repo
git status --short --branch
```

### Recuperar el contexto de PowerShell

Si cierras PowerShell o reinicias Windows, recupera las variables con este
bloque:

```powershell
$repo = Join-Path $env:USERPROFILE "source\zero-click-lab"
$sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$javaHome = Join-Path $env:ProgramFiles "Android\Android Studio\jbr"
$adb = Join-Path $sdk "platform-tools\adb.exe"
$emulator = Join-Path $sdk "emulator\emulator.exe"
$env:ANDROID_HOME = $sdk
$env:JAVA_HOME = $javaHome
```

No vuelvas a clonar el repositorio ni a crear `local.properties`. Los apartados
posteriores recuperan `$avdName`, `$serial` y `$apk` cuando los necesitan.

## 4. Comprobar Native Core seguro (opcional)

No necesitas este paso para instalar la app Android. Ejecútalo si quieres
comprobar primero el parser seguro desde Windows; si no, continúa en el
apartado 5. Cada ejecución crea un build nuevo fuera del repositorio:

```powershell
Set-Location $repo
$buildRoot = Join-Path $env:LOCALAPPDATA "EchoCallLab\builds"
New-Item -ItemType Directory -Path $buildRoot -Force | Out-Null
$build = Join-Path $buildRoot ("native-safe-" + (Get-Date -Format "yyyyMMdd-HHmmss"))
cmake -S .\native-core -B $build -G "Visual Studio 17 2022" -A x64 -DENABLE_ASAN=OFF
if ($LASTEXITCODE -ne 0) { throw "Falló la configuración de Native Core" }
cmake --build $build --config Debug --target test_safe_parser receiver_safe
if ($LASTEXITCODE -ne 0) { throw "Falló la compilación de los targets seguros" }
ctest --test-dir $build -C Debug --output-on-failure
if ($LASTEXITCODE -ne 0) { throw "Fallaron los tests seguros de Native Core" }
```

CTest prueba únicamente los targets seguros. Comprueba también la muestra
válida con `receiver_safe`:

```powershell
$receiverSafe = Join-Path $build "Debug\receiver_safe.exe"
$validSample = Join-Path $repo "samples\benign\valid_call_control.bin"
if (-not (Test-Path -LiteralPath $receiverSafe)) { throw "No se generó receiver_safe.exe" }
$safeOutput = & $receiverSafe $validSample
$safeOutput
$safeOutputText = $safeOutput -join "`n"
if ($safeOutputText -notmatch '(?m)^status=accepted code=ok(?:\s|$)') {
    throw "Native Core no aceptó la muestra benigna"
}
```

Resultado requerido:

```text
status=accepted code=ok
```

## 5. Preparar Android Studio y Android SDK

Inicia Android Studio:

```powershell
$studio = Join-Path $env:ProgramFiles "Android\Android Studio\bin\studio64.exe"
if (-not (Test-Path -LiteralPath $studio)) { throw "No se encuentra Android Studio en su ruta estándar" }
Start-Process -FilePath $studio
```

Completa el asistente inicial con la instalación **Standard** y conserva como
SDK la ruta predeterminada `$env:LOCALAPPDATA\Android\Sdk`. Abre el proyecto
`android-app` del repositorio y, en la ventana principal de Android Studio,
entra en **Tools > SDK Manager**.

Instala los componentes desde las dos pestañas del SDK Manager:

1. En **SDK Platforms**, selecciona **Android 16 (API 36)** y comprueba que
   incluye **Android SDK Platform 36**.
2. En **SDK Tools**, selecciona:
   - **Android SDK Platform-Tools**;
   - **Android Emulator**;
3. Activa **Show Package Details** en **SDK Tools** y selecciona estas versiones
   exactas:
   - **Android SDK Build-Tools 35.0.0**;
   - **NDK (Side by side) 27.0.12077973**;
   - **CMake 3.22.1**.
4. Pulsa **Apply** y después **OK**, acepta las licencias que muestre Android
   Studio y espera a que finalicen todas las instalaciones.

El proyecto no declara `buildToolsVersion`. Build Tools `35.0.0` se instala
porque es la versión mínima y predeterminada de AGP 8.12, no por
`compileSdk=36`. No es necesario cerrar Android Studio para continuar con las
comprobaciones.

Define las rutas del SDK y del JBR incluido en Android Studio:

```powershell
$repo = Join-Path $env:USERPROFILE "source\zero-click-lab"
$sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$javaHome = Join-Path $env:ProgramFiles "Android\Android Studio\jbr"
$adb = Join-Path $sdk "platform-tools\adb.exe"
$emulator = Join-Path $sdk "emulator\emulator.exe"

if (-not (Test-Path -LiteralPath $repo)) { throw "No se encuentra el repositorio en $repo" }
if (-not (Test-Path -LiteralPath $javaHome)) { throw "No se encuentra el JBR de Android Studio" }

$env:ANDROID_HOME = $sdk
$env:JAVA_HOME = $javaHome
```

Crea `local.properties` con una ruta válida para el formato de propiedades de
Java y verifica todos los componentes:

```powershell
$sdkForProperties = $sdk.Replace('\', '/')
$localProperties = Join-Path $repo "android-app\local.properties"
Set-Content -LiteralPath $localProperties -Value "sdk.dir=$sdkForProperties" -Encoding ascii

$requiredSdkPaths = @(
    (Join-Path $sdk "platform-tools\adb.exe"),
    (Join-Path $sdk "platforms\android-36\android.jar"),
    (Join-Path $sdk "build-tools\35.0.0\aapt2.exe"),
    (Join-Path $sdk "ndk\27.0.12077973"),
    (Join-Path $sdk "cmake\3.22.1\bin\cmake.exe"),
    (Join-Path $sdk "emulator\emulator.exe")
)
foreach ($path in $requiredSdkPaths) {
    if (-not (Test-Path -LiteralPath $path)) { throw "Falta el componente Android: $path" }
}

$java = Join-Path $javaHome "bin\java.exe"
$javaVersionOutput = (& $java -version 2>&1) -join "`n"
$javaVersionOutput
if ($javaVersionOutput -notmatch 'version "(\d+)') { throw "No se pudo determinar la versión del JBR" }
$javaMajor = [int]$Matches[1]
if ($javaMajor -lt 17) { throw "AGP 8.12 requiere JDK 17 o posterior; versión detectada=$javaMajor" }
& $adb version
& (Join-Path $sdk "cmake\3.22.1\bin\cmake.exe") --version
```

## 6. Preparar la aceleración y crear el AVD API 36

Comprueba primero la aceleración del emulador:

```powershell
if (-not (Test-Path -LiteralPath $emulator)) {
    throw "Android Emulator no está instalado correctamente: falta $emulator"
}
& $emulator -accel-check
if ($LASTEXITCODE -ne 0) {
    throw "Android Emulator está instalado, pero la comprobación de aceleración ha fallado"
}
```

Si falla, no continúes: revisa **El emulador no arranca o la aceleración falla**
en el apartado 12 y repite la comprobación.

Abre Android Studio y crea el dispositivo desde **Tools > Device Manager >
Create Virtual Device** con esta configuración:

- perfil de hardware: **Pixel 9 Pro**, utilizado en la validación de referencia;
  no es un requisito funcional y puede usarse otro perfil **Phone** compatible;
- nombre del AVD: `EchoCall_Lab_API_36`;
- system image de referencia: **Android 16 (API 36, "Baklava"), Google Play
  Intel x86_64 Atom System Image**, con ABI `x86_64`; los requisitos
  funcionales son API 36 y ABI `x86_64`;
- orientación y opciones avanzadas: valores predeterminados.

Si la system image todavía no está instalada, descárgala desde Device Manager
antes de crear el AVD. No selecciones una imagen ARM ni la variante
experimental **16 KB Page Size** para este recorrido. Al terminar, vuelve a
PowerShell y verifica que el AVD con el nombre exacto de referencia existe:

```powershell
$avdName = "EchoCall_Lab_API_36"
$availableAvds = & $emulator -list-avds
$availableAvds
if ($availableAvds -notcontains $avdName) { throw "No se encuentra el AVD $avdName" }
```

Arranca el emulador en una ventana visible y espera hasta cinco minutos a que
Android complete el boot. `-avd $avdName` inicia específicamente ese AVD; los
nombres de otros dispositivos almacenados no intervienen en esta selección:

```powershell
Start-Process -FilePath $emulator -ArgumentList @("-avd", $avdName)
$deadline = (Get-Date).AddMinutes(5)
$serial = $null
do {
    Start-Sleep -Seconds 2
    $emulatorSerials = @(& $adb devices | Select-String "^emulator-\d+\s+device$" | ForEach-Object { ($_.Line -split "\s+")[0] })
    foreach ($candidate in $emulatorSerials) {
        $candidateAvdName = @(& $adb -s $candidate emu avd name) |
            ForEach-Object { $_.Trim() } |
            Where-Object { $_ -and $_ -ne "OK" } |
            Select-Object -First 1
        if ($candidateAvdName -eq $avdName) {
            $serial = $candidate
            break
        }
    }
} until ($serial -or (Get-Date) -ge $deadline)
if (-not $serial) { throw "ADB no detectó el AVD $avdName dentro del tiempo esperado" }

& $adb -s $serial wait-for-device
$bootDeadline = (Get-Date).AddMinutes(5)
do {
    Start-Sleep -Seconds 2
    $bootCompleted = (& $adb -s $serial shell getprop sys.boot_completed).Trim()
} until ($bootCompleted -eq "1" -or (Get-Date) -ge $bootDeadline)
if ($bootCompleted -ne "1") { throw "Android no completó el arranque dentro del tiempo esperado" }

& $adb devices
$runningAvdName = @(& $adb -s $serial emu avd name) |
    ForEach-Object { $_.Trim() } |
    Where-Object { $_ -and $_ -ne "OK" } |
    Select-Object -First 1
$apiLevel = (& $adb -s $serial shell getprop ro.build.version.sdk).Trim()
$abi = (& $adb -s $serial shell getprop ro.product.cpu.abi).Trim()
if ($runningAvdName -ne $avdName) { throw "El serial $serial no corresponde al AVD $avdName" }
if ($apiLevel -ne "36") { throw "El AVD no usa API 36: API detectada=$apiLevel" }
if ($abi -ne "x86_64") { throw "El AVD no usa ABI x86_64: ABI detectada=$abi" }
"AVD validado: nombre=$runningAvdName serial=$serial API=$apiLevel ABI=$abi"
```

Los comandos posteriores reutilizan `$serial`; no escribas manualmente un
número de puerto de emulador.

## 7. Compilar EchoCall Patched

Compila la variante real `patchedDebug` con el wrapper del repositorio:

```powershell
$repo = Join-Path $env:USERPROFILE "source\zero-click-lab"
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$env:JAVA_HOME = Join-Path $env:ProgramFiles "Android\Android Studio\jbr"
Set-Location (Join-Path $repo "android-app")
& .\gradlew.bat --version
if ($LASTEXITCODE -ne 0) { throw "Gradle Wrapper no pudo iniciarse" }
& .\gradlew.bat :app:assemblePatchedDebug
if ($LASTEXITCODE -ne 0) { throw "Falló assemblePatchedDebug" }

$apk = Join-Path $repo "android-app\app\build\outputs\apk\patched\debug\app-patched-debug.apk"
if (-not (Test-Path -LiteralPath $apk)) { throw "No se generó el APK Patched en $apk" }
Get-Item -LiteralPath $apk | Select-Object FullName, Length, LastWriteTime
```

Esta tarea compila Patched con `safe_parser.c`. No la sustituyas por una tarea
de la variante Vulnerable.

## 8. Instalar y arrancar EchoCall Patched

Recupera `$serial` si abriste una PowerShell nueva. El bloque identifica el AVD
de referencia por su nombre aunque haya otros emuladores conectados:

```powershell
$sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$adb = Join-Path $sdk "platform-tools\adb.exe"
$repo = Join-Path $env:USERPROFILE "source\zero-click-lab"
$apk = Join-Path $repo "android-app\app\build\outputs\apk\patched\debug\app-patched-debug.apk"
$avdName = "EchoCall_Lab_API_36"
$emulatorSerials = @(& $adb devices | Select-String "^emulator-\d+\s+device$" | ForEach-Object { ($_.Line -split "\s+")[0] })
$serial = $null
foreach ($candidate in $emulatorSerials) {
    $candidateAvdName = @(& $adb -s $candidate emu avd name) |
        ForEach-Object { $_.Trim() } |
        Where-Object { $_ -and $_ -ne "OK" } |
        Select-Object -First 1
    if ($candidateAvdName -eq $avdName) {
        $serial = $candidate
        break
    }
}
if (-not $serial) { throw "No se encuentra el AVD $avdName conectado y en estado device" }
$apiLevel = (& $adb -s $serial shell getprop ro.build.version.sdk).Trim()
$abi = (& $adb -s $serial shell getprop ro.product.cpu.abi).Trim()
if ($apiLevel -ne "36") { throw "El emulador conectado no usa API 36" }
if ($abi -ne "x86_64") { throw "El emulador conectado no usa ABI x86_64" }
if (-not (Test-Path -LiteralPath $apk)) { throw "No se encuentra el APK Patched en $apk" }
```

Instala el APK, limpia Logcat e inicia EchoCall Patched:

```powershell
& $adb -s $serial install -r $apk
if ($LASTEXITCODE -ne 0) { throw "ADB no pudo instalar EchoCall Patched" }
$installedPath = (& $adb -s $serial shell pm path com.echocall.lab.patched) -join "`n"
if ($LASTEXITCODE -ne 0 -or $installedPath -notmatch "package:") { throw "El package Patched no está instalado" }
$installedPath

& $adb -s $serial logcat -c
& $adb -s $serial shell am force-stop com.echocall.lab.patched
& $adb -s $serial shell am start -n com.echocall.lab.patched/com.echocall.lab.MainActivity
if ($LASTEXITCODE -ne 0) { throw "No se pudo arrancar MainActivity de Patched" }
Start-Sleep -Seconds 2

$receiverLog = & $adb -s $serial logcat -d -s "EchoCallUDP:I" "*:S"
$receiverLog | Select-String -SimpleMatch "Socket bound on UDP port 43568"
$receiverLogText = $receiverLog -join "`n"
if ($receiverLogText -notmatch "Socket bound on UDP port 43568") { throw "EchoCall no está escuchando en UDP 43568" }
```

Mantén la ventana de EchoCall en primer plano. En **Lab Mode** debe verse
`PATCHED · Fijado al compilar` y `UDP listening on port 43568`.

## 9. Configurar UDP del host hacia el emulador

La dirección `127.0.0.1` del host Windows no entra automáticamente en el
Android Emulator. Configura la redirección antes de enviar cualquier muestra:

```powershell
$redirections = (& $adb -s $serial emu redir list) -join "`n"
if ($redirections -match "udp:43568" -and $redirections -notmatch "udp:43568.*43568") {
    & $adb -s $serial emu redir del udp:43568
    if ($LASTEXITCODE -ne 0) { throw "No se pudo retirar una redirección UDP 43568 incompatible" }
    $redirections = ""
}
if ($redirections -notmatch "udp:43568.*43568") {
    & $adb -s $serial emu redir add udp:43568:43568
    if ($LASTEXITCODE -ne 0) { throw "No se pudo crear udp:43568:43568" }
}
$redirections = (& $adb -s $serial emu redir list) -join "`n"
$redirections
if ($redirections -notmatch "udp:43568.*43568") { throw "La redirección UDP no quedó activa" }
```

Si aparece un error de puerto ocupado, no continúes hasta resolverlo. Comprueba
qué proceso utiliza el puerto host:

```powershell
Get-NetUDPEndpoint -LocalPort 43568 -ErrorAction SilentlyContinue | Select-Object LocalAddress, LocalPort, OwningProcess
```

## 10. Comprobar que EchoCall recibe y procesa un paquete válido

`valid_call_control.bin` es un paquete de control válido. Esta prueba comprueba
que la redirección UDP, el receptor Android y el parser Patched funcionan
juntos. `status=accepted code=ok` significa que EchoCall ha recibido y aceptado
el paquete; la comprobación del PID confirma que la app no se ha reiniciado.

Ejecuta desde la raíz del repositorio:

```powershell
Set-Location $repo
$appProcessBefore = ((& $adb -s $serial shell pidof com.echocall.lab.patched) -join "").Trim()
if ([string]::IsNullOrWhiteSpace($appProcessBefore)) { throw "EchoCall Patched no está vivo antes de la prueba" }

& $adb -s $serial logcat -c
python .\tools\send_udp_packet.py --host 127.0.0.1 --port 43568 --file .\samples\benign\valid_call_control.bin
if ($LASTEXITCODE -ne 0) { throw "Falló el envío UDP de la muestra benigna" }
Start-Sleep -Seconds 2

$benignLog = & $adb -s $serial logcat -d -s "EchoCallUDP:I" "*:S"
$benignLog | Select-String -SimpleMatch "status=accepted code=ok"
$benignLogText = $benignLog -join "`n"
if ($benignLogText -notmatch "status=accepted code=ok") { throw "No se observó la aceptación de valid_call_control.bin" }

$appProcessAfter = ((& $adb -s $serial shell pidof com.echocall.lab.patched) -join "").Trim()
if ($appProcessAfter -ne $appProcessBefore) { throw "El proceso Patched terminó o se reinició durante la prueba benigna" }
"Prueba benigna validada: status=accepted code=ok; proceso=$appProcessAfter"
```

El resultado requerido en Logcat es:

```text
status=accepted code=ok
```

## 11. Comprobar el rechazo seguro de un paquete demasiado grande (opcional)

`oversized_complete_payload.bin` contiene un payload de 64 bytes, frente al
máximo de 32 que admite Patched. El resultado esperado es
`payload_too_large`, y la aplicación debe seguir viva con el mismo PID.

Ejecuta esta prueba únicamente si la pantalla muestra `PATCHED · Fijado al
compilar` y el package instalado es `com.echocall.lab.patched`:

```powershell
$installedPatched = (& $adb -s $serial shell pm path com.echocall.lab.patched) -join "`n"
if ($installedPatched -notmatch "package:") { throw "No está instalado com.echocall.lab.patched" }
$appProcessBefore = ((& $adb -s $serial shell pidof com.echocall.lab.patched) -join "").Trim()
if ([string]::IsNullOrWhiteSpace($appProcessBefore)) { throw "EchoCall Patched no está vivo antes de la prueba defensiva" }

& $adb -s $serial logcat -c
Set-Location $repo
python .\tools\send_udp_packet.py --host 127.0.0.1 --port 43568 --file .\samples\malformed\oversized_complete_payload.bin
if ($LASTEXITCODE -ne 0) { throw "Falló el envío UDP de la muestra defensiva" }
Start-Sleep -Seconds 2

$defensiveLog = & $adb -s $serial logcat -d -s "EchoCallUDP:I" "*:S"
$expectedRejection = "status=rejected code=payload_too_large declared_length=64 actual_length=64 maximum=32"
$defensiveLog | Select-String -SimpleMatch $expectedRejection
$defensiveLogText = $defensiveLog -join "`n"
if ($defensiveLogText -notmatch [regex]::Escape($expectedRejection)) { throw "No se observó el rechazo payload_too_large esperado" }

$appProcessAfter = ((& $adb -s $serial shell pidof com.echocall.lab.patched) -join "").Trim()
if ([string]::IsNullOrWhiteSpace($appProcessAfter)) { throw "Patched no permanece vivo después del rechazo" }
if ($appProcessAfter -ne $appProcessBefore) { throw "Patched se reinició durante la prueba defensiva" }
"Prueba defensiva validada: payload_too_large; proceso vivo=$appProcessAfter"
```

El resultado requerido es:

```text
status=rejected code=payload_too_large declared_length=64 actual_length=64 maximum=32
```

No envíes `oversized_complete_payload.bin` a EchoCall Vulnerable ni a
`receiver_vuln` como comprobación rutinaria.

## 12. Resolución de problemas

### `python`, `cmake` o `git` no se reconocen

Cierra todas las ventanas de PowerShell, abre una nueva y repite la
verificación del apartado 2. Comprueba qué ejecutable está resolviendo Windows:

```powershell
Get-Command git, python, cmake, ctest | Select-Object Name, Source
```

Si `python` abre Microsoft Store, desactiva los alias `python.exe` y
`python3.exe` en **Configuración > Aplicaciones > Alias de ejecución de
aplicaciones** y vuelve a abrir PowerShell.

### CMake no encuentra Visual Studio

Comprueba el workload C++:

```powershell
$vswhere = Join-Path ${env:ProgramFiles(x86)} "Microsoft Visual Studio\Installer\vswhere.exe"
& $vswhere -latest -products * -requires Microsoft.VisualStudio.Component.VC.Tools.x86.x64 -property installationPath
```

Si no devuelve una ruta, abre **Visual Studio Installer**, modifica **Build
Tools 2022** e instala **Desktop development with C++** con sus componentes
recomendados.

### Gradle no encuentra SDK, Java, NDK o CMake

```powershell
$repo = Join-Path $env:USERPROFILE "source\zero-click-lab"
$env:ANDROID_HOME = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$env:JAVA_HOME = Join-Path $env:ProgramFiles "Android\Android Studio\jbr"
Get-Content -LiteralPath (Join-Path $repo "android-app\local.properties")
Test-Path (Join-Path $env:ANDROID_HOME "ndk\27.0.12077973")
Test-Path (Join-Path $env:ANDROID_HOME "cmake\3.22.1\bin\cmake.exe")
& (Join-Path $env:JAVA_HOME "bin\java.exe") -version
```

Los dos `Test-Path` deben devolver `True` y Java debe indicar versión 17 o
posterior.

### SDK Manager muestra Emulator instalado, pero falta `emulator.exe`

Comprueba la ruta física antes de investigar la virtualización:

```powershell
$sdk = Join-Path $env:LOCALAPPDATA "Android\Sdk"
$emulator = Join-Path $sdk "emulator\emulator.exe"
Test-Path -LiteralPath $emulator
Get-ChildItem -LiteralPath (Join-Path $sdk "emulator") -ErrorAction SilentlyContinue
```

Si `Test-Path` devuelve `False`, la instalación de **Android Emulator** está
incompleta aunque SDK Manager la muestre seleccionada. Revisa o reinstala ese
componente desde **Tools > SDK Manager > SDK Tools** y confirma que existe
físicamente `$sdk\emulator\emulator.exe`. La ausencia del ejecutable no es un
fallo de aceleración y no se corrige activando `HypervisorPlatform`.

### El emulador no arranca o la aceleración falla

- Lee primero el mensaje completo de `& $emulator -accel-check`.
- Confirma VT-x o AMD-V en UEFI.
- No uses una imagen ARM; el AVD de referencia es `x86_64`.

Si el diagnóstico indica que falta un hipervisor compatible, abre PowerShell
como administrador y habilita Windows Hypervisor Platform:

```powershell
Enable-WindowsOptionalFeature -Online -FeatureName HypervisorPlatform -All -NoRestart
Restart-Computer
```

Después del reinicio, recupera las variables de PowerShell y ejecuta de nuevo
`& $emulator -accel-check`.

### ADB no muestra el emulador como `device`

```powershell
& $adb kill-server
& $adb start-server
& $adb devices
```

Espera a que el AVD termine de arrancar. No continúes si aparece `offline`.

### La redirección UDP no existe

```powershell
& $adb -s $serial emu redir list
```

Debe aparecer una entrada que relacione el puerto host UDP 43568 con el puerto
guest 43568. La redirección se pierde al cerrar el emulador y debe recrearse en
la siguiente sesión.

### EchoCall no escucha o no aparecen logs

```powershell
& $adb -s $serial shell am start -n com.echocall.lab.patched/com.echocall.lab.MainActivity
Start-Sleep -Seconds 2
& $adb -s $serial logcat -d -s "EchoCallUDP:I" "*:S"
```

Mantén la actividad visible. `MainActivity.onStop()` detiene el receptor, por
lo que EchoCall debe permanecer abierta durante los envíos.

### Los paths `tools` o `samples` no existen

```powershell
$repo = Join-Path $env:USERPROFILE "source\zero-click-lab"
Set-Location $repo
Test-Path .\tools\send_udp_packet.py
Test-Path .\samples\benign\valid_call_control.bin
Test-Path .\samples\malformed\oversized_complete_payload.bin
```

Los tres resultados deben ser `True`.

## 13. Comprobación final

EchoCall Lab está listo cuando:

- `EchoCall_Lab_API_36` funciona con API 36 y ABI `x86_64`;
- EchoCall Patched está instalado, abierto y escuchando en UDP 43568;
- la redirección `udp:43568:43568` está activa;
- `valid_call_control.bin` produce `status=accepted code=ok` sin reiniciar la
  app;
- si haces la prueba opcional, Patched responde `payload_too_large` y conserva
  el mismo PID.
