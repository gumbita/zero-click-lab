# EchoCall Android

La aplicación Android proporciona el entorno donde una entrada UDP alcanza
automáticamente Kotlin, JNI y un parser C. La interfaz simula mensajería y
llamadas para hacer visible cuándo ocurre el procesamiento respecto de las
acciones de aceptar o rechazar.

## Recorrido de una entrada

```text
UDP :43568
   ↓
UdpPacketReceiver
   ↓
Kotlin registra el evento y persiste pending
   ↓
NativeBridge.parsePacket(byte[])
   ↓ JNI
native_bridge.c
   ↓
parser Vulnerable o Patched fijado al compilar
```

El receptor pertenece al ciclo de vida de la Activity, mantiene una cola FIFO
y entrega los bytes al flujo técnico. Antes de JNI, Preferences DataStore
guarda un marcador; un retorno normal lo limpia. Si el proceso termina dentro
del parser, el relanzamiento puede mostrar que aquella operación no llegó al
punto normal de limpieza.

El marcador no diagnostica por sí solo un overflow. Ese diagnóstico procede de
ASan, logs y señal de terminación. Consulta la
[arquitectura](../docs/architecture.md).

## Código compartido y selección nativa

- `app/src/main/java/`: Activity, receptor UDP, estado, navegación y UI Compose;
- `app/src/main/cpp/`: gateway JNI y selección CMake;
- `app/src/{vulnerable,patched}/`: identidad visual de cada variante;
- `app/src/{vulnerableAsan,patchedAsan}/`: recursos de los builds ASan;
- `../native-core/`: contrato y parsers C reutilizados.

El flavor transmite `ECHOCALL_PARSER_IMPLEMENTATION=VULNERABLE|PATCHED` a
CMake. Solo `vulnerable_parser.c` o `safe_parser.c` entra en la biblioteca
nativa de cada APK; la UI no selecciona el parser en runtime.

## Variantes y builds

**Variantes:**

- Vulnerable: implementación deliberadamente insegura.
- Patched: implementación que valida el máximo antes del payload.

**Build types:**

- Debug: ejecución funcional y depuración ordinaria.
- ASan: instrumentación experimental `x86_64` para errores de memoria nativa.

| Variante | Build | Tarea Gradle | `applicationId` |
|---|---|---|---|
| Vulnerable | Debug | `assembleVulnerableDebug` | `com.echocall.lab.vulnerable` |
| Patched | Debug | `assemblePatchedDebug` | `com.echocall.lab.patched` |
| Vulnerable | ASan | `assembleVulnerableAsan` | `com.echocall.lab.vulnerable.asan` |
| Patched | ASan | `assemblePatchedAsan` | `com.echocall.lab.patched.asan` |

ASan no es una tercera o cuarta lógica de parser. Instrumenta la biblioteca y
empaqueta su runtime para hacer observables determinadas operaciones inválidas.

## Requisitos

| Componente | Versión configurada |
|---|---|
| Java target | 17 |
| Android SDK | compile/target 36; min 28 |
| NDK | `27.0.12077973` |
| CMake | `3.22.1` |
| Gradle Wrapper | `8.13` |
| Android Gradle Plugin | `8.12.2` |
| Kotlin | `2.0.21` |

Configura el SDK mediante `ANDROID_HOME`, `ANDROID_SDK_ROOT` o un
`local.properties` no versionado. Usa el wrapper incluido.

## Compilar

Desde `android-app/` en Windows:

```text
.\gradlew.bat :app:assemblePatchedDebug
.\gradlew.bat :app:assembleVulnerableDebug
.\gradlew.bat :app:assemblePatchedAsan
.\gradlew.bat :app:assembleVulnerableAsan
```

En Linux/macOS usa `./gradlew`. Los APK quedan en `app/build/outputs/apk/` y no
se versionan. Compilar Vulnerable no ejecuta muestras.

## Comprobación segura con Patched

Con un emulador o dispositivo propio:

```text
adb devices
adb install -r app/build/outputs/apk/patched/debug/app-patched-debug.apk
adb shell am start -n com.echocall.lab.patched/com.echocall.lab.MainActivity
```

Desde la raíz del repositorio, envía la muestra benigna:

```text
python tools/send_udp_packet.py --host <IP_O_REDIRECCION_AUTORIZADA> --port 43568 --file samples/benign/valid_call_control.bin
```

El resultado nativo esperado es `status=accepted code=ok`. Para un emulador,
la redirección host→guest debe configurarse explícitamente; consulta la
[guía de reproducción](../docs/reproduction.md).

## Dónde observar

- log `EchoCallUDP`: recepción, despacho y resultado normalizado;
- pantalla técnica **Modo Lab**: package, parser compilado, estado UDP, último
  resultado y eventos;
- interfaz de llamada: navegación posterior a un retorno válido o rechazo;
- relanzamiento: marcador de procesamiento interrumpido cuando no hubo limpieza
  normal;
- ASan/logcat: diagnóstico de memoria en los builds instrumentados.

Para interpretar las observaciones usa [Resultados](../docs/results.md) y
[Evidencias](../docs/evidencias/README.md). No envíes la muestra oversized a
Vulnerable como comprobación rutinaria.
