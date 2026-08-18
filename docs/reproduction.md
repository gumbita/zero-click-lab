# Guía de reproducción segura

Esta guía separa comprobaciones rutinarias de la ruta experimental Vulnerable.
Trabaja únicamente en sistemas propios y consulta [`SECURITY.md`](../SECURITY.md).

## Nivel 1 — Native Core seguro

```text
git clone https://github.com/gumbita/zero-click-lab.git
cd zero-click-lab
```

Requiere CMake 3.20 o posterior y un compilador C17. Usa un directorio de build
fuera del repositorio:

```text
cmake -S native-core -B <TEMP_BUILD_DIR> -DENABLE_ASAN=OFF
cmake --build <TEMP_BUILD_DIR> --target test_safe_parser receiver_safe
ctest --test-dir <TEMP_BUILD_DIR> --output-on-failure
```

CTest ejecuta el parser Patched y `receiver_safe`; no invoca
`receiver_vuln`. Comprobación manual benigna:

```text
<TEMP_BUILD_DIR>/receiver_safe samples/benign/valid_call_control.bin
```

Resultado esperado:

```text
status=accepted code=ok
```

## Nivel 2 — Android Patched

### Requisitos

Consulta versiones y configuración en [`android-app/README.md`](../android-app/README.md).
Necesitas un JDK compatible con el target Java 17, Android SDK 36, NDK
`27.0.12077973`, CMake `3.22.1`,
un emulador/dispositivo autorizado y ADB.

### Build, instalación y arranque

En Windows:

```text
cd android-app
.\gradlew.bat :app:assemblePatchedDebug
adb devices
adb install -r app/build/outputs/apk/patched/debug/app-patched-debug.apk
adb shell am start -n com.echocall.lab.patched/com.echocall.lab.MainActivity
cd ..
```

En Linux o macOS usa `./gradlew` en lugar de `.\gradlew.bat`.

### Redirección UDP para Android Emulator

Identifica el serial, por ejemplo `emulator-5554`, y añade una redirección
host→guest antes de enviar:

```text
adb -s emulator-5554 emu redir add udp:43568:43568
adb -s emulator-5554 emu redir list
```

La sintaxis `redir add protocol:host-port:guest-port` pertenece a la consola
oficial del emulador. Si usas un dispositivo físico, envía a su IP dentro de
una red propia en vez de crear esta redirección.

### Muestra benigna

Con la app Patched visible y escuchando:

```text
python tools/send_udp_packet.py --host 127.0.0.1 --port 43568 --file samples/benign/valid_call_control.bin
adb -s emulator-5554 logcat -d -s EchoCallUDP
```

Resultado esperado: `status=accepted code=ok` y proceso vivo.

### Oversized solo contra Patched

Confirma primero que el package activo es `com.echocall.lab.patched` y que no
hay otra variante escuchando. Después:

```text
python tools/send_udp_packet.py --host 127.0.0.1 --port 43568 --file samples/malformed/oversized_complete_payload.bin
adb -s emulator-5554 logcat -d -s EchoCallUDP
```

Resultado esperado:

```text
status=rejected code=payload_too_large declared_length=64 actual_length=64 maximum=32
```

La app debe permanecer viva. Esta observación concreta no demuestra seguridad
general de Patched.

## Nivel 3 — Instrumentación ASan

Nivel experimental adicional, separado del onboarding. Vulnerable y Patched
son las variantes; ASan es el build type que instrumenta su código nativo para
detectar determinados errores de memoria. Los builds `vulnerableAsan` y
`patchedAsan` son `x86_64` y requieren el runtime ASan del NDK configurado.
Compilarlos no ejecuta ninguna entrada:

```text
cd android-app
.\gradlew.bat :app:assemblePatchedAsan
.\gradlew.bat :app:assembleVulnerableAsan
```

No se proporciona aquí un comando para enviar la muestra oversized a
Vulnerable. La ejecución final Vulnerable ASan ya se realizó una vez, terminó
en `SIGABRT` y está clasificada como **NO REPETIR**. No debe incluirse en CI,
scripts de inicio rápido ni demostraciones rutinarias.

## Captura mínima de una comprobación nueva

Registra antes de ejecutar:

- commit y `git status`;
- versión de Python/CMake/JDK/SDK/NDK/Gradle;
- variante, package y ABI;
- tamaño y SHA-256 de la muestra;
- comando exacto y hora;
- PID antes y después cuando aplique.

Conserva después la salida completa, el código de terminación y cualquier
desviación. Una ausencia de mensajes en un filtro parcial no equivale a
ausencia de errores.

## Resolución de problemas

- `SDK location not found`: configura `ANDROID_HOME`/`ANDROID_SDK_ROOT` o un
  `local.properties` local.
- `Expected exactly one x86_64 ASan runtime`: verifica el NDK fijado por el
  proyecto.
- `EADDRINUSE`: detén la otra variante; todas escuchan `43568/UDP`.
- Sin recepción en emulador: comprueba `adb emu redir list`, el serial y que la
  app está en primer plano/estado iniciado.
- `device not found`: vuelve a comprobar `adb devices`; no interpretes el fallo
  del entorno como resultado del parser.

Los resultados consolidados están en [`results.md`](results.md) y la evidencia
en [`evidencias/`](evidencias/).

## Estado de validación de esta guía

Comprobaciones realizadas durante el polish del 2026-08-18:

| Comprobación | Estado | Resultado |
|---|---|---|
| Configuración/build Native Core seguro | PASS | MinGW GCC 13.2.0, CMake 3.29.2 |
| CTest | PASS | 9/9; solo `test_safe_parser` y `receiver_safe` |
| `assemblePatchedDebug` | PASS | 43 tareas |
| `assembleVulnerableDebug` | PASS | Build sin ejecución |
| `assemblePatchedAsan` | PASS | Build `x86_64` sin ejecución |
| `assembleVulnerableAsan` | PASS | Build `x86_64` sin ejecución |
| Instalación, arranque y UDP Android | NO PROBADO | ADB no detectó dispositivos conectados |
| Oversized contra Vulnerable | NO EJECUTADO | Exclusión deliberada de seguridad |
