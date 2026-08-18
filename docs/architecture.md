# Arquitectura vigente

EchoCall es una aplicación Android propia para estudiar procesamiento
preinteracción y validación de memoria dentro de un laboratorio controlado.

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
parser C compilado
    ├── Vulnerable
    └── Patched
```

`UdpPacketReceiver` procesa el datagrama recibido antes de que la persona
acepte o rechace una llamada simulada. Kotlin persiste primero un marcador de
operación pendiente y después invoca la interfaz JNI común.

Gradle define el flavor de seguridad y transmite a CMake
`ECHOCALL_PARSER_IMPLEMENTATION=VULNERABLE|PATCHED`. CMake selecciona una sola
fuente nativa, y `native_bridge.c` enlaza `NativeBridge.parsePacket()` con esa
implementación. Por tanto:

- no existe selector runtime entre Vulnerable y Patched;
- cada APK contiene un único parser EchoCall;
- la interfaz Kotlin/JNI y la UI son compartidas;
- los `applicationId` permiten distinguir las cuatro variantes;
- Debug y ASan cambian la instrumentación, no el contrato ECLB.

## Componentes

| Componente | Responsabilidad |
|---|---|
| `android-app/` | UDP, ciclo de vida, UI, marcador, JNI y variantes |
| `native-core/` | Contrato C, parsers, CLI y tests seguros |
| `samples/` | Entradas ECLB versionadas |
| `tools/` | Emisor UDP controlado |
| `app/` | Modelo Python de referencia y trazabilidad histórica |
| `docs/evidencias/` | Registro y evidencia técnica seleccionada |

## Límites del modelo

ECLB y el flujo de llamada son sintéticos. La arquitectura no contiene código
de WhatsApp, no implementa RTCP real y no reproduce exactamente
CVE-2019-3568. El resultado experimental acredita una escritura fuera de
límites dentro de EchoCall; no acredita RCE ni control del flujo.
