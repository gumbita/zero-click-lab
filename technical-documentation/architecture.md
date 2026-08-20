# Arquitectura de EchoCall Lab

EchoCall modela una superficie de procesamiento automático: un datagrama entra
por UDP y alcanza código C sin depender de una acción de aceptar o rechazar en
la interfaz. La aplicación registra primero que va a entrar en JNI para poder
distinguir un retorno normal de una interrupción del proceso.

## Flujo de datos

```text
Sender controlado
      ↓ datagrama ECLB, UDP :43568
UdpPacketReceiver
      ↓ bytes recibidos
Kotlin / estado de EchoCall
      ↓ marca pending en Preferences DataStore
NativeBridge.parsePacket(byte[])
      ↓ JNI
native_bridge.c
      ↓
parser C fijado en el build
      ├── vulnerable_parse_packet()
      └── safe_parse_packet()       ← variante Patched
```

`UdpPacketReceiver` recibe el datagrama y activa el procesamiento antes de que
la persona interactúe con la llamada simulada. Kotlin conserva el contexto de
la interfaz y transforma el resultado JNI en eventos observables. El contrato
binario está definido en la [especificación ECLB](packet-format.md).

## Frontera Kotlin/JNI/C

La interfaz pública común es `NativeBridge.parsePacket()`. El gateway
[`native_bridge.c`](../android-app/app/src/main/cpp/native_bridge.c) convierte
el array de bytes y normaliza el resultado del parser para Kotlin. El parser no
se selecciona desde la UI.

Gradle transmite a CMake uno de estos valores:

```text
ECHOCALL_PARSER_IMPLEMENTATION=VULNERABLE
ECHOCALL_PARSER_IMPLEMENTATION=PATCHED
```

CMake incorpora una sola fuente:

```text
VULNERABLE → native-core/src/vulnerable_parser.c
PATCHED    → native-core/src/safe_parser.c
```

Así, cada APK contiene una implementación de parser. La inspección de los
candidatos documentados correlacionó flavors, configuración CMake, objetos,
símbolos y contenido del APK; el parser contrario estaba ausente.

## Dos variantes, dos formas de build

```text
Vulnerable ─┬─ Debug
            └─ ASan

Patched ────┬─ Debug
            └─ ASan
```

Vulnerable y Patched son las variantes conceptuales. Debug y ASan son build
types. ASan añade instrumentación al código nativo y empaqueta el runtime
necesario para el entorno `x86_64`; no altera el contrato ECLB ni crea otro
parser.

## Marcador pre-JNI

Antes de invocar JNI, la aplicación persiste un marcador con el identificador
del escenario, variante, longitud, instante y origen. Solo un retorno normal
permite limpiarlo.

```text
datagrama
   ↓
PENDING_MARKER_PERSISTED
   ↓
NATIVE_PARSE_STARTED
   ├── retorno normal → PENDING_MARKER_CLEARED
   └── proceso abortado → marcador disponible tras relanzar
```

El marcador permite observar que una operación marcada no alcanzó la limpieza
normal. No diagnostica por sí mismo la causa: la atribución de un overflow
requiere el informe ASan, logs, señal y demás evidencia de ejecución.

## Componentes

| Componente | Responsabilidad |
|---|---|
| [`android-app/`](../android-app/README.md) | UDP, ciclo de vida, UI, estado, DataStore y JNI |
| [`native-core/`](../native-core/README.md) | Contrato C, parsers, CLI y CTest |
| [`samples/`](../samples/README.md) | Entradas ECLB versionadas |
| [`tools/`](../tools/README.md) | Generación de muestras y sender UDP |
| [`technical-documentation/evidence/`](evidence/README.md) | Resultados, hashes y procedencia |

## Puntos de observación

- **Código fuente:** orden de validaciones y tamaño de la reserva.
- **CTest:** aceptación/rechazo de entradas en la ruta Patched.
- **Log Android:** recepción, entrada JNI y resultado normalizado.
- **Marcador persistente:** retorno normal o procesamiento interrumpido.
- **ASan:** acceso inválido, tamaño del `WRITE` y región afectada.
- **Reversing:** estructura estática de los binarios E-028/E-029.

Los resultados observados se separan de su interpretación en
[`experimental-results.md`](experimental-results.md).
