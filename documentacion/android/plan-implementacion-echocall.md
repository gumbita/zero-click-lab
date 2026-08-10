# Plan de implementación de EchoCall Lab Vulnerable/Patched

- **Estado general:** FASES 0, 1, 2, 3, 4 Y 5 VALIDADAS; FASE 6 PENDIENTE.
- **Rama prevista:** `feature/echocall-ui`.
- **Línea base protegida:** `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c`.
- **Cierre versionado de Fase 1:** `26b0638442a5f31b134ba259a8afcbfc0d40d35d`.
- **Cierre versionado de Fase 2:** `ece2e13584838d1e56da117a634ff53b51faa17b`.
- **Cierre versionado de Fase 3:** `aa69cba406fa78fd088019ec75dcd33a0ff05856`.
- **Cierre versionado de Fase 4:** `8d7add26aa22b5884b1ae401e5abe6c4429fd5d6`
  (`8d7add2 Add patched blocked-call handling`).
- **Documento asociado:** `documentacion/android/diseno-interfaz-echocall.md`.
- **Auditoría inicial del repositorio:** 2026-08-04.
- **Revisión y consolidación documental:** 2026-08-10.
- **Alcance de este cierre:** Fase 5, sus nueve rutas funcionales aprobadas y
  los dos documentos vinculantes; Fase 6 permanece fuera de alcance.

> Ninguna fase posterior debe iniciarse sin autorización expresa después de revisar la fase anterior.

## 1. Objetivo

Organizar la evolución del laboratorio Android hacia dos aplicaciones instalables y comparables:

- `EchoCall Lab — Vulnerable`
- `EchoCall Lab — Patched`

El desarrollo será incremental, reversible y trazable, preservando la línea base histórica UDP/JNI/C/ASan confirmada por el repositorio y sus evidencias.

## 2. Punto de partida

### 2.1. Commits protegidos

- `3bcceb36748aaf385dfa6c4b8e43b0a213767de4` — **Harden UDP ingress and recovery**.
- `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c` — **Document Android UDP and ASan evidence**.

### 2.2. Rama de origen

```text
feature/native-core
```

No se reescribirá ni se utilizará como rama de trabajo.

### 2.3. Rama de desarrollo

```text
feature/echocall-ui
```

Debe crearse o verificarse desde el commit protegido. Si ya existe, se auditará antes de modificarla.

### 2.4. Evidencias históricas confirmadas

- E-022: variante histórica ASan, modo VULNERABLE, entrada oversized.
- E-025: variante histórica ASan, modo SAFE, entrada oversized.

E-025 acredita el comportamiento del parser SAFE histórico, que sirve como antecedente técnico de la futura variante Patched, pero no pertenece a un APK final Patched. E-022 y E-025 describen la línea base histórica anterior al rediseño y no constituyen evidencia de los futuros APK finales, que necesitarán capturas nuevas e identificación propia.

### 2.5. Convenciones de estado de las afirmaciones

- **HECHO CONFIRMADO:** observado en el repositorio, los commits protegidos o las evidencias conservadas.
- **DECISIÓN DE DISEÑO APROBADA:** requisito vinculante para la evolución futura.
- **PROPUESTA TÉCNICA PENDIENTE DE AUDITORÍA:** solución prevista que todavía no está implementada ni validada.
- **CRITERIO DE ACEPTACIÓN FUTURO:** condición que deberá demostrarse en la fase correspondiente.
- **LIMITACIÓN:** frontera conocida o propiedad que no puede darse por acreditada.

### 2.6. Línea base Android confirmada el 2026-08-04

**HECHO HISTÓRICO CONFIRMADO.** La inspección inicial del repositorio mostró
que:

- `android-app/app/build.gradle.kts` conservaba `namespace` y `applicationId`
  `com.echocall.lab`, además de un build type `asan`, pero aún no contenía los
  product flavors objetivo;
- `android-app/app/src/main/cpp/CMakeLists.txt` compilaba `safe_parser.c` y
  `vulnerable_parser.c` dentro de la misma biblioteca;
- `native_bridge.c` y `NativeBridge.kt` exponían rutas diferenciadas para ambos
  parsers;
- `MainActivity.kt` conservaba selección SAFE/VULNERABLE en runtime;
- la línea base no contenía una `IncomingCallScreen`: `showIncomingCall` hacía
  visible una tarjeta dentro del único composable y registraba `CALL_INCOMING`
  antes del retorno del parser;
- el header ECLB no contiene checksum; `safe_parse_packet()` y
  `vulnerable_parse_packet()` calculan una suma del payload módulo 256 en
  `parser_result.checksum`, que `native_bridge.c` devuelve como texto únicamente
  en resultados aceptados;
- `UdpPacketReceiver.kt` fijaba el puerto del laboratorio en `43568`.

La matriz de variantes, las identidades y la separación CMake/JNI dejaron de ser
objetivos futuros al completarse la Fase 1. La navegación y el estado simulado en
memoria se implementaron en Fase 2. Fase 3 añadió las pantallas y el estado de
llamada simulada. Fase 4 integró el rechazo oversized exclusivamente en Patched
Debug. Fase 5 incorporó el marker persistente sin ejecutar UDP, muestras,
oversized, ASan ni Vulnerable. El escenario oversized Vulnerable y los APK
finales congelados siguen pendientes de fases posteriores.

### 2.7. Resultado consolidado de Fase 1

**HECHO VALIDADO.** Un único módulo de aplicación Android `:app` genera cuatro
variantes y dos identidades conceptuales de producto. El parser queda fijado al
compilar, no existe selector runtime y Kotlin expone una única entrada JNI
`parsePacket`, además de una consulta de solo lectura de la implementación
compilada.

### 2.8. Resultado consolidado de Fase 2

**HECHO VALIDADO.** La aplicación continúa siendo single-activity y usa
`EchoCallApp` como raíz Compose. `EchoCallNavHost` concentra cinco destinos y
abre `ConversationsScreen` como pantalla inicial. `EchoCallStateHolder` es la
única fuente mutable del estado simulado de producto y permanece separado del
estado técnico de Lab mode.

Navigation Compose, los cinco contactos ficticios, chat, historial, envío local,
actualización de preview/timestamp/orden y reset confirmado fueron validados en
`vulnerableDebug` y `patchedDebug`. No se enviaron datagramas ni se procesaron
muestras durante la validación de Fase 2.

### 2.9. Resultado consolidado de Fase 3

**HECHO VALIDADO.** `CurrentCall` separa dirección, fase y resultado. Las
pantallas `OutgoingCallScreen`, `IncomingCallScreen` y `ActiveCallScreen`
implementan llamadas simuladas sin audio ni telefonía real. La llamada saliente
es completamente local; el flujo entrante solo crea `currentCall` después de
que `NativeBridge.parsePacket()` devuelve `status=accepted code=ok` y se registra
`NATIVE_PARSE_OK`.

Marta Soler es un mapping fijo del simulador para la entrada ECLB válida; su
nombre no está contenido en el datagrama. Aceptar y Rechazar operan sobre el
estado compartido y no vuelven a ejecutar JNI. Los resultados `COMPLETED`,
`REJECTED` y la cancelación local `CANCELLED` se integran en el historial.

### 2.10. Resultado consolidado de Fase 4

**HECHO VALIDADO.** `BlockedCallAttempt` permanece separado de `CurrentCall`.
Cuando Patched devuelve `status=rejected code=payload_too_large`, la aplicación
registra `PACKET_REJECTED_INVALID_LENGTH`, crea el aviso bloqueado para el mapping
local de Marta Soler y añade `INCOMING/BLOCKED` al historial sin mostrar
`IncomingCallScreen`. Cerrar `BlockedCallScreen` elimina el aviso visual, no el
registro de sesión. El resultado JNI completo permanece disponible en Lab mode.

### 2.11. Resultado consolidado de Fase 5

**HECHO VALIDADO.** Preferences DataStore 1.2.1 conserva un
`PendingProcessingMarker` técnico con `scenarioId`, `variant`, `packetLength`,
`timestamp` y `source`. `PendingProcessingStore` espera a que `markPending()`
complete antes de `NATIVE_PARSE_STARTED` y JNI, y espera `clearPending()` tras
todo retorno normal antes de resolver `accepted` o `rejected`.

La lectura de startup navega centralizadamente a
`InterruptedProcessingScreen` cuando existe marker. Abrir Lab no lo elimina;
**Cerrar y continuar** espera la limpieza y vuelve a Conversaciones. El marker
no crea un `CallRecord`, no reconstruye historial y no atribuye automáticamente
`CallOutcome.INTERRUPTED`.

## 3. Reglas generales

1. Una fase por cambio lógico.
2. No mezclar arquitectura, UI, persistencia y pruebas destructivas.
3. Cada fase termina con revisión antes de commit y push.
4. No comenzar una fase sin autorización expresa.
5. Mostrar todos los archivos modificados y el diff relevante.
6. Ejecutar primero pruebas automáticas no destructivas.
7. Señalar las pruebas manuales con `🧪 PRUEBA MANUAL NECESARIA`.
8. No ejecutar Vulnerable ASan con una entrada oversized durante el desarrollo visual.
9. Reservar Vulnerable ASan + oversized para la Fase 8.
10. No usar `git add .`.
11. No usar `git stash`.
12. No usar `git clean`.
13. No borrar, mover ni versionar archivos no relacionados.
14. No modificar `feature/native-core`.
15. No presentar E-022/E-025 como evidencia de APK finales.
16. No afirmar RCE ni equivalencia exacta con CVE-2019-3568.
17. Solo una conversación o agente podrá escribir en el repositorio Android a la vez.

## 4. Directorios protegidos

No modificar sin autorización:

```text
docs/evidencias/
memoria/
native-core/build-asan/
notas/
artefactos externos
APK existentes
.so externas
XLSX pendiente
```

En el cierre de Fase 5 se autorizan las nueve rutas funcionales auditadas y estos
dos documentos:

```text
documentacion/android/diseno-interfaz-echocall.md
documentacion/android/plan-implementacion-echocall.md
```

Las demás fuentes internas se inspeccionan sin modificación.

## 5. Estrategia técnica

### 5.1. Variantes objetivo

**IMPLEMENTADO Y VALIDADO EN FASE 1.** La matriz es:

| Flavor | Build type | Variante |
|---|---|---|
| vulnerable | debug | `vulnerableDebug` |
| vulnerable | asan | `vulnerableAsan` |
| patched | debug | `patchedDebug` |
| patched | asan | `patchedAsan` |

### 5.2. Identidades validadas

**IMPLEMENTADO Y VALIDADO EN FASE 1.** Las identidades de instalación son:

| Variante | `applicationId` | Nombre instalado |
|---|---|---|
| `vulnerableDebug` | `com.echocall.lab.vulnerable` | EchoCall Lab — Vulnerable |
| `patchedDebug` | `com.echocall.lab.patched` | EchoCall Lab — Patched |
| `vulnerableAsan` | `com.echocall.lab.vulnerable.asan` | EchoCall Lab — Vulnerable ASan |
| `patchedAsan` | `com.echocall.lab.patched.asan` | EchoCall Lab — Patched ASan |

### 5.3. Principios

**DECISIÓN DE DISEÑO APROBADA.** Se conservarán estos principios:

- UI y lógica compartidas.
- Parser fijado al compilar.
- Sin selector runtime.
- UDP `43568` en todas las variantes.
- Los cuatro packages pueden coexistir instalados; las pruebas funcionales son
  secuenciales, con una sola aplicación activa y escuchando en `43568` y
  detención de la anterior antes de iniciar la siguiente.
- Vulnerable funcional con entradas válidas.
- Patched visualmente equivalente.
- Diferencia observable solo donde corresponde.

### 5.4. Contrato funcional aprobado

**DECISIÓN DE DISEÑO APROBADA.** Las dos aplicaciones compartirán la misma experiencia normal de mensajería y llamadas, con:

- pantalla principal de conversaciones;
- contactos ficticios Marta Soler, Pau Ferrer, Lucía Navarro, Dani Campos e Irene Vidal;
- Marta Soler asociada visualmente al escenario UDP, sin afirmar que su identidad procede del datagrama;
- historial de llamadas;
- llamada saliente simulada;
- llamada entrante únicamente después del retorno nativo `accepted`;
- procesamiento nativo anterior a las acciones Aceptar/Rechazar, que no repetirán el parseo;
- llamada activa sin audio real;
- pantalla **Llamada bloqueada** en Patched ante `payload_too_large`;
- marcador persistente de operación nativa incompleta;
- Lab mode separado de la experiencia normal y sin selector de parser;
- ausencia de botón de overflow, backend y audio real.

**LIMITACIÓN.** El laboratorio no demuestra RCE ni equivalencia exacta con CVE-2019-3568.

### 5.5. Contrato de llamadas y resultado nativo

**DECISIÓN DE DISEÑO APROBADA.** El modelo futuro separará dirección y
resultado:

- `CallDirection`: `INCOMING`, `OUTGOING`;
- `CallOutcome`: `COMPLETED`, `REJECTED`, `MISSED`, `BLOCKED`, `INTERRUPTED`,
  `CANCELLED`.

`INCOMING` y `OUTGOING` no formarán parte de `CallOutcome`. Los tests del modelo,
autómata e historial deberán verificar esta separación y los mapeos de aceptar,
rechazar, expirar, cancelar, bloquear y finalizar.

**HECHO CONFIRMADO.** `parser_result.checksum` existe en la línea base y se
devuelve como `checksum=<valor>` cuando el resultado es aceptado. Es una suma de
los bytes del payload módulo 256 calculada por cada parser; no es un campo del
header ECLB. La futura representación `NativeParseResult` podrá exponerlo como
valor derivado opcional con ese significado explícito.

**HECHO VALIDADO EN FASE 3.** El evento técnico `CALL_INCOMING` puede registrarse
al recibir la señal, pero `IncomingCallScreen` solo se muestra después de
`status=accepted code=ok` y `NATIVE_PARSE_OK`.

`CallOutcome.INTERRUPTED` permanece definido en el modelo, pero el marker de
Fase 5 no lo asigna automáticamente ni crea un registro de historial.

### 5.6. Persistencia actual

| Información | Persistencia |
|---|---|
| contactos y previews precargadas | código local |
| mensajes nuevos | memoria de sesión |
| historial | memoria de sesión |
| llamada actual | memoria |
| configuración del parser | build-time |
| `PendingProcessingMarker` | Preferences DataStore privado por app |

DataStore no persiste mensajes, conversaciones ni historial. El marker es
estado técnico independiente y tampoco identifica por sí solo por qué terminó
un procesamiento.

## 6. Validación

### 6.1. Automática

**ESTRATEGIA DE VALIDACIÓN.** Según la fase:

- builds Gradle;
- pruebas unitarias;
- pruebas de estado y navegación;
- pruebas nativas existentes;
- inspección de manifests;
- inspección de símbolos o contenido de APK;
- `git diff --check`;
- ausencia de rutas personales o artefactos accidentales.

### 6.2. Manual

Cada prueba deberá incluir:

- precondiciones;
- variante exacta;
- pasos o comandos;
- pantalla esperada;
- logs esperados;
- criterio de éxito;
- condición de parada;
- evidencia a conservar.

### 6.3. Orden seguro

1. Build.
2. Apertura.
3. Navegación local.
4. Entrada válida.
5. Patched oversized.
6. Vulnerable ASan + oversized solo en Fase 8.

## 7. Estrategia de commits

Un commit por fase validada. No crear commit antes de la revisión de la usuaria.

| Fase | Mensaje orientativo |
|---|---|
| 0 | `Document EchoCall final design and implementation plan` |
| 1 | `Add vulnerable and patched Android variants` |
| 2 | `Add shared EchoCall models and navigation` |
| 3 | `Implement simulated messaging and call flows` |
| 4 | `Integrate patched rejection into EchoCall UI` |
| 5 | `Track incomplete native operations` |
| 6 | `Refine EchoCall UI and accessibility` |
| 7 | `Stabilize EchoCall variants and regressions` |
| 8 | `Document final EchoCall ASan comparison` |

## 8. Estrategia de rollback

- Cada fase debe ser revertible por commit.
- No reescribir commits publicados.
- No usar `git reset --hard` con archivos ajenos presentes.
- No borrar archivos untracked.
- Ante regresión UDP/JNI, comparar con `3bcceb3`.
- Ante regresión documental, comparar con `8b20ffe`.

## 9. Seguimiento general

| Fase | Estado | Commit | Automática | Manual | Evidencias | Observaciones |
|---|---|---|---|---|---|---|
| 0 — Diseño y planificación | VALIDADA | — | Completada | No aplica | Markdown | Completada |
| 1 — Arquitectura de variantes | VALIDADA | `26b0638` | Completada | Completada | Entrada válida; sin oversized | Cerrada y publicada |
| 2 — Modelos y navegación | VALIDADA | `ece2e13` | Completada | Completada | Capturas temporales | Cerrada y publicada; sin UDP ni muestras |
| 3 — Mensajería y llamadas normales | VALIDADA | `aa69cba` | Completada | Completada | Entrada válida | Cerrada y publicada; cero oversized |
| 4 — Integración Patched | VALIDADA | `8d7add2` | Completada | Completada | Patched Debug oversized | Cerrada y publicada |
| 5 — Operación nativa incompleta | VALIDADA | Commit que contiene esta revisión | Completada | Completada | Simulación segura, sin UDP ni muestras | Marker persistente prudente; sin `CallRecord` automático |
| 6 — Visual y accesibilidad | PENDIENTE | — | Pendiente | Pendiente | Capturas | — |
| 7 — Congelación y regresión no destructiva | PENDIENTE | — | Pendiente | Pendiente | Patched oversized y controles válidos | — |
| 8 — Evidencia final y única ejecución vulnerable | PENDIENTE | — | Pendiente | Pendiente | Comparación final nueva | Una única ejecución Vulnerable ASan |

Estados permitidos:

- `PENDIENTE`
- `EN DISEÑO`
- `EN IMPLEMENTACIÓN`
- `BLOQUEADA`
- `EN VALIDACIÓN`
- `VALIDADA`
- `COMMIT CREADO`
- `PUBLICADA`

## 10. Fase 0 — Diseño y planificación

### Objetivo

Auditar el punto de partida, contrastar el plan con el documento de diseño y consolidar este plan sin modificar código.

### Precondiciones

- Rama y commit identificados.
- Estado Git inspeccionado.
- Línea base protegida.
- Archivos untracked identificados.

### Alcance autorizado

- Inspeccionar Kotlin, Gradle, CMake, manifests, parsers, evidencias y documentación.
- Actualizar únicamente `documentacion/android/plan-implementacion-echocall.md`.
- Contrastar `documentacion/android/diseno-interfaz-echocall.md` en modo de solo lectura.
- Proponer arquitectura sin implementarla.

### Fuera de alcance

- Cambiar código.
- Ejecutar builds.
- Instalar APK.
- Enviar UDP.
- Ejecutar cualquier entrada oversized.
- Modificar evidencias.

### Archivos previstos

```text
documentacion/android/diseno-interfaz-echocall.md
documentacion/android/plan-implementacion-echocall.md
```

### Riesgos

- Proponer una arquitectura incompatible.
- Presentar propuestas como existentes.
- Mezclar evidencia histórica y diseño futuro.

### Validación automática

- `git diff --check`
- comprobación de encabezados y tablas Markdown;
- búsquedas terminológicas y de denominaciones obsoletas;
- enlaces y rutas internas verificadas;
- commits verificados;
- ningún cambio fuera del plan autorizado.

### Prueba manual

No aplica: la Fase 0 es exclusivamente documental.

### Criterios de aceptación

- [x] Línea base correcta.
- [x] Hechos, decisiones y propuestas diferenciados.
- [x] Arquitectura documentada.
- [x] Pantallas documentadas.
- [x] Fases documentadas.
- [x] Sin cambios de código.
- [x] Revisión y aprobación de la usuaria.

### Punto de parada

Mostrar el archivo revisado y su diff. No hacer commit ni iniciar Fase 1 sin autorización.

### Checklist

- [x] Auditoría
- [x] Diseño
- [x] Plan
- [x] Revisión
- [x] Validación
- [ ] Commit
- [ ] Push

## 11. Fase 1 — Arquitectura de variantes

### Objetivo

Crear Vulnerable/Patched × Debug/ASan con identidades separadas y parser fijado al compilar.

### Precondiciones

- Fase 0 aprobada.
- Gradle/CMake auditados.
- Rama controlada.

### Alcance autorizado

**IMPLEMENTADO Y VALIDADO.** La Fase 1 incluyó:

- Flavor dimension `security`.
- Flavors `vulnerable` y `patched`.
- Build types `debug` y `asan`.
- Nombres y `applicationId`.
- Selección nativa por compilación.
- UI actual temporal.

### Fuera de alcance

- Rediseño completo.
- Contactos y navegación final.
- Cambiar ECLB.
- Ejecutar una entrada oversized en Vulnerable.

### Archivos de implementación

- `android-app/app/build.gradle.kts`;
- `android-app/app/src/main/cpp/CMakeLists.txt`;
- `android-app/app/src/main/cpp/native_bridge.c`;
- `android-app/app/src/main/java/com/echocall/lab/MainActivity.kt`;
- `android-app/app/src/main/java/com/echocall/lab/NativeBridge.kt`;
- recursos `app_name` de `src/vulnerable`, `src/patched`,
  `src/vulnerableAsan` y `src/patchedAsan`;
- eliminación del nombre genérico `src/asan/res/values/strings.xml`;
- eliminación del asset Android local
  `src/main/assets/oversized_complete_payload.bin`.

La muestra canónica externa permanece en
`samples/malformed/oversized_complete_payload.bin` para fases expresamente
autorizadas.

El Manifest, `UdpPacketReceiver.kt`, ECLB y los parsers de `native-core` no se
modificaron.

### Riesgos

- Explosión de variantes.
- `applicationId` duplicados.
- Empaquetar ambos parsers.
- Romper ASan o Debug.
- Continuar pese a una separación nativa no demostrada.

### Validación automática

- `assembleVulnerableDebug`
- `assembleVulnerableAsan`
- `assemblePatchedDebug`
- `assemblePatchedAsan`
- manifests;
- Gradle, CMake y lista efectiva de fuentes compiladas;
- símbolos y strings de cada `.so` y contenido de cada APK;
- correlación binaria completa de las diez bibliotecas nativas.

### Prueba manual

```text
🧪 PRUEBA MANUAL NECESARIA
```

- se instalaron simultáneamente los cuatro packages;
- se abrieron y detuvieron secuencialmente;
- se comprobó nombre, identidad, carga JNI y parser fijo;
- cada variante recibió exactamente un datagrama de la muestra válida de 17
  bytes por `43568/UDP` y devolvió `status=accepted code=ok`;
- no se envió oversized.

### Resultado consolidado

- Vulnerable compila únicamente `vulnerable_parser.c`; Patched compila
  únicamente `safe_parser.c`.
- Existe una sola entrada JNI `parsePacket` y un getter de implementación
  compilada de solo lectura; no existe `parsePacketVulnerable`.
- Debug contiene `arm64-v8a`, `armeabi-v7a`, `x86` y `x86_64`; ASan solo
  `x86_64`, con runtime y `wrap.sh`.
- Las 10/10 bibliotecas se correlacionaron desde APK hasta CMake, fuentes y
  objetos; el símbolo del parser esperado está presente y el contrario ausente.
- Los cuatro APK provisionales cargaron JNI, mostraron el parser correcto,
  conservaron PID y no registraron crash. Las dos variantes ASan no registraron
  error AddressSanitizer.
- La muestra usada fue `samples/benign/valid_call_control.bin`, 17 bytes,
  SHA-256
  `912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E`.

**LIMITACIÓN.** Los APK y sus hashes son artefactos provisionales de validación
de Fase 1, no artefactos finales congelados. La prueba válida no demuestra el
comportamiento oversized de estas variantes, seguridad general de Patched,
explotación, RCE, control del flujo, compromiso del dispositivo ni equivalencia
exacta con WhatsApp o CVE-2019-3568. La separación binaria acredita composición,
no explotabilidad. E-022 y E-025 siguen siendo evidencia histórica anterior.

### Criterios de aceptación

- [x] Cuatro variantes compilan.
- [x] Cuatro IDs distintos.
- [x] Sin selector runtime.
- [x] Parser correcto.
- [x] El APK Patched no empaqueta la implementación vulnerable, verificado en Gradle, CMake, fuentes compiladas, símbolos de la `.so` y contenido del APK.
- [x] Separación razonablemente garantizada; Fase 1 no bloqueada.
- [x] ASan carga.
- [x] Puerto 43568.
- [x] Sin prueba destructiva.

### Punto de parada

La implementación, validación y cierre Git finalizaron en `26b0638`. Fase 2 se
inició únicamente tras su aprobación.

### Checklist

- [x] Implementada
- [x] Automática
- [x] Manual
- [x] Revisada
- [x] Commit
- [x] Push

## 12. Fase 2 — Modelos y navegación

### Objetivo

Introducir estado, datos ficticios y navegación sin completar todavía todos los flujos de llamada.

### Precondiciones

- Fase 1 validada y revisada.
- Cuatro variantes identificables sin selector runtime.
- Contrato de navegación del documento de diseño aprobado.

### Alcance autorizado

**IMPLEMENTADO Y VALIDADO.** Fase 2 incluyó:

- `Contact`, `Message` y `CallRecord`;
- `CallDirection`: `INCOMING`, `OUTGOING`;
- `CallOutcome`: `COMPLETED`, `REJECTED`, `MISSED`, `BLOCKED`, `INTERRUPTED`,
  `CANCELLED`;
- `FakeEchoCallData` con cinco contactos y datos ficticios;
- `EchoCallStateHolder` y `EchoCallUiState` como estado de producto en memoria;
- `EchoCallApp` como raíz Compose;
- Navigation Compose `2.9.8` y `EchoCallNavHost`;
- `ConversationsScreen`, `ChatScreen`, `CallHistoryScreen`, `LabModeScreen` y
  `AboutScreen`;
- envío local, actualización de preview/timestamp, reordenación y reset con
  confirmación.

### Fuera de alcance

- persistencia tras `process death`, DataStore y Room;
- backend, cuentas y contactos reales;
- llamadas reales o pantallas de llamada;
- asociación UDP → Marta;
- `OutgoingCallScreen`, `IncomingCallScreen`, `ActiveCallScreen`,
  `BlockedCallScreen` e `InterruptedProcessingScreen`;
- marcador persistente y `NativeParseResult` estructurado;
- oversized y cualquier entrada oversized en Vulnerable.

### Archivos previstos

Las 16 rutas funcionales validadas son `build.gradle.kts`, `MainActivity.kt` y
los catorce Kotlin compartidos bajo `data/`, `model/`, `navigation/` y `ui/`.
`MainActivity` ya no contiene la UI monolítica. No se crearon recursos de red:
los avatares usan iniciales locales.

### Riesgos

- Estado concentrado en la Activity: mitigado con `EchoCallStateHolder`.
- Perder eventos UDP: no observado; la infraestructura y Lab mode se preservaron.
- Navegación inconsistente: mitigado con rutas centralizadas y `contactId`.
- Pérdida tras `process death`: limitación aceptada de esta fase.

### Validación automática

- `assembleVulnerableDebug`: correcto;
- `assemblePatchedDebug`: correcto;
- búsquedas estáticas de arquitectura y dependencias prohibidas;
- `git diff --check`;
- preservación por diff de UDP, JNI, CMake, Manifest, `native-core` y `samples`.

No se añadieron tests en esta fase y no se construyeron ASan o Release.

### Prueba manual

`vulnerableDebug` y `patchedDebug` arrancaron en `ConversationsScreen`, mostraron
los cinco contactos y navegaron a Chat, Llamadas, Modo Lab y Acerca de. El envío
local actualizó preview y orden, persistió al navegar mientras vivía el proceso
y desapareció al confirmar **Restablecer datos**. Cancelar el diálogo conservó
los cambios. Lab mode mantuvo package, parser y estado UDP correctos.

No se enviaron datagramas, no se pulsó la muestra válida, no se procesó
oversized y no hubo crashes.

### Criterios de aceptación

- [x] Pantalla inicial correcta.
- [x] Cinco contactos.
- [x] Chats accesibles.
- [x] Mensaje local, preview, timestamp y orden.
- [x] Historial, Lab mode y Acerca de accesibles.
- [x] Reset confirmado restaura el dataset inicial.
- [x] Dirección y resultado de llamada no se mezclan.
- [x] `INCOMING` y `OUTGOING` no aparecen en `CallOutcome`.
- [x] Apps visualmente equivalentes.
- [x] UDP y estado técnico preservados.

### Punto de parada

Fase 2 está validada y versionada en `ece2e13`.

### Checklist

- [x] Implementada
- [x] Automática
- [x] Manual
- [x] Revisada
- [x] Commit
- [x] Push

## 13. Fase 3 — Mensajería y llamadas normales

### Objetivo

Completar flujos cotidianos y conectar solo entradas UDP válidas.

### Precondiciones

- Fase 2 validada y revisada.
- Navegación, modelos y estado compartido disponibles.
- Receptor UDP y gateway JNI auditados sin cambiar el puerto `43568`.

### Alcance autorizado

**IMPLEMENTADO Y VALIDADO.** Fase 3 incluyó:

- `CurrentCall` y `CallPhase`: `OUTGOING`, `INCOMING`, `ACTIVE`;
- `OutgoingCallScreen`, transición local determinista desde **Llamando…** y
  cancelación `OUTGOING/CANCELLED`;
- `IncomingCallScreen` únicamente después de `accepted/ok`;
- Aceptar y Rechazar sin segunda invocación JNI;
- `ActiveCallScreen`, contador local, Silenciar y Altavoz visuales;
- Finalizar y actualizar historial con `COMPLETED` o `REJECTED`;
- asociación local de una entrada ECLB válida aceptada con Marta Soler.

### Fuera de alcance

- Patched oversized.
- Marcador.
- Entrada oversized en Vulnerable.

### Archivos previstos

Las 12 rutas funcionales validadas son `CurrentCall.kt`, las tres pantallas de
llamada y ocho archivos compartidos bajo `data/`, `navigation/` y `ui/`.
`NativeBridge.kt`, `UdpPacketReceiver.kt`, JNI, CMake y los parsers permanecieron
sin cambios.

### Riesgos

- Reejecutar el parser al aceptar.
- Mostrar llamada antes de `accepted`.
- Mezclar llamada local y UDP.
- Temporizadores sin cancelar.

### Validación automática

- `assembleVulnerableDebug`: correcto;
- `assemblePatchedDebug`: correcto;
- búsquedas estáticas del flujo, pantallas y preservación técnica;
- `git diff --check`.

### Prueba manual

La llamada saliente a Pau Ferrer transitó localmente de **Llamando…** a activa;
se observaron temporizador, mute y speaker visuales, y finalizar produjo
`OUTGOING/COMPLETED`. La cancelación `OUTGOING/CANCELLED` está implementada, pero
su validación visual no fue suficientemente sólida para usarla como evidencia
principal.

Con `samples/benign/valid_call_control.bin` (17 bytes, SHA-256
`912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E`):

- Vulnerable Debug mantuvo el PID 4723; `NATIVE_PARSE_OK` 14:53:00.643,
  `currentCall` 14:53:00.644 e `IncomingCallScreen` 14:53:00.946; Rechazar
  produjo `INCOMING/REJECTED`;
- Patched Debug mantuvo el PID 4973; `NATIVE_PARSE_OK` 14:54:55.246,
  `currentCall` 14:54:55.247 e `IncomingCallScreen` 14:54:55.439; Aceptar y
  Finalizar produjeron `INCOMING/COMPLETED`.

Los timestamps son observaciones concretas, no una garantía universal del
scheduler. El balance fue dos datagramas válidos, dos `NATIVE_PARSE_STARTED`,
dos `NATIVE_PARSE_OK`, cero rechazos del parser, cero crashes y cero oversized.

La secuencia validada fue:

```text
UDP
→ CONTROL_PACKET_RECEIVED
→ NATIVE_PARSE_STARTED
→ NativeBridge.parsePacket()
→ status=accepted code=ok
→ NATIVE_PARSE_OK
→ currentCall INCOMING para Marta Soler
→ IncomingCallScreen
```

Marta Soler es un mapping fijo del simulador; ECLB no contiene ese nombre.
Aceptar y Rechazar ocurren después del procesamiento nativo y no reejecutan JNI.

**LIMITACIÓN.** Fase 3 no demuestra comportamiento oversized, mitigación
Patched, `heap-buffer-overflow` en las nuevas variantes, crash ASan, RCE,
explotación, telefonía o audio reales, protocolo RTCP real, ni equivalencia
exacta con WhatsApp o CVE-2019-3568. Solo valida el flujo normal con esa entrada
válida concreta.

### Criterios de aceptación

- [x] Llamada saliente.
- [x] Llamada entrante por entrada válida.
- [x] Procesamiento previo.
- [x] Aceptar abre llamada activa.
- [x] Rechazar registra dirección `INCOMING` y resultado `REJECTED`.
- [x] Finalizar o cancelar registra dirección y resultado por separado.
- [x] Proceso vivo.
- [x] Sin oversized.

### Punto de parada

Fase 3 quedó validada y versionada en `aa69cba`; Fase 4 se inició únicamente
después de su aprobación.

### Checklist

- [x] Implementada
- [x] Automática
- [x] Manual
- [x] Revisada
- [x] Commit
- [x] Push

## 14. Fase 4 — Integración Patched

### Objetivo

Conectar `payload_too_large` con Llamada bloqueada, historial y Lab mode.

### Precondiciones

- Fase 3 validada y revisada.
- Patched acepta entradas válidas y el flujo entrante respeta el retorno `accepted`.
- Variante Patched y parser compilado identificados inequívocamente.

### Alcance autorizado

**IMPLEMENTADO Y VALIDADO.** Fase 4 incluyó:

- `BlockedCallAttempt` separado de `CurrentCall`;
- `BlockedCallScreen` con una única acción **Cerrar**;
- navegación centralizada tras `rejected/payload_too_large`;
- historial con dirección `INCOMING` y resultado `BLOCKED`;
- mapping local a Marta Soler, sin atribuir el nombre a ECLB;
- resultado JNI completo y evento técnico conservados en Lab mode;
- eliminación del aviso visual al cerrar, conservando el registro en memoria.

### Fuera de alcance

- Entrada oversized en Vulnerable.
- RCE.
- Cambiar límites sin justificación.

### Archivos previstos

Las siete rutas funcionales validadas son `BlockedCallAttempt.kt`,
`BlockedCallScreen.kt`, `EchoCallDestination.kt`, `EchoCallNavHost.kt`,
`EchoCallApp.kt`, `LabModeScreen.kt` y `EchoCallStateHolder.kt`.
`NativeBridge.kt`, `UdpPacketReceiver.kt`, JNI, CMake, parsers, Gradle, Manifest,
`native-core` y `samples` permanecieron sin cambios.

### Riesgos

- Mostrar llamada antes del rechazo.
- Duplicar historial.
- Ocultar datos técnicos.
- Presentar Patched como seguridad absoluta.

### Validación automática

- `assemblePatchedDebug`: correcto;
- búsquedas estáticas del mapeo, navegación, historial y preservación técnica;
- `git diff --check`;
- cero builds Vulnerable o ASan.

### Prueba manual

La ejecución autoritativa usó Patched Debug (`com.echocall.lab.patched`) y un
único envío de `samples/malformed/oversized_complete_payload.bin` (77 bytes,
SHA-256 `516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA`).

El parser devolvió `status=rejected code=payload_too_large declared_length=64
actual_length=64 maximum=32`. El PID se mantuvo de 4569 a 4569. El orden
observado fue: datagrama 10:49:46.313; `CONTROL_PACKET_RECEIVED` y
`NATIVE_PARSE_STARTED` 10:49:46.391; retorno rechazado 10:49:46.447;
`PACKET_REJECTED_INVALID_LENGTH` 10:49:46.449; estado bloqueado 10:49:46.510; y
`BlockedCallScreen` 10:49:47.085. Estos tiempos describen esta ejecución, no una
garantía universal del scheduler.

Conteos: un oversized enviado, un datagrama recibido, un
`NATIVE_PARSE_STARTED`, un `payload_too_large`, un
`PACKET_REJECTED_INVALID_LENGTH`, una pantalla bloqueada y un registro
`BLOCKED`; cero `NATIVE_PARSE_OK`, crashes/fatal, informes ASan y ejecuciones
Vulnerable. No se mostró `IncomingCallScreen`.

**LIMITACIÓN.** La ejecución demuestra el rechazo de esta muestra concreta y la
supervivencia del proceso observado. No acredita seguridad general de Patched,
mitigación completa, bloqueo de un exploit real, RCE, explotación ni
equivalencia exacta con WhatsApp o CVE-2019-3568. ECLB y la muestra pertenecen
al laboratorio.

### Criterios de aceptación

- [x] Rechazo `payload_too_large`.
- [x] Sin llamada normal ni `CurrentCall INCOMING`.
- [x] Proceso vivo.
- [x] Texto comprensible sin detalles técnicos en la pantalla normal.
- [x] Detalle técnico completo en Lab.
- [x] `BLOCKED` distinto del rechazo manual `REJECTED`.
- [x] Sin ejecución Vulnerable ASan + oversized.

### Punto de parada

Fase 4 validada, cerrada y publicada en
`8d7add26aa22b5884b1ae401e5abe6c4429fd5d6` (`8d7add2 Add patched blocked-call
handling`). La ejecución autoritativa fue Patched Debug, no Patched ASan.

### Checklist

- [x] Implementada
- [x] Automática
- [x] Manual
- [x] Revisada
- [x] Commit
- [x] Push

## 15. Fase 5 — Operación nativa incompleta

### Objetivo

Persistir un marcador antes de JNI y mostrar un aviso prudente cuando no haya retorno.

### Precondiciones

- Fase 4 validada y revisada.
- Gateway JNI centralizado y orden de procesamiento documentado.
- Estrategia de persistencia auditada antes de adoptarla.

### Alcance autorizado

**IMPLEMENTADO Y VALIDADO.** Fase 5 añadió exclusivamente la dependencia
`androidx.datastore:datastore-preferences:1.2.1` y usa la API suspendida `edit`
sin bloquear el hilo principal. El flujo espera la finalización correcta de
`markPending()` antes de JNI y la de `clearPending()` tras todo retorno normal.

- `PendingProcessingMarker` contiene solo `scenarioId`, `variant`,
  `packetLength`, `timestamp` y `source`;
- `PendingProcessingStore` centraliza `markPending()`, `readPending()` y
  `clearPending()`;
- la lectura inicial detecta el marker antes de mostrar el grafo normal;
- `InterruptedProcessingScreen` ofrece **Abrir Modo Lab** y **Cerrar y
  continuar**;
- abrir Lab conserva el marker; cerrar espera su limpieza y vuelve a
  Conversaciones;
- Lab muestra los campos y explica que el marker no identifica la causa;
- el marker permanece separado de `CurrentCall`, `BlockedCallAttempt`,
  `CallRecord` y el estado de producto.

### Fuera de alcance

- entrada oversized en Vulnerable;
- informe ASan simulado;
- atribución automática.

### Archivos previstos

Las nueve rutas funcionales son `build.gradle.kts`, `MainActivity.kt`,
`EchoCallDestination.kt`, `EchoCallNavHost.kt`, `EchoCallApp.kt`,
`LabModeScreen.kt`, `PendingProcessingStore.kt`,
`PendingProcessingMarker.kt` e `InterruptedProcessingScreen.kt`. No se
modificaron JNI, UDP, CMake, parsers, ECLB, Manifest, `native-core` ni
`samples`.

### Riesgos

- falso positivo;
- escritura asíncrona no completada;
- carrera con JNI;
- marcador sin limpiar.

### Validación automática

- una sola llamada Kotlin efectiva a `NativeBridge.parsePacket()`;
- orden estático `markPending → NATIVE_PARSE_STARTED → JNI →
  clearPending → accepted/rejected`;
- dependencia DataStore única y modelo mínimo;
- builds `vulnerableDebug` y `patchedDebug` correctos;
- preservación nativa y `git diff --check` correctos.

### Prueba manual

La validación fue deliberadamente no destructiva. El extra interno
`com.echocall.lab.extra.PENDING_MARKER_TEST_COMMAND`, disponible solo en una
aplicación debuggable, admite `mark`, `read` y `clear`; no aparece en la UI, no
ejecuta JNI y no procesa muestras.

El estado inicial fue `marker=null → ConversationsScreen`. Se sembró
`scenarioId=voip_control_packet`, `variant=com.echocall.lab.patched`,
`packetLength=17`, `source=test`; tras `force-stop` y relanzamiento apareció
`InterruptedProcessingScreen`. Lab mostró el marker. **Cerrar y continuar**
registró `INTERRUPTED_MARKER_CLEARED_BY_USER`; el siguiente relanzamiento volvió
a `marker=null → ConversationsScreen`.

Los PID 8790, 8878, 8948 y 9139 corresponden a `force-stop` y relanzamientos
controlados, no a crashes. No se usaron UDP, JNI durante la simulación,
muestras, oversized, ASan ni Vulnerable.

**LIMITACIÓN.** Fase 5 no demuestra `heap-buffer-overflow`, comportamiento
oversized Vulnerable, ASan, `SIGABRT`, crash nativo, explotación, RCE, control
del flujo ni equivalencia exacta con WhatsApp o CVE-2019-3568. Un marker
encontrado tampoco identifica por sí solo por qué terminó el procesamiento.

### Criterios de aceptación

- [x] Persistencia completada antes de JNI.
- [x] Limpieza tras todo retorno normal.
- [x] Aviso correcto y navegación centralizada.
- [x] Sin atribución automática de `heap-buffer-overflow`, crash o RCE.
- [x] Sin `CallRecord(INTERRUPTED)` automático.
- [x] Sin ejecución destructiva.

### Punto de parada

Fase 5 validada; versionada en el commit que contiene esta revisión. Detenerse
sin iniciar Fase 6.

### Checklist

- [x] Implementada
- [x] Automática
- [x] Manual
- [x] Revisada
- [x] Commit incluido en este cierre
- [x] Push incluido en este cierre

## 16. Fase 6 — Visual y accesibilidad

### Objetivo

Refinar la presentación sin cambiar la semántica técnica.

### Precondiciones

- Fase 5 validada y revisada.
- Flujos funcionales y estados técnicos estabilizados.
- Equivalencia funcional Vulnerable/Patched comprobada con entradas válidas.

### Alcance autorizado

- tema;
- espaciado;
- tipografía;
- avatares;
- iconos;
- contraste;
- descripciones;
- áreas táctiles;
- estados vacíos;
- rotación;
- textos;
- distintivos.

### Fuera de alcance

- red nueva;
- audio;
- backend;
- cambio de parser;
- entradas oversized.

### Archivos previstos

- tema, recursos, componentes y pantallas compartidos creados en fases anteriores;
- `android-app/app/src/main/AndroidManifest.xml` si la auditoría confirma que el tema base debe adaptarse.

### Riesgos

- Alterar la semántica técnica durante un cambio visual.
- Diferenciar Vulnerable y Patched solo mediante color.
- Introducir regresiones de navegación, recomposición o ciclo de vida.

### Validación automática

- build;
- pruebas Compose disponibles;
- lint;
- recursos.

### Prueba manual

- claro/oscuro;
- rotación;
- volver;
- textos largos;
- todas las pantallas;
- comparación entre apps.

### Criterios de aceptación

- [ ] UI coherente.
- [ ] Apps equivalentes.
- [ ] Iconos accesibles.
- [ ] Estado estable.
- [ ] Sin estética de hacking.

### Punto de parada

Detenerse antes de la congelación.

### Checklist

- [ ] Implementada
- [ ] Automática
- [ ] Manual
- [ ] Revisada
- [ ] Commit
- [ ] Push

## 17. Fase 7 — Congelación y regresión no destructiva

### Objetivo

Congelar candidatos y verificar todo sin ejecutar Vulnerable ASan con una entrada oversized.

### Precondiciones

- Fase 6 validada y revisada.
- Cambios funcionales cerrados.
- Matriz de variantes, procedimientos y criterios de evidencia revisados.

### Alcance autorizado

- clean builds;
- hashes candidatos;
- entrada válida;
- Patched oversized;
- ciclo de vida;
- `EADDRINUSE`;
- navegación;
- historial;
- marcador con simulación segura;
- auditoría de si el hook debuggable
  `com.echocall.lab.extra.PENDING_MARKER_TEST_COMMAND` se conserva o se retira
  antes de congelar los APK candidatos;
- revisión documental.

### Fuera de alcance

- Entrada oversized en Vulnerable.
- Cambios funcionales grandes.

### Archivos previstos

- configuración, código y recursos ya implementados en las fases anteriores;
- documentación operativa y manifiestos de hashes que se autoricen expresamente en Fase 7.

No se prevé introducir arquitectura nueva en esta fase.

### Riesgos

- Congelar artefactos que no correspondan a la variante declarada.
- Alterar código después de calcular hashes.
- Ejecutar por error Vulnerable ASan con una entrada oversized.

### Validación automática

- cuatro clean builds;
- tests completos;
- manifests;
- símbolos;
- hashes;
- `git diff --check`;
- estado Git controlado.

### Prueba manual

- smoke test;
- entrada válida;
- Patched oversized;
- segundo plano/primer plano;
- `EADDRINUSE`;
- navegación;
- llamadas locales.

### Criterios de aceptación

- [ ] Cuatro APK congelados.
- [ ] Hashes.
- [ ] Regresión no destructiva completa.
- [ ] Patched oversized.
- [ ] Sin ejecución Vulnerable ASan + oversized.
- [ ] Sin cambios de código pendientes.

### Punto de parada

Solicitar autorización para Fase 8.

### Checklist

- [ ] Implementada
- [ ] Automática
- [ ] Manual
- [ ] Revisada
- [ ] Commit
- [ ] Push

## 18. Fase 8 — Evidencia final y única ejecución vulnerable

### Objetivo

Generar evidencia primaria nueva sobre los APK finales congelados mediante:

- una captura final de Patched ASan con una entrada oversized;
- una única ejecución autorizada de Vulnerable ASan con la misma muestra.

Patched ASan + oversized y la única ejecución Vulnerable ASan + oversized quedan
reservadas para Fase 8. La restricción de ejecución única se aplica
exclusivamente a Vulnerable ASan + oversized sobre el APK final congelado y
requiere autorización expresa.

### Precondiciones

- APK congelados y hasheados.
- Muestra hasheada.
- Procedimiento revisado.
- Autorización expresa.
- Emulador o dispositivo dedicado.
- Red controlada.
- Redirección verificada.
- Captura preparada.

### Alcance autorizado

1. Capturar Patched ASan + oversized sobre el APK final congelado.
2. Verificar rechazo y proceso vivo.
3. Ejecutar Vulnerable ASan + oversized una única vez y solo tras la autorización expresa.
4. Capturar ASan y terminación.
5. Simbolizar.
6. Comparar.
7. Documentar hashes, PID y limitaciones.

### Fuera de alcance

- RCE;
- control del flujo;
- shellcode;
- terceros;
- Internet;
- repetir la ejecución Vulnerable ASan + oversized.

### Archivos previstos

- APK finales y muestra oversized congelados, solo como entradas de la validación;
- rutas de evidencia final que se autoricen expresamente antes de ejecutar la fase;
- símbolos y fuentes nativas previamente identificados para la simbolización.

La Fase 8 no autoriza cambios funcionales en Gradle, CMake, Kotlin, JNI o C.

### Riesgos

- APK equivocado.
- Log perdido.
- Repetición accidental.
- Binarios no equivalentes.
- Interpretación excesiva.

### Validación automática

- hashes;
- package/variant;
- PID;
- receptor/puerto;
- firmas;
- simbolización;
- manifest.

### Prueba manual

```text
🧪 PRUEBA MANUAL NECESARIA
```

Procedimiento exacto y lineal. Completar primero la captura final Patched. Confirmar de nuevo la autorización y detenerse inmediatamente después de la única ejecución Vulnerable ASan, aunque el resultado sea incompleto; cualquier repetición requeriría una decisión nueva fuera de este plan.

### Criterios de aceptación — Patched

- [ ] mismo input;
- [ ] `payload_too_large`;
- [ ] proceso vivo;
- [ ] sin firmas buscadas en el log capturado;
- [ ] Llamada bloqueada;
- [ ] hashes.

### Criterios de aceptación — Vulnerable

- [ ] mismo input;
- [ ] parser inequívoco;
- [ ] informe ASan;
- [ ] escritura y tamaños;
- [ ] terminación;
- [ ] simbolización;
- [ ] una sola ejecución.

### Interpretación

- [ ] sin RCE;
- [ ] sin control del flujo;
- [ ] sin equivalencia exacta;
- [ ] hechos separados de interpretación.

### Evidencias esperadas

- metadatos;
- hashes;
- salida del emisor;
- log;
- UI;
- PID;
- simbolización;
- comparación;
- manifest SHA-256.

### Punto de parada

Cerrar el laboratorio y clasificar la ejecución Vulnerable ASan como no repetir.

### Checklist

- [ ] Preparación
- [ ] Patched final
- [ ] Autorización vulnerable
- [ ] Vulnerable única
- [ ] Simbolización
- [ ] Documentación
- [ ] Revisión
- [ ] Commit
- [ ] Push

## 19. Dependencias

Ninguna fase posterior puede comenzar sin la revisión y aprobación de la anterior. La transición de Fase 7 a Fase 8 exige además autorización expresa para la única ejecución Vulnerable ASan + oversized.

```text
Fase 0
  ↓
Fase 1
  ↓
Fase 2
  ↓
Fase 3
  ↓
Fase 4
  ↓
Fase 5
  ↓
Fase 6
  ↓
Fase 7
  ↓ autorización expresa
Fase 8
```

## 20. Criterios globales de finalización

- [x] Diseño aprobado.
- [x] Plan actualizado.
- [x] Línea base preservada.
- [x] Rama publicada.
- [x] Cuatro variantes.
- [x] Parser fijado.
- [x] La separación nativa de Patched ha sido verificada en Gradle, CMake, fuentes compiladas, símbolos de la `.so` y contenido del APK; la implementación vulnerable no se empaqueta.
- [x] La separación quedó razonablemente garantizada y Fase 1 no quedó bloqueada.
- [x] Vulnerable conserva la condición deliberada.
- [x] Conversaciones.
- [x] Chat y mensajería local en memoria.
- [x] Historial ficticio en memoria.
- [x] Llamada saliente.
- [x] Llamada entrante válida.
- [x] Llamada activa.
- [x] Llamada bloqueada.
- [x] Marcador prudente.
- [x] Lab mode inicial separado de la pantalla principal.
- [ ] Regresión UDP/JNI.
- [ ] Evidencias finales nuevas.
- [ ] Limitaciones académicas.

## 21. Riesgos transversales

| Riesgo | Probabilidad | Impacto | Tratamiento |
|---|---:|---:|---|
| Romper UDP con Compose | Media | Alto | Fases y entrada válida |
| Empaquetar ambos parsers | Media | Alto | Mitigado y verificado en Fase 1 mediante Gradle, CMake, fuentes, objetos, símbolos y APK |
| Selector runtime residual | Media | Medio | Eliminado y verificado estática y funcionalmente en Fase 1 |
| ASan obsoleto/no soportado desde 2023 y con posibles errores | Media | Alto | Entorno y limitación documentados; HWASan como recomendación actual en entornos compatibles |
| Falso positivo del marcador | Media | Medio | Texto prudente |
| Hook debuggable del marker en APK candidatos | Baja | Medio | Auditar en Fase 7 si se conserva o retira |
| Estado duplicado | Media | Medio | UDF/ViewModel adaptados |
| Alcance excesivo | Alta | Medio | Sin backend/audio/plugins |
| Confusión con CVE real | Media | Alto | Fuentes y ECLB explícito |
| Pérdida de trazabilidad | Baja | Alto | Commits, hashes y manifest |
| Repetición de Vulnerable ASan + oversized | Baja | Alto | Una única ejecución, solo en Fase 8 y con autorización expresa |

## 22. Fuentes

### Internas

- commits `3bcceb36748aaf385dfa6c4b8e43b0a213767de4` y `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c`;
- `documentacion/android/diseno-interfaz-echocall.md`;
- `docs/evidencias/registro_validacion_experimental.md`;
- `docs/evidencias/artefactos/E-022/`;
- `docs/evidencias/artefactos/E-025/`;
- `android-app/app/build.gradle.kts`;
- `android-app/build.gradle.kts`;
- `android-app/settings.gradle.kts`;
- `android-app/app/src/main/AndroidManifest.xml`;
- `android-app/app/src/main/cpp/CMakeLists.txt`;
- `android-app/app/src/main/cpp/native_bridge.c`;
- `android-app/app/src/main/java/com/echocall/lab/MainActivity.kt`;
- `android-app/app/src/main/java/com/echocall/lab/NativeBridge.kt`;
- `android-app/app/src/main/java/com/echocall/lab/UdpPacketReceiver.kt`;
- `native-core/CMakeLists.txt`;
- `native-core/include/packet_format.h`;
- `native-core/include/parser_result.h`;
- `native-core/src/parser_result.c`;
- `native-core/src/safe_parser.c`;
- `native-core/src/vulnerable_parser.c`.

### Oficiales

- [Build variants y source sets](https://developer.android.com/build/build-variants),
  **verificado el 2026-08-05**: product flavors, build types, prioridad de
  source sets y `androidComponents.beforeVariants`.
- [Configuración del módulo app](https://developer.android.com/build/configure-app-module),
  **verificado el 2026-08-05**: separación entre namespace y `applicationId`.
- [Integración Gradle/CMake](https://developer.android.com/studio/projects/gradle-external-native-builds),
  **verificado el 2026-08-05**: argumentos CMake, targets y ABI filters.
- [Arquitectura](https://developer.android.com/topic/architecture),
  [recomendaciones](https://developer.android.com/topic/architecture/recommendations),
  [UI layer](https://developer.android.com/topic/architecture/ui-layer) y
  [Navigation](https://developer.android.com/guide/navigation), **verificados el
  2026-08-05**: capas, UDF, state holders y navegación por destinos.
- [API de Preferences DataStore](https://developer.android.com/reference/androidx/datastore/preferences/core/PreferencesKt),
  **verificada el 2026-08-05**: `edit` es transaccional, completa tras persistir
  en disco y lanza `IOException` ante fallo de escritura.
- [Android NDK ASan](https://developer.android.com/ndk/guides/asan), **verificado
  el 2026-08-05**: obsoleto/no soportado desde 2023, todavía utilizable con
  posibles errores; HWASan es la recomendación actual en entornos compatibles.
- [Material 3](https://developer.android.com/develop/ui/compose/designsystems/material3),
  [semántica Compose](https://developer.android.com/develop/ui/compose/accessibility/semantics)
  y [accesibilidad](https://developer.android.com/guide/topics/ui/accessibility/principles),
  **verificados el 2026-08-05**.
- [Advisory Meta CVE-2019-3568](https://www.facebook.com/security/advisories/cve-2019-3568),
  **no verificado directamente el 2026-08-05**: redirigió a acceso/bloqueo.
- [CVE Program](https://www.cve.org/CVERecord?id=CVE-2019-3568): la interfaz
  web requirió JavaScript; el [registro JSON oficial](https://cveawg.mitre.org/api/cve/CVE-2019-3568)
  fue **verificado el 2026-08-05** y describe RTCP y CWE-122.
- [NVD CVE-2019-3568](https://nvd.nist.gov/vuln/detail/CVE-2019-3568),
  **verificado el 2026-08-05**: describe RTCP y no publica el root cause interno
  ni una cadena completa de explotación.

## 23. Registro de actualizaciones

| Fecha | Cambio | Estado |
|---|---|---|
| 2026-08-05 | Borrador inicial consolidado | Pendiente de incorporación y auditoría |
| 2026-08-05 | Auditoría documental: E-022/E-025, jerarquía Markdown, Fase 8, separación nativa y rutas internas | EN DISEÑO |
| 2026-08-05 | Revisión final cruzada de diseño/plan, llamadas, checksum, UI histórica, RTCP y fuentes oficiales | EN DISEÑO — pendiente de aprobación |
| 2026-08-07 | Consolidación del resultado validado de Fase 1 y preparación selectiva del cierre Git | VALIDADA — commit pendiente |
| 2026-08-07 | Consolidación de UI de mensajería, navegación, estado local y reset de Fase 2 | VALIDADA — cierre Git autorizado |
| 2026-08-10 | Consolidación de llamadas simuladas y flujo UDP válido de Fase 3 | VALIDADA — cierre Git autorizado |
| 2026-08-10 | Consolidación del rechazo Patched y pantalla de llamada bloqueada de Fase 4 | VALIDADA, CERRADA Y PUBLICADA — `8d7add2` |
| 2026-08-10 | Persistencia pre-JNI, marker técnico e InterruptedProcessingScreen de Fase 5 | VALIDADA — versionada en el commit que contiene esta revisión |
