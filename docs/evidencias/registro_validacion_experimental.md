# Registro de validación experimental

> **REGISTRO HISTÓRICO**
>
> Las expresiones como «funcionalidad actual» dentro de entradas antiguas
> describen el estado existente en la fecha de esa evidencia, no necesariamente
> el `HEAD` actual. La arquitectura final de EchoCall está documentada en el
> [diseño Android](../../documentacion/android/diseno-interfaz-echocall.md) y el
> [plan de implementación](../../documentacion/android/plan-implementacion-echocall.md).

Este documento es la matriz maestra de evidencias del laboratorio del TFM.
Distingue evidencia primaria, reconstrucción documental, resultado reportado y
trabajo pendiente. El código, un commit o un artefacto actual pueden confirmar
que una funcionalidad existe, pero no sustituyen una salida experimental
histórica que no se haya conservado.

Los campos no documentados se marcan como `ND`. Las fases `P0` a `P5` son
categorías documentales de este registro, no fases formales de desarrollo.

## Criterio de clasificación

| Estado | Significado |
|---|---|
| `PRIMARIA` | Existe un log, captura, transcripción, salida original o artefacto conservado que respalda directamente el resultado. |
| `RECONSTRUIDA DOCUMENTALMENTE` | Código, commits, recursos o artefactos actuales confirman la funcionalidad o configuración, sin sustituir la evidencia experimental original. |
| `REPORTADA` | Resultado comunicado, pero sin artefacto primario suficiente en el conjunto actual. |
| `REPORTADA SIN ARTEFACTO PRIMARIO` | Forma explícita de `REPORTADA` usada para E-015–E-020. |
| `PENDIENTE` | Prueba o artefacto todavía no obtenido. |

La recomendación de repetición se registra por separado. Una evidencia primaria
puede no estar reproducida y, aun así, no ser recomendable repetirla.

## Matriz maestra

| ID | Fase | Prueba | Entrada utilizada | Componente y modo | Resultado observado o estado | Artefacto primario | Estado | Base de confirmación |
|---|---|---|---|---|---|---|---|---|
| E-001 | P0 Python | Python MVP y tests | ND | Python MVP, MIXTO | Resultado comunicado; detalles y exit codes ND | No conservado aquí | `REPORTADA` | Resultado reportado |
| E-002 | P1 Native | `receiver_safe` con muestra válida | Muestra válida; identidad histórica pendiente | `receiver_safe`, SAFE | Aceptación comunicada | No conservado aquí | `REPORTADA` | Código y resultado reportado |
| E-003 | P1 Native | `receiver_safe` rechazando oversized | Oversized; identidad histórica pendiente | `receiver_safe`, SAFE | Rechazo comunicado | No conservado aquí | `REPORTADA` | Código y resultado reportado |
| E-004 | P1 Native | `receiver_vuln` con muestra válida | Muestra válida; identidad histórica pendiente | `receiver_vuln`, VULNERABLE | Aceptación comunicada | No conservado aquí | `REPORTADA` | Código y resultado reportado |
| E-005 | P2 Diferencial | Corrupción nativa instrumentada | Oversized; identidad con E-006 pendiente | Parser vulnerable, VULNERABLE | `heap-buffer-overflow`, escritura de 64 bytes y destino de 32 bytes comunicados | Informe original no conservado aquí | `REPORTADA` | Código y resultado reportado |
| E-006 | P2 Diferencial | Parser seguro con la entrada de E-005 | Longitudes declarada y real de 64; identidad histórica pendiente | `receiver_safe`, SAFE | `payload_too_large`, máximo 32 comunicado | Log original no conservado aquí | `REPORTADA` | Código y resultado reportado |
| E-007 | P3 Android | Creación y build inicial de la app Compose | Código Android | Aplicación Android | Build correcto comunicado | Log de build no conservado | `REPORTADA` | Código, commit y resultado reportado |
| E-008 | P3 Android | Conexión JNI mínima | App y biblioteca nativa | JNI | `Native JNI connected` comunicado | Log o captura original no conservados | `REPORTADA` | Código, commit y resultado reportado |
| E-009 | P3 Android | Parser SAFE real desde Android | `valid_call_control.bin`; identidad histórica no registrada | Android, JNI y SAFE | Aceptación comunicada | Salida de dispositivo no conservada | `REPORTADA` | Código, commit y resultado reportado |
| E-010 | P3 Android | Parser VULNERABLE real con muestra válida | `valid_call_control.bin`; identidad histórica no registrada | Android, JNI y VULNERABLE | Aceptación comunicada | Salida de dispositivo no conservada | `REPORTADA` | Código, commit y resultado reportado |
| E-011 | P4 E2E | Entorno emulado | Configuración AVD | Android Emulator | `EchoCall_Lab_API_36` comunicado | Exportación histórica del AVD no conservada | `REPORTADA` | Configuración local actual y resultado reportado |
| E-012 | P4 E2E | Llamada entrante simulada | Asset local | Flujo de llamada | Flujo visual comunicado | Captura o log original de la fase no conservados | `REPORTADA` | Código, commit y resultado reportado |
| E-013 | P4 E2E | Procesamiento automático preinteracción | Asset local | App, JNI y parser, MIXTO | Procesamiento previo a Accept/Reject comunicado | Log temporal original no conservado | `REPORTADA` | Código, commit y resultado reportado |
| E-014 | P4 E2E | Eventos visibles del flujo | Misma sesión; identidad histórica no registrada | UI y parser, MIXTO | Secuencia visible comunicada | Captura o log causal original no conservados | `REPORTADA` | Código, captura contextual y resultado reportado |
| E-015 | P4 Reconstrucción | Base Compose, build y conexión JNI | Código y recursos actuales | Android y JNI | La estructura y las funciones JNI existen; el resultado histórico no se eleva de nivel | No | `RECONSTRUIDA DOCUMENTALMENTE` | Código y commit |
| E-016 | P4 Reconstrucción | Integración SAFE/VULNERABLE con muestra válida | Asset válido actual | Android, JNI y ambos parsers | Las dos rutas y botones válidos existen; ejecuciones históricas sin salida primaria | No | `RECONSTRUIDA DOCUMENTALMENTE` | Código y commits |
| E-017 | P4 Reconstrucción | Simulación automática local y selección de oversized | Asset oversized actual | Android, SAFE/VULNERABLE | El flujo automático y la selección del asset existen; resultados históricos sin artefacto | No | `RECONSTRUIDA DOCUMENTALMENTE` | Código y commit |
| E-018 | P4 Reconstrucción | Build ASan x86_64 y empaquetado | Configuración Gradle/CMake actual | Variante ASan | Build type, ABI, flags, runtime, wrapper y `applicationId` confirmables documentalmente | No para la validación histórica | `RECONSTRUIDA DOCUMENTALMENTE` | Código, commit y APK actual no histórico |
| E-019 | P4 Android ASan | Carga de ASan, smoke válido y SAFE local oversized | Entradas válida y oversized reportadas | Android ASan y SAFE | Carga mediante `/proc/<pid>/maps`, aceptación válida y rechazo SAFE comunicados | Salidas originales no conservadas aquí | `REPORTADA SIN ARTEFACTO PRIMARIO` | Código, APK actual y resultado reportado |
| E-020 | P4 Android ASan | VULNERABLE local oversized y simbolización | Oversized reportado | Android ASan y VULNERABLE | Overflow local y simbolización comunicados; la correspondencia fuente puede reconstruirse con símbolos de E-022 | Informe local y salida original de simbolización no conservados | `REPORTADA SIN ARTEFACTO PRIMARIO` | Código, símbolos E-022 y resultado reportado |
| E-021 | P5 Android UDP | Control válido por UDP en Android ASan | `samples/benign/valid_call_control.bin`; 17 bytes; hash actual posprueba `912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E` | UDP, JNI y VULNERABLE | Datagrama recibido, despacho VULNERABLE, `status=accepted code=ok`; PID 6847 vivo | Transcripción E-021–E-024 | `PRIMARIA` | Transcripción y hash actual |
| E-022 | P5 Android UDP | Oversized por UDP en Android ASan | `samples/malformed/oversized_complete_payload.bin`; 77 bytes; hash actual posprueba `516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA` | UDP, JNI, VULNERABLE y ASan | `heap-buffer-overflow`, escritura de 64 bytes, región de 32 bytes, `ABORTING`, `SIGABRT`; PID 6847 desaparece | Log íntegro, transcripción y biblioteca no strip | `PRIMARIA` | Log, transcripción, símbolos y manifest SHA-256 |
| E-023 | P5 Robustez UDP | Ciclo de vida y ráfagas | Muestra válida actual; 17 bytes | UDP y ambos parsers | Stop, restart, bind único, diez retornos SAFE y uno VULNERABLE; PID 5624 vivo | Transcripción E-021–E-024 | `PRIMARIA` | Transcripción y código |
| E-024 | P5 Robustez UDP | `EADDRINUSE` y recuperación | Puerto 43568 ocupado; después muestra válida | UDP y SAFE | Error visible, un retry, bind único y parse SAFE aceptado; PID 5971 vivo | Transcripción E-021–E-024 | `PRIMARIA` | Transcripción y código |
| E-025 | P5 Android UDP | SAFE oversized por UDP con cadena de custodia | `samples/malformed/oversized_complete_payload.bin`; 77 bytes; SHA-256 previo `516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA` | UDP, JNI, SAFE y ASan | Recepción y despacho SAFE; `payload_too_large`; PID 5397 permanece vivo; sin firmas de error buscadas en el log | Metadatos, sender, log y UI anterior/posterior | `PRIMARIA` | Cinco artefactos E-025 y hashes verificados |

## Revisión de E-007–E-014

Estas entradas conservan el estado `REPORTADA`. La confirmación documental de
la implementación no mejora artificialmente su nivel experimental.

| ID | Confirmación mediante código o Git | Fuentes | Evidencia primaria que falta | Base de confirmación |
|---|---|---|---|---|
| E-007 | Existe un proyecto Kotlin/Compose con una única Activity y configuración Android compilable. | [`android-app/app/build.gradle.kts`](../../android-app/app/build.gradle.kts), [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt), commit [`a0bc257`](https://github.com/gumbita/zero-click-lab/commit/a0bc2572dd9e436ee759f0e97b8c6f3acc367688) | Salida original completa de `assembleDebug`, comando, fecha y exit code. | Código, commit, resultado reportado |
| E-008 | `NativeBridge.nativeStatus()` y su función JNI devuelven el texto de conexión. | [`NativeBridge.kt`](../../android-app/app/src/main/java/com/echocall/lab/NativeBridge.kt), función `Java_com_echocall_lab_NativeBridge_nativeStatus` en [`native_bridge.c`](../../android-app/app/src/main/cpp/native_bridge.c), commit [`a0bc257`](https://github.com/gumbita/zero-click-lab/commit/a0bc2572dd9e436ee759f0e97b8c6f3acc367688) | Captura o log original del texto observado en dispositivo. | Código, commit, resultado reportado |
| E-009 | El asset válido se pasa a `NativeBridge.parsePacket()` y CMake reutiliza `safe_parser.c` desde `native-core`. | [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt), [`CMakeLists.txt`](../../android-app/app/src/main/cpp/CMakeLists.txt), [`native_bridge.c`](../../android-app/app/src/main/cpp/native_bridge.c), commit [`a0bc257`](https://github.com/gumbita/zero-click-lab/commit/a0bc2572dd9e436ee759f0e97b8c6f3acc367688) | Log o captura original con entrada, resultado completo, PID y contexto de ejecución. | Código, commit, resultado reportado |
| E-010 | El asset válido se pasa a `NativeBridge.parsePacketVulnerable()` y CMake reutiliza `vulnerable_parser.c`. | [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt), [`native_bridge.c`](../../android-app/app/src/main/cpp/native_bridge.c), commit [`1bb7747`](https://github.com/gumbita/zero-click-lab/commit/1bb7747711d94be4c7c3053c1042e224c157c268) | Log o captura original del resultado válido VULNERABLE y PID. | Código, commit, resultado reportado |
| E-011 | La configuración local actual del AVD se denomina `EchoCall_Lab_API_36`, usa `android-36` y arquitectura `x86_64`. | `%USERPROFILE%/.android/avd/EchoCall_Lab_API_36.avd/config.ini` y `.ini` actuales, no versionados | Exportación o captura histórica del AVD y build fingerprint de la sesión original. | Configuración local actual, resultado reportado |
| E-012 | `EchoCallLabScreen` conserva el botón `Simulate incoming call` y el panel de llamada simulada. | [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt), commit [`33ff251`](https://github.com/gumbita/zero-click-lab/commit/33ff2512918ec65f9cfe1ac63a6afe6918eae4f0) | Captura o grabación original de la fase básica. | Código, commit, resultado reportado |
| E-013 | El flujo local carga el paquete y llama a JNI sin depender de Accept o Reject; esos botones representan una decisión posterior. | Función `EchoCallLabScreen` en [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt), commit [`33ff251`](https://github.com/gumbita/zero-click-lab/commit/33ff2512918ec65f9cfe1ac63a6afe6918eae4f0) | Log temporal original que pruebe el orden causal antes de cualquier interacción. | Código, commit, resultado reportado |
| E-014 | El código contiene `CALL_INCOMING`, `CONTROL_PACKET_RECEIVED`, `NATIVE_PARSE_STARTED` y estados finales. | [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt); [captura contextual](artefactos/contexto/echocall-vulnerable-20260731-134143.png) no concluyente | Captura o log causal de la sesión original con la secuencia completa. | Código, captura contextual, resultado reportado |

## Reconstrucción documental E-015–E-020

Estas entradas consolidan el trabajo realizado sin afirmar que el repositorio
conserve las salidas experimentales originales.

### E-015 — Base Compose, build inicial y conexión JNI

- **Objetivo:** establecer la app Android Compose y comprobar la conexión
  Kotlin → JNI → C.
- **Funcionalidad actual:** `MainActivity`, `NativeBridge.nativeStatus()` y
  `Java_com_echocall_lab_NativeBridge_nativeStatus` siguen presentes.
- **Fuentes:** [`android-app/app/build.gradle.kts`](../../android-app/app/build.gradle.kts),
  [`NativeBridge.kt`](../../android-app/app/src/main/java/com/echocall/lab/NativeBridge.kt),
  [`native_bridge.c`](../../android-app/app/src/main/cpp/native_bridge.c) y
  commit [`a0bc257`](https://github.com/gumbita/zero-click-lab/commit/a0bc2572dd9e436ee759f0e97b8c6f3acc367688).
- **Evidencia primaria faltante:** salida original del primer build y captura o
  log de `Native JNI connected`.
- **Estado:** `RECONSTRUIDA DOCUMENTALMENTE`.

### E-016 — Parsers SAFE y VULNERABLE con muestra válida

- **Objetivo:** mantener dos funciones JNI separadas y verificar el control
  válido en ambas rutas.
- **Funcionalidad actual:** los botones manuales abren
  `valid_call_control.bin`; `parsePacket()` llama al parser SAFE y
  `parsePacketVulnerable()` al vulnerable.
- **Fuentes:** [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt),
  [`NativeBridge.kt`](../../android-app/app/src/main/java/com/echocall/lab/NativeBridge.kt),
  [`native_bridge.c`](../../android-app/app/src/main/cpp/native_bridge.c),
  commits [`a0bc257`](https://github.com/gumbita/zero-click-lab/commit/a0bc2572dd9e436ee759f0e97b8c6f3acc367688)
  y [`1bb7747`](https://github.com/gumbita/zero-click-lab/commit/1bb7747711d94be4c7c3053c1042e224c157c268).
- **Evidencia primaria faltante:** salidas originales de dispositivo para ambas
  rutas, con hash previo de la muestra y PID.
- **Estado:** `RECONSTRUIDA DOCUMENTALMENTE`.

### E-017 — Simulación automática local

- **Objetivo:** procesar automáticamente una entrada local antes de Accept o
  Reject y seleccionar la ruta mediante SAFE/VULNERABLE.
- **Funcionalidad actual:** `EchoCallLabScreen` carga
  `oversized_complete_payload.bin`, genera los eventos y despacha al JNI según
  el modo seleccionado.
- **Fuentes:** [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt),
  [asset Android oversized histórico en `33ff251`](https://github.com/gumbita/zero-click-lab/blob/33ff2512918ec65f9cfe1ac63a6afe6918eae4f0/android-app/app/src/main/assets/oversized_complete_payload.bin)
  y commit [`33ff251`](https://github.com/gumbita/zero-click-lab/commit/33ff2512918ec65f9cfe1ac63a6afe6918eae4f0).
  El asset fue retirado de la aplicación en `26b0638`; la muestra canónica
  externa permanece bajo `samples/malformed/`.
- **Evidencia primaria faltante:** captura y log originales de la simulación,
  incluyendo orden temporal y resultado.
- **Estado:** `RECONSTRUIDA DOCUMENTALMENTE`.

### E-018 — Build Android ASan x86_64 y empaquetado

- **Objetivo:** disponer de una variante ASan separada sin alterar Debug.
- **Funcionalidad actual:** build type `asan`, `applicationIdSuffix = ".asan"`,
  filtro `x86_64`, opción `ENABLE_ANDROID_ASAN`, flags de compilación y enlace,
  generación desde el NDK del runtime y `wrap.sh`, y packaging específico.
- **Fuentes:** [`android-app/app/build.gradle.kts`](../../android-app/app/build.gradle.kts),
  opción `ENABLE_ANDROID_ASAN` en
  [`CMakeLists.txt`](../../android-app/app/src/main/cpp/CMakeLists.txt), recursos
  [`main/strings.xml`](../../android-app/app/src/main/res/values/strings.xml) y
  [`asan/strings.xml` histórico en `3bcceb3`](https://github.com/gumbita/zero-click-lab/blob/3bcceb36748aaf385dfa6c4b8e43b0a213767de4/android-app/app/src/asan/res/values/strings.xml),
  commit [`ad5045f`](https://github.com/gumbita/zero-click-lab/commit/ad5045feee02684ea4e368aab0ebcc6d5c1836dd).
  El nombre ASan genérico fue sustituido por recursos de variante y retirado en
  `26b0638`.
- **Comprobación actual no histórica:** el APK ASan presente contiene
  `lib/x86_64/libechocall_native.so`,
  `lib/x86_64/libclang_rt.asan-x86_64-android.so` y
  `lib/x86_64/wrap.sh`; su `applicationId` es `com.echocall.lab.asan` y solo
  declara `x86_64`. Esto no identifica el APK ejecutado en E-022.
- **Evidencia primaria faltante:** salidas originales de `assembleDebug`,
  `assembleAsan` e inspección del APK correspondientes a aquella fase.
- **Estado:** `RECONSTRUIDA DOCUMENTALMENTE`.

### E-019 — Carga de ASan, smoke válido y SAFE local oversized

- **Objetivo:** confirmar que ASan y `libechocall_native.so` estaban cargadas,
  que una entrada válida seguía aceptándose y que SAFE rechazaba oversized sin
  error ASan.
- **Funcionalidad actual:** el APK actual empaqueta el runtime y el flujo SAFE
  local llama a `NativeBridge.parsePacket()` con el asset oversized.
- **Fuentes:** [`android-app/app/build.gradle.kts`](../../android-app/app/build.gradle.kts),
  [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt),
  commit [`ad5045f`](https://github.com/gumbita/zero-click-lab/commit/ad5045feee02684ea4e368aab0ebcc6d5c1836dd)
  y resultado reportado durante el proyecto.
- **Evidencia primaria faltante:** salida original de `/proc/<pid>/maps`, PID,
  resultado válido, rechazo `payload_too_large` y búsqueda íntegra de errores
  ASan de esas sesiones.
- **Estado:** `REPORTADA SIN ARTEFACTO PRIMARIO`.

### E-020 — VULNERABLE local oversized y simbolización

- **Objetivo:** detectar el overflow en el flujo local instrumentado y resolver
  sus frames hasta los fuentes.
- **Funcionalidad actual:** la ruta local VULNERABLE usa el asset oversized; el
  parser conserva `malloc(32)` y la copia de 64 bytes. El log y la biblioteca
  no strip de E-022 comparten Build ID y permiten sostener documentalmente la
  correspondencia fuente, pero prueban la ejecución UDP E-022, no la ejecución
  local E-020.
- **Fuentes:** [`MainActivity.kt`](../../android-app/app/src/main/java/com/echocall/lab/MainActivity.kt),
  [`vulnerable_parser.c`](../../native-core/src/vulnerable_parser.c),
  [`native_bridge.c`](../../android-app/app/src/main/cpp/native_bridge.c),
  [log E-022](artefactos/E-022/asan-udp-vulnerable-20260731-140415.log),
  biblioteca no strip externa identificada en el manifest y commit
  [`ad5045f`](https://github.com/gumbita/zero-click-lab/commit/ad5045feee02684ea4e368aab0ebcc6d5c1836dd).
- **Correspondencia documental:** `vulnerable_parser.c:83` reserva 32 bytes;
  `vulnerable_parser.c:93` ejecuta la copia; `native_bridge.c:53` invoca el
  parser; `native_bridge.c:120` selecciona la ruta vulnerable.
- **Evidencia primaria faltante:** log íntegro de la ejecución local y salida
  original de las herramientas de simbolización.
- **Estado:** `REPORTADA SIN ARTEFACTO PRIMARIO`.
- **Repetición:** `NO REPETIR`; E-022 ya aporta una ejecución concluyente del
  mismo defecto por la ruta UDP objetivo del laboratorio.

## Tabla de cobertura Android

La columna “Reproducible actualmente” describe la viabilidad técnica deducible
del repositorio y entorno actuales; no afirma que la prueba se haya vuelto a
ejecutar durante esta consolidación.

| ID | Funcionalidad | Estado | Artefacto primario | Reconstrucción Git/código | Reproducible actualmente | Repetición recomendada | Cadena de custodia |
|---|---|---|---|---|---|---|---|
| E-007 | Build Android inicial | `REPORTADA` | No | Sí | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | Falta salida original y vínculo a commit/APK. |
| E-008 | Conexión JNI mínima | `REPORTADA` | No | Sí | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | Falta captura o log original. |
| E-009 | SAFE válido en Android | `REPORTADA` | No | Sí | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | Falta hash previo y salida original. |
| E-010 | VULNERABLE válido en Android | `REPORTADA` | No | Sí | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | Falta hash previo y salida original. |
| E-011 | AVD API 36 x86_64 | `REPORTADA` | No | Parcial, mediante configuración local actual | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | Falta exportación histórica y build fingerprint. |
| E-012 | Llamada simulada | `REPORTADA` | No | Sí | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | Falta captura o grabación original. |
| E-013 | Parse automático preinteracción | `REPORTADA` | No | Sí | Sí, no destructiva en SAFE/válida | `RECOMENDABLE NO DESTRUCTIVA` | Falta log causal con timestamps. |
| E-014 | Secuencia visible de eventos | `REPORTADA` | Solo captura contextual no concluyente | Sí | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | La captura conservada no demuestra por sí sola el despacho. |
| E-015 | Base Compose/build/JNI | `RECONSTRUIDA DOCUMENTALMENTE` | No | Sí | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | La reconstrucción no prueba la primera ejecución. |
| E-016 | Ambos parsers con control válido | `RECONSTRUIDA DOCUMENTALMENTE` | No | Sí | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | Falta emparejar la entrada mediante hash previo. |
| E-017 | Simulación automática local | `RECONSTRUIDA DOCUMENTALMENTE` | No | Sí | SAFE sí; VULNERABLE oversized no debe ejecutarse | `RECOMENDABLE NO DESTRUCTIVA` | Documentar solo una futura ruta no destructiva. |
| E-018 | Build y empaquetado ASan x86_64 | `RECONSTRUIDA DOCUMENTALMENTE` | No histórico | Sí | Sí, sin abrir la app | `RECOMENDABLE NO DESTRUCTIVA` | APK actual posterior; APK histórico no conservado. |
| E-019 | Runtime cargado, smoke y SAFE oversized local | `REPORTADA SIN ARTEFACTO PRIMARIO` | No | Parcial | Sí, no destructiva | `RECOMENDABLE NO DESTRUCTIVA` | Faltan `/proc/maps`, PID, log y hashes previos. |
| E-020 | VULNERABLE oversized local y simbolización | `REPORTADA SIN ARTEFACTO PRIMARIO` | No para la ejecución local | Parcial mediante E-022 | Simbolización sí; overflow no debe repetirse | `NO REPETIR` | E-022 no debe presentarse como log de la ruta local. |
| E-021 | Control válido VULNERABLE por UDP | `PRIMARIA` | Sí, transcripción | Sí | Sí, no destructiva | `NO NECESARIA` | Hash de muestra actual posprueba; exit code del emisor ND. |
| E-022 | VULNERABLE oversized por UDP | `PRIMARIA` | Sí, log, transcripción y símbolos | Sí | Técnicamente sí, pero no debe ejecutarse | `NO REPETIR` | APK histórico exacto no conservado; hash de entrada posprueba. |
| E-023 | Ciclo de vida y ráfagas UDP | `PRIMARIA` | Sí, transcripción | Sí | Sí, no destructiva | `NO NECESARIA` | Varios eventos se conservan en una transcripción conjunta. |
| E-024 | `EADDRINUSE` y retry | `PRIMARIA` | Sí, transcripción | Sí | Sí, no destructiva | `NO NECESARIA` | Exit codes auxiliares ND. |
| E-025 | SAFE oversized por UDP con custodia integral | `PRIMARIA` | Sí, cinco artefactos | Sí | Sí, pero la evidencia ya está cerrada | `NO REPETIR` | Commit, APK, muestra, PID, sender, log y UI identificados antes/después. |

## E-025 — SAFE oversized por UDP

E-025 conserva una ejecución SAFE por UDP realizada con la variante ASan y el
commit de código
`3bcceb36748aaf385dfa6c4b8e43b0a213767de4` (`Harden UDP ingress and recovery`).
La evidencia se clasifica `PRIMARIA` y no necesita repetirse.

Datos de cadena de custodia:

- inicio registrado: `2026-08-04T12:17:42.4536933+02:00`;
- fin registrado: `2026-08-04T12:17:50.4997099+02:00`;
- package y variante: `com.echocall.lab.asan`, ASan;
- parser y transporte: SAFE, UDP, puertos host/guest 43568;
- APK: `android-app/app/build/outputs/apk/asan/app-asan.apk`, 23 179 791
  bytes, SHA-256
  `964198FC0316E1FA149067523778097604B60D0E48E635A2545ACB266EDC5182`;
- muestra: `samples/malformed/oversized_complete_payload.bin`, 77 bytes,
  SHA-256 calculado antes del envío
  `516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA`;
- PID antes/después: 5397/5397; proceso vivo;
- emisor: exit code 0 y
  `destination=127.0.0.1:43568 file=oversized_complete_payload.bin bytes_sent=77`;
- log: recepción `length=77`, despacho
  `Dispatching datagram mode=SAFE length=77` y retorno
  `status=rejected code=payload_too_large declared_length=64 actual_length=64 maximum=32`;
- UI anterior y posterior: `EchoCall Lab — ASan`, package/variante, SAFE
  marcado, VULNERABLE desmarcado, fuente UDP de 77 bytes, resultado SAFE y
  `PACKET_REJECTED`.

Artefactos:

- [metadatos](artefactos/E-025/e025-safe-udp-oversized-20260804-121742-metadata.txt):
  1 361 bytes; SHA-256
  `163250586C651FAEAAA4226A06197FD0D395CD84B8933B08AD436DB3FD873E45`;
- [salida del emisor](artefactos/E-025/e025-safe-udp-oversized-20260804-121742-sender.txt):
  79 bytes; SHA-256
  `C0EAA651A54FA70296A7AEE873358FEA1584094D46053EC0D713F54538BDA4F5`;
- [log íntegro](artefactos/E-025/e025-safe-udp-oversized-20260804-121742.log):
  2 774 bytes; SHA-256
  `D06D6FABF8E7333011CB51C66019BBAA7F14564EE96FD8AFED3B5AE44A664EDB`;
- [UI anterior](artefactos/E-025/e025-safe-before-20260804-121742.xml):
  10 642 bytes; SHA-256
  `2FD09851FAD75DC0E1AFB2D313321BB2CE48B100D2EB7543B4BA9FF396263FF3`;
- [UI posterior](artefactos/E-025/e025-safe-after-20260804-121742.xml):
  15 139 bytes; SHA-256
  `96552323FA40F7C6C79414197933865DDA189BA8704F5371B7BEFF416B7DD6AC`.

La búsqueda literal en el log incluyó `AddressSanitizer`,
`heap-buffer-overflow`, `stack-buffer-overflow`, `use-after-free`,
`LeakSanitizer`, `UndefinedBehaviorSanitizer`, `runtime error:`, `Fatal signal`,
`SIGSEGV`, `SIGABRT`, `ABORTING`, `F libc` y `F DEBUG`.

**No se encontraron en el log capturado las firmas de error buscadas.** Esta
observación se limita al log conservado: no afirma que ASan garantice ausencia
de cualquier error ni que la aplicación sea completamente segura.

## Comparativa end-to-end SAFE/VULNERABLE

| Propiedad | SAFE — E-025 | VULNERABLE — E-022 |
|---|---|---|
| Instrumentación | ASan | ASan |
| Transporte | UDP | UDP |
| Datagrama | 77 bytes | 77 bytes |
| Parser | SAFE | VULNERABLE |
| Resultado | `payload_too_large`; `declared_length=64`; `actual_length=64`; `maximum=32` | `heap-buffer-overflow`; `WRITE of size 64`; región heap de 32 bytes |
| Frame nativo relevante | Rechazo antes de la copia vulnerable | `__asan_memcpy`; `vulnerable_parse_packet` |
| Estado del proceso | PID 5397 vivo antes y después | PID 6847 termina |
| Señal/informe | Sin firmas de error buscadas en el log capturado | `SIGABRT`; `ABORTING` |
| Cadena de custodia | Commit, APK, muestra, sender, log y UI identificados | Log y símbolos consistentes; APK histórico exacto no conservado |

Ambas evidencias emplean la misma muestra lógica oversized y el mismo valor de
SHA-256 documentado, pero la identidad de E-022 se calculó posprueba. Los APK
no son el mismo binario histórico: E-025 conserva el hash previo del APK usado;
E-022 no conserva su APK exacto. La comparación acredita comportamientos
diferentes del laboratorio. No demuestra RCE, control del flujo ni
explotabilidad completa, y no afirma equivalencia exacta con CVE-2019-3568.

## Custodia e interpretación de E-021–E-025

Artefactos primarios conservados:

- [Log ASan E-022](artefactos/E-022/asan-udp-vulnerable-20260731-140415.log):
  64 698 bytes; SHA-256
  `F59B0BCCC33F2B9E6BCCA28DA80145F59C04A2E93B9F101A999A042185EDED7D`.
- Biblioteca no strip E-022, excluida de Git normal y conservada como artefacto
  externo: 106 528 bytes; SHA-256
  `BA86A1DDB9881A6BF22F07907DCE14995242C42F4B25981FC1F1DD6649490453`;
  Build ID `c455a1c576ff356de665e37770bd209913e6e7b2`.
- [Transcripción saneada E-021–E-024](artefactos/E-021_E-024_sesion_powershell_sanitizada.txt):
  369 039 bytes y 4 258 líneas; SHA-256
  `92C0CF67B87D2D126504FA9FDD59455753EA56363B40AFD1865EB25866595496`.
  Es un derivado documental; sustituye las 915 apariciones del prefijo de
  perfil personal de Windows por `<USER_HOME>` sin alterar otros bytes.
- Transcripción original E-021–E-024, excluida de Git por contener rutas
  personales: 372 699 bytes y 4 258 líneas; SHA-256
  `ECED591432B4783142303B530FD42AC41CFD6493722FA410B4C4DC0C9A860F14`.
- [Captura contextual](artefactos/contexto/echocall-vulnerable-20260731-134143.png):
  204 949 bytes; SHA-256
  `EE7DDFD7804D18DB51F1C415934B0E57EF98DE485E4EB79EDE94D45CF634A8E0`.

El log E-022 registra un `heap-buffer-overflow`, una escritura de 64 bytes,
acceso inmediatamente posterior a una región de 32 bytes, `__asan_memcpy`,
`vulnerable_parse_packet`, `ABORTING` y `SIGABRT`. El runtime ASan tiene Build
ID `3a151d09f677bf8add3834376cb104781a668003`; el Build ID de la biblioteca
nativa coincide con el binario no strip custodiado. E-022 es una única ejecución
concluyente y se clasifica `NO REPETIR`.

La captura contextual muestra el selector VULNERABLE activado, pero conserva un
resultado SAFE anterior. No demuestra por sí sola E-021 ni E-022. La
transcripción contiene consultas repetidas del mismo Logcat; sus 4 258 líneas
no representan 4 258 observaciones independientes.

## Cadena de custodia de entradas y binarios

- Los hashes del [manifest](artefactos/manifest_sha256.md) demuestran la
  identidad de los ficheros actualmente presentes.
- Un hash calculado durante la consolidación, después de una sesión histórica,
  no demuestra por sí solo qué fichero exacto se envió en aquella sesión.
- No se registró antes o durante E-022 el SHA-256 de la muestra; por eso el hash
  actual de `oversized_complete_payload.bin` es consistente, pero posprueba.
- El APK ASan actual es posterior a E-022 y no se presenta como el APK de esa
  ejecución.
- No se conserva el APK exacto usado en E-022 ni su hash.
- El tamaño y hash del log E-022, el Build ID registrado y la biblioteca no
  strip son consistentes entre sí.
- Esta limitación no invalida el fallo observado, pero reduce la capacidad de
  reconstrucción binaria exacta de la sesión.
- E-025 registra antes de la prueba tanto el SHA-256 de la muestra como el del
  APK, y conserva metadatos, sender, log y UI anterior/posterior.

El fallo observado demuestra una escritura fuera de límites detectada por ASan
en este laboratorio. No demuestra por sí solo ejecución remota de código,
explotabilidad completa ni equivalencia exacta con CVE-2019-3568.

## APK actual frente al APK histórico

Los APK actuales están fuera del manifest de custodia y bajo un directorio de
build ignorado. Los hashes siguientes describen únicamente los ficheros
presentes durante esta consolidación.

| Artefacto | Estado | Fecha o commit asociado | SHA-256 disponible | Relación con E-022/E-025 | Limitación |
|---|---|---|---|---|---|
| APK ASan histórico ejecutado en E-022 | No conservado | Sesión E-022 del 31/07/2026 14:04; commit exacto ND | ND | Fue el APK ejecutado | No puede reconstruirse binariamente de forma exacta. |
| APK ASan usado en E-025 | Excluido de Git; 23 179 791 bytes | Commit `3bcceb36748aaf385dfa6c4b8e43b0a213767de4` | `964198FC0316E1FA149067523778097604B60D0E48E635A2545ACB266EDC5182` | Identificado para E-025; no es el APK histórico de E-022 | El APK no se versiona y puede desaparecer del directorio de build. |
| `android-app/app/build/outputs/apk/debug/app-debug.apk` actual | Presente fuera de custodia; 24 328 807 bytes | Marca temporal del fichero: 03/08/2026 09:48:42 +02:00; commit exacto no vinculado | `C973BA082EAA07CAF748E3E1675B432F9921B0EC6F6C9D0832BC5D175C47E25F` | Sin relación directa | Puede cambiar al recompilar; no es evidencia de E-022. |

Una inspección actual con `aapt dump badging` identifica
`com.echocall.lab` para Debug y `com.echocall.lab.asan` para ASan. El APK Debug
actual declara `arm64-v8a`, `armeabi-v7a`, `x86` y `x86_64`; el APK ASan actual
declara únicamente `x86_64`. La inspección del ZIP confirma runtime ASan y
`wrap.sh` solo en el APK ASan actual. Son comprobaciones documentales actuales,
no artefactos experimentales históricos.

## Versiones del entorno consolidadas

| Elemento | Versión o valor confirmado | Fuente exacta | Alcance |
|---|---|---|---|
| JBR actual | OpenJDK `21.0.6`, build `21.0.6+-13391695-b895.109` | Ejecución actual de `gradlew.bat --version` usando el JBR de Android Studio | Herramienta disponible al consolidar; no demuestra el JBR usado en E-022. |
| Nivel Java del proyecto | Java/JVM target 17 | `compileOptions` y `kotlinOptions` en [`android-app/app/build.gradle.kts`](../../android-app/app/build.gradle.kts) | Configuración actual del código. |
| Gradle Wrapper | `8.13` | [`android-app/gradle/wrapper/gradle-wrapper.properties`](../../android-app/gradle/wrapper/gradle-wrapper.properties) y comprobación actual `gradlew.bat --version` | Configuración actual. |
| Android Gradle Plugin | `8.12.2` | Bloque `plugins` de [`android-app/build.gradle.kts`](../../android-app/build.gradle.kts) | Configuración actual. |
| Kotlin plugins | `2.0.21` | Bloque `plugins` de [`android-app/build.gradle.kts`](../../android-app/build.gradle.kts) | Configuración actual. |
| Android NDK | `27.0.12077973` | `android.ndkVersion` en [`android-app/app/build.gradle.kts`](../../android-app/app/build.gradle.kts) | Configuración actual y origen previsto del runtime/wrapper. |
| CMake Android | `3.22.1` | `externalNativeBuild.cmake.version` y `cmake_minimum_required` en [`build.gradle.kts`](../../android-app/app/build.gradle.kts) y [`CMakeLists.txt`](../../android-app/app/src/main/cpp/CMakeLists.txt) | Configuración actual. |
| Android API de compilación/objetivo | `compileSdk=36`, `targetSdk=36`, `minSdk=28` | Bloque `android` de [`android-app/app/build.gradle.kts`](../../android-app/app/build.gradle.kts) | Configuración actual. |
| AVD local actual | `EchoCall_Lab_API_36`, `android-36`, `x86_64` | `%USERPROFILE%/.android/avd/EchoCall_Lab_API_36.avd/config.ini` actual | Configuración local no versionada; vínculo histórico con E-022 no cerrado. |
| Variante ASan | `com.echocall.lab.asan`, ABI `x86_64` | [`android-app/app/build.gradle.kts`](../../android-app/app/build.gradle.kts) y `aapt dump badging` del APK actual | Configuración y APK actuales. |
| Arquitectura del runtime ASan | `x86_64` | Nombre `libclang_rt.asan-x86_64-android.so`, APK actual y log E-022 | Confirmada para el runtime custodiado en la sesión. |

Versiones no consolidadas en la evidencia disponible:

- versión exacta del JDK/JBR utilizado durante E-022;
- versión exacta de Android Emulator y `platform-tools` de la sesión E-022;
- build fingerprint completo del AVD durante E-022;
- versión semántica exacta de Clang/ASan más allá de su pertenencia al NDK
  configurado y del Build ID del runtime;
- commit exacto y SHA-256 del APK histórico ejecutado en E-022.

Para cada una aplica: **Versión no consolidada en la evidencia disponible**.

## Estado del XLSX

El archivo local `registro_evidencias_zero_click.xlsx`, excluido temporalmente
de Git, existe, tiene 134 036 bytes y SHA-256
`E423F751925BD4E144E103BF8478C788928A87B3FE033003481F2297D52D76AB`.

Contenido interno, fórmulas y formato pendientes de revisión con una herramienta
estructurada compatible con XLSX. No se utiliza una lectura parcial como
sustituto ni se certifica que el libro refleje esta consolidación Markdown.

## Resultados excluidos como evidencia concluyente

- La recepción aislada de 77 bytes sin log de despacho no identifica el parser.
- Los intentos que solo contienen `Datagram received` no prueban SAFE ni
  VULNERABLE.
- `device 'emulator-5554' not found` es una incidencia del entorno.
- `code -1 (SI_QUEUE)` describe el origen de SIGABRT, no un exit code numérico.
- El código actual y los commits no sustituyen logs históricos ausentes.
- Un crash no prueba explotabilidad, control del flujo ni ejecución de código.

## Pendientes prioritarios

1. Conservar builds y smoke tests no destructivos de E-007–E-019 con comando,
   stdout, stderr, exit code, commit y hashes previos.
2. Emparejar SAFE/VULNERABLE válidos mediante una muestra cuyo hash se calcule
   antes de ambas pruebas.
3. Exportar de forma no sensible la configuración relevante del AVD.
4. Revisar estructuralmente el XLSX y decidir si debe sincronizarse con este
   registro Markdown.
5. No repetir E-020, E-022 ni E-025: las evidencias UDP comparativas ya están
   preservadas.

## Custodia Git

El commit documental incorpora únicamente Markdown, el log E-022, la captura
contextual, los cinco artefactos E-025 y el derivado saneado de la transcripción.

Permanecen excluidos el XLSX pendiente de revisión estructural, la transcripción
original con rutas personales, la biblioteca `.so`, los APK y los artefactos de
build. El [manifest](artefactos/manifest_sha256.md) separa expresamente
artefactos versionados, externos/excluidos y pendientes de revisión.
