# Plan de implementación de EchoCall Lab Vulnerable/Patched

- **Estado general:** FASE 1 VALIDADA — implementación y validación completadas; pendiente únicamente de cierre Git.
- **Rama prevista:** `feature/echocall-ui`.
- **Línea base protegida:** `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c`.
- **Documento asociado:** `documentacion/android/diseno-interfaz-echocall.md`.
- **Auditoría inicial del repositorio:** 2026-08-04.
- **Revisión y consolidación documental:** 2026-08-05.
- **Alcance de este cierre:** actualización exclusiva de este plan y del documento de diseño asociado; las fuentes técnicas se inspeccionan en modo de solo lectura.

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
objetivos futuros al completarse la Fase 1. La nueva navegación, el marcador
persistente y los APK finales congelados siguen pendientes de fases posteriores.

### 2.7. Resultado consolidado de Fase 1

**HECHO VALIDADO.** Un único módulo de aplicación Android `:app` genera cuatro
variantes y dos identidades conceptuales de producto. El parser queda fijado al
compilar, no existe selector runtime y Kotlin expone una única entrada JNI
`parsePacket`, además de una consulta de solo lectura de la implementación
compilada.

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

En este cierre documental de Fase 1 solo se autoriza modificar:

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

**DECISIÓN DE DISEÑO APROBADA.** El evento o estado visual `CALL_INCOMING` puede
registrarse antes del retorno del parser en la línea base histórica; la futura
`IncomingCallScreen` todavía no existe y solo podrá mostrarse después de
`status=accepted code=ok`.

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
| 1 — Arquitectura de variantes | VALIDADA | Pendiente | Completada | Completada | Entrada válida; sin oversized | Implementación y validación completadas; pendiente únicamente de cierre Git |
| 2 — Modelos y navegación | PENDIENTE | — | Pendiente | Pendiente | Capturas opcionales | — |
| 3 — Mensajería y llamadas normales | PENDIENTE | — | Pendiente | Pendiente | Entrada válida | — |
| 4 — Integración Patched | PENDIENTE | — | Pendiente | Pendiente | Patched oversized | — |
| 5 — Operación nativa incompleta | PENDIENTE | — | Pendiente | Pendiente | Sin entrada oversized vulnerable | — |
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

La implementación y validación están completadas. Detenerse tras el cierre Git;
no iniciar Fase 2. Cualquier regresión de la separación nativa antes del commit
bloquearía el cierre.

### Checklist

- [x] Implementada
- [x] Automática
- [x] Manual
- [x] Revisada
- [ ] Commit
- [ ] Push

## 12. Fase 2 — Modelos y navegación

### Objetivo

Introducir estado, datos ficticios y navegación sin completar todavía todos los flujos de llamada.

### Precondiciones

- Fase 1 validada y revisada.
- Cuatro variantes identificables sin selector runtime.
- Contrato de navegación del documento de diseño aprobado.

### Alcance autorizado

- `Contact`
- `Message`
- `CallRecord`
- `CallDirection`: `INCOMING`, `OUTGOING`.
- `CallOutcome`: `COMPLETED`, `REJECTED`, `MISSED`, `BLOCKED`, `INTERRUPTED`, `CANCELLED`.
- `NativeParseResult` con checksum derivado opcional, no perteneciente al header ECLB.
- Datos locales.
- Conversaciones.
- Chat.
- Historial inicial.
- Lab mode inicial.
- Navegación.

### Fuera de alcance

- Llamada activa completa.
- Oversized.
- Marcador persistente.
- Entrada oversized en Vulnerable.

### Archivos previstos

- `android-app/app/src/main/java/com/echocall/lab/MainActivity.kt` como punto de partida que deberá descomponerse;
- nuevos modelos, estado, navegación y pantallas compartidas bajo el package `com.echocall.lab`, con rutas definitivas pendientes de la auditoría de Fase 2;
- recursos compartidos y recursos locales de los cinco contactos ficticios.

### Riesgos

- Estado concentrado en la Activity.
- Perder eventos UDP.
- Navegación inconsistente.

### Validación automática

- cuatro builds;
- tests de modelos, incluida la separación `CallDirection`/`CallOutcome`;
- tests de navegación;
- compilación Compose.

### Prueba manual

- abrir contactos;
- escribir mensajes;
- abrir historial;
- abrir Lab mode;
- volver;
- tema claro/oscuro.

### Criterios de aceptación

- [ ] Pantalla inicial correcta.
- [ ] Cinco contactos.
- [ ] Chats accesibles.
- [ ] Mensaje local.
- [ ] Historial y Lab accesibles.
- [ ] Dirección y resultado de llamada no se mezclan.
- [ ] `INCOMING` y `OUTGOING` no aparecen en `CallOutcome`.
- [ ] Apps visualmente equivalentes.
- [ ] UDP sigue compilando.

### Punto de parada

Detenerse antes de implementar llamadas completas.

### Checklist

- [ ] Implementada
- [ ] Automática
- [ ] Manual
- [ ] Revisada
- [ ] Commit
- [ ] Push

## 13. Fase 3 — Mensajería y llamadas normales

### Objetivo

Completar flujos cotidianos y conectar solo entradas UDP válidas.

### Precondiciones

- Fase 2 validada y revisada.
- Navegación, modelos y estado compartido disponibles.
- Receptor UDP y gateway JNI auditados sin cambiar el puerto `43568`.

### Alcance autorizado

- llamada saliente;
- `Llamando…`;
- llamada entrante tras `accepted`;
- Aceptar/Rechazar;
- llamada activa;
- contador;
- Silenciar/Altavoz;
- Finalizar;
- historial de sesión.

### Fuera de alcance

- Patched oversized.
- Marcador.
- Entrada oversized en Vulnerable.

### Archivos previstos

- pantallas y estado compartidos creados en Fase 2;
- `android-app/app/src/main/java/com/echocall/lab/UdpPacketReceiver.kt`;
- `android-app/app/src/main/java/com/echocall/lab/NativeBridge.kt`;
- coordinador o gateway compartido cuya ruta definitiva se establezca tras la auditoría.

### Riesgos

- Reejecutar el parser al aceptar.
- Mostrar llamada antes de `accepted`.
- Mezclar llamada local y UDP.
- Temporizadores sin cancelar.

### Validación automática

- builds;
- tests de estados y del autómata de llamada;
- tests de historial y mapeos `COMPLETED`, `REJECTED`, `MISSED` y `CANCELLED`;
- test de que Aceptar/Rechazar no invocan JNI.

### Prueba manual

- llamada saliente;
- finalizar;
- muestra válida en Vulnerable;
- muestra válida en Patched;
- aceptar;
- rechazar;
- PID vivo;
- eventos coherentes.

### Criterios de aceptación

- [ ] Llamada saliente.
- [ ] Llamada entrante por entrada válida.
- [ ] Procesamiento previo.
- [ ] Aceptar abre llamada activa.
- [ ] Rechazar registra dirección `INCOMING` y resultado `REJECTED`.
- [ ] Finalizar o cancelar registra dirección y resultado por separado.
- [ ] Proceso vivo.
- [ ] Sin oversized.

### Punto de parada

Detenerse antes de Patched oversized.

### Checklist

- [ ] Implementada
- [ ] Automática
- [ ] Manual
- [ ] Revisada
- [ ] Commit
- [ ] Push

## 14. Fase 4 — Integración Patched

### Objetivo

Conectar `payload_too_large` con Llamada bloqueada, historial y Lab mode.

### Precondiciones

- Fase 3 validada y revisada.
- Patched acepta entradas válidas y el flujo entrante respeta el retorno `accepted`.
- Variante Patched y parser compilado identificados inequívocamente.

### Alcance autorizado

- `BlockedCallScreen`;
- historial `BLOCKED`;
- Ver detalles;
- Lab mode completo;
- recuperación UDP preservada.

### Fuera de alcance

- Entrada oversized en Vulnerable.
- RCE.
- Cambiar límites sin justificación.

### Archivos previstos

- pantallas, historial, estado y Lab mode compartidos creados en fases anteriores;
- gateway JNI compartido y mapeo del resultado nativo;
- `native-core/src/safe_parser.c`, solo para inspección y regresión salvo autorización técnica expresa.

### Riesgos

- Mostrar llamada antes del rechazo.
- Duplicar historial.
- Ocultar datos técnicos.
- Presentar Patched como seguridad absoluta.

### Validación automática

- tests de mapeo;
- tests de historial;
- cuatro builds;
- parser tests.

### Prueba manual

- válida Patched;
- oversized Patched;
- proceso vivo;
- Llamada bloqueada;
- historial;
- Ver detalles;
- búsqueda negativa en log capturado.

### Criterios de aceptación

- [ ] Rechazo.
- [ ] Sin llamada normal.
- [ ] Proceso vivo.
- [ ] Texto comprensible.
- [ ] Detalle técnico en Lab.
- [ ] Sin ejecución Vulnerable ASan + oversized.

### Punto de parada

No ejecutar Vulnerable ASan con una entrada oversized.

### Checklist

- [ ] Implementada
- [ ] Automática
- [ ] Manual
- [ ] Revisada
- [ ] Commit
- [ ] Push

## 15. Fase 5 — Operación nativa incompleta

### Objetivo

Persistir un marcador antes de JNI y mostrar un aviso prudente cuando no haya retorno.

### Precondiciones

- Fase 4 validada y revisada.
- Gateway JNI centralizado y orden de procesamiento documentado.
- Estrategia de persistencia auditada antes de adoptarla.

### Alcance autorizado

**PROPUESTA TÉCNICA PENDIENTE DE AUDITORÍA.** Se evaluará Preferences DataStore o una alternativa justificada; ninguna se considera implementada en la línea base histórica.

Si se adopta DataStore, se usará su API suspendida `edit`: actualización
read-modify-write atómica, finalización solo después de la persistencia durable
en disco y propagación de excepción si falla la transformación o la escritura.
Una coroutine apropiada esperará a que la operación complete correctamente
antes de invocar JNI, sin bloquear el hilo principal. Tras un retorno normal de
JNI se esperará también la persistencia de la limpieza del marcador.

- `pending=true` antes de JNI.
- limpiar tras retorno.
- detectar al iniciar.
- pantalla de interrupción.
- descartar aviso.
- exponer en Lab mode.

### Fuera de alcance

- entrada oversized en Vulnerable;
- informe ASan simulado;
- atribución automática.

### Archivos previstos

- gateway JNI compartido cuya ruta definitiva se establezca en las fases anteriores;
- nuevo repositorio del marcador persistente y modelos asociados, con rutas pendientes de la decisión técnica;
- Lab mode, pantalla de interrupción e historial compartidos.

### Riesgos

- falso positivo;
- escritura asíncrona no completada;
- carrera con JNI;
- marcador sin limpiar.

### Validación automática

- tests del repositorio;
- arranque con pendiente sí/no;
- limpieza tras retorno simulado;
- builds.

### Prueba manual

- simulación segura de pendiente;
- reinicio;
- aviso;
- descartar;
- Lab mode.

### Criterios de aceptación

- [ ] Persistencia fiable antes de JNI.
- [ ] Limpieza tras retorno.
- [ ] Aviso correcto.
- [ ] Sin atribución automática de `heap-buffer-overflow` o RCE.
- [ ] Sin ejecución destructiva.

### Punto de parada

Detenerse antes de cualquier prueba vulnerable.

### Checklist

- [ ] Implementada
- [ ] Automática
- [ ] Manual
- [ ] Revisada
- [ ] Commit
- [ ] Push

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

Patched ASan + oversized puede ejecutarse en las Fases 4, 7 y 8; en la Fase 8
se realizará su captura final sobre el APK congelado. La restricción de ejecución
única se aplica exclusivamente a Vulnerable ASan + oversized sobre el APK final
congelado y requiere autorización expresa.

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
- [ ] Rama publicada.
- [x] Cuatro variantes.
- [x] Parser fijado.
- [x] La separación nativa de Patched ha sido verificada en Gradle, CMake, fuentes compiladas, símbolos de la `.so` y contenido del APK; la implementación vulnerable no se empaqueta.
- [x] La separación quedó razonablemente garantizada y Fase 1 no quedó bloqueada.
- [x] Vulnerable conserva la condición deliberada.
- [ ] Conversaciones.
- [ ] Chat.
- [ ] Historial.
- [ ] Llamada saliente.
- [ ] Llamada entrante válida.
- [ ] Llamada activa.
- [ ] Llamada bloqueada.
- [ ] Marcador prudente.
- [ ] Lab mode.
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
