# EchoCall Lab

[![Safe CI](https://github.com/gumbita/zero-click-lab/actions/workflows/ci.yml/badge.svg)](https://github.com/gumbita/zero-click-lab/actions/workflows/ci.yml)

EchoCall Lab es un laboratorio experimental Android/JNI/C diseñado para
estudiar, de forma controlada y reproducible, el procesamiento automático de
datos antes de la interacción de la persona usuaria y su relación con errores
de seguridad de memoria.

El laboratorio implementa un flujo `UDP → Kotlin → JNI → C` en el que los
paquetes recibidos alcanzan automáticamente un parser nativo. Esto permite
comparar dos implementaciones del mismo componente: **Vulnerable** y
**Patched**. Ambas procesan ECLB, un formato binario sintético creado para el
proyecto.

La variante Vulnerable reproduce intencionadamente un patrón de validación
incorrecta de longitudes antes de una copia de memoria. Patched valida el
límite relevante antes de procesar el payload. Las pruebas nativas, los builds
Android, AddressSanitizer (ASan) y el reversing permiten observar esa diferencia
desde el código fuente, la ejecución y el binario.

El caso real que motiva la pregunta técnica es
[CVE-2019-3568](https://www.cve.org/CVERecord?id=CVE-2019-3568), cuyo registro
oficial describe un desbordamiento de buffer en una pila VoIP. EchoCall no
reproduce aquel producto: usa código, arquitectura y protocolo propios para
aislar experimentalmente un patrón de procesamiento automático y validación de
longitudes.

## Qué problema estudia

En un escenario *zero-click*, datos procedentes de una fuente externa pueden
alcanzar una superficie de procesamiento sin que exista una acción deliberada
de la persona destinataria. La cuestión de seguridad es qué validaciones se
aplican antes de que esos datos lleguen a operaciones sensibles, especialmente
cuando la ruta cruza hacia código nativo y gestión manual de memoria.

En EchoCall Lab, esa idea se modela haciendo que un datagrama ECLB se procese al
recibirlo, antes de cualquier acción de aceptar o rechazar en la interfaz. Esta
es la propiedad concreta del modelo, no una definición universal de todas las
vulnerabilidades zero-click.

## Arquitectura

```text
Sender controlado
      ↓ UDP :43568
EchoCall Android
      ↓
UdpPacketReceiver
      ↓
Kotlin
      ↓ JNI
NativeBridge.parsePacket()
      ↓
parser C fijado al compilar
      ├── Vulnerable
      └── Patched
```

Cada APK incorpora un solo parser. Gradle selecciona el flavor de seguridad y
CMake compila únicamente `vulnerable_parser.c` o `safe_parser.c`; no existe un
selector runtime entre ambos. Debug y ASan son formas de construir las dos
variantes, no parsers adicionales.

La [arquitectura completa](docs/architecture.md) explica el receptor, el
marcador persistente pre-JNI, la selección nativa y los puntos de observación.

## Vulnerable vs. Patched

El contraste conceptual —simplificado, no sustituto del código real— es:

```c
/* Vulnerable: el tamaño de copia procede de la entrada. */
buffer = malloc(32);
memcpy(buffer, payload, declared_length);
```

```c
/* Patched: el límite se comprueba antes del procesamiento. */
if (declared_length > MAX_PAYLOAD) {
    reject();
}
```

En el código real, Vulnerable comprueba primero que la longitud declarada
coincide con la recibida, pero no que quepa en la reserva de 32 bytes. Patched
aplica el máximo semántico antes de recorrer el payload:

- [`vulnerable_parser.c`](native-core/src/vulnerable_parser.c)
- [`safe_parser.c`](native-core/src/safe_parser.c), usado por Patched
- [especificación canónica de ECLB](docs/02_packet_format.md)

## Qué demuestra el laboratorio

```text
Recepción automática de una entrada ECLB controlada
                        ↓
                 Kotlin → JNI → C
                        ↓
       longitud declarada = longitud real = 64
                        ↓
        ┌───────────────┴────────────────┐
        ↓                                ↓
Vulnerable                         Patched
copia 64 bytes en                 valida máximo 32
destino de 32                          ↓
        ↓                         rechazo controlado
ASan observa escritura
fuera de límites
```

ASan es instrumentación de diagnóstico: ayuda a detectar accesos de memoria
inválidos durante una ejecución y aporta ubicación, tipo de operación y límites
de la región afectada. En EchoCall hizo observable el overflow de la ruta
Vulnerable; no convierte Debug/ASan en variantes lógicas diferentes.

## Resultado experimental

La misma muestra ECLB de 77 bytes —payload declarado y real de 64 bytes— se
aplicó a los dos candidatos Android ASan congelados:

| Variante | Resultado observado |
|---|---|
| Patched + ASan | `payload_too_large`; proceso vivo; sin informe ASan observado en la ventana documentada |
| Vulnerable + ASan | `heap-buffer-overflow`; `WRITE` de 64 bytes; región heap de 32 bytes; `SIGABRT` |

El resultado enlaza una diferencia de código con una diferencia observable de
ejecución. [Resultados](docs/results.md) explica qué demuestra cada dato;
[reversing](docs/reversing.md) muestra cómo la validación y la copia aparecen en
el análisis estático.

## Quick start seguro

El recorrido inicial construye y ejecuta únicamente Native Core Patched:

```text
cmake -S native-core -B <TEMP_BUILD_DIR> -DENABLE_ASAN=OFF
cmake --build <TEMP_BUILD_DIR> --target test_safe_parser receiver_safe
ctest --test-dir <TEMP_BUILD_DIR> --output-on-failure
```

CTest ejecuta `test_safe_parser` y `receiver_safe`; no invoca
`receiver_vuln`. Continúa con [Android Patched y la reproducción
segura](docs/reproduction.md). No envíes la muestra oversized a Vulnerable como
prueba rutinaria.

## Ruta de aprendizaje

| Pregunta | Documento |
|---|---|
| ¿Cómo llega una entrada hasta C? | [Arquitectura](docs/architecture.md) |
| ¿Qué contiene un paquete ECLB? | [Formato ECLB](docs/02_packet_format.md) |
| ¿Dónde está la diferencia de validación? | [Native Core](native-core/README.md) |
| ¿Qué ocurrió en el experimento? | [Resultados](docs/results.md) |
| ¿Qué revela el binario? | [Reversing](docs/reversing.md) |
| ¿Cómo ejecuto comprobaciones seguras? | [Reproducción](docs/reproduction.md) |
| ¿En qué pruebas se basa cada afirmación? | [Evidencias](docs/evidencias/README.md) |
| ¿Hasta dónde llegan las conclusiones? | [Limitaciones](docs/limitations.md) |

El [mapa de aprendizaje](docs/README.md) ofrece el recorrido completo.

## Estructura del repositorio

| Ruta | Función |
|---|---|
| [`android-app/`](android-app/README.md) | Aplicación Android, recepción UDP, Kotlin y JNI |
| [`native-core/`](native-core/README.md) | Contrato C, parsers, CLI y tests seguros |
| [`samples/`](samples/README.md) | Entradas ECLB explicadas y versionadas |
| [`tools/`](tools/README.md) | Generador de muestras y emisor UDP controlado |
| [`docs/`](docs/README.md) | Centro de aprendizaje técnico |
| [`docs/evidencias/`](docs/evidencias/README.md) | Registro, hashes, procedencia y capturas seleccionadas |

## Evidencias

Las conclusiones combinan cuatro clases de prueba:

- código fuente y tests de Native Core;
- resultados dinámicos instrumentados con ASan;
- reversing estático E-028/E-029;
- hashes, manifiestos y registros de procedencia.

La relación entre los APK fuente, la muestra y la custodia externa se conserva
en [procedencia del experimento Android](docs/evidencias/procedencia-experimento-android.md).

## Alcance y limitaciones

EchoCall usa una arquitectura sintética y el protocolo propio ECLB. La evidencia
demuestra una escritura fuera de límites concreta en el laboratorio y el
rechazo temprano de esa condición por Patched. No demuestra RCE, secuestro del
flujo, compromiso completo, seguridad general de Patched ni equivalencia
binaria con CVE-2019-3568. Consulta [Limitaciones](docs/limitations.md) para los
límites de custodia, ASan y validez externa.

## Uso responsable

El repositorio contiene código deliberadamente vulnerable. Úsalo únicamente en
infraestructura propia y aislada. Consulta [SECURITY.md](SECURITY.md) antes de
ejecutar muestras o herramientas.

## Referencias técnicas

- [CVE.org — CVE-2019-3568](https://www.cve.org/CVERecord?id=CVE-2019-3568)
- [CVE Services — registro CNA](https://cveawg.mitre.org/api/cve/CVE-2019-3568)
- [Meta Security Advisory — CVE-2019-3568](https://www.facebook.com/security/advisories/cve-2019-3568)
- [Android NDK — Address Sanitizer](https://developer.android.com/ndk/guides/asan)
- [LLVM/Clang — AddressSanitizer](https://clang.llvm.org/docs/AddressSanitizer.html)

## Autoría, licencia y citación

**Autora:** Àngels Gumbau Granero

Copyright © 2026 Àngels Gumbau Granero.

El material original de EchoCall Lab cuya titularidad corresponde a la autora
se distribuye bajo la [GNU General Public License v3.0](LICENSE)
(`GPL-3.0-only`).

Si utilizas EchoCall Lab en un trabajo académico, educativo o técnico,
consulta [`CITATION.cff`](CITATION.cff) para citar el proyecto.
