# Getting Started de EchoCall Lab

Esta guía lleva desde un entorno nuevo hasta la primera ejecución segura de
EchoCall Patched. El recorrido no ejecuta el parser Vulnerable con entradas
malformadas ni reproduce el crash experimental documentado.

Trabaja únicamente en equipos, emuladores y redes propios o expresamente
autorizados. Antes de continuar, revisa [`SECURITY.md`](../SECURITY.md).

## Requisitos y versiones

Las versiones siguientes proceden de los archivos de build del repositorio.

| Componente | Versión | Tipo de requisito |
|---|---|---|
| Python | 3.10 o posterior | Mínimo deducido de la sintaxis del código |
| CMake para Native Core | 3.20 o posterior | Mínimo declarado |
| Compilador C | Compatible con C17 | Estándar requerido |
| Gradle Wrapper | 8.13 | Versión fijada; usa el wrapper incluido |
| Android Gradle Plugin | 8.12.2 | Versión fijada |
| Kotlin | 2.0.21 | Versión fijada |
| Android SDK | `compileSdk=36`, `targetSdk=36` | Configuración fijada |
| Android mínimo | `minSdk=28` | Mínimo admitido por la aplicación |
| Android NDK | `27.0.12077973` | Versión fijada |
| CMake para Android | 3.22.1 | Versión fijada |
| Java/JVM | Target 17 | Target de compilación |
| AVD de referencia | API 36, ABI `x86_64` | Configuración recomendada |

`minSdk=28` no es la configuración de referencia del emulador. Solo indica la
versión mínima de Android aceptada por la aplicación. Para reproducibilidad,
usa un AVD API 36 con ABI `x86_64`.

## 1. Clonar el repositorio

Instala Git y clona la URL real del proyecto:

```text
git clone https://github.com/gumbita/zero-click-lab.git
cd zero-click-lab
```

Todos los comandos siguientes indican explícitamente desde qué directorio se
ejecutan.

## 2. Comprobar Python

Las utilidades de `tools/` generan muestras ECLB deterministas y envían un
archivo como un único datagrama UDP. Solo utilizan la biblioteca estándar; no
hay `requirements.txt` ni dependencias Python que instalar.

Desde la raíz del repositorio:

```text
python --version
```

Usa Python 3.10 o posterior. La sintaxis actual, incluida la unión de tipos
`int | None`, requiere como mínimo Python 3.10.

## 3. Construir Native Core seguro

Necesitas CMake 3.20 o posterior y un compilador compatible con C17, como GCC,
Clang o MSVC con soporte suficiente para el estándar utilizado. Configura el
build fuera del repositorio para no mezclar salidas generadas con las fuentes.

Desde la raíz del repositorio:

```text
cmake -S native-core -B <TEMP_BUILD_DIR> -DENABLE_ASAN=OFF
cmake --build <TEMP_BUILD_DIR> --target test_safe_parser receiver_safe
ctest --test-dir <TEMP_BUILD_DIR> --output-on-failure
```

CTest ejecuta `test_safe_parser` y `receiver_safe`; no invoca
`receiver_vuln`. Después puede hacerse una comprobación manual benigna:

```text
<TEMP_BUILD_DIR>/receiver_safe samples/benign/valid_call_control.bin
```

Resultado esperado:

```text
status=accepted code=ok
```

No uses `receiver_vuln` con entradas malformadas como comprobación rutinaria.

## 4. Preparar Android Studio y el SDK

Instala Android Studio y, desde **SDK Manager**, prepara:

- Android SDK Platform 36;
- Android SDK Platform-Tools, que incluye ADB;
- Android NDK `27.0.12077973`;
- CMake `3.22.1`;
- JDK 17, que coincide con el target Java/JVM 17 del proyecto.

Usa `ANDROID_HOME` como variable de entorno recomendada cuando las herramientas
no localicen el SDK. Como alternativa, crea `android-app/local.properties`, que
no se versiona, con una ruta válida para tu equipo:

```properties
sdk.dir=C:\\Android\\Sdk
```

En Linux o macOS, por ejemplo:

```properties
sdk.dir=/opt/android-sdk
```

El wrapper Gradle está incluido en el repositorio; no instales otra versión de
Gradle global para sustituirlo.

## 5. Crear el AVD de referencia

En **Device Manager** de Android Studio crea un dispositivo virtual con:

- imagen de sistema API 36;
- ABI `x86_64`;
- red del emulador sin exposición a terceros.

Inicia el AVD y espera a que Android complete el arranque. La ABI `x86_64` es
también la utilizada por los builds ASan experimentales, aunque este onboarding
solo construye Patched Debug.

## 6. Comprobar el emulador

Desde cualquier terminal donde ADB esté disponible:

```text
adb devices
```

Anota el serial mostrado, normalmente similar a `emulator-5554`. El estado
debe ser `device`; `offline` o una lista vacía no permiten continuar.

En los comandos siguientes sustituye `<serial>` por ese valor.

## 7. Compilar EchoCall Patched

Desde la raíz del repositorio, en Windows:

```text
cd android-app
.\gradlew.bat :app:assemblePatchedDebug
```

En Linux o macOS:

```text
cd android-app
./gradlew :app:assemblePatchedDebug
```

El APK generado queda en:

```text
android-app/app/build/outputs/apk/patched/debug/app-patched-debug.apk
```

La compilación de Patched no ejecuta muestras ni inicia el receptor.

## 8. Instalar y arrancar Patched

Desde `android-app/`:

```text
adb -s <serial> install -r app/build/outputs/apk/patched/debug/app-patched-debug.apk
adb -s <serial> shell am start -n com.echocall.lab.patched/com.echocall.lab.MainActivity
```

Mantén la aplicación iniciada y comprueba que la pantalla técnica indica que
el receptor UDP escucha en el puerto `43568`.

## 9. Configurar UDP antes de enviar

La dirección `127.0.0.1` del host no llega automáticamente al invitado. Antes
de enviar cualquier muestra, crea la redirección host→guest:

```text
adb -s <serial> emu redir add udp:43568:43568
adb -s <serial> emu redir list
```

La lista debe mostrar `udp:43568:43568`. Si usas un dispositivo físico
autorizado, envía a su IP dentro de tu red controlada y no uses `adb emu redir`.

Vuelve ahora a la raíz del repositorio, porque `tools/` y `samples/` se
referencian desde ella:

```text
cd ..
```

## 10. Primera prueba benigna

Opcionalmente limpia el buffer de log antes del envío:

```text
adb -s <serial> logcat -c
```

Desde la raíz del repositorio, envía la muestra válida:

```text
python tools/send_udp_packet.py --host 127.0.0.1 --port 43568 --file samples/benign/valid_call_control.bin
adb -s <serial> logcat -d -s EchoCallUDP
```

El emisor confirma únicamente el envío. En EchoCall o Logcat debe observarse:

```text
status=accepted code=ok
```

La aplicación debe permanecer viva.

## 11. Prueba defensiva opcional

Esta comprobación utiliza una entrada oversized exclusivamente contra Patched.
Antes de ejecutarla, confirma que el package activo es
`com.echocall.lab.patched` y que no hay otra variante escuchando en el puerto.

```text
adb -s <serial> logcat -c
python tools/send_udp_packet.py --host 127.0.0.1 --port 43568 --file samples/malformed/oversized_complete_payload.bin
adb -s <serial> logcat -d -s EchoCallUDP
```

Resultado esperado:

```text
status=rejected code=payload_too_large declared_length=64 actual_length=64 maximum=32
```

Patched debe rechazar la entrada y permanecer vivo. No repitas esta prueba
contra Vulnerable: el resultado experimental concluyente ya está documentado y
clasificado como **NO REPETIR**.

## 12. Resolución de problemas

- **SDK no localizado:** define `ANDROID_HOME` o comprueba
  `android-app/local.properties` y su `sdk.dir`.
- **ADB no detecta el emulador:** confirma que el AVD terminó de arrancar,
  ejecuta `adb devices` y reinicia el servidor con `adb kill-server` seguido de
  `adb start-server` si fuera necesario.
- **Redirección UDP ausente:** ejecuta `adb -s <serial> emu redir list` antes de
  enviar desde `127.0.0.1` y vuelve a añadir `udp:43568:43568` si falta.
- **Puerto ocupado (`EADDRINUSE`):** detén la otra variante o proceso que use
  `43568/UDP`; solo una instancia puede escuchar el puerto.
- **NDK incorrecto:** instala exactamente `27.0.12077973` desde SDK Manager y
  deja que Gradle use el `ndkVersion` fijado por el proyecto.
- **Error de runtime ASan:** solo aplica a builds experimentales ASan; verifica
  el NDK fijado y la ABI `x86_64`. No es necesario para Patched Debug.
- **Logs no visibles:** comprueba el serial, usa `logcat -d -s EchoCallUDP` y
  verifica que la aplicación esté iniciada y escuchando.
- **Rutas `tools/` o `samples/` inexistentes:** vuelve a la raíz
  `zero-click-lab/`; no ejecutes esos comandos desde `android-app/`.

## 13. Siguientes pasos

- [Arquitectura](architecture.md)
- [Formato ECLB](packet-format.md)
- [Reproducción experimental](experimental-reproduction.md)
- [Resultados experimentales](experimental-results.md)
- [Ingeniería inversa](reverse-engineering.md)
- [Limitaciones](limitations.md)
- [Evidencia y trazabilidad](evidence/README.md)

La reproducción experimental desarrolla los builds instrumentados y la
captura de nuevas comprobaciones sin convertir la ruta Vulnerable en parte del
onboarding.
