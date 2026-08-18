# Diseño del laboratorio de emulación CVE-2019-3568

> **DOCUMENTO HISTÓRICO / DISEÑO INICIAL**
>
> Este documento conserva decisiones y alternativas consideradas durante el
> diseño del laboratorio. Algunas fueron sustituidas durante la implementación
> y el contenido no debe interpretarse como una descripción exacta del estado
> actual. La referencia vigente es la
> [arquitectura de EchoCall](architecture.md); la identidad y cronología del
> experimento se conservan en su
> [ficha de procedencia](evidencias/procedencia-experimento-android.md).

## 1. Decisión general

El laboratorio será una **app Android de mensajería/VoIP simulada** que emula el patrón técnico de CVE-2019-3568 mediante procesamiento automático de paquetes de control de llamada.

La aplicación no será un clon de WhatsApp, no usará su marca, no replicará su interfaz de forma exacta y no interactuará con infraestructura real. Será una app propia de laboratorio, con estética de mensajería moderna y flujo de llamada entrante, diseñada para estudiar de forma segura un patrón de vulnerabilidad documentado.

La parte vulnerable estará implementada en **C**, integrada en Android mediante **JNI/NDK**, para reproducir de forma controlada un fallo de memoria tipo **heap-based buffer overflow**. La capa Android se encargará de la interfaz, simulación de llamada, recepción de eventos, visualización de logs y comparación entre modo vulnerable y modo seguro.

---

## 2. Objetivo técnico del laboratorio

El objetivo técnico es reproducir de forma controlada el patrón:

```text id="rwdv75"
paquete de control entrante
→ procesamiento automático por la app
→ parser nativo en C
→ lectura de cabecera y campo de longitud
→ copia de payload a buffer heap
→ ausencia de validación suficiente
→ fallo de memoria observable
→ versión segura con validación estricta
```

El laboratorio busca aproximarse al caso real de CVE-2019-3568 en los aspectos relevantes:

* aplicación móvil;
* flujo de llamada VoIP;
* paquete de control recibido;
* procesamiento sin interacción del usuario;
* parser nativo;
* campo de longitud;
* fallo de memoria;
* modo vulnerable;
* modo mitigado;
* documentación y evidencias.

---

## 3. Relación con CVE-2019-3568

CVE-2019-3568 afectó a la pila VoIP de WhatsApp. La descripción pública de NVD indica que un buffer overflow en la pila VoIP podía permitir ejecución remota de código mediante una serie de paquetes RTCP especialmente construidos enviados al número de teléfono objetivo.

El laboratorio no reproducirá WhatsApp real ni el exploit real. En su lugar, emulará el patrón técnico documentado:

* procesamiento automático de paquetes relacionados con llamada;
* estructura de paquete inspirada en RTCP;
* longitud declarada por la entrada;
* parser nativo vulnerable;
* copia insegura de payload;
* mitigación mediante validación de tamaño, estructura y consistencia.

---

## 4. Identidad de la app

### Decisión

La app será una **app genérica de mensajería/VoIP**, con parecido razonable a aplicaciones modernas de chat, pero sin copiar WhatsApp.

### Nombre provisional

Opciones:

```text id="wm89lu"
EchoCall Lab
VoxChat Lab
PulseCall Lab
SignalWave Lab
ZeroVoice Lab
```

### Recomendación

Nombre recomendado:

```text id="t34iho"
EchoCall Lab
```

Motivo: suena a llamada, laboratorio y comunicación, sin asociarse directamente a WhatsApp.

### Límites de diseño

No se usará:

* nombre WhatsApp;
* logo de WhatsApp;
* colores idénticos;
* iconografía registrada;
* textos o pantallas copiadas;
* capturas reales de WhatsApp.

Sí se usará:

* lista de chats simulada;
* contacto ficticio;
* pantalla de conversación;
* llamada entrante simulada;
* estado de procesamiento de paquetes;
* panel técnico de laboratorio.

---

## 5. Tipo de aplicación

### Decisión

La app principal será **Android**.

### Justificación

CVE-2019-3568 afectó a aplicaciones móviles de WhatsApp, incluyendo WhatsApp para Android y WhatsApp Business para Android. Por tanto, una app Android con código nativo C se aproxima mucho más al contexto real que una web app o una demo únicamente de escritorio.

### Arquitectura Android

La app se construirá como:

```text id="tfatwq"
Android Kotlin/Java
    ↓ JNI
Native C parser vulnerable / seguro
    ↓
Logs + evidencias + UI
```

---

## 6. Nivel de parecido con WhatsApp

### Objetivo inicial

Nivel 3:

```text id="dzwrmg"
App Android con interfaz de mensajería/llamada, recepción de paquete de control invisible, parser nativo y 
comparación vulnerable/seguro.
```

### Objetivo ampliado

Nivel 4, solo cuando el nivel 3 esté validado:

```text id="qoxp9l"
App Android más completa, con simulación avanzada de llamada, flujo de eventos más realista,
posible receptor UDP local y demo visual preparada para presentación.
```

### Regla de avance

No se pasa a Nivel 4 hasta que funcione:

* app Android;
* llamada entrante simulada;
* parser nativo vulnerable;
* parser seguro;
* muestra válida;
* muestra malformada;
* evidencia del fallo;
* evidencia de mitigación;
* logs separados.

---

## 7. Arquitectura general

### Decisión

Arquitectura híbrida:

```text id="d2c3xj"
Android Kotlin/Java + JNI/NDK + C + Python + FastAPI
```

### Capas

```text id="zrta81"
1. Aplicación Android EchoCall
   - app de mensajería/VoIP simulada;
   - experiencia de usuario normal;
   - llamada entrante;
   - procesamiento automático invisible;
   - sin indicadores técnicos visibles salvo en modo investigación.

2. Módulo nativo vulnerable
   - libechocall_vuln.so;
   - parser C de paquetes de control de llamada;
   - validación insuficiente de longitud;
   - heap-based buffer overflow controlado.

3. Módulo nativo corregido
   - libechocall_safe.so;
   - parser C corregido;
   - validación estricta de cabecera, tipo, longitud y consistencia;
   - rechazo seguro de paquetes malformados.

4. Entorno de investigación
   - generación de paquetes;
   - envío UDP local;
   - muestras válidas y malformadas;
   - fuzzing;
   - logs;
   - sanitizers;
   - comparación vulnerable/corregido.
```

### Ajuste importante

FastAPI no debe ser el corazón de la vulnerabilidad. El corazón debe estar en Android + C/JNI.

FastAPI será apoyo para:

* generar paquetes;
* recibir logs;
* lanzar pruebas desde escritorio;
* servir dashboard;
* guardar evidencias;
* facilitar la demo.

---

## 8. Frontend, interfaz y evidencias

### Decisión

La app tendrá dos niveles de interfaz:

1. **Interfaz normal de usuario**, orientada a parecer una app real de mensajería/VoIP.
2. **Modo investigación**, orientado a pruebas, demostración y extracción de evidencias técnicas.

Además, las evidencias completas del fallo se recogerán mediante herramientas externas como Logcat, `adb`, logs exportados, sanitizers y, opcionalmente, un dashboard auxiliar.

---

### 8.1 Interfaz normal de usuario

La interfaz principal será Android nativo, preferiblemente con Kotlin y Jetpack Compose.

La app visible para el usuario se diseñará como una aplicación genérica de mensajería y llamadas, sin mostrar de forma directa que existe una vulnerabilidad o un laboratorio técnico.

Pantallas principales:

```text id="y3ootx"
EchoCall
├── Chats
├── Conversación
├── Llamada entrante
├── Llamada fallida / llamada bloqueada
└── Historial de llamadas
```

#### Pantalla 1 — Lista de chats

```text id="c9g3ba"
EchoCall

Lab Contact
Te llamo ahora

Alice
Última llamada: hace 1 min

System Contact
Nuevo aviso
```

#### Pantalla 2 — Conversación

```text id="urslo6"
Lab Contact

Hola, ¿puedes hablar ahora?
Te llamo en un momento.
```

#### Pantalla 3 — Llamada entrante

```text id="ypyyc0"
Incoming call

Lab Contact

[Responder]    [Rechazar]
```

El procesamiento del paquete de control de llamada debe ocurrir antes de que el usuario pulse `Responder` o `Rechazar`, para representar el comportamiento zero-click.

#### Pantalla 4 — Llamada fallida o llamada bloqueada

En la versión vulnerable:

```text id="kzt60y"
Call failed

The call could not be established.
```

En la versión corregida:

```text id="t8cat9"
Call blocked

The incoming call could not be verified.
```

#### Pantalla 5 — Historial de llamadas

```text id="u16ly0"
Calls

Lab Contact
Missed call · just now

Alice
Voice call · yesterday
```

La interfaz normal no mostrará detalles como `heap overflow`, `memcpy`, `declared_length`, `ASan`, `packet malformed` o `CVE-2019-3568`.

---

### 8.2 Modo investigación

La app incluirá un modo investigación para uso técnico, demostración controlada y extracción de evidencias.

Este modo podrá activarse desde una opción no visible en el flujo normal, por ejemplo:

```text id="fp4eq9"
Settings → About EchoCall → pulsar 7 veces sobre Version
```

o mediante un acceso explícito en builds de laboratorio:

```text id="bl98je"
Settings → Research Mode
```

El modo investigación mostrará información técnica limitada pero útil:

```text id="nhr5qk"
Research Mode

Parser engine: Vulnerable / Patched
Native module: libechocall_vuln.so / libechocall_safe.so
Packet source: UDP / Local sample
Last sample: oversized_payload.bin
Last result: call processing failed / packet rejected
Export logs
```

Este modo no debe convertir la app en un panel técnico desde el principio. Su función es permitir explicar y demostrar el laboratorio cuando sea necesario.

---

### 8.3 Evidencias externas para la memoria

Las evidencias completas se recogerán fuera de la interfaz normal mediante:

```text id="t4nt83"
- Android Studio Logcat;
- adb logcat;
- ficheros de log exportados;
- logs nativos;
- salidas de ASan/HWASan;
- capturas de pantalla;
- dashboard externo opcional.
```

Eventos técnicos registrados:

```text id="y66bkf"
CALL_INCOMING
CONTROL_PACKET_RECEIVED
PACKET_HEADER_PARSED
DECLARED_LENGTH_READ
PAYLOAD_COPY_ATTEMPT
NATIVE_PARSE_STARTED
NATIVE_PARSE_FAILED
NATIVE_PARSE_OK
PACKET_REJECTED_INVALID_LENGTH
PACKET_REJECTED_INVALID_TYPE
SAFE_VALIDATION_OK
SAFE_VALIDATION_FAILED
```

Estos eventos no se mostrarán en la app normal. Se usarán para análisis, memoria, anexos y comparación técnica entre versión vulnerable y versión corregida.

---

### 8.4 Dashboard auxiliar opcional

Como ampliación, se podrá crear un dashboard externo con React/FastAPI.

Este dashboard no formará parte de la app móvil principal. Su función será facilitar:

```text id="0k0x79"
- visualización de logs;
- comparación vulnerable/corregido;
- selección de muestras;
- envío de paquetes;
- exportación de evidencias;
- preparación de demo.
```

El dashboard será una herramienta de investigación, no la interfaz de usuario de EchoCall.

---

### 8.5 Regla de diseño

La app debe parecer una aplicación real de mensajería/VoIP.

La vulnerabilidad debe aparecer como resultado del análisis interno del módulo nativo, no como una funcionalidad visible para el usuario.

Por tanto:

```text id="851sv8"
Interfaz normal:
experiencia de mensajería y llamada.

Modo investigación:
demostración técnica controlada.

Evidencias externas:
memoria, logs, ASan/HWASan, anexos y dashboard opcional.
```

---

## 9. Backend

### Decisión

Backend auxiliar con **FastAPI**.

### Funciones

FastAPI se usará para:

* generar o servir muestras;
* almacenar logs;
* mostrar dashboard web opcional;
* enviar paquetes UDP al emulador/dispositivo;
* coordinar pruebas;
* exportar evidencias.

### Endpoints posibles

```text id="3qdlhu"
GET  /status
GET  /samples
POST /samples/generate
POST /send/udp
GET  /logs
POST /logs/upload
POST /lab/run
```

### Importante

La app Android debe poder funcionar con muestras locales incluso si FastAPI no está activo. Así evitas que el backend sea un punto único de fallo.

---

## 10. Comunicación de paquetes

### Decisión objetivo

```text id="pznj3v"
UDP local + muestras locales como plan B.
```

### En emulador Android

Cuando una app dentro del emulador Android necesita comunicarse con un servicio que corre en la máquina host, Android documenta que `127.0.0.1` apunta al propio loopback del emulador, y que para acceder al loopback del host se debe usar `10.0.2.2`.

Por tanto:

```text id="idageg"
FastAPI en host:
http://127.0.0.1:8000

Desde emulador Android:
http://10.0.2.2:8000
```

### En dispositivo físico

En dispositivo físico se usará la IP local del equipo en la red WiFi:

```text id="a9wvvf"
http://IP_DEL_PC:8000
```

### Transporte objetivo

```text id="sx33bs"
Python sender
   ↓ UDP
Android UDP receiver
   ↓ JNI
native C parser
   ↓ logs/UI
```

### Plan B

Si UDP se complica:

```text id="46sqcf"
samples/*.bin incluidos en la app
   ↓
Kotlin lee bytes del recurso local
   ↓ JNI
native C parser
   ↓ logs/UI
```

Este plan B mantiene lo importante: paquete binario, parser nativo, longitud, payload, buffer y mitigación.

---

## 11. Formato de paquetes

### Decisión

Se trabajará con dos niveles:

```text id="pyufar"
Nivel 1: RTCP-inspired
Nivel 2: RTCP parcial
```

No se implementará RTCP completo desde el primer día.

### Motivo

RFC 3550 define RTCP como paquetes de control con una cabecera fija similar a RTP, seguida de elementos estructurados que varían según el tipo de paquete. También indica que varios paquetes RTCP suelen enviarse juntos como paquetes compuestos y que esto se apoya en el campo de longitud de cada paquete RTCP.

### Formato Nivel 1 — RTCP-inspired

```text id="nym1xy"
MAGIC(4) | VERSION(1) | FLAGS(1) | PACKET_TYPE(1) | LENGTH(2) | SSRC(4) | PAYLOAD(N)
```

Campos:

| Campo       | Descripción                                        |
| ----------- | -------------------------------------------------- |
| MAGIC       | Identificador del laboratorio, por ejemplo `ECLB`. |
| VERSION     | Versión del formato del laboratorio.               |
| FLAGS       | Flags simuladas.                                   |
| PACKET_TYPE | Tipo de paquete de control.                        |
| LENGTH      | Longitud declarada del payload.                    |
| SSRC        | Identificador de stream simulado.                  |
| PAYLOAD     | Contenido procesado por el parser.                 |

### Formato Nivel 2 — RTCP parcial

Inspirado en RFC 3550:

```text id="y6lp6f"
V/P/RC | PT | LENGTH | SSRC | PAYLOAD
```

Campos:

| Campo   | Descripción                                                          |
| ------- | -------------------------------------------------------------------- |
| V       | Versión RTP/RTCP simulada.                                           |
| P       | Padding flag.                                                        |
| RC      | Reception report count o subtipo simulado.                           |
| PT      | Packet type.                                                         |
| LENGTH  | Longitud en palabras de 32 bits o simplificada de forma documentada. |
| SSRC    | Synchronization source identifier.                                   |
| PAYLOAD | Datos variables según tipo.                                          |

### Formato Nivel 3 — Ampliación

Paquetes compuestos:

```text id="le1tg7"
RTCP_PACKET_1 | RTCP_PACKET_2 | RTCP_PACKET_3
```

El parser seguro debe validar que las longitudes declaradas por cada subpaquete cuadran con el tamaño total recibido.

---

## 12. Tipo de vulnerabilidad simulada

### Decisión

```text id="z34ly5"
Heap-based buffer overflow.
```

### Patrón vulnerable

```text id="fxjt61"
payload_len = campo LENGTH controlado por el paquete
heap_buffer = malloc(MAX_SIZE)
memcpy(heap_buffer, payload, payload_len)
```

La versión vulnerable no valida correctamente si `payload_len` supera `MAX_SIZE` o si coincide con la longitud real disponible.

### Evidencia esperada

* fallo de memoria observable;
* crash controlado;
* detección por sanitizers;
* logs previos al fallo;
* comparación con versión segura.

### Relación con CVE

La elección se justifica porque CVE-2019-3568 está documentada como buffer overflow en la pila VoIP de WhatsApp, con CWE-787 en NVD y CWE-122 según la referencia asociada a Facebook.

---

## 13. Parser vulnerable y parser corregido

### Decisión

El laboratorio tendrá **dos artefactos nativos separados**:

```text
libechocall_vuln.so
libechocall_safe.so
```

La separación entre vulnerable y corregido será real, no solo un `if`, un flag o dos funciones dentro de la misma librería.

Esta decisión permite comparar de forma clara:

```text
misma entrada
→ parser vulnerable
→ fallo de memoria observable

misma entrada
→ parser corregido
→ rechazo controlado o procesamiento seguro
```

### Justificación

Esta separación se considera más profesional porque reproduce mejor el escenario de investigación de vulnerabilidades:

* existe una versión vulnerable;
* existe una versión corregida;
* se pueden comparar comportamientos;
* se puede documentar el cambio defensivo;
* se puede analizar el impacto de la validación añadida;
* se evita que el modo seguro y el vulnerable queden mezclados en una única implementación.

### Artefactos Android

En Android se compilarán dos librerías nativas:

```text
android-app/app/src/main/cpp/
├── vuln/
│   ├── vulnerable_parser.c
│   ├── packet_format.h
│   └── CMakeLists.txt
│
├── safe/
│   ├── safe_parser.c
│   ├── packet_format.h
│   └── CMakeLists.txt
│
└── common/
    ├── logger.c
    ├── logger.h
    ├── packet_format.h
    └── parser_result.h
```

Resultado esperado:

```text
libechocall_vuln.so
libechocall_safe.so
```

### Artefactos CLI para laboratorio

Además de las librerías Android, se crearán dos binarios para pruebas en WSL/Linux:

```text
native-core/build/
├── receiver_vuln
└── receiver_safe
```

Estos binarios permitirán:

* probar paquetes `.bin` fuera de Android;
* ejecutar AddressSanitizer;
* obtener evidencias limpias;
* automatizar pruebas;
* preparar fuzzing;
* comparar versión vulnerable y versión corregida antes de integrarlo en la app móvil.

### Relación entre Android y CLI

El código del parser debe compartirse todo lo posible:

```text
native-core/
├── include/
│   ├── packet_format.h
│   └── parser_result.h
│
├── src/
│   ├── vulnerable_parser.c
│   ├── safe_parser.c
│   └── logger.c
│
├── cli/
│   ├── receiver_vuln.c
│   └── receiver_safe.c
│
└── android-jni/
    ├── echocall_vuln_jni.c
    └── echocall_safe_jni.c
```

La lógica vulnerable y la lógica segura estarán separadas, pero ambas usarán el mismo formato de paquete y las mismas muestras de prueba.

---

## 14. Evidencia del fallo

### Decisión

Se buscará una evidencia parecida a la investigación real, pero sin hacer trivial la vulnerabilidad desde la UI.

### Cómo se hará

Habrá dos niveles de evidencia:

#### Evidencia visible en la app

La app mostrará señales de fallo, pero no explicará todo de forma inmediata:

```text id="pel0ev"
Call processing failed
Native parser error
Unexpected packet processing failure
Control packet rejected
```

#### Evidencia técnica en modo laboratorio

En logs técnicos y build debug se guardarán pistas más claras:

```text id="fiwzi7"
declared_length
actual_payload_size
max_buffer_size
parser_mode
validation_result
native_error
sanitizer_output
```

### Sanitizers

Se usarán herramientas de detección de errores de memoria en builds de laboratorio. Android NDK soporta AddressSanitizer para detectar bugs de memoria en aplicaciones con código nativo, y también HWAddressSanitizer en dispositivos Arm64 compatibles con Android 10/API 29 o superior.

### Recomendación

```text id="lphoin"
Debug técnico:
ASan/HWASan + logs detallados.

Demo visual:
error visible pero no completamente explicado.

Memoria:
explicación completa del fallo, evidencias y mitigación.
```

---

## 15. Modo vulnerable y modo seguro

### Decisión

El modo vulnerable y el modo seguro estarán separados mediante **dos librerías nativas distintas** y, opcionalmente, dos binarios CLI equivalentes.

```text
Modo vulnerable:
libechocall_vuln.so
receiver_vuln

Modo seguro:
libechocall_safe.so
receiver_safe
```

### En Android

La app cargará ambas librerías nativas en entorno de laboratorio:

```text
System.loadLibrary("echocall_vuln")
System.loadLibrary("echocall_safe")
```

La interfaz permitirá seleccionar qué motor usar:

```text
Mode: Vulnerable
Mode: Safe
```

Pero la separación real estará en la capa nativa, no solo en la interfaz.

### En pruebas de laboratorio

Las mismas muestras se ejecutarán contra ambos artefactos:

```text
samples/oversized_payload.bin
    → receiver_vuln
    → fallo observable

samples/oversized_payload.bin
    → receiver_safe
    → rechazo controlado
```

### Ventaja para la memoria

Esta separación permite documentar una comparación muy clara:

| Elemento                         | Versión vulnerable           | Versión corregida  |
| -------------------------------- | ---------------------------- | ------------------ |
| Validación de cabecera           | Parcial o insuficiente       | Estricta           |
| Validación de tipo               | Ausente o débil              | Tipos permitidos   |
| Validación de longitud máxima    | Ausente o insuficiente       | Obligatoria        |
| Longitud declarada vs real       | No comprobada correctamente  | Comprobada         |
| Copia de payload                 | Riesgosa                     | Limitada           |
| Resultado con paquete malformado | Fallo/crash/error de memoria | Rechazo seguro     |
| Evidencia                        | ASan/logs/crash controlado   | Logs de mitigación |

### Regla de diseño

No se implementará como una única función con un parámetro `safe=true/false`.

La separación debe ser visible en:

* nombres de archivos;
* nombres de librerías;
* scripts de compilación;
* logs;
* documentación;
* resultados de prueba.

### Decisión final

El laboratorio tendrá dos artefactos separados:

```text
Vulnerable:
libechocall_vuln.so
receiver_vuln

Corregido:
libechocall_safe.so
receiver_safe
```

Esta decisión sustituye a la opción anterior de una única librería con dos funciones.

---

## 16. Simulación zero-click en la UI

### Decisión

La simulación zero-click será:

```text id="bmm54y"
llamada entrante + paquete de control invisible + procesamiento automático previo a cualquier acción del usuario.
```

### Flujo

```text id="uc8yp3"
1. La app muestra una conversación con un contacto ficticio.
2. Aparece una llamada entrante.
3. La app recibe o carga automáticamente un paquete de control.
4. El usuario todavía no ha pulsado aceptar ni rechazar.
5. El paquete se pasa al parser nativo.
6. En modo vulnerable se produce fallo.
7. En modo seguro se rechaza el paquete.
8. La UI muestra el resultado.
```

### Punto importante

La demo debe dejar claro que:

```text id="knvvpa"
el procesamiento del paquete ocurre por llegada de la llamada, no por una acción explícita del usuario.
```

---

## 17. Muestras de paquetes

### Muestras iniciales

```text id="md089e"
valid_call_control.bin
oversized_payload.bin
length_mismatch.bin
truncated_packet.bin
```

### Muestras ampliadas

```text id="mnolv4"
invalid_type.bin
invalid_magic.bin
compound_valid.bin
compound_malformed.bin
padding_inconsistent.bin
```

### Clasificación

| Muestra                  | Objetivo                                                 |
| ------------------------ | -------------------------------------------------------- |
| `valid_call_control.bin` | Comprobar flujo normal.                                  |
| `oversized_payload.bin`  | Activar fallo en parser vulnerable.                      |
| `length_mismatch.bin`    | Probar inconsistencia entre longitud declarada y real.   |
| `truncated_packet.bin`   | Probar paquete incompleto.                               |
| `invalid_type.bin`       | Probar tipo no permitido.                                |
| `compound_malformed.bin` | Probar longitudes inconsistentes en paquetes compuestos. |

---

## 18. Logs

### Decisión

Todos los logs, pero separados por función.

```text id="o2pbo0"
logs/
├── app_events.log
├── packet_events.log
├── native_vulnerable.log
├── native_safe.log
├── security_events.log
├── sanitizer_output.log
└── test_results.log
```

### Eventos mínimos

```text id="yndpo0"
CALL_INCOMING
CONTROL_PACKET_RECEIVED
PACKET_HEADER_PARSED
DECLARED_LENGTH_READ
PAYLOAD_COPY_ATTEMPT
NATIVE_PARSE_STARTED
NATIVE_PARSE_FAILED
NATIVE_PARSE_OK
PACKET_REJECTED_INVALID_LENGTH
PACKET_REJECTED_INVALID_TYPE
SAFE_VALIDATION_OK
SAFE_VALIDATION_FAILED
```

---

## 19. Mitigaciones

### Decisión

La versión segura aplicará varias capas de mitigación.

### Mitigaciones obligatorias

* validación de `MAGIC` o cabecera;
* validación de versión;
* validación de tipo permitido;
* validación de longitud máxima;
* validación de longitud real frente a longitud declarada;
* rechazo de paquetes truncados;
* rechazo de paquetes compuestos inconsistentes;
* copias con límites;
* logs de seguridad;
* fail-safe ante error.

### Mitigaciones ampliadas

* parser aislado en proceso separado;
* límites de tamaño de paquete;
* límites de frecuencia;
* fuzzing;
* pruebas unitarias;
* sanitizers;
* build seguro;
* hardening de compilación.

---

## 20. Fuzzing

### Decisión

Se incluirá fuzzing como ampliación técnica fuerte.

### Fase 1

Fuzzer simple en Python:

```text id="mg1wfp"
generar paquetes aleatorios
→ enviarlos al parser seguro
→ comprobar que no crashea
→ registrar rechazos
```

### Fase 2

AFL++ o libFuzzer.

### Recomendación

Orden recomendado:

```text id="cwel5e"
1. Primero muestras manuales.
2. Después fuzzer simple en Python.
3. Después libFuzzer para el parser C.
4. AFL++ si hay tiempo y el entorno ya está estable.
```

No conviene empezar por AFL++ porque podría retrasar el MVP.

---

## 21. Tecnologías de ejecución

### Decisión recomendada

| Componente             | Tecnología                                          |
| ---------------------- | --------------------------------------------------- |
| App móvil              | Android Studio                                      |
| Lenguaje app           | Kotlin                                              |
| UI                     | Jetpack Compose                                     |
| Código nativo          | C                                                   |
| Integración nativa     | JNI / Android NDK                                   |
| Build nativo           | CMake                                               |
| Backend auxiliar       | FastAPI                                             |
| Generación de paquetes | Python                                              |
| Transporte             | UDP local + samples locales                         |
| Detección memoria      | ASan / HWASan si procede                            |
| Pruebas core           | WSL2 Ubuntu                                         |
| Pruebas app            | Android Emulator + dispositivo físico si es posible |
| Capturas               | Android Studio / adb / logs exportados              |

### Por qué WSL2

WSL2 será útil para:

* compilar prototipos C;
* probar el parser fuera de Android;
* ejecutar scripts Python;
* preparar fuzzing;
* usar herramientas Linux.

### Por qué Android Studio

Android Studio será el entorno principal para:

* UI Android;
* JNI;
* NDK;
* CMake;
* emulador;
* logs;
* depuración.

---

## 22. Repositorio

### Decisión

Repositorio bien estructurado, separado y documentado.

Nombre recomendado:

```text id="b8jc08"
echocall-lab-cve-2019-3568
```

Estructura:

```text id="ob6thi"
echocall-lab-cve-2019-3568/
├── README.md
├── LICENSE
├── docs/
│   ├── 01_threat_model.md
│   ├── 02_packet_format.md
│   ├── 03_native_parser.md
│   ├── 04_android_app.md
│   ├── 05_testing.md
│   ├── 06_mitigations.md
│   └── 07_relation_to_cve_2019_3568.md
│
├── android-app/
│   ├── app/
│   ├── build.gradle
│   ├── settings.gradle
│   └── README.md
│
├── native-core/
│   ├── include/
│   │   └── packet_format.h
│   ├── src/
│   │   ├── vulnerable_parser.c
│   │   ├── safe_parser.c
│   │   └── logger.c
│   ├── tests/
│   ├── CMakeLists.txt
│   └── README.md
│
├── backend/
│   ├── api.py
│   ├── requirements.txt
│   └── README.md
│
├── tools/
│   ├── generate_packet.py
│   ├── send_udp_packet.py
│   ├── fuzz_simple.py
│   └── run_lab.py
│
├── samples/
│   ├── valid_call_control.bin
│   ├── oversized_payload.bin
│   ├── length_mismatch.bin
│   └── truncated_packet.bin
│
├── logs/
│   └── .gitkeep
│
└── tests/
    ├── test_valid_packet.py
    ├── test_malformed_packets.py
    └── README.md
```

### README inicial

El `README.md` debe explicar desde el principio:

* qué es el laboratorio;
* qué CVE inspira el diseño;
* qué se emula;
* qué no se emula;
* cómo ejecutar el modo seguro;
* cómo ejecutar el modo vulnerable;
* cómo interpretar logs;
* limitaciones éticas;
* advertencia de que no es un exploit contra WhatsApp.

---

## 23. Plan de implementación por fases

### Fase 0 — Diseño

* cerrar formato de paquete;
* cerrar arquitectura;
* crear repo;
* documentar límites.

### Fase 0.5 — Prototipo lógico en Python

* definir y documentar un formato binario sintético de 13 bytes de cabecera;
* generar muestras locales benignas y malformadas de forma reproducible;
* comparar un parser Python con validación insuficiente y otro defensivo;
* procesar automáticamente, en una única pasada, los `.bin` del inbox local;
* registrar eventos comparables y contener las excepciones del parser;
* cubrir generación, parsers y procesador mediante pruebas aisladas;
* demostrar el patrón lógico sin corrupción de memoria, heap overflow ni RCE.

Esta fase es un prototipo seguro en Python. No implementa la vulnerabilidad de
memoria que se estudiará posteriormente y queda separada de la Fase 1 nativa.

### Fase 1 — Parser C fuera de Android

* implementar parser vulnerable;
* implementar parser seguro;
* generar muestras;
* ejecutar muestras desde CLI;
* obtener crash/evidencia;
* obtener rechazo seguro.

### Fase 2 — Android mínimo

* crear app Android;
* crear UI básica;
* integrar JNI;
* llamar al parser con muestras locales;
* mostrar resultado en pantalla.

### Fase 3 — Simulación zero-click

* pantalla de chat;
* llamada entrante simulada;
* procesamiento automático;
* paquete invisible;
* modo vulnerable/seguro;
* logs accesibles desde modo investigación, Logcat o exportación de evidencias.

### Fase 4 — UDP local

* receptor UDP en Android o backend;
* envío desde Python;
* pruebas en emulador;
* documentación de red local.

### Fase 5 — Evidencias

* logs separados;
* capturas;
* ASan/HWASan si es viable;
* comparación vulnerable/seguro;
* tabla de resultados.

### Fase 6 — Fuzzing

* fuzzer simple;
* libFuzzer;
* AFL++ si el entorno lo permite.

### Fase 7 — Nivel 4

* interfaz más pulida;
* demo visual;
* dashboard React opcional;
* README final;
* publicación GitHub.

---

## 24. Decisiones cerradas

| Aspecto                | Decisión                                       |
| ---------------------- | ---------------------------------------------- |
| Identidad              | App genérica de mensajería/VoIP                |
| Plataforma principal   | Android                                        |
| Nivel inicial          | Nivel 3                                        |
| Nivel aspiracional     | Nivel 4                                        |
| UI principal           | Android nativo                                 |
| UI recomendada         | Kotlin + Jetpack Compose                       |
| React                  | Dashboard auxiliar opcional                    |
| Backend                | FastAPI auxiliar                               |
| Parser                 | C                                              |
| Integración            | JNI/NDK                                        |
| Vulnerabilidad         | Heap-based buffer overflow                     |
| Transporte             | UDP local + samples locales                    |
| Formato                | RTCP-inspired → RTCP parcial                   |
| Modo vulnerable/seguro | Dos librerías nativas separadas y dos binarios CLI equivalentes |
| Zero-click             | Llamada entrante + paquete invisible           |
| Evidencia              | Logs + sanitizers + comparación                |
| Fuzzing                | Python simple → libFuzzer → AFL++              |
| Plan B                 | Samples locales sin UDP                        |
| Fuera de alcance       | WhatsApp real, exploit real, Pegasus, terceros |

---

## 25. Decisiones que aún quedan abiertas

Estas decisiones se cerrarán antes de programar:

1. Nombre definitivo de la app.
2. Kotlin o Java para la capa Android.
3. Jetpack Compose o XML layouts.
4. Decidir si las dos librerías nativas se cargarán en la misma app o mediante build variants separados.
5. Si UDP entra en Fase 3 o Fase 4.
6. Si React queda fuera o se mantiene como dashboard final.
7. Si el primer formato será `RTCP-inspired` o directamente `RTCP parcial`.
8. Si se probará en emulador, dispositivo físico o ambos.
9. Nivel exacto de detalle de logs visibles en la UI.
10. Herramienta principal de sanitización: ASan, HWASan o prototipo CLI con ASan en Linux.

---

## 26. Frase para la memoria

Se diseñará una aplicación Android de laboratorio, denominada provisionalmente EchoCall Lab, que emula el patrón técnico de CVE-2019-3568 mediante una interfaz de mensajería/VoIP simulada y un parser nativo vulnerable integrado con JNI. La aplicación procesará automáticamente paquetes de control de llamada inspirados en RTCP, sin interacción explícita del usuario, y permitirá comparar una versión vulnerable basada en validación insuficiente de longitud con una versión mitigada que valida estructura, tamaño, tipo y consistencia antes del procesamiento. El laboratorio no reproduce WhatsApp real ni su exploit, sino que recrea de forma controlada el patrón técnico relevante para estudiar impacto, evidencias y mitigaciones.
