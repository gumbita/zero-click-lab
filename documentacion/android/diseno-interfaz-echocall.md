# Diseño final de interfaz y arquitectura de EchoCall Lab

> Estado del documento: **FASES 0 A 6 — VALIDADAS; FASES 7 Y 8 — PENDIENTES**
>
> Rama de trabajo: `feature/echocall-ui`
>
> Línea base: `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c`
>
> Cierre versionado de Fase 1: `26b0638442a5f31b134ba259a8afcbfc0d40d35d`
>
> Cierre versionado de Fase 2: `ece2e13584838d1e56da117a634ff53b51faa17b`
>
> Cierre versionado de Fase 3: `aa69cba406fa78fd088019ec75dcd33a0ff05856`
>
> Cierre versionado de Fase 4: `8d7add26aa22b5884b1ae401e5abe6c4429fd5d6`
> (`8d7add2 Add patched blocked-call handling`)
>
> Cierre versionado de Fase 5: `e1da09eaea29a1f9f2ab0e395a6bb5c829c478f1`
> (`e1da09e Track incomplete native operations`)
>
> Fecha de auditoría inicial del repositorio: 2026-08-04
>
> Fecha de revisión y consolidación documental: 2026-08-10

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

**DECISIÓN CUMPLIDA EN FASE 3.** `IncomingCallScreen` solo se muestra después de
un retorno `status=accepted code=ok`.

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

**CONFIRMADO EN FASES 1 A 6.** Se mantiene un único módulo de aplicación Android
`:app` que genera cuatro variantes, con dos identidades de producto
Vulnerable/Patched y una Activity compartida. La estructura implementada hasta
Fase 6 es:

```text
MainActivity (ciclo de vida UDP, PendingProcessingStore y setContent)
└── EchoCallApp
    ├── EchoCallStateHolder (estado simulado de producto en memoria)
    │   ├── CurrentCall (OUTGOING, INCOMING o ACTIVE)
    │   └── BlockedCallAttempt (rechazo previo a establecer llamada)
    ├── PendingProcessingMarker (estado técnico persistente, separado)
    ├── estado técnico del laboratorio, separado
    └── EchoCallNavHost
        ├── ConversationsScreen (destino inicial)
        ├── ChatScreen(contactId)
        ├── CallHistoryScreen
        ├── LabModeScreen
        ├── AboutScreen
        ├── OutgoingCallScreen
        ├── IncomingCallScreen
        ├── ActiveCallScreen
        ├── BlockedCallScreen
        └── InterruptedProcessingScreen

NativeBridge.parsePacket() -> libechocall_native.so
└── parser único fijado por flavor/CMake
```

El estado simulado desciende desde `EchoCallStateHolder` hacia los composables y
las acciones ascienden mediante callbacks. `EchoCallNavHost` concentra la
navegación y pasa solo `contactId` al chat; las pantallas no reciben directamente
el `NavController`. El estado técnico de Lab mode no forma parte del state holder
de producto. La UI no elige el parser ni invoca dos rutas alternativas.

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

**CONFIRMADO EN FASES 1 A 6.** El código Kotlin, JNI/C y la UI permanecen
compartidos en `src/main`. Fases 2 a 5 añadieron allí:

- `EchoCallApp` como raíz Compose y `EchoCallNavHost` como grafo;
- modelos y datos ficticios bajo `model/` y `data/`;
- `EchoCallStateHolder` como única fuente mutable del estado simulado;
- pantallas compartidas de conversaciones, chat, historial, Lab mode y acerca de;
- modelo, estado y pantallas compartidas para llamadas simuladas;
- estado y pantalla separados para intentos bloqueados antes de establecer llamada;
- store, marker técnico persistente y pantalla de procesamiento interrumpido;
- tema Material 3 claro/oscuro, recursos vectoriales locales y presentación
  accesible compartida;
- avatares locales por iniciales.

El receptor UDP, el contrato JNI y el estado técnico del laboratorio se
conservaron. El estado coordinado de llamada se implementó sin modificar JNI ni
UDP. Preferences DataStore conserva exclusivamente el marker técnico entre
procesos; no persiste mensajes, conversaciones ni historial.

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

**CONFIRMADO EN FASES 2 A 5.** Modelos compartidos mínimos:

| Modelo | Campos mínimos |
|---|---|
| `Contact` | `id`, `displayName`, `initials`, `preview`, `timestamp` |
| `Message` | `id`, `contactId`, `text`, `isOutgoing`, `timestamp` |
| `CallRecord` | `id`, `contactId`, `direction`, `outcome`, `timestamp` |
| `EchoCallUiState` | contactos, mensajes e historial simulado |
| `CurrentCall` | `contactId`, `direction`, `phase`, `startedAtMillis` |
| `BlockedCallAttempt` | `id`, `contactId` |
| `PendingProcessingMarker` | `scenarioId`, `variant`, `packetLength`, `timestamp`, `source` |

Enums implementados:

- `CallDirection`: `INCOMING`, `OUTGOING`;
- `CallOutcome`: `COMPLETED`, `REJECTED`, `MISSED`, `BLOCKED`, `INTERRUPTED`,
  `CANCELLED`.
- `CallPhase`: `OUTGOING`, `INCOMING`, `ACTIVE`.

**PROPUESTA PENDIENTE.** `NativeParseResult`, `LabEvent` y `UdpState` se
incorporarán únicamente si una fase posterior los necesita y audita.

**CONFIRMADO EN FASE 3.** El estado de llamada mantiene dirección, fase y
resultado como conceptos separados:

- `OUTGOING → ACTIVE → COMPLETED` para una llamada saliente completada;
- `OUTGOING → CANCELLED` si la llamada saliente se finaliza antes de activarse;
- `INCOMING → ACTIVE → COMPLETED` después de aceptar y finalizar;
- `INCOMING → REJECTED` al rechazar.

**CONFIRMADO EN FASE 4.** `BLOCKED` se registra sin crear `CurrentCall` cuando
Patched devuelve `payload_too_large`; `BlockedCallAttempt` representa solo el
aviso visual asociado. `MISSED` e `INTERRUPTED` permanecen preparados en el
modelo. Fase 5 no crea automáticamente un `CallRecord` ni una transición
`INTERRUPTED` al encontrar un marker pendiente.

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

**CONFIRMADO EN FASE 2.** Contactos ficticios y estables:

| ID | Nombre | Vista previa inicial | Tiempo simulado |
|---|---|---|---|
| `marta_soler` | Marta Soler | ¿Puedes hablar ahora? | 09:42 |
| `pau_ferrer` | Pau Ferrer | Vale, luego te llamo | Ayer |
| `lucia_navarro` | Lucía Navarro | Perfecto 👍 | Ayer |
| `dani_campos` | Dani Campos | Llamada perdida | Lun |
| `irene_vidal` | Irene Vidal | Nos vemos luego | Dom |

Los avatares implementados son círculos locales con iniciales (`MS`, `PF`, `LN`,
`DC`, `IV`) y no tienen dependencias de red. Los datos iniciales residen en
`FakeEchoCallData` y el estado de sesión trabaja con copias.

**LIMITACIÓN.** No se usarán fotografías, nombres, teléfonos, correos ni otros
datos reales. Marta Soler se asocia al escenario UDP por decisión del
simulador, no por contenido del paquete.

## 12. Pantalla principal

**CONFIRMADO EN FASE 2.** `ConversationsScreen` es el destino inicial. Muestra:

- barra superior con título **EchoCall**;
- acción visible **Llamadas**;
- menú de tres puntos con **Modo Lab**, **Acerca de** y **Restablecer datos**;
- lista de los cinco contactos con avatar/iniciales, nombre, último mensaje y
  hora o fecha simulada.

No mostrará JNI, UDP, ASan, offsets, heap, `memcpy`, `payload_too_large`, CVSS
ni controles de explotación.

## 13. Conversación

**CONFIRMADO EN FASE 3.** `ChatScreen(contactId)` incluye volver, avatar, nombre,
acción de llamada local habilitada, mensajes precargados, campo de texto y envío
local.

Para Marta Soler se precargará, como mínimo:

1. Marta: «Hola, ¿tienes un momento?»
2. Tú: «Sí, dime»
3. Marta: «¿Puedes hablar ahora?»

Los mensajes nuevos se añaden localmente durante la sesión. El envío descarta
texto vacío, actualiza preview y timestamp, reordena la conversación y conserva
el mensaje al navegar mientras vive el proceso. No se envían por red ni se
conservan tras finalizar el proceso.

**LIMITACIÓN.** La mensajería es contexto visual de una app de comunicaciones;
no representa el vector de CVE-2019-3568.

## 14. Historial

**CONFIRMADO EN FASES 2, 3 Y 4.** `CallHistoryScreen` es accesible desde la barra
superior y muestra avatar, contacto, dirección y resultado separados, con
fecha/hora ficticia. Sus registros iniciales forman parte del estado compartido
en memoria y se restauran junto con conversaciones y mensajes. Fase 3 incorpora
los registros locales `COMPLETED`, `REJECTED` y `CANCELLED`; Fase 4 incorpora
`BLOCKED` para el rechazo automático previo a establecer la llamada simulada.

La dirección será `INCOMING` u `OUTGOING`. El resultado será `COMPLETED`,
`REJECTED`, `MISSED`, `BLOCKED`, `INTERRUPTED` o `CANCELLED`. Los registros se
mantendrán solo en memoria de la sesión. El marker persistente de Fase 5 es
estado técnico independiente: no reconstruye historial ni genera
automáticamente `CallOutcome.INTERRUPTED`.

No habrá agenda del sistema, telefonía real ni estadísticas avanzadas.

## 15. Llamada saliente

**CONFIRMADO EN FASE 3.** Desde `ChatScreen`, la acción de teléfono navega a
`OutgoingCallScreen(contactId)`:

```text
teléfono → “Llamando…” → transición determinista → llamada activa
         → Finalizar → registro en historial
```

La pantalla muestra avatar, nombre, estado y **Finalizar**. La transición se
controla con un temporizador local y cancelable asociado al ciclo de vida.
Finalizar durante `OUTGOING` registra dirección `OUTGOING` y resultado
`CANCELLED`; finalizar después de llegar a `ACTIVE` registrará `OUTGOING` y
`COMPLETED`.

**LIMITACIÓN.** Esta acción no enviará UDP, no invocará JNI y no ejecutará la
vulnerabilidad.

## 16. Llamada entrante

**CONFIRMADO EN FASE 3.** `IncomingCallScreen` solo se muestra cuando el parser
nativo haya devuelto `status=accepted code=ok` para un datagrama UDP válido.
Muestra a Marta Soler, **Llamada entrante**, **Aceptar** y **Rechazar**.

- **Aceptar:** abre `ActiveCallScreen` sin volver a ejecutar el parser.
- **Rechazar:** cierra la llamada y añade `REJECTED` al historial, sin volver a
  ejecutar el parser.

La dirección registrada es `INCOMING`. Una llamada aceptada y posteriormente
finalizada tiene resultado `COMPLETED`. La expiración automática a `MISSED` no
se implementó en Fase 3.

El evento técnico `CALL_INCOMING` se conserva como indicación de señal
recibida, pero no autoriza la navegación antes del retorno `accepted`.

## 17. Llamada activa

**CONFIRMADO EN FASE 3.** `ActiveCallScreen` es común a llamadas salientes y
entrantes aceptadas. Muestra avatar, nombre, contador de duración, **Silenciar**,
**Altavoz** y **Finalizar**.

Silenciar y Altavoz modificarán solo su estado visual y su descripción
accesible. El contador avanza mientras el estado es activo. Finalizar detiene el
contador, navega fuera de la llamada y registra el resultado `COMPLETED`.

**LIMITACIÓN.** No existe audio, conexión VoIP, vídeo, teclado numérico ni
llamada del sistema operativo.

## 18. Llamada bloqueada

**CONFIRMADO EN FASE 4.** En Patched, un retorno
`status=rejected code=payload_too_large` procedente de UDP crea un
`BlockedCallAttempt`, abre `BlockedCallScreen` y añade al historial un registro
con dirección `INCOMING` y resultado `BLOCKED`. No crea `CurrentCall INCOMING`.
Marta Soler es el mapping local del simulador; su nombre no procede de ECLB.

Texto normal:

> **Llamada bloqueada**
>
> EchoCall rechazó esta llamada antes de establecerla.

La única acción es **Cerrar**. Al cerrarla se elimina el estado visual
`BlockedCallAttempt`, pero el registro `BLOCKED` permanece en el historial de la
sesión.

La pantalla normal no mostrará longitudes, `heap-buffer-overflow` o `memcpy`.
No muestra Aceptar o Rechazar y no afirmará que la aplicación sea completamente
segura. `REJECTED` queda reservado al rechazo manual de una llamada válida;
`BLOCKED` representa el rechazo de Patched antes de establecerla.

**LIMITACIÓN.** En Vulnerable ASan, la muestra oversized puede terminar el
proceso antes de que Kotlin reciba un resultado. No puede mostrarse una
pantalla normal posterior dentro de ese proceso. Vulnerable Debug tampoco
garantiza una manifestación concreta.

## 19. Operación nativa incompleta

### 19.1 Semántica

**CONFIRMADO EN FASE 5.** Antes de entrar en JNI se persiste:

```text
pending = true
variant = <applicationId actual>
source = udp|local_sample|test
packetLength = <bytes>
timestamp = <instante>
scenarioId = voip_control_packet
```

Solo tras un retorno normal de JNI se completa `clearPending()`. Si al arrancar
el marker permanece, se crea exclusivamente estado técnico visual y se muestra
`InterruptedProcessingScreen`; no se construye `CallRecord` ni se añade
`INTERRUPTED` al historial:

> **Procesamiento interrumpido**
>
> El procesamiento anterior no devolvió el control a EchoCall.
>
> Esto no determina por sí solo la causa de la interrupción. Consulta el Modo
> Lab y la instrumentación de la ejecución para más detalles.

Acciones: **Abrir Modo Lab** y **Cerrar y continuar**. Abrir Lab no limpia el
marker. Cerrar espera `clearPending()`, elimina el estado visual y vuelve a
Conversaciones.

### 19.2 Interpretación prudente

**LIMITACIÓN.** El marcador solo acredita que una llamada JNI marcada no
registró un retorno normal. No atribuye por sí solo la causa a ASan,
`heap-buffer-overflow`, CVE-2019-3568, explotación o RCE. La atribución requiere
evidencia instrumental externa.

**CONFIRMADO EN FASE 5.** El helper compartido escribe el marker desde una
coroutine apropiada y espera a que la persistencia termine correctamente
antes de entrar en JNI, sin bloquear el hilo principal. Tras un retorno normal
de JNI espera también la limpieza persistente. Las excepciones Kotlin
controladas intentan limpiar el marker; una terminación sin retorno lo conserva.

## 20. Lab mode

**CONFIRMADO HASTA FASE 6.** `LabModeScreen` está separado y contiene:

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

**CONFIRMADO EN FASE 4.** `PACKET_REJECTED_INVALID_LENGTH` se conserva en la
ruta UDP para el resultado exacto `rejected/payload_too_large`. Lab mode muestra
el resultado JNI completo, el ID técnico y el orden observado; los códigos no se
renombran.

### G. Estado de aplicación

- sección actual;
- último resultado visible;
- operación anterior incompleta, con `scenarioId`, `variant`, `packetLength`,
  `timestamp` y `source` cuando existe marker;
- Marta Soler como contacto asociado por el simulador.

El texto de Fase 5 aclara que el marker indica que comenzó un procesamiento y
no se alcanzó la limpieza posterior al retorno normal, sin identificar por sí
solo la causa.

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

**CONFIRMADO EN FASE 3.** Orden causal implementado y validado para una entrada
válida:

```text
Datagrama UDP en 43568
→ CONTROL_PACKET_RECEIVED
→ markPending() completado
→ NATIVE_PARSE_STARTED
→ NativeBridge.parsePacket()
→ libechocall_native.so llama al único parser compilado
→ retorna status=accepted code=ok
→ clearPending() completado
→ NATIVE_PARSE_OK
→ crea currentCall INCOMING para Marta Soler
→ navega a IncomingCallScreen
→ la persona acepta o rechaza
```

El procesamiento técnico termina antes de mostrar las acciones Aceptar y
Rechazar. Aceptar y Rechazar cambian únicamente el estado de producto y no
vuelven a ejecutar JNI. Marta Soler es un mapping fijo del simulador: su nombre
no procede de ECLB.

**CONFIRMADO EN FASE 5.** Antes de `NATIVE_PARSE_STARTED`, EchoCall espera a
que Preferences DataStore complete `markPending()`. Tras el retorno normal JNI
espera igualmente `clearPending()` antes de tratar `accepted` o `rejected`.

### 21.2 Oversized en Patched

**CONFIRMADO EN FASE 4.** El orden implementado y validado es:

```text
UDP → CONTROL_PACKET_RECEIVED → markPending() completado
→ NATIVE_PARSE_STARTED → NativeBridge.parsePacket()
→ status=rejected code=payload_too_large → clearPending() completado
→ PACKET_REJECTED_INVALID_LENGTH → BlockedCallAttempt
→ BlockedCallScreen + historial INCOMING/BLOCKED
```

No se crea `CurrentCall INCOMING` ni se muestra `IncomingCallScreen`. Fase 5
incorporó el marker sin cambiar el resultado funcional validado en Fase 4.

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

**CONFIRMADO EN FASES 2 Y 5.** Política:

| Información | Persistencia |
|---|---|
| contactos, conversaciones y previews precargadas | código/recursos locales |
| avatares | recursos locales |
| mensajes nuevos | memoria de la sesión |
| historial | memoria de la sesión |
| mute, altavoz y llamada activa | memoria/estado de UI |
| configuración del parser | compilación; nunca almacenamiento runtime |
| marcador nativo incompleto | Preferences DataStore privado por app |

Fase 2 implementó las filas en memoria y los datos precargados. El
comando **Restablecer datos** reconstruye mensajes, previews, orden e historial
desde `FakeEchoCallData`, sin modificar parser, JNI, UDP ni resultados de Lab
mode. Fase 5 añadió Preferences DataStore solo para
`PendingProcessingMarker`; no se introdujeron Room ni persistencia de producto.

Preferences DataStore se usa por ser clave-valor, pequeño, transaccional y
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
acción explícita **Cerrar y continuar** es responsable de ello.

**CONFIRMADO EN FASE 5.** Si DataStore no confirma la escritura, no se invoca
JNI. El marker sobrevivió a un `force-stop` y fue detectado al relanzar; esta
prueba acredita persistencia entre procesos, no protección frente a un crash
nativo ni la causa de una interrupción.

## 23. Tema y accesibilidad

**VALIDADO EN FASE 6.** `EchoCallTheme` aplica Material 3 con esquemas locales
claro y oscuro seleccionados mediante `isSystemInDarkTheme()`. El Manifest usa
`@style/Theme.EchoCall`, con recursos `values` y `values-night` coherentes con
el fondo Compose. No existe selector manual de tema y Vulnerable/Patched
comparten la misma identidad visual.

La presentación usa roles de `MaterialTheme.colorScheme`, vectores locales y
`CallScreenLayout` como estructura común desplazable para las pantallas de
llamada. Se eliminaron los glifos usados como iconos, se añadieron descripciones
a acciones, objetivos táctiles Material y semántica de dirección para los
mensajes. El historial expresa dirección y resultado mediante texto y no
depende solo del color. Silenciar y Altavoz exponen `Role.Switch`, estado
`Activado`/`Desactivado` y una etiqueta visible.

Lab mode y Acerca de se reorganizaron en secciones legibles sin alterar sus
acciones o límites. La validación manual cubrió claro y oscuro, el cambio de
tema con el mismo PID Patched 10300, font scale 1.3 y su restauración a 1.0,
Conversations, Chat, ActiveCall, Lab e InterruptedProcessing, además de la
equivalencia visual Vulnerable/Patched. Los pares principales calculados dieron
contrastes de 7.25:1 a 16.36:1 en claro y 5.55:1 a 14.36:1 en oscuro; esos
intervalos no se generalizan a toda combinación posible.

**LIMITACIÓN.** Fase 6 no realizó una validación completa con TalkBack ni
incorporó una suite automatizada Compose. La comprobación de accesibilidad se
limitó a inspección estática, semántica Compose, árbol de UI mediante UI
Automator, revisión visual, contraste, touch targets, font scale 1.3 y estados
checkable. Esto no acredita cumplimiento total de accesibilidad. Fase 7 podrá
incluir un smoke test limitado con TalkBack si resulta práctico.

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
- **estado: validada**.

### Fase 1 — Arquitectura de variantes

- introducir flavors Vulnerable/Patched y cruzarlos con Debug/ASan;
- fijar `applicationId` y nombres;
- seleccionar un único parser mediante Gradle/CMake/JNI;
- conservar UDP `43568`;
- eliminar el selector runtime, sin rediseñar todavía toda la UI.
- **estado: validada y versionada en
  `26b0638442a5f31b134ba259a8afcbfc0d40d35d`**.

### Fase 2 — Modelos y navegación

- crear modelos, datos ficticios y estado de sesión;
- crear navegación, principal, conversación, historial, Lab mode inicial y
  pantalla Acerca de;
- permitir mensajería local, actualización de preview/orden y reset confirmado;
- mantener separados el estado simulado de producto y el estado técnico;
- no conectar todavía los flujos UDP a pantallas normales de llamada.
- **estado: validada**.

### Fase 3 — Mensajería y llamadas normales

- implementar saliente, entrante válida, aceptar, rechazar y activa;
- contador, mute, altavoz, finalizar e historial;
- integrar solo control válido UDP; ninguna oversized.
- **estado: validada**.

### Fase 4 — Integración Patched

- mapear `payload_too_large` a llamada bloqueada e historial;
- completar detalles de Lab mode;
- conservar la recuperación UDP;
- probar oversized únicamente en Patched.
- **estado: validada, cerrada y publicada en
  `8d7add26aa22b5884b1ae401e5abe6c4429fd5d6`**.

### Fase 5 — Operación nativa incompleta

- persistir `pending` antes de JNI y limpiar tras retorno;
- detectar el marcador al reiniciar;
- mostrar aviso, Lab mode y descarte prudentes;
- validar con simulación no corruptora cuando sea posible;
- no ejecutar una entrada oversized en Vulnerable.
- **estado: validada, cerrada y publicada en
  `e1da09eaea29a1f9f2ab0e395a6bb5c829c478f1`**.

### Fase 6 — Visual y accesibilidad

- tema claro/oscuro, contraste, tamaños, descripciones y foco;
- rotación, recomposición, estados vacíos, textos e iconos;
- equivalencia visual entre Vulnerable y Patched.
- **estado: validada; versionada en el commit que contiene esta revisión**.

### Fase 7 — Congelación y regresión no destructiva

- builds desde estado limpio sin borrar artefactos ajenos;
- muestra válida en ambas apps, oversized solo Patched;
- ciclo de vida, `EADDRINUSE`, historial, Lab mode y marcador simulado;
- auditar si el hook debuggable del marker se conserva o se retira de los APK
  candidatos finales;
- realizar un smoke test limitado con TalkBack si resulta práctico;
- revisar la equivalencia visual final Vulnerable/Patched;
- fijar los APK candidatos y sus hashes.
- **estado: pendiente**.

### Fase 8 — Evidencia final y única ejecución vulnerable

- realizar una captura final Patched ASan + oversized sobre el APK congelado;
- ejecutar Vulnerable ASan + oversized sobre el APK final congelado una única
  vez y solo con autorización expresa;
- mantener Patched ASan + oversized y la única ejecución Vulnerable ASan +
  oversized reservadas para la evidencia final de Fase 8;
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

**VALIDADA.** Fase 2 añadió Navigation Compose
`androidx.navigation:navigation-compose:2.9.8`, `EchoCallApp` como raíz,
`EchoCallNavHost` con `ConversationsScreen` como destino inicial y las pantallas
`ChatScreen`, `CallHistoryScreen`, `LabModeScreen` y `AboutScreen`. Las pantallas
reciben callbacks en lugar del `NavController`; el chat se identifica solo por
`contactId`.

`Contact`, `Message`, `CallRecord`, `CallDirection` y `CallOutcome` modelan los
datos ficticios. `EchoCallStateHolder` mantiene una única fuente mutable del
estado de producto en memoria, separada del estado técnico de Lab mode. El envío
local actualiza mensaje, preview, timestamp y orden. **Restablecer datos** exige
confirmación y recupera el dataset inicial.

`vulnerableDebug` y `patchedDebug` compilaron y arrancaron en conversaciones,
mostraron los cinco contactos, navegaron a chat, historial, Lab mode y Acerca de,
enviaron mensajes locales, conservaron el cambio al navegar y restauraron el
dataset inicial. Lab mode mantuvo package, parser y estado UDP correctos. Durante
estas pruebas no se enviaron datagramas, no se procesaron muestras y no hubo
crashes.

**LIMITACIÓN.** Fase 2 no implementa persistencia tras `process death`,
DataStore, Room, backend, cuentas, contactos reales, llamadas reales, asociación
UDP → Marta, pantallas de llamada ni comportamiento oversized. No demuestra
explotación, RCE, seguridad general, reproducción exacta de WhatsApp o
CVE-2019-3568 ni el escenario zero-click completo. La integración automática con
la experiencia normal de llamada corresponde a Fase 3.

### Fase 3 — Mensajería y llamadas normales

**VALIDADA.** Se implementaron `CurrentCall`, las fases `OUTGOING`, `INCOMING` y
`ACTIVE`, y las pantallas `OutgoingCallScreen`, `IncomingCallScreen` y
`ActiveCallScreen`. La llamada saliente de Pau Ferrer transitó localmente de
**Llamando…** a activa; se observaron temporizador, mute y altavoz visuales, y
finalizar añadió `OUTGOING/COMPLETED`. La cancelación
`OUTGOING/CANCELLED` está implementada, pero no se usa como evidencia
experimental principal porque su validación visual no fue suficientemente
sólida.

En las ejecuciones autorizadas con la muestra válida de 17 bytes, Vulnerable
Debug mantuvo el PID 4723: `NATIVE_PARSE_OK` se registró a las 14:53:00.643,
`currentCall` a las 14:53:00.644 e `IncomingCallScreen` a las 14:53:00.946; el
rechazo añadió `INCOMING/REJECTED`. Patched Debug mantuvo el PID 4973:
`NATIVE_PARSE_OK` se registró a las 14:54:55.246, `currentCall` a las
14:54:55.247 e `IncomingCallScreen` a las 14:54:55.439; aceptar y finalizar
añadió `INCOMING/COMPLETED`. Estos timestamps son observaciones de ejecuciones
concretas, no una garantía universal del scheduler.

El balance fue dos datagramas válidos, dos `NATIVE_PARSE_STARTED`, dos
`NATIVE_PARSE_OK`, cero rechazos del parser, cero crashes y cero oversized. Se
utilizó exclusivamente `samples/benign/valid_call_control.bin` (17 bytes,
SHA-256 `912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E`).
Aceptar y Rechazar no reejecutaron JNI.

**LIMITACIÓN.** Fase 3 no demuestra comportamiento oversized, mitigación
Patched, `heap-buffer-overflow` en las nuevas variantes, crash ASan, RCE,
explotación, telefonía o audio reales, protocolo RTCP real, ni equivalencia
exacta con WhatsApp o CVE-2019-3568. Solo valida el flujo normal con esa entrada
válida concreta.

### Fase 4 — Integración Patched

**VALIDADA.** Se implementaron `BlockedCallAttempt` y `BlockedCallScreen`, con
navegación centralizada, historial `INCOMING/BLOCKED` y resultado técnico
completo solo en Lab mode. El estado bloqueado es independiente de `CurrentCall`:
cerrar la pantalla limpia el aviso visual y conserva el historial. `REJECTED`
continúa significando rechazo manual de una llamada válida.

La ejecución autoritativa utilizó únicamente Patched Debug
(`com.echocall.lab.patched`) y
`samples/malformed/oversized_complete_payload.bin` (77 bytes, SHA-256
`516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA`). El
resultado fue `status=rejected code=payload_too_large declared_length=64
actual_length=64 maximum=32`; el PID permaneció 4569.

El orden observado fue: datagrama a las 10:49:46.313,
`CONTROL_PACKET_RECEIVED` y `NATIVE_PARSE_STARTED` a las 10:49:46.391, retorno
`rejected/payload_too_large` a las 10:49:46.447,
`PACKET_REJECTED_INVALID_LENGTH` a las 10:49:46.449, creación del estado
bloqueado a las 10:49:46.510 y presentación de `BlockedCallScreen` a las
10:49:47.085. Son observaciones de esta ejecución concreta, no una garantía
universal del scheduler.

El balance fue un oversized enviado, un datagrama recibido, un
`NATIVE_PARSE_STARTED`, un `payload_too_large`, un
`PACKET_REJECTED_INVALID_LENGTH`, una pantalla bloqueada y un registro
`BLOCKED`; hubo cero `NATIVE_PARSE_OK`, crashes/fatal, informes ASan y ejecuciones
Vulnerable. No se presentó `IncomingCallScreen`.

**LIMITACIÓN.** Esta observación demuestra que Patched rechazó esta muestra y el
proceso permaneció vivo. No demuestra seguridad general, mitigación completa,
bloqueo de un exploit real, RCE, explotación o equivalencia exacta con WhatsApp
o CVE-2019-3568. ECLB y la muestra pertenecen al laboratorio.

### Fase 5 — Operación nativa incompleta

**VALIDADA, CERRADA Y PUBLICADA.** Se incorporó
`androidx.datastore:datastore-preferences:1.2.1`,
`PendingProcessingMarker`, `PendingProcessingStore`, lectura al iniciar,
`InterruptedProcessingScreen` e integración en Lab mode. La única ruta Kotlin
de parseo espera `markPending()` antes de `NATIVE_PARSE_STARTED` y JNI; después
de todo retorno normal espera `clearPending()` antes de tratar `accepted` o
`rejected`.

La validación controlada utilizó el extra interno debuggable
`com.echocall.lab.extra.PENDING_MARKER_TEST_COMMAND`, con las operaciones seguras
`mark`, `read` y `clear`. No ejecuta JNI ni está expuesto como botón. El estado
inicial fue `marker=null` y Conversaciones. Se persistió
`scenarioId=voip_control_packet`, `variant=com.echocall.lab.patched`,
`packetLength=17` y `source=test`; tras `force-stop` y relanzamiento apareció
`InterruptedProcessingScreen`, Lab mostró sus campos y la interpretación
prudente, y **Cerrar y continuar** registró
`INTERRUPTED_MARKER_CLEARED_BY_USER`. El siguiente relanzamiento volvió a
`marker=null` y Conversaciones.

Los PID observados fueron 8790, 8878, 8948 y 9139. Los cambios se debieron a
`force-stop` y relanzamientos controlados, no a un crash. No se usaron UDP, JNI
en la simulación, muestras, oversized ni ASan.

**LIMITACIÓN.** El marker demuestra solo que un procesamiento marcado comenzó
y no alcanzó el punto normal de limpieza. No identifica la causa ni acredita
crash nativo, `heap-buffer-overflow`, `SIGABRT`, ASan, explotación, RCE o
control del flujo. Fase 5 tampoco demuestra comportamiento oversized
Vulnerable ni equivalencia exacta con WhatsApp o CVE-2019-3568; no validó el
escenario vulnerable real y no crea automáticamente un registro
`CallOutcome.INTERRUPTED`.

El cierre quedó versionado en
`e1da09eaea29a1f9f2ab0e395a6bb5c829c478f1`
(`e1da09e Track incomplete native operations`).

### Fase 6 — Visual y accesibilidad

**VALIDADA.** Se implementaron `EchoCallTheme`, esquemas Material 3 claro y
oscuro del sistema, `Theme.EchoCall`, paletas y vectores locales, además de
`CallScreenLayout`. Las pantallas comparten jerarquía visual, scroll cuando
pueden desbordar, objetivos táctiles adecuados y semántica de accesibilidad.
Silenciar y Altavoz publican rol y estado; el historial expresa dirección y
resultado en texto; Lab y Acerca de se reorganizaron sin modificar el
comportamiento técnico.

`assembleVulnerableDebug` y `assemblePatchedDebug` finalizaron correctamente
con 43 tareas cada uno. Los APK provisionales de validación midieron 33104622 y
33104242 bytes, con SHA-256
`9C173998CF4E4B85712923AE9FABB321D1BE2753D0B2A267682A29AAF35C5135` y
`ABE656B5BD96F55377B718555D6031485C09383DACBAB8BA1B23642AEF11D16D`,
respectivamente. No son los artefactos finales congelados.

La validación manual cubrió claro y oscuro, el cambio con el mismo PID Patched
10300, font scale 1.3 y vuelta a 1.0, las pantallas principales, estados
checkable mediante UI Automator y equivalencia visual Vulnerable/Patched. No se
usó TalkBack. Las incidencias de lanzamiento con un componente abreviado y de
captura inicial del marker fueron operativas y se resolvieron sin abrir tráfico,
JNI o muestras.

**LIMITACIÓN.** Fase 6 no ejecutó UDP, muestras, oversized ni ASan; no modificó
parser, JNI, CMake o UDP ni revalidó la vulnerabilidad. Tampoco realizó una
auditoría completa con TalkBack ni añadió tests Compose, por lo que no acredita
cumplimiento total de accesibilidad. La regresión técnica completa corresponde
a Fase 7.

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
oversized sobre su APK final congelado. Ambas ejecuciones ASan oversized quedan
reservadas para Fase 8. Conservar comandos,
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

- [x] misma interfaz, navegación, contactos, mensajes, llamadas y avatares;
- [x] lista de conversaciones como pantalla inicial;
- [x] cinco contactos ficticios y fallback de iniciales;
- [x] conversaciones y mensajes locales funcionales;
- [x] historial accesible;
- [x] llamada saliente simulada sin UDP/JNI;
- [x] llamada entrante solo tras paquete válido aceptado;
- [x] procesamiento nativo anterior a Aceptar/Rechazar;
- [x] llamada activa con avatar, nombre, duración, mute, altavoz y finalizar;
- [x] interfaz en español y tema del sistema;
- [x] diferencias visuales discretas, no basadas solo en color.

### Comportamiento técnico

- [x] ambas apps conservan `43568/UDP` y se prueban secuencialmente;
- [ ] se preservan ciclo de vida, cola, escucha única, `EADDRINUSE` y Retry;
- [x] Patched acepta entrada válida y rechaza oversized antes de la copia;
- [x] Patched muestra llamada bloqueada y registra `BLOCKED`;
- [x] Vulnerable acepta entrada válida;
- [x] el marcador se escribe antes de JNI, se limpia tras retorno y se detecta
      después de un no retorno;
- [x] el aviso interrumpido no atribuye automáticamente una causa.

### Comunicación y límites

- [x] Lab mode informa variante, app, parser, UDP, resultado, eventos y límites;
- [x] la experiencia normal no expone detalles técnicos innecesarios;
- [x] no existe botón de overflow/explotación/RCE;
- [x] no hay backend, cuentas, audio, telefonía real o red externa;
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
| navegación a Incoming antes de `accepted` | rompe la propiedad central | mitigado en Fase 3: orden causal observado `NATIVE_PARSE_OK → currentCall → IncomingCallScreen` |
| callbacks UDP duplicados tras recomposición | doble parse o historial duplicado | receptor fuera de composables y consumo FIFO con identidad de evento |
| temporizadores sobreviven a la pantalla | duración/estado incorrectos | reloj inyectable y jobs cancelados por estado/ciclo de vida |
| marcador no llega a disco antes del abort | falso negativo | esperar transacción DataStore antes de JNI y probar el orden |
| marcador queda pendiente por fallo no nativo | falso positivo | situarlo junto al gateway y redactar inferencia limitada |
| hook interno debuggable del marker permanece en candidatos finales | superficie de prueba innecesaria | auditar en Fase 7 si se conserva o retira tras cumplir su función |
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
