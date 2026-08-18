# EchoCall Lab

[![Safe CI](https://github.com/gumbita/zero-click-lab/actions/workflows/ci.yml/badge.svg)](https://github.com/gumbita/zero-click-lab/actions/workflows/ci.yml)

Laboratorio controlado de Android/JNI/C para estudiar procesamiento previo a la
interacción y validación de memoria en escenarios *zero-click* sintéticos. El
contexto de investigación está inspirado en el patrón descrito públicamente
para CVE-2019-3568, sin reproducir su implementación.

EchoCall es una aplicación propia. No contiene código de WhatsApp, no implementa
RTCP real y no constituye un exploit contra WhatsApp ni contra terceros. Utiliza
el formato sintético ECLB, un receptor UDP local y parsers creados para este
laboratorio. La experimentación se limita a entornos propios y controlados; no
se ha demostrado ejecución remota de código (RCE).

En este repositorio, *zero-click* describe que el paquete entrante llega al
parser antes de que la persona pulse Aceptar o Rechazar. No significa que se
haya demostrado compromiso remoto, explotación completa o ausencia absoluta de
interacción en todos los niveles del sistema.

## Estado actual

El laboratorio actual incluye:

- una aplicación Android de mensajería y llamadas simuladas;
- recepción UDP en el puerto `43568`;
- procesamiento automático previo a la interacción de la persona usuaria;
- integración Kotlin → JNI → C mediante `NativeBridge.parsePacket()`;
- variantes Vulnerable y Patched con el parser fijado al compilar;
- builds Debug y ASan;
- evidencia experimental final de las Fases 8A y 8B cerrada y documentada.

La interfaz normal es compartida por ambas variantes. Los detalles técnicos se
mantienen en Modo Lab y en la documentación de investigación.

## Arquitectura

```text
PC / sender
    ↓ UDP :43568
EchoCall Android
    ↓
UdpPacketReceiver
    ↓
Kotlin
    ↓ JNI
NativeBridge.parsePacket()
    ↓
parser C
    ├── Vulnerable
    └── Patched
```

Cada APK contiene una sola implementación de parser. La elección no se realiza
en runtime: el flavor selecciona la fuente nativa durante el build.

## Variantes del laboratorio

### Vulnerable

Implementación deliberadamente insegura que copia la longitud declarada sobre
una reserva fija sin validar antes el máximo relevante.

### Patched

Implementación defensiva que valida el límite semántico antes de procesar el
payload.

## Builds e instrumentación

Debug y AddressSanitizer son dos formas de construir las mismas variantes, no
parsers adicionales. ASan instrumenta el código nativo para detectar
determinados errores de memoria durante una ejecución.

| Variante | Build | Tarea Gradle | `applicationId` |
|---|---|---|---|
| Vulnerable | Debug | `assembleVulnerableDebug` | `com.echocall.lab.vulnerable` |
| Patched | Debug | `assemblePatchedDebug` | `com.echocall.lab.patched` |
| Vulnerable | ASan | `assembleVulnerableAsan` | `com.echocall.lab.vulnerable.asan` |
| Patched | ASan | `assemblePatchedAsan` | `com.echocall.lab.patched.asan` |

## Resultado experimental principal

La comparación final utilizó la misma muestra canónica ECLB de 77 bytes sobre
los dos candidatos ASan congelados:

- **Patched ASan:** devolvió `payload_too_large`, limpió el marcador pendiente y
  mantuvo el proceso vivo; no se observó un informe ASan en esa ejecución.
- **Vulnerable ASan:** ASan detectó un `heap-buffer-overflow` durante un `WRITE`
  de 64 bytes sobre una región heap de 32 bytes; el proceso terminó mediante
  `SIGABRT`.

Este resultado demuestra instrumentalmente una escritura fuera de límites en
heap dentro de EchoCall Lab. No demuestra RCE, secuestro del flujo de control ni
equivalencia exacta con CVE-2019-3568. Tampoco permite afirmar seguridad general
de la variante Patched.

El alcance, los conteos, las huellas y las limitaciones se encuentran en el
[diseño Android](documentacion/android/diseno-interfaz-echocall.md) y el
[plan de implementación](documentacion/android/plan-implementacion-echocall.md).

## Estructura del repositorio

| Ruta | Función |
|---|---|
| [`android-app/`](android-app/README.md) | Aplicación EchoCall Android, Compose, UDP y JNI. |
| [`native-core/`](native-core/) | Parsers C, receptores CLI y tests nativos. |
| [`samples/`](samples/README.md) | Muestras ECLB benignas y malformadas del laboratorio. |
| [`tools/`](tools/README.md) | Utilidades auxiliares para generar muestras y realizar envíos controlados. |
| [`documentacion/android/`](documentacion/android/) | Documentación autoritativa del estado Android actual. |
| [`docs/evidencias/`](docs/evidencias/) | Registro y evidencia histórica versionada. |
| [`docs/`](docs/) | Arquitectura, ECLB, reproducción, resultados, reversing y límites. |

## Por dónde empezar

1. Este `README.md`.
2. [Guía de reproducción segura](docs/reproduction.md).
3. [Arquitectura vigente](docs/architecture.md).
4. [Android](android-app/README.md) y [Native Core](native-core/README.md).
5. [Resultados](docs/results.md), [reversing](docs/reversing.md) y
   [limitaciones](docs/limitations.md).
6. [Evidencias seleccionadas](docs/evidencias/README.md).

El [mapa documental](docs/README.md) distingue la referencia actual de los
documentos históricos.

## Reproducibilidad

Inicio rápido seguro desde la raíz del repositorio:

```text
cmake -S native-core -B <TEMP_BUILD_DIR> -DENABLE_ASAN=OFF
cmake --build <TEMP_BUILD_DIR> --target test_safe_parser receiver_safe
ctest --test-dir <TEMP_BUILD_DIR> --output-on-failure
```

Después continúa con Android Patched en la [guía operativa](docs/reproduction.md).
El quick start no construye ni ejecuta la ruta Vulnerable y nunca envía
automáticamente la muestra oversized.

La trazabilidad final separa dos hitos:

- **commit fuente de los APK candidatos:**
  `7bbb5ba984c55edfe2d0c6254253fb0ed9f2065d`;
- **cierre documental de Fase 8:**
  `b0d26dec60a6abbafd5ff98928be377014cd5b99`.

Los APK experimentales no se atribuyen al commit documental. Sus hashes,
procedencia y resultados detallados están registrados en la documentación
Android. Los artefactos primarios finales se mantienen bajo custodia externa
selectiva y no se copian a este repositorio.

Las comprobaciones seguras de Native Core se describen en su README. La
ejecución de muestras malformadas requiere un procedimiento experimental
autorizado; no forma parte del inicio rápido.

## Uso responsable

Consulta [SECURITY.md](SECURITY.md) antes de ejecutar componentes del
laboratorio. No utilices el receptor, las muestras ni las herramientas contra
sistemas o personas de terceros.

## Referencias técnicas

- [NVD — CVE-2019-3568](https://nvd.nist.gov/vuln/detail/CVE-2019-3568)
- [CVE Services — CVE-2019-3568](https://cveawg.mitre.org/api/cve/CVE-2019-3568)
- [Meta Security Advisory — CVE-2019-3568](https://www.facebook.com/security/advisories/cve-2019-3568)
- [Android NDK — Address Sanitizer](https://developer.android.com/ndk/guides/asan)

Estas fuentes delimitan el contexto público. ECLB, sus parsers, tamaños,
eventos y líneas de código son decisiones exclusivas de EchoCall Lab.
