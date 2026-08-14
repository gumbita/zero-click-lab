# Zero-click Lab / EchoCall Lab

Laboratorio controlado de investigación sobre patrones de vulnerabilidades
*zero-click*, inspirado principalmente en el patrón descrito públicamente para
CVE-2019-3568.

EchoCall es una aplicación propia. No contiene código de WhatsApp, no implementa
RTCP real y no constituye un exploit contra WhatsApp ni contra terceros. Utiliza
el formato sintético ECLB, un receptor UDP local y parsers creados para este
laboratorio. La experimentación se limita a entornos propios y controlados; no
se ha demostrado ejecución remota de código (RCE).

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

| Variante | Nombre instalado | `applicationId` | Parser |
|---|---|---|---|
| `vulnerableDebug` | EchoCall Lab — Vulnerable | `com.echocall.lab.vulnerable` | Vulnerable |
| `patchedDebug` | EchoCall Lab — Patched | `com.echocall.lab.patched` | Patched |
| `vulnerableAsan` | EchoCall Lab — Vulnerable ASan | `com.echocall.lab.vulnerable.asan` | Vulnerable |
| `patchedAsan` | EchoCall Lab — Patched ASan | `com.echocall.lab.patched.asan` | Patched |

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
| [`android-app/`](android-app/) | Aplicación EchoCall Android, Compose, UDP, JNI y variantes. |
| [`native-core/`](native-core/) | Parsers C, receptores CLI y tests nativos. |
| [`samples/`](samples/) | Muestras ECLB benignas y malformadas del laboratorio. |
| [`tools/`](tools/) | Utilidades controladas, incluido el emisor UDP. |
| [`documentacion/android/`](documentacion/android/) | Documentación autoritativa del estado Android actual. |
| [`docs/evidencias/`](docs/evidencias/) | Registro y evidencia histórica versionada. |
| [`docs/`](docs/) | Especificación ECLB y documentación histórica. |
| [`app/`](app/) | MVP Python inicial, conservado como componente histórico. |
| [`tests/`](tests/) | Tests seguros del MVP Python y sus muestras. |

## Por dónde empezar

1. Este `README.md`.
2. [`documentacion/android/diseno-interfaz-echocall.md`](documentacion/android/diseno-interfaz-echocall.md).
3. [`documentacion/android/plan-implementacion-echocall.md`](documentacion/android/plan-implementacion-echocall.md).
4. [`native-core/src/vulnerable_parser.c`](native-core/src/vulnerable_parser.c).
5. [`native-core/src/safe_parser.c`](native-core/src/safe_parser.c).
6. [`android-app/app/src/main/cpp/native_bridge.c`](android-app/app/src/main/cpp/native_bridge.c).
7. [`UdpPacketReceiver.kt`](android-app/app/src/main/java/com/echocall/lab/UdpPacketReceiver.kt).
8. [`tools/send_udp_packet.py`](tools/send_udp_packet.py).
9. [`docs/evidencias/`](docs/evidencias/).

El [mapa documental](docs/README.md) distingue la referencia actual de los
documentos históricos.

## Reproducibilidad

La trazabilidad final separa dos hitos:

- **commit fuente de los APK candidatos:**
  `7bbb5ba984c55edfe2d0c6254253fb0ed9f2065d`;
- **cierre documental de Fase 8:**
  `b0d26dec60a6abbafd5ff98928be377014cd5b99`.

Los APK experimentales no se atribuyen al commit documental. Sus hashes,
procedencia y resultados detallados están registrados en la documentación
Android. Los artefactos primarios finales se mantienen bajo custodia externa
selectiva y no se copian a este repositorio.

Las comprobaciones seguras del MVP Python y de Native Core se describen en sus
respectivos README. La ejecución de muestras malformadas requiere un
procedimiento experimental autorizado; no forma parte del inicio rápido.

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
