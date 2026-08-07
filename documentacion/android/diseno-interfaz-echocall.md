# Diseño final de interfaz y arquitectura de EchoCall Lab

> Estado del documento: **FASE 1 — implementación y validación completadas; pendiente de cierre Git**
>
> Rama de trabajo: `feature/echocall-ui`
>
> Línea base: `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c`
>
> Fecha de auditoría inicial del repositorio: 2026-08-04
>
> Fecha de revisión y consolidación documental: 2026-08-05

## Índice

1. [Propósito](#1-propósito)
2. [Alcance](#2-alcance)
3. [Línea base técnica](#3-línea-base-técnica)
4. [Decisiones sustituidas](#4-decisiones-sustituidas)
5. [Arquitectura Vulnerable/Patched](#5-arquitectura-vulnerablepatched)
6. [Variantes de compilación](#6-variantes-de-compilación)
7. [Identidades y packages](#7-identidades-y-packages)
8. [Base de código compartida](#8-base-de-código-compartida)
9. [Separación nativa](#9-separación-nativa)
10. [Modelo de datos](#10-modelo-de-datos)
11. [Contactos simulados](#11-contactos-simulados)
12. [Pantalla principal](#12-pantalla-principal)
13. [Conversación](#13-conversación)
14. [Historial](#14-historial)
15. [Llamada saliente](#15-llamada-saliente)
16. [Llamada entrante](#16-llamada-entrante)
17. [Llamada activa](#17-llamada-activa)
18. [Llamada bloqueada](#18-llamada-bloqueada)
19. [Operación nativa incompleta](#19-operación-nativa-incompleta)
20. [Lab mode](#20-lab-mode)
21. [Flujo UDP → JNI → C → UI](#21-flujo-udp--jni--c--ui)
22. [Persistencia](#22-persistencia)
23. [Tema y accesibilidad](#23-tema-y-accesibilidad)
24. [Seguridad y límites](#24-seguridad-y-límites)
25. [Extensibilidad futura](#25-extensibilidad-futura)
26. [Fases de implementación](#26-fases-de-implementación)
27. [Validaciones por fase](#27-validaciones-por-fase)
28. [Criterios de aceptación](#28-criterios-de-aceptación)
29. [Riesgos técnicos](#29-riesgos-técnicos)
30. [Fuentes](#30-fuentes)

### Convenciones de evidencia

- **CONFIRMADO:** hecho observado en el repositorio, su historial o sus
  evidencias conservadas.
- **DECISIÓN DE DISEÑO:** requisito vinculante de la evolución final.
- **PROPUESTA PENDIENTE:** solución técnica prevista, todavía sin implementar
  ni validar.
- **CRITERIO DE ACEPTACIÓN FUTURO:** propiedad que deberá demostrarse en la
  fase correspondiente.
- **MÉTODO DE VERIFICACIÓN:** comprobación prevista para acreditar un criterio.
- **CONDICIÓN DE BLOQUEO:** incumplimiento que impide continuar sin una decisión
  explícita.
- **LIMITACIÓN:** frontera expresa de lo que el laboratorio representa o puede
  demostrar.
- **RIESGO:** condición que debe comprobarse o mitigarse en una fase posterior.

## 1. Propósito

**DECISIÓN DE DISEÑO.** Evolucionar la base Android actual hacia dos
aplicaciones de comunicaciones simuladas, funcionales, instalables
simultáneamente y visualmente equivalentes:

- **EchoCall Lab — Vulnerable**;
- **EchoCall Lab — Patched**.

La diferencia de seguridad se fijará durante la compilación. No existirá un
selector de parser modificable por la persona usuaria.

El producto seguirá siendo un laboratorio académico controlado. Su propósito
es comparar dos implementaciones nativas ante la misma entrada, no reproducir
WhatsApp, construir un exploit ni acreditar ejecución remota de código.

## 2. Alcance

**DECISIÓN DE DISEÑO.** La entrega incluirá una base compartida para:

- interfaz en español y navegación;
- cinco contactos ficticios y conversaciones locales;
- mensajería simulada en memoria;
- llamada saliente simulada;
- llamada entrante derivada de un paquete ECLB aceptado;
- llamada activa sin audio;
- historial de llamadas en memoria;
- recepción UDP en `43568`;
- integración Kotlin/JNI/C;
- Lab mode separado de la experiencia normal;
- detección prudente de una operación nativa que no devolvió el control.

**FUERA DE ALCANCE / LIMITACIÓN.** No habrá backend, cuentas, telefonía del
sistema, VoIP real, audio, vídeo, cifrado real, contactos del dispositivo,
sincronización, adjuntos, notificaciones remotas, publicación de APK, firma de
distribución ni conexión a servicios de Internet.

**FASE 0.** Este documento es únicamente un contrato de diseño. En esta fase no
se modifica ni se ejecuta código Android, Gradle, CMake, C, JNI, scripts o
evidencias.

## 3. Línea base técnica

### 3.1 Punto de partida Git

**CONFIRMADO.** La auditoría del 2026-08-04 se realizó en:

| Elemento | Valor observado |
|---|---|
| Rama | `feature/echocall-ui` |
| `HEAD` | `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c` |
| Upstream | `origin/feature/echocall-ui` |
| Divergencia con el upstream propio | `0 0` |
| Divergencia `HEAD...origin/feature/native-core` | `0 0` |

La rama `feature/echocall-ui` ya existía local y remotamente, apuntaba al
commit requerido y no fue reconstruida. La rama protegida
`feature/native-core` no se ha reescrito ni alterado.

### 3.2 Commits protegidos

**CONFIRMADO.** La línea base publicada queda definida por:

- `3bcceb36748aaf385dfa6c4b8e43b0a213767de4`, **Harden UDP ingress and
  recovery**: recepción UDP, ciclo de vida, cola FIFO y recuperación;
- `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c`, **Document Android UDP and
  ASan evidence**: consolidación documental y artefactos E-021–E-025.

Ambos commits y `feature/native-core` son base estable: no se modificarán ni
reescribirán. Durante las fases de esta evolución no se utilizarán como destino
de merges; esta es una política del flujo de trabajo del laboratorio, no una
limitación intrínseca de Git.

### 3.3 Arquitectura Android observada

**CONFIRMADO.** El punto de partida tiene:

- un proyecto Android con un único módulo `:app`;
- `namespace = "com.echocall.lab"` y
  `applicationId = "com.echocall.lab"`;
- un build type `asan`, derivado de `debug`, con sufijo `.asan`, ABI
  `x86_64`, runtime ASan y `wrap.sh`;
- un build type `release` sin minificación, aunque no forma parte de la entrega
  final solicitada;
- una única Activity Compose;
- Material 3 disponible, pero un tema de Manifest fijado a
  `Theme.Material.Light.NoActionBar`;
- un `MainActivity.kt` monolítico que mezcla UI, estado, carga de assets,
  selección del parser, despacho JNI, eventos y coordinación UDP;
- un selector runtime `SAFE`/`VULNERABLE` mediante `MutableStateFlow` y
  `RadioButton`;
- un receptor `UdpPacketReceiver` encapsulado, asociado al ciclo
  `onStart`/`onStop`, con una escucha, cola FIFO, `Retry` y tratamiento de
  errores recuperables;
- puerto único `43568/UDP`;
- una clase `NativeBridge` con dos entradas JNI públicas para parsear:
  `parsePacket()` y `parsePacketVulnerable()`;
- una única biblioteca `libechocall_native.so` que compila a la vez
  `safe_parser.c` y `vulnerable_parser.c`;
- dos assets locales: control válido y muestra oversized.

### 3.4 Parsers y evidencia

**CONFIRMADO.** `safe_parser.c` valida que la longitud declarada no supere 32
bytes antes de la copia. `vulnerable_parser.c` conserva deliberadamente una
reserva de 32 bytes y copia `declared_length` sin validar ese máximo.

**CONFIRMADO.** E-022 conserva una única ejecución UDP/VULNERABLE/ASan con
`heap-buffer-overflow`, escritura de 64 bytes, región de 32 bytes, `ABORTING` y
`SIGABRT`. E-025 conserva la comparación UDP/SAFE/ASan: devuelve
`payload_too_large` y el proceso observado permanece vivo.

**LIMITACIÓN.** E-022 y E-025 pertenecen a la aplicación anterior al rediseño.
No serán presentadas como evidencia de los futuros APK finales. E-022 no
conserva el APK histórico exacto; E-025 sí conserva una cadena de custodia más
completa. Ninguna acredita RCE, control del flujo o equivalencia exacta con
CVE-2019-3568.

### 3.5 Limitaciones estructurales del punto de partida

**CONFIRMADO EN LA AUDITORÍA INICIAL.** La estructura observada el 2026-08-04
todavía no satisfacía el contrato final:

1. solo existen dos identidades instalables (`debug` y `asan`), no cuatro;
2. ambos parsers se empaquetan en la misma biblioteca;
3. la implementación se selecciona en runtime;
4. la UI técnica aparece en la pantalla principal;
5. el estado `showIncomingCall`, el evento `CALL_INCOMING` y una tarjeta de
   llamada simulada dentro del único composable pueden aparecer antes del
   retorno del parser; la futura `IncomingCallScreen` todavía no existe;
6. no hay navegación entre las once responsabilidades de pantalla;
7. no hay modelos de contacto, mensaje o historial;
8. no hay marcador persistente de retorno JNI incompleto;
9. el tema del Manifest fuerza una base clara;
10. los eventos técnicos y el estado de aplicación están ligados a un único
    composable.

**CONFIRMADO.** La línea base no contiene navegación ni una pantalla
`IncomingCallScreen`: `showIncomingCall` controla una `Card` dentro de
`EchoCallLabScreen`. En la ruta UDP, ese estado y los eventos `CALL_INCOMING`,
`CONTROL_PACKET_RECEIVED` y `NATIVE_PARSE_STARTED` se establecen antes de que
JNI devuelva el resultado.

**DECISIÓN DE DISEÑO.** La futura `IncomingCallScreen` solo se mostrará después
de un retorno `status=accepted code=ok`.

**RIESGO.** El rediseño deberá impedir que el estado visual previo o el nombre
histórico `CALL_INCOMING` provoquen navegación anticipada.

## 4. Decisiones sustituidas

**DECISIÓN DE DISEÑO.** Las siguientes decisiones reemplazan cualquier
propuesta previa incompatible:

| Se descarta | Se adopta |
|---|---|
| una app con selector `SAFE`/`VULNERABLE` | dos apps con parser fijo al compilar |
| nombre de producto “Safe” | nombre de producto “Patched” |
| `com.echocall.lab` y `com.echocall.lab.asan` como identidades finales | cuatro `applicationId` explícitos |
| ambos parsers dentro de toda `.so` | parser fijado al compilar; exclusión del parser vulnerable en Patched verificada en Fase 1 |
| laboratorio técnico como pantalla inicial | lista normal de conversaciones |
| controles de oversized dentro de la UI | oversized solo mediante emisor UDP externo |
| caller genérico `EchoCall Test` | Marta Soler como asociación visual del simulador |
| mostrar llamada antes de conocer el resultado nativo | navegar a llamada entrante solo tras `accepted` |
| tema claro forzado | Material 3 siguiendo claro/oscuro del sistema |
| E-022/E-025 como posible evidencia final | nueva evidencia solo tras congelar APK finales |

**LIMITACIÓN.** Marta Soler no se extrae de ECLB. Es una asociación visual
determinista del simulador; ECLB no contiene un nombre de contacto.

## 5. Arquitectura Vulnerable/Patched

**CONFIRMADO EN FASE 1.** Se mantiene un único módulo de aplicación Android
`:app` que genera cuatro variantes, con dos identidades de producto
Vulnerable/Patched y una Activity compartida. La estructura futura prevista es:

```text
MainActivity (ciclo de vida UDP)
└── EchoCallApp / NavHost
    ├── UI compartida (pantallas Material 3)
    ├── AppStateHolder o ViewModel
    │   ├── repositorio simulado en memoria
    │   ├── coordinador de llamadas
    │   ├── estado UDP y eventos de laboratorio
    │   └── repositorio del marcador persistente
    └── NativeGateway
        └── NativeBridge.parsePacket() -> libechocall_native.so
            └── parser único fijado por flavor/CMake
```

Se aplicará flujo unidireccional: el estado desciende hacia los composables y
las acciones de usuario o eventos UDP ascienden al state holder. La UI no
elegirá el parser ni invocará directamente dos rutas alternativas.

**DECISIÓN DE DISEÑO.** No se crearán dos módulos app ni dos copias manuales.
Las diferencias por flavor se limitarán a identidad, recursos informativos y
selección nativa.

## 6. Variantes de compilación

### 6.1 Matriz implementada

**CONFIRMADO EN FASE 1.** Se añadió la dimensión `security`:

- flavors: `vulnerable`, `patched`;
- build types de entrega del laboratorio: `debug`, `asan`.

| Variante Gradle | Parser | Instrumentación | ABI validada |
|---|---|---|---|
| `vulnerableDebug` | VULNERABLE | no ASan | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` |
| `vulnerableAsan` | VULNERABLE | ASan | `x86_64` |
| `patchedDebug` | PATCHED | no ASan | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` |
| `patchedAsan` | PATCHED | ASan | `x86_64` |

**CONFIRMADO EN FASE 1.** Cada flavor pasa a CMake un valor cerrado,
`-DECHOCALL_PARSER_IMPLEMENTATION=VULNERABLE` o `PATCHED`. El build type `asan`
mantiene `-DENABLE_ANDROID_ASAN=ON`.

**CONFIRMADO EN FASE 1.** Las variantes `release` quedaron deshabilitadas con
`androidComponents.beforeVariants` en Android Gradle Plugin 8.12.2. La matriz de
aplicación queda limitada a las cuatro variantes anteriores.

**LIMITACIÓN.** ASan se mantiene aquí por continuidad experimental con la línea
base x86_64. La documentación oficial del NDK indica que ASan está obsoleto y
no soportado desde 2023: todavía puede utilizarse, pero podría contener errores.
HWASan es la recomendación actual en entornos ARM64 compatibles. Migrar de
instrumentación no forma parte de esta evolución.

## 7. Identidades y packages

**DECISIÓN DE DISEÑO.** Identidades finales:

| Variante | Nombre instalado | `applicationId` |
|---|---|---|
| `vulnerableDebug` | EchoCall Lab — Vulnerable | `com.echocall.lab.vulnerable` |
| `patchedDebug` | EchoCall Lab — Patched | `com.echocall.lab.patched` |
| `vulnerableAsan` | EchoCall Lab — Vulnerable ASan | `com.echocall.lab.vulnerable.asan` |
| `patchedAsan` | EchoCall Lab — Patched ASan | `com.echocall.lab.patched.asan` |

Los cuatro IDs permiten instalación simultánea. Compartirán el namespace
Kotlin `com.echocall.lab`, de modo que el código y los nombres JNI permanezcan
comunes; `applicationId` seguirá siendo la identidad de instalación.

**CONFIRMADO EN FASE 1.** Cada flavor define su `applicationId` base y el build
type conserva `.asan` como sufijo. Los nombres Debug se resuelven mediante
recursos de flavor y los nombres ASan completos mediante los source sets de
variante `vulnerableAsan` y `patchedAsan`.

## 8. Base de código compartida

**CONFIRMADO EN FASE 1.** El código Kotlin, JNI/C y la UI existentes permanecen
compartidos en `src/main`. En las fases posteriores también residirá allí:

- Activity, tema, navegación y pantallas;
- modelos y datos ficticios;
- state holder/ViewModel y repositorios en memoria;
- receptor UDP y coordinador de paquetes;
- contrato Kotlin del gateway nativo;
- modelos de resultado nativo y eventos;
- persistencia del marcador;
- recursos visuales comunes y avatares locales.

Los source sets `src/vulnerable` y `src/patched` contendrán solo recursos o
constantes inevitables de variante. Los source sets de build type conservarán
solo lo relativo a instrumentación. No habrá forks de pantallas, contactos,
navegación o lógica normal.

**PROPUESTA PENDIENTE.** Organización lógica mínima, sin crear módulos
prematuramente:

```text
com.echocall.lab
├── data        # datos ficticios, sesión y marcador
├── model       # Contact, Message, CallRecord, resultados y estados
├── native      # NativeBridge y NativeGateway
├── udp         # UdpPacketReceiver y modelos de ingreso
└── ui
    ├── navigation
    ├── screens
    ├── components
    └── theme
```

## 9. Separación nativa

### 9.1 Problema observado en la línea base

**CONFIRMADO EN LA LÍNEA BASE DEL 2026-08-04.** El CMake Android enumeraba
simultáneamente `safe_parser.c` y `vulnerable_parser.c`; `native_bridge.c`
incluía ambos headers y exportaba dos rutas JNI. Por ello, cambiar la selección
en Kotlin no eliminaba del APK la implementación no usada.

### 9.2 Separación implementada

**CONFIRMADO EN FASE 1.** CMake:

1. exige `ECHOCALL_PARSER_IMPLEMENTATION` con uno de dos valores cerrados;
2. elige una única fuente: `safe_parser.c` para PATCHED o
   `vulnerable_parser.c` para VULNERABLE;
3. compila siempre los componentes comunes (`native_bridge.c`,
   `parser_result.c` y headers comunes);
4. define una macro privada inequívoca para el bridge;
5. falla en configuración ante un valor ausente o desconocido;
6. mantiene ASan como dimensión ortogonal del build type.

El bridge expone una sola función `parsePacket(ByteArray)` y
`getCompiledParserImplementation()` como consulta de solo lectura. Mediante
compilación condicional, esa única ruta llama a `safe_parse_packet` o
`vulnerable_parse_packet`.

```text
vulnerable flavor ── CMake VULNERABLE ── native_bridge + vulnerable_parser
patched flavor    ── CMake PATCHED    ── native_bridge + safe_parser
```

**DECISIÓN DE DISEÑO.** Patched y Vulnerable tendrán un único parser fijado al
compilar y no ofrecerán selección de implementación en runtime.

**VALIDADO EN FASE 1.** Las diez instancias de `libechocall_native.so` se
correlacionaron desde los cuatro APK hasta sus intermediarios, configuración
CMake, comandos de compilación y objetos enlazados. Vulnerable contiene
únicamente `vulnerable_parser.c`; Patched contiene únicamente `safe_parser.c`.
En las bibliotecas no stripped está presente el símbolo esperado y ausente el
contrario. Los tres JNI actuales están presentes y
`parsePacketVulnerable` está ausente. Debug no presenta instrumentación ASan y
ASan sí está instrumentado y empaqueta runtime y `wrap.sh`.

**LIMITACIÓN.** La separación binaria acredita la composición de los
artefactos; no demuestra explotabilidad, seguridad general ni ausencia de
otras vulnerabilidades.

**RIESGO.** Los nombres JNI actuales están codificados con el namespace
`com_echocall_lab`. Mantener ese namespace evita multiplicar símbolos por
`applicationId`. Cualquier cambio del package Kotlin exigirá auditar la carga
JNI antes de implementarlo.

## 10. Modelo de datos

**PROPUESTA PENDIENTE.** Modelos compartidos:

| Modelo | Campos mínimos |
|---|---|
| `Contact` | `id`, `displayName`, `avatarResource`, `fallbackInitials`, `preview`, `simulatedTimeLabel` |
| `Message` | `id`, `contactId`, `sender`, `body`, `simulatedOrder`, `simulatedTimeLabel` |
| `CallRecord` | `id`, `contactId`, `direction`, `outcome`, `startedAt`, `endedAt`, `durationSeconds`, `scenarioId` |
| `NativeParseResult` | `implementation`, `status`, `code`, campos ECLB opcionales, `checksum` derivado opcional y texto original controlado |
| `LabEvent` | `id` técnico exacto, etiqueta española, timestamp monotónico/real y detalles permitidos |
| `UdpState` | estado, puerto, última fuente, tamaño, error recuperable y disponibilidad de retry |
| `NativeOperationMarker` | `pending`, variante, origen, longitud, timestamp, `scenarioId` |
| `EchoCallUiState` | sección/ruta, contactos, mensajes de sesión, historial, llamada, UDP, resultado y eventos |

Enums previstos:

- `MessageSender`: `CONTACT`, `USER`;
- `CallDirection`: `INCOMING`, `OUTGOING`;
- `CallOutcome`: `COMPLETED`, `REJECTED`, `MISSED`, `BLOCKED`, `INTERRUPTED`,
  `CANCELLED`;
- `ParserImplementation`: `VULNERABLE`, `PATCHED`, solo lectura;
- estados de llamada: `IDLE`, `DIALING`, `INCOMING`, `ACTIVE`, `BLOCKED`,
  `INTERRUPTED`.

**PROPUESTA PENDIENTE.** El autómata mantendrá dirección, estado y resultado
como conceptos separados:

- `DIALING → ACTIVE → COMPLETED` para una llamada saliente completada;
- `DIALING → CANCELLED` si la llamada saliente se finaliza antes de activarse;
- `INCOMING → ACTIVE → COMPLETED` después de aceptar y finalizar;
- `INCOMING → REJECTED` al rechazar y `INCOMING → MISSED` si expira sin acción;
- un rechazo nativo Patched produce `BLOCKED` y una operación anterior sin
  retorno produce `INTERRUPTED`.

**CONFIRMADO.** El header ECLB solo contiene `MAGIC`, `VERSION`, `FLAGS`,
`PACKET_TYPE`, `LENGTH` y `SSRC`, seguido del payload. El campo C
`parser_result.checksum` es un valor derivado: `safe_parse_packet()` y
`vulnerable_parse_packet()` suman los bytes del payload y almacenan el resultado
módulo 256; `native_bridge.c` lo devuelve como `checksum=<valor>` únicamente
cuando el parseo resulta aceptado. No es un campo del header ECLB. La línea base
Kotlin no tiene todavía un modelo `NativeParseResult`; muestra la cadena devuelta
por JNI.

**DECISIÓN DE DISEÑO.** Las marcas de tiempo visibles de los datos precargados
serán simuladas y deterministas. Las duraciones de llamadas de la sesión podrán
usar un reloj monotónico para no depender de cambios del reloj de pared.

## 11. Contactos simulados

**DECISIÓN DE DISEÑO.** Contactos ficticios y estables:

| ID propuesto | Nombre | Vista previa | Tiempo simulado |
|---|---|---|---|
| `marta_soler` | Marta Soler | Te llamo cuando salga. | 12:18 |
| `pau_ferrer` | Pau Ferrer | ¿Quedamos a las siete? | 11:42 |
| `lucia_navarro` | Lucía Navarro | Te he enviado las fotos. | Ayer |
| `dani_campos` | Dani Campos | Luego hablamos con calma. | Ayer |
| `irene_vidal` | Irene Vidal | ¡Gracias! | Lun |

Los avatares serán recursos locales ficticios, coherentes visualmente y sin
dependencias de red. El modelo aceptará una referencia de recurso opcional y
siempre tendrá iniciales de respaldo (`MS`, `PF`, `LN`, `DC`, `IV`). Así podrá
incorporar avatares ilustrados posteriores sin cambiar el contrato de datos.

**LIMITACIÓN.** No se usarán fotografías, nombres, teléfonos, correos ni otros
datos reales. Marta Soler se asocia al escenario UDP por decisión del
simulador, no por contenido del paquete.

## 12. Pantalla principal

**DECISIÓN DE DISEÑO.** `ConversationsScreen` será el destino inicial. Mostrará:

- barra superior con título **EchoCall**;
- acción visible **Historial**;
- menú de tres puntos con **Lab mode**, **Acerca de EchoCall** y
  **Restablecer datos simulados**;
- lista de los cinco contactos con avatar/iniciales, nombre, último mensaje y
  hora o fecha simulada;
- distintivo discreto **Vulnerable** o **Patched**, y **ASan** cuando proceda.

No mostrará JNI, UDP, ASan, offsets, heap, `memcpy`, `payload_too_large`, CVSS
ni controles de explotación.

## 13. Conversación

**DECISIÓN DE DISEÑO.** `ChatScreen(contactId)` incluirá volver, avatar, nombre,
acción de llamada, mensajes precargados, campo de texto y enviar.

Para Marta Soler se precargará, como mínimo:

1. Marta: «¿Has llegado ya?»
2. Tú: «Sí, acabo de llegar.»
3. Marta: «Perfecto, te llamo cuando salga.»

Los mensajes nuevos se añadirán localmente a la conversación durante la
sesión. No se enviarán por red ni se conservarán tras finalizar el proceso.

**LIMITACIÓN.** La mensajería es contexto visual de una app de comunicaciones;
no representa el vector de CVE-2019-3568.

## 14. Historial

**DECISIÓN DE DISEÑO.** `CallHistoryScreen` será accesible desde la barra
superior y mostrará avatar, contacto, dirección y resultado como campos
separados, fecha/hora simulada o de sesión y duración cuando exista.

La dirección será `INCOMING` u `OUTGOING`. El resultado será `COMPLETED`,
`REJECTED`, `MISSED`, `BLOCKED`, `INTERRUPTED` o `CANCELLED`. Los registros se
mantendrán solo en memoria de la sesión, salvo la reconstrucción puntual de un
registro interrumpido desde el marcador persistente al reiniciar.

No habrá agenda del sistema, telefonía real ni estadísticas avanzadas.

## 15. Llamada saliente

**DECISIÓN DE DISEÑO.** Desde `ChatScreen`, la acción de teléfono navegará a
`OutgoingCallScreen(contactId)`:

```text
teléfono → “Llamando…” → transición determinista → llamada activa
         → Finalizar → registro en historial
```

La pantalla mostrará avatar, nombre, estado y **Finalizar**. La transición se
controlará con un temporizador local y cancelable asociado al ciclo de vida.
Finalizar durante `DIALING` registrará dirección `OUTGOING` y resultado
`CANCELLED`; finalizar después de llegar a `ACTIVE` registrará `OUTGOING` y
`COMPLETED`.

**LIMITACIÓN.** Esta acción no enviará UDP, no invocará JNI y no ejecutará la
vulnerabilidad.

## 16. Llamada entrante

**DECISIÓN DE DISEÑO.** `IncomingCallScreen` solo se mostrará cuando el parser
nativo haya devuelto `status=accepted code=ok` para un datagrama UDP válido.
Mostrará a Marta Soler, **Llamada entrante**, **Aceptar** y **Rechazar**.

- **Aceptar:** abre `ActiveCallScreen` sin volver a ejecutar el parser.
- **Rechazar:** cierra la llamada y añade `REJECTED` al historial, sin volver a
  ejecutar el parser.

La dirección registrada será `INCOMING`. Una llamada aceptada y posteriormente
finalizada tendrá resultado `COMPLETED`; una llamada que expire sin acción tendrá
resultado `MISSED`.

El evento técnico `CALL_INCOMING` podrá conservarse como indicación de señal
recibida, pero no autoriza la navegación antes del retorno `accepted`.

## 17. Llamada activa

**DECISIÓN DE DISEÑO.** `ActiveCallScreen` será común a llamadas salientes y
entrantes aceptadas. Mostrará avatar, nombre, contador de duración, **Silenciar**,
**Altavoz** y **Finalizar**.

Silenciar y Altavoz modificarán solo su estado visual y su descripción
accesible. El contador avanzará mientras el estado sea activo. Finalizar
detendrá el contador, navegará fuera de la llamada y registrará la duración.

**LIMITACIÓN.** No existe audio, conexión VoIP, vídeo, teclado numérico ni
llamada del sistema operativo.

## 18. Llamada bloqueada

**DECISIÓN DE DISEÑO.** En Patched, un retorno
`status=rejected code=payload_too_large` procedente de UDP abrirá
`BlockedCallScreen` y añadirá `BLOCKED` al historial.

Texto normal:

> **Llamada bloqueada**
>
> EchoCall ha rechazado automáticamente una señal de llamada no válida antes de
> cualquier acción del usuario.

Acciones: **Volver** y **Ver detalles**. Ver detalles abrirá Lab mode.

La pantalla normal no mostrará longitudes, `heap-buffer-overflow` o `memcpy`.
No afirmará que la aplicación sea completamente segura.

**LIMITACIÓN.** En Vulnerable ASan, la muestra oversized puede terminar el
proceso antes de que Kotlin reciba un resultado. No puede mostrarse una
pantalla normal posterior dentro de ese proceso. Vulnerable Debug tampoco
garantiza una manifestación concreta.

## 19. Operación nativa incompleta

### 19.1 Semántica

**DECISIÓN DE DISEÑO.** Justo antes de entrar en JNI se persistirá:

```text
pending = true
variant = VULNERABLE|PATCHED
origin = UDP
packetLength = <bytes>
timestamp = <instante>
scenarioId = voip_control_packet
```

Solo tras un retorno normal de JNI se persistirá `pending = false`. Si al
arrancar permanece `true`, se construirá un registro de sesión
`INTERRUPTED` y se mostrará `InterruptedProcessingScreen`:

> **Procesamiento anterior interrumpido**
>
> La ejecución anterior no devolvió el control desde el componente nativo.
>
> Consulta Lab mode y el informe instrumental para determinar la causa.

Acciones: **Abrir Lab mode**, **Descartar aviso** y **Volver a la aplicación**.
Descartar limpiará explícitamente el marcador pendiente.

### 19.2 Interpretación prudente

**LIMITACIÓN.** El marcador solo acredita que una llamada JNI marcada no
registró un retorno normal. No atribuye por sí solo la causa a ASan,
`heap-buffer-overflow`, CVE-2019-3568, explotación o RCE. La atribución requiere
evidencia instrumental externa.

**DECISIÓN DE DISEÑO.** En FASE 5, el gateway escribirá el marcador desde una
coroutine apropiada y esperará a que la persistencia termine correctamente
antes de entrar en JNI, sin bloquear el hilo principal. Tras un retorno normal
de JNI esperará también la limpieza persistente del marcador. Debe verificarse
que no quedan falsos positivos por excepciones Kotlin previas a la entrada real;
el límite del marcador se situará lo más cerca posible de la llamada externa.

## 20. Lab mode

**DECISIÓN DE DISEÑO.** `LabModeScreen` estará separado y contendrá:

### A. Escenario

- `VoIP control packet processing`;
- `Inspired by CVE-2019-3568`;
- laboratorio sintético, no WhatsApp real;
- ECLB no es RTCP real;
- no se reconstruye el root cause privado del producto.

### B. Aplicación

- nombre de variante, `applicationId`, build type y ABI;
- Debug o ASan e instrumentación;
- estado JNI conectado/error.

### C. Implementación del parser

- **VULNERABLE** o **PATCHED**, solo lectura;
- **Fixed at build time**;
- sin selector.

### D. UDP

- estado, puerto `43568`, última fuente y tamaño;
- último error recuperable;
- **Retry UDP receiver** cuando proceda.

### E. Último resultado

Cuando JNI haya retornado: implementation, status, code, version, flags, type,
declared length, actual length, maximum y SSRC. En resultados aceptados podrá
mostrarse `checksum`, identificado expresamente como suma derivada del payload
módulo 256 calculada por el parser, nunca como campo del header ECLB.

### F. Eventos

**CONFIRMADO.** Los IDs existentes auditados son:

- `CALL_INCOMING`;
- `CONTROL_PACKET_RECEIVED`;
- `NATIVE_PARSE_STARTED`;
- `NATIVE_PARSE_OK`;
- `PACKET_REJECTED`;
- `NATIVE_PARSE_ERROR`.

Además existe `PACKET_REJECTED_INVALID_LENGTH` en la simulación local anterior,
pero no en la ruta UDP consolidada. Antes de conservar, retirar o especializar
ese ID se auditarán sus consumidores. Lab mode mostrará una etiqueta española,
el ID técnico exacto y el orden temporal; los códigos no se renombrarán sin una
decisión explícita.

### G. Estado de aplicación

- sección actual;
- último resultado visible;
- operación anterior incompleta;
- Marta Soler como contacto asociado por el simulador.

### H. Limitaciones

- laboratorio controlado;
- sin audio, backend o telefonía real;
- sin RCE ni control del flujo demostrados;
- sin equivalencia exacta con CVE-2019-3568.

Acciones permitidas: **Simular llamada válida**, **Procesar muestra válida**,
**Retry UDP receiver** y **Restablecer datos simulados**. No existirá ningún
botón de overflow, explotación o RCE. La muestra oversized se enviará solo con
el emisor UDP externo y en las fases autorizadas.

## 21. Flujo UDP → JNI → C → UI

### 21.1 Entrada válida

**DECISIÓN DE DISEÑO.** Orden causal:

```text
Datagrama UDP en 43568
→ UdpPacketReceiver copia bytes y fuente
→ cola FIFO / coordinador de ingreso
→ registra eventos técnicos
→ persiste NativeOperationMarker(pending=true)
→ NativeGateway llama a NativeBridge.parsePacket()
→ libechocall_native.so llama al único parser compilado
→ retorna accepted
→ persiste pending=false
→ actualiza resultado/eventos
→ navega a IncomingCallScreen(Marta Soler)
→ la persona acepta o rechaza
```

El procesamiento técnico termina antes de mostrar las acciones Aceptar y
Rechazar.

### 21.2 Oversized en Patched

```text
UDP → marcador pending=true → JNI → safe_parse_packet
→ payload_too_large → marcador pending=false
→ BlockedCallScreen → historial BLOCKED
```

### 21.3 Oversized en Vulnerable

```text
UDP → marcador pending=true → JNI → vulnerable_parse_packet
→ posible escritura fuera de límites
→ si no hay retorno: el proceso puede terminar y pending permanece true
→ en el próximo inicio: InterruptedProcessingScreen
```

**LIMITACIÓN.** Si Vulnerable Debug retorna normalmente pese a la conducta
indefinida, el marcador se limpiará y la UI tratará el resultado realmente
devuelto. No se inventará un crash ni una animación de explotación.

### 21.4 UDP y ciclo de vida

**DECISIÓN DE DISEÑO.** Se conservarán puerto `43568`, una única escucha,
inicio/parada ligados a la Activity, cola FIFO, error `EADDRINUSE` recuperable y
un solo **Retry UDP receiver**. La redirección host→emulador seguirá siendo una
configuración externa. Los cuatro packages pueden coexistir instalados, pero las
pruebas funcionales se ejecutan secuencialmente: solo una aplicación permanece
activa y escuchando en `43568/UDP`, y se fuerza la detención de la anterior antes
de iniciar la siguiente. Este procedimiento se verificó en Fase 1.

## 22. Persistencia

**PROPUESTA PENDIENTE.** Política:

| Información | Persistencia |
|---|---|
| contactos, conversaciones y previews precargadas | código/recursos locales |
| avatares | recursos locales |
| mensajes nuevos | memoria de la sesión |
| historial | memoria de la sesión |
| mute, altavoz y llamada activa | memoria/estado de UI |
| configuración del parser | compilación; nunca almacenamiento runtime |
| marcador nativo incompleto | Preferences DataStore privado por app |

Preferences DataStore se propone por ser clave-valor, pequeño, transaccional y
compatible con coroutines. La API suspendida `edit` realiza una operación
read-modify-write atómica; la coroutine completa después de que los datos se
hayan persistido duraderamente en disco y lanza una excepción si falla la
transformación o la escritura. Una coroutine apropiada esperará esa finalización
correcta antes de invocar JNI, sin bloquear el hilo principal; no bastará con
lanzar una escritura asíncrona y continuar inmediatamente. Tras el retorno
normal se esperará igualmente la persistencia de `pending=false`.

**DECISIÓN DE DISEÑO.** No se introducirá Room. Cada `applicationId` tendrá su
propio sandbox y marcador. **Restablecer datos simulados** reiniciará mensajes e
historial de sesión, pero no borrará silenciosamente un marcador pendiente; la
acción explícita **Descartar aviso** será responsable de ello.

**RIESGO.** La semántica de durabilidad y los fallos de escritura se validarán
antes de depender del marcador. Si DataStore no puede confirmar la escritura,
no se invocará JNI y Lab mode mostrará un error técnico prudente.

## 23. Tema y accesibilidad

**PROPUESTA PENDIENTE.** Crear un tema Material 3 Compose con esquemas claro y
oscuro seleccionados mediante `isSystemInDarkTheme()`, y sustituir el tema claro
forzado del Manifest por una base DayNight compatible.

Requisitos:

- no codificar colores directamente en pantallas;
- no diferenciar Vulnerable/Patched solo mediante rojo/verde;
- usar texto y distintivos discretos además del color;
- tamaños táctiles Material, contraste suficiente y soporte de font scale;
- `contentDescription` para iconos accionables y estado descriptivo para mute
  y altavoz;
- orden de foco coherente y agrupación semántica de avatar/nombre/resumen;
- navegación atrás consistente;
- recomposición sin duplicar temporizadores, eventos o llamadas JNI;
- revisión en tema claro/oscuro, rotación y tamaños compactos.

Los avatares decorativos podrán ocultarse del árbol semántico cuando el nombre
adyacente ya identifique al contacto; los avatares accionables deberán tener
descripción.

## 24. Seguridad y límites

**DECISIÓN DE DISEÑO.** La vulnerabilidad deliberada se conserva sin ampliar su
peligrosidad. La aplicación vulnerable debe funcionar con entradas válidas y
activar la condición insegura solo con la muestra malformada preparada para el
laboratorio.

No se añadirá:

- ejecución de comandos, shellcode o persistencia maliciosa;
- acceso a datos externos;
- red real fuera del laboratorio;
- control del flujo, payload RCE o mecanismos de terceros;
- afirmaciones de que el dispositivo fue comprometido;
- estética de hacking, CTF o marcas comerciales ajenas.

**LIMITACIÓN.** ECLB es un formato sintético de 13 bytes de cabecera y máximo
semántico de 32 bytes. No es RTCP real. El laboratorio reproduce de forma
controlada el patrón abstracto de procesamiento automático, validación de
longitud y escritura fuera de límites; no el protocolo, heap, paquete, exploit
o root cause privado de CVE-2019-3568.

**REQUISITOS FUTUROS, NO IMPLEMENTAR AHORA:** `LAB_SAFETY.md`, `SECURITY.md`,
`LICENSE`, advertencia de vulnerabilidad deliberada, uso solo en emulador o
dispositivo dedicado, no exposición del puerto, hashes y procedimientos de
compilación/reproducción.

No se publicarán APK ni GitHub Releases y no se firmarán builds de distribución.

## 25. Extensibilidad futura

**PROPUESTA PENDIENTE.** Identificar el escenario actual mediante el ID estable
`voip_control_packet` y evitar que la navegación principal dependa del literal
`CVE-2019-3568`. `LabModeScreen` recibirá un modelo de escenario, aunque en esta
entrega solo exista uno.

Esto permite añadir en el futuro información de otros escenarios sin cambiar
los modelos de conversaciones o llamadas.

**FUERA DE ALCANCE.** No se crearán ahora plugins, escenarios vacíos, CVE no
investigados, procesadores de imágenes, previsualizaciones reales ni adjuntos
vulnerables.

## 26. Fases de implementación

Cada fase se implementará sola, se validará, mostrará su diff y pruebas
manuales, y se detendrá hasta recibir validación. Commit y push requerirán
autorización expresa.

### Fase 0 — Diseño y planificación

- auditar la línea base, las evidencias y las fuentes oficiales;
- consolidar y revisar este diseño y el plan de implementación;
- no modificar ni ejecutar código;
- **estado: completada**.

### Fase 1 — Arquitectura de variantes

- introducir flavors Vulnerable/Patched y cruzarlos con Debug/ASan;
- fijar `applicationId` y nombres;
- seleccionar un único parser mediante Gradle/CMake/JNI;
- conservar UDP `43568`;
- eliminar el selector runtime, sin rediseñar todavía toda la UI.
- **estado: implementación y validación completadas; pendiente únicamente de
  cierre Git mediante el commit autorizado**.

### Fase 2 — Modelos y navegación

- crear modelos, datos ficticios y estado de sesión;
- crear tema, navegación, principal, conversación, historial y Lab mode inicial;
- no conectar todavía todos los flujos UDP a las nuevas pantallas.
- **estado: pendiente**.

### Fase 3 — Mensajería y llamadas normales

- implementar saliente, entrante válida, aceptar, rechazar y activa;
- contador, mute, altavoz, finalizar e historial;
- integrar solo control válido UDP; ninguna oversized.
- **estado: pendiente**.

### Fase 4 — Integración Patched

- mapear `payload_too_large` a llamada bloqueada e historial;
- completar detalles de Lab mode;
- conservar la recuperación UDP;
- probar oversized únicamente en Patched.
- **estado: pendiente**.

### Fase 5 — Operación nativa incompleta

- persistir `pending` antes de JNI y limpiar tras retorno;
- detectar el marcador al reiniciar;
- mostrar aviso, Lab mode y descarte prudentes;
- validar con simulación no corruptora cuando sea posible;
- no ejecutar una entrada oversized en Vulnerable.
- **estado: pendiente**.

### Fase 6 — Visual y accesibilidad

- tema claro/oscuro, contraste, tamaños, descripciones y foco;
- rotación, recomposición, estados vacíos, textos e iconos;
- equivalencia visual entre Vulnerable y Patched.
- **estado: pendiente**.

### Fase 7 — Congelación y regresión no destructiva

- builds desde estado limpio sin borrar artefactos ajenos;
- muestra válida en ambas apps, oversized solo Patched;
- ciclo de vida, `EADDRINUSE`, historial, Lab mode y marcador simulado;
- fijar los APK candidatos y sus hashes.
- **estado: pendiente**.

### Fase 8 — Evidencia final y única ejecución vulnerable

- realizar una captura final Patched ASan + oversized sobre el APK congelado;
- ejecutar Vulnerable ASan + oversized sobre el APK final congelado una única
  vez y solo con autorización expresa;
- mantener explícito que Patched ASan + oversized también puede ejecutarse en
  las Fases 4 y 7, sin aplicar a Patched la restricción de ejecución única;
- hashes previos de APK y muestra, PID antes/después, log completo,
  simbolización y comparación;
- no reutilizar E-022/E-025 como evidencia de los APK finales.
- **estado: pendiente**.

## 27. Validaciones por fase

### Fase 0 — Diseño y planificación

Automáticas: estructura Markdown, tablas, cercos de código, enlaces internos,
terminología, coherencia cruzada y `git diff --check`. Manuales: no aplica.
**Estado: completada.**

### Fase 1 — Arquitectura de variantes

**VALIDADA.** Se construyeron las cuatro variantes:

- `assembleVulnerableDebug`;
- `assembleVulnerableAsan`;
- `assemblePatchedDebug`;
- `assemblePatchedAsan`;
- `vulnerableDebug`: `com.echocall.lab.vulnerable`, **EchoCall Lab —
  Vulnerable**;
- `vulnerableAsan`: `com.echocall.lab.vulnerable.asan`, **EchoCall Lab —
  Vulnerable ASan**;
- `patchedDebug`: `com.echocall.lab.patched`, **EchoCall Lab — Patched**;
- `patchedAsan`: `com.echocall.lab.patched.asan`, **EchoCall Lab — Patched
  ASan**.

Debug empaqueta `arm64-v8a`, `armeabi-v7a`, `x86` y `x86_64`; ASan empaqueta
solo `x86_64`, su runtime y `wrap.sh`. La auditoría binaria correlacionó 10/10
bibliotecas desde el APK hasta CMake: el parser esperado está presente, el
contrario ausente, Debug no está instrumentado con ASan, ASan sí lo está y el
JNI antiguo `parsePacketVulnerable` no existe.

Los cuatro packages se instalaron simultáneamente y se probaron de forma
secuencial, con una sola app activa y escuchando en `43568/UDP`. Cada variante
mostró el parser compilado correcto, cargó JNI, recibió exactamente un datagrama
de la muestra canónica `samples/benign/valid_call_control.bin` (17 bytes,
SHA-256
`912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E`) y
devolvió `status=accepted code=ok` conservando su PID. No hubo crashes; las dos
variantes ASan no registraron errores AddressSanitizer.

El asset Android histórico `oversized_complete_payload.bin` se retiró en 1B.4.
La muestra canónica externa permanece en
`samples/malformed/oversized_complete_payload.bin` y no se ejecutó en las nuevas
variantes durante la Fase 1.

**LIMITACIÓN.** Esta validación solo demuestra equivalencia funcional para esa
entrada válida concreta y composición binaria de los artefactos provisionales.
No demuestra seguridad general de Patched, explotación, RCE, control del flujo,
compromiso del dispositivo, equivalencia exacta con WhatsApp o
CVE-2019-3568, ni el comportamiento oversized de las nuevas variantes. E-022 y
E-025 siguen siendo evidencia histórica anterior a esta arquitectura.

### Fase 2 — Modelos y navegación

Automáticas previstas: tests unitarios de datos y reducers/state holder,
incluida la separación entre `CallDirection` y `CallOutcome`; tests Compose de
destino inicial, lista, chat, envío local, historial, menú y Lab mode; tests de
navegación con IDs estables.

Manuales previstas: recorrer las pantallas, verificar textos y cinco contactos,
enviar mensajes locales, restablecer datos y comparar visualmente ambos flavors.

### Fase 3 — Mensajería y llamadas normales

Automáticas previstas: tests del autómata de llamada, reloj inyectable,
aceptar/rechazar/finalizar, mapeos `COMPLETED`, `REJECTED`, `MISSED` y
`CANCELLED`, historial y garantía de que las acciones salientes no llaman al
gateway nativo.

Manuales previstas: llamada saliente completa; paquete válido secuencial en
Patched y Vulnerable; verificar que la UI entrante aparece después de
`NATIVE_PARSE_OK`; aceptar y rechazar sin segundo parse; comprobar proceso vivo.

### Fase 4 — Integración Patched

Automáticas previstas: mapeo exacto de `payload_too_large`, navegación a
Blocked, historial `BLOCKED`, Ver detalles y regresión del receptor UDP.

Manuales previstas: muestra válida Patched; oversized solo Patched; proceso
vivo, pantalla sin detalles técnicos, Lab mode con resultado completo y retry
tras `EADDRINUSE`.

### Fase 5 — Operación nativa incompleta

Automáticas previstas: serialización del marcador, orden
`write pending → JNI → clear`, fallo de persistencia, retorno normal, detección
al iniciar, descarte y reconstrucción de historial; gateway falso que simule no
limpiar el marcador sin corromper memoria.

Manuales previstas: sembrar el estado interrumpido mediante mecanismo de test
no destructivo, reiniciar, leer el aviso, abrir Lab mode y descartarlo. No
ejecutar overflow.

### Fase 6 — Visual y accesibilidad

Automáticas previstas: tests Compose semánticos, labels, estados toggle,
navegación y accessibility checks compatibles con las versiones fijadas.

Manuales previstas: claro/oscuro del sistema, font scale, TalkBack, contraste,
rotación, back, áreas táctiles, contadores y comparación visual flavor a flavor.

### Fase 7 — Congelación y regresión no destructiva

Automáticas previstas: los cuatro builds, tests unitarios/instrumentados,
`diff --check`, inspección de APK/ABI/parser y regresiones no destructivas.

Manuales previstas: matriz completa válida, Patched oversized, ciclo de vida,
ráfaga controlada, `EADDRINUSE`, retry, historial, Lab mode y marcador simulado.

### Fase 8 — Evidencia final y única ejecución vulnerable

Automáticas previstas: SHA-256 previo de APK/muestra, captura de versión y
entorno, verificación de package/ABI/parser y simbolización del log.

Manuales previstas: captura final Patched ASan oversized sobre el APK congelado
y, solo tras nueva autorización expresa, una única ejecución Vulnerable ASan
oversized sobre su APK final congelado. Patched ASan + oversized puede ejecutarse
en las Fases 4, 7 y 8 y no está sujeto a unicidad. Conservar comandos,
stdout/stderr, exit codes, timestamps, PID, log íntegro y hashes.

## 28. Criterios de aceptación

### Identidad y compilación

- [x] dos apps conceptuales instalables simultáneamente;
- [x] cuatro variantes compilables con los IDs acordados;
- [x] Debug y ASan diferenciados, ASan x86_64;
- [x] trabajo realizado solo en `feature/echocall-ui`;
- [x] base `feature/native-core` y commits protegidos preservados.

### Separación de seguridad

- [x] parser fijado al compilar;
- [x] no existe selector runtime;
- [x] el APK Patched no empaqueta la implementación vulnerable, verificado en
      Gradle, CMake, fuentes compiladas, símbolos de la `.so` y contenido del
      APK;
- [x] la separación quedó razonablemente garantizada y Fase 1 no quedó
      bloqueada;
- [x] Vulnerable no puede seleccionar el parser seguro;
- [x] la UI muestra VULNERABLE/PATCHED como solo lectura;
- [x] el código vulnerable deliberado no se corrigió ni se hizo más peligroso.

### Experiencia compartida

- [ ] misma interfaz, navegación, contactos, mensajes, llamadas y avatares;
- [ ] lista de conversaciones como pantalla inicial;
- [ ] cinco contactos ficticios y fallback de iniciales;
- [ ] conversaciones y mensajes locales funcionales;
- [ ] historial accesible;
- [ ] llamada saliente simulada sin UDP/JNI;
- [ ] llamada entrante solo tras paquete válido aceptado;
- [ ] procesamiento nativo anterior a Aceptar/Rechazar;
- [ ] llamada activa con avatar, nombre, duración, mute, altavoz y finalizar;
- [ ] interfaz en español y tema del sistema;
- [ ] diferencias visuales discretas, no basadas solo en color.

### Comportamiento técnico

- [x] ambas apps conservan `43568/UDP` y se prueban secuencialmente;
- [ ] se preservan ciclo de vida, cola, escucha única, `EADDRINUSE` y Retry;
- [ ] Patched acepta entrada válida y rechaza oversized antes de la copia;
- [ ] Patched muestra llamada bloqueada y registra `BLOCKED`;
- [x] Vulnerable acepta entrada válida;
- [ ] el marcador se escribe antes de JNI, se limpia tras retorno y se detecta
      después de un no retorno;
- [ ] el aviso interrumpido no atribuye automáticamente una causa.

### Comunicación y límites

- [ ] Lab mode informa variante, app, parser, UDP, resultado, eventos y límites;
- [ ] la experiencia normal no expone detalles técnicos innecesarios;
- [x] no existe botón de overflow/explotación/RCE;
- [ ] no hay backend, cuentas, audio, telefonía real o red externa;
- [x] no se afirma RCE, control del flujo o seguridad completa;
- [x] no se afirma equivalencia exacta con CVE-2019-3568;
- [x] E-022/E-025 se mantienen como evidencia histórica, no final;
- [ ] no se publican ni firman APK de distribución.

## 29. Riesgos técnicos

| Riesgo | Impacto | Mitigación/validación prevista |
|---|---|---|
| Gradle cruza flavors también con `release` | variantes no deseadas | mitigado en Fase 1: `release` deshabilitado con Android Components |
| una configuración CMake inválida cae en un parser por defecto | APK con seguridad equivocada | CMake debe fallar si falta o no reconoce el valor |
| ambos parsers permanecen en la `.so` | separación solo cosmética | mitigado y verificado en Fase 1 mediante Gradle, CMake, fuentes, objetos, símbolos y APK |
| etiqueta UI y parser nativo divergen | evidencia engañosa | mitigado en Fase 1 con identidad compilada consultada desde JNI y contrastada con flavor |
| cambio de namespace rompe símbolos JNI | error de carga | conservar namespace compartido y smoke test de cada variante |
| source sets de flavor/build type pisan recursos | nombre o distintivo incorrecto | mitigado en Fase 1 con recursos de variante ASan explícitos e inspección del resultado |
| ASan está obsoleto/no soportado desde 2023 y podría contener errores | fragilidad en toolchains futuras | congelar NDK/ABI actual, documentar el límite y mantener HWASan como recomendación para entornos compatibles |
| las dos apps compiten por UDP 43568 | `EADDRINUSE` esperado | ejecución secuencial y retry conservado; no cambiar el puerto |
| navegación a Incoming antes de `accepted` | rompe la propiedad central | coordinador único y test causal de orden/eventos |
| callbacks UDP duplicados tras recomposición | doble parse o historial duplicado | receptor fuera de composables y consumo FIFO con identidad de evento |
| temporizadores sobreviven a la pantalla | duración/estado incorrectos | reloj inyectable y jobs cancelados por estado/ciclo de vida |
| marcador no llega a disco antes del abort | falso negativo | esperar transacción DataStore antes de JNI y probar el orden |
| marcador queda pendiente por fallo no nativo | falso positivo | situarlo junto al gateway y redactar inferencia limitada |
| reset borra evidencia diagnóstica | pérdida de contexto | descarte explícito separado del reset simulado |
| estado solo en memoria se pierde al matar proceso | mensajes/historial desaparecen | comportamiento aceptado y explicado; solo el marcador persiste |
| detalles técnicos filtran a UI normal | experiencia incoherente | modelos normalizados para UI y detalles completos solo en Lab mode |
| oversized vulnerable se ejecuta antes de tiempo | crash innecesario | prohibirla hasta Fase 8 y exigir autorización expresa única |
| evidencia histórica se atribuye a APK nuevos | conclusión inválida | hashes nuevos, IDs nuevos y registro final independiente |

## 30. Fuentes

### 30.1 Fuentes internas inspeccionadas

- commits `3bcceb36748aaf385dfa6c4b8e43b0a213767de4` y
  `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c`;
- `android-app/app/build.gradle.kts`;
- `android-app/build.gradle.kts` y `android-app/settings.gradle.kts`;
- `android-app/app/src/main/AndroidManifest.xml`;
- `android-app/app/src/main/java/com/echocall/lab/MainActivity.kt`;
- `android-app/app/src/main/java/com/echocall/lab/NativeBridge.kt`;
- `android-app/app/src/main/java/com/echocall/lab/UdpPacketReceiver.kt`;
- `android-app/app/src/main/cpp/CMakeLists.txt`;
- `android-app/app/src/main/cpp/native_bridge.c`;
- `native-core/CMakeLists.txt`;
- `native-core/include/packet_format.h` y `parser_result.h`;
- `native-core/src/parser_result.c`, `safe_parser.c` y
  `vulnerable_parser.c`;
- `docs/evidencias/registro_validacion_experimental.md`;
- `docs/evidencias/artefactos/E-022/` y
  `docs/evidencias/artefactos/E-025/`.

### 30.2 Android y Gradle — documentación oficial

- [Configure build variants](https://developer.android.com/build/build-variants):
  product flavors, dimensiones, combinación con build types, prioridad de
  source sets, `applicationId` por variante y desactivación de variantes con
  `androidComponents.beforeVariants`.
- [Configure the app module](https://developer.android.com/build/configure-app-module):
  diferencia entre namespace y `applicationId`, e identidad única en el
  dispositivo.
- [Link Gradle to your native library](https://developer.android.com/studio/projects/gradle-external-native-builds):
  integración CMake, argumentos por flavor, targets y ABI filters.
- [Guide to app architecture](https://developer.android.com/topic/architecture):
  single-activity, capas, state holders y separación UI/datos.
- [UI layer](https://developer.android.com/topic/architecture/ui-layer): flujo
  unidireccional y producción de estado.
- [Navigation](https://developer.android.com/guide/navigation): destinos,
  rutas, grafo y Navigation Compose.
- [Preferences DataStore API](https://developer.android.com/reference/androidx/datastore/preferences/core/PreferencesKt):
  `edit` actualiza transaccionalmente, completa tras la persistencia durable en
  disco y lanza `IOException` si falla la escritura.
- [Address Sanitizer](https://developer.android.com/ndk/guides/asan):
  ASan está obsoleto/no soportado desde 2023, todavía puede usarse con posibles
  errores y HWASan es la recomendación actual en entornos compatibles.
- [Material Design 3 in Compose](https://developer.android.com/develop/ui/compose/designsystems/material3):
  MaterialTheme y selección claro/oscuro del sistema.
- [Semantics in Compose](https://developer.android.com/develop/ui/compose/accessibility/semantics)
  y [principios de accesibilidad](https://developer.android.com/guide/topics/ui/accessibility/principles):
  etiquetas, roles, estados y alternativas al color.
- [Test your Compose layout](https://developer.android.com/develop/ui/compose/testing):
  pruebas mediante semántica, acciones y sincronización.

### 30.3 CVE-2019-3568 — fuentes públicas prioritarias

- [Advisory oficial de Facebook/Meta](https://www.facebook.com/security/advisories/cve-2019-3568).
  **NO VERIFICADO DIRECTAMENTE EL 2026-08-05:** la URL redirigió a una pantalla
  de acceso/bloqueo de Facebook. Se conserva como referencia primaria enlazada
  por los registros oficiales, pero no se le atribuye contenido no leído.
- [CVE-2019-3568 en CVE Program](https://www.cve.org/CVERecord?id=CVE-2019-3568).
  La interfaz web requiere JavaScript y no expuso el registro en este entorno.
  El [registro JSON oficial de CVE Services](https://cveawg.mitre.org/api/cve/CVE-2019-3568)
  sí fue **VERIFICADO EL 2026-08-05**: la descripción de la CNA usa RTCP y
  clasifica el problema como CWE-122.
- [CVE-2019-3568 en NVD](https://nvd.nist.gov/vuln/detail/CVE-2019-3568).
  **VERIFICADO EL 2026-08-05:** describe un buffer overflow en el stack VoIP,
  paquetes RTCP especialmente preparados, versiones afectadas, CWE-787 de NVD
  y CWE-122 de la CNA. El registro no publica el root cause interno, el heap
  exacto ni una cadena completa de explotación.

No se usan estas fuentes para afirmar que ECLB sea RTCP o que EchoCall Lab
reproduzca el producto real. Cualquier cambio futuro en registros dinámicos se
revalidará antes de la memoria o evidencia final.
