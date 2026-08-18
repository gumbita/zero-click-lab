# EchoCall Android

Aplicación Android propia del laboratorio. Recibe datagramas ECLB en
`43568/UDP`, persiste un marcador antes de JNI y entrega los bytes a
`NativeBridge.parsePacket()`. El parser queda fijado durante el build: cada APK
contiene la implementación Vulnerable o la Patched, nunca un selector runtime.

## Arquitectura y estructura

```text
UDP :43568 → UdpPacketReceiver → Kotlin → NativeBridge.parsePacket()
                                      ↓ JNI
                                 parser C único
```

- `app/src/main/java/`: receptor UDP, estado, navegación y UI Compose;
- `app/src/main/cpp/`: gateway JNI y selección CMake del parser;
- `app/src/{vulnerable,patched}/`: identidad visual de cada flavor;
- `app/src/{vulnerableAsan,patchedAsan}/`: identidad de las combinaciones ASan;
- `../native-core/`: fuentes C reutilizadas por CMake.

## Requisitos comprobables en el proyecto

- Java source/bytecode target 17; usa un JDK compatible (el polish se validó
  con el JBR 21 incluido en Android Studio);
- Android SDK Platform 36 (`compileSdk=36`, `targetSdk=36`, `minSdk=28`);
- Android NDK `27.0.12077973`;
- CMake `3.22.1`;
- Gradle Wrapper `8.13`;
- Android Gradle Plugin `8.12.2`;
- Kotlin `2.0.21`.

Configura el SDK mediante `ANDROID_HOME`, `ANDROID_SDK_ROOT` o un
`local.properties` local que no se versionará. Usa siempre el wrapper incluido.

## Variantes

| Variante | `applicationId` | Parser | Instrumentación |
|---|---|---|---|
| `vulnerableDebug` | `com.echocall.lab.vulnerable` | Vulnerable | Debug |
| `patchedDebug` | `com.echocall.lab.patched` | Patched | Debug |
| `vulnerableAsan` | `com.echocall.lab.vulnerable.asan` | Vulnerable | ASan, `x86_64` |
| `patchedAsan` | `com.echocall.lab.patched.asan` | Patched | ASan, `x86_64` |

CMake recibe `ECHOCALL_PARSER_IMPLEMENTATION` desde el flavor e incluye solo
`vulnerable_parser.c` o `safe_parser.c`. La interfaz Kotlin/JNI es compartida.

## Build

Desde `android-app/` en Windows:

```text
.\gradlew.bat :app:assemblePatchedDebug
.\gradlew.bat :app:assembleVulnerableDebug
.\gradlew.bat :app:assemblePatchedAsan
.\gradlew.bat :app:assembleVulnerableAsan
```

En Linux o macOS sustituye `.\gradlew.bat` por `./gradlew`. Para el inicio
seguro basta `patchedDebug`; compilar Vulnerable no ejecuta ninguna muestra.

Los APK se generan bajo `app/build/outputs/apk/` y permanecen ignorados.

## Instalación y arranque de Patched

Con un dispositivo o emulador propio conectado mediante ADB:

```text
adb install -r app/build/outputs/apk/patched/debug/app-patched-debug.apk
adb shell am start -n com.echocall.lab.patched/com.echocall.lab.MainActivity
```

Confirma el dispositivo con `adb devices` y mantén una sola variante activa:
todas escuchan el mismo puerto. La redirección host→emulador depende del
entorno y debe configurarse explícitamente; no asumas que el loopback del host
es el loopback del emulador.

## Comprobación benigna

Cuando Patched esté visible y escuchando, vuelve a la raíz del repositorio y
envía únicamente la muestra benigna desde un host autorizado:

```text
python tools/send_udp_packet.py --host <IP_DEL_DISPOSITIVO_O_REDIRECCION> --port 43568 --file samples/benign/valid_call_control.bin
```

El resultado nativo esperado es `status=accepted code=ok`. El procesamiento se
inicia al recibir el datagrama y antes de una interacción explícita de la
persona usuaria.

## Patched y ASan

Patched valida el máximo semántico de 32 bytes antes del procesamiento. Para la
muestra canónica de 77 bytes (`declared_length=64`, `actual_length=64`) devuelve
`payload_too_large`. Este rechazo concreto no demuestra seguridad general.

Las variantes ASan instrumentan la biblioteca nativa y empaquetan el runtime
para un entorno `x86_64` controlado. Sirven para diagnóstico de memoria, no
para producción ni como garantía de ausencia de errores.

## Precauciones con Vulnerable

Vulnerable contiene deliberadamente una reserva de 32 bytes seguida de una
copia gobernada por la longitud de entrada. No envíes la muestra oversized a
Vulnerable como parte de un quick check, CI o prueba rutinaria. La ejecución
final ya está documentada y clasificada como no repetir.

Consulta la [guía de reproducción](../docs/reproduction.md), los
[resultados](../docs/results.md) y [`SECURITY.md`](../SECURITY.md).
