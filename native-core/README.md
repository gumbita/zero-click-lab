# Native Core de EchoCall Lab

`native-core` contiene la implementación C compartida por las herramientas CLI
y la aplicación Android. Su objetivo es comparar dos parsers del formato
sintético ECLB dentro de un laboratorio controlado:

- `safe_parser.c`: implementación defensiva usada por las variantes Patched;
- `vulnerable_parser.c`: implementación deliberadamente vulnerable usada por
  las variantes Vulnerable;
- `receiver_safe` y `receiver_vuln`: frontends CLI para sus respectivos parsers;
- tests unitarios y de integración segura mediante CTest.

## Contrato ECLB

La cabecera ocupa 13 bytes. Los enteros multibyte se interpretan explícitamente
en big-endian y el payload comienza en el offset 13.

```text
MAGIC(4) | VERSION(1) | FLAGS(1) | TYPE(1) | LENGTH(2) | SSRC(4) | PAYLOAD(N)
```

El contrato actual está definido por
[`include/packet_format.h`](include/packet_format.h) y documentado en
[`../docs/02_packet_format.md`](../docs/02_packet_format.md). La versión vigente
usa `MAGIC=ECLB`, `VERSION=1`, `TYPE=1` y un máximo defensivo de 32 bytes para el
payload Patched.

## Parsers

### Patched / safe

`safe_parse_packet()` valida argumentos, cabecera, magic, versión, tipo, máximo
de payload y coincidencia entre longitud declarada y real antes de procesar el
payload. Una longitud superior a 32 devuelve `payload_too_large`.

### Vulnerable

`vulnerable_parse_packet()` conserva deliberadamente la condición experimental:
reserva un destino de 32 bytes y copia la longitud declarada después de comprobar
su coincidencia con la longitud real, pero sin validar el máximo de destino.

Este código existe únicamente con fines académicos. No debe ejecutarse con
muestras malformadas fuera del procedimiento controlado y autorizado del
laboratorio.

## CLI

Ambos receptores leen un archivo local y presentan el resultado normalizado del
parser. Aplican además un límite de lectura de 1 MiB, propio de la CLI y ajeno al
formato ECLB.

| Programa | Parser |
|---|---|
| `receiver_safe` | Patched / safe |
| `receiver_vuln` | Vulnerable |

La recepción UDP pertenece a la aplicación Android y no a estas CLI. La
integración Android compila una sola fuente de parser por variante y accede a
ella a través de JNI.

## Build y tests seguros

Ejecuta desde `<REPO_ROOT>` y sustituye `<TEMP_BUILD_DIR>` por un directorio
temporal fuera del repositorio:

```text
cmake -S native-core -B <TEMP_BUILD_DIR> -DENABLE_ASAN=OFF
cmake --build <TEMP_BUILD_DIR>
ctest --test-dir <TEMP_BUILD_DIR> --output-on-failure
```

CTest ejecuta únicamente `test_safe_parser` y `receiver_safe`; no invoca
`receiver_vuln`. Para una comprobación manual benigna puede usarse:

```text
<TEMP_BUILD_DIR>/receiver_safe samples/benign/valid_call_control.bin
```

Resultado esperado:

```text
status=accepted code=ok
```

No se incluye una receta rápida para ejecutar `receiver_vuln` con muestras
malformadas. Las muestras y evidencias experimentales se describen en la
[guía de reproducción](../docs/reproduction.md), la
[documentación Android](../android-app/README.md) y el
[registro de evidencias](../docs/evidencias/README.md).

## Límites

- ECLB no es RTCP real.
- Native Core no contiene código de WhatsApp.
- El fallo del parser Vulnerable pertenece a EchoCall Lab.
- La evidencia acredita una escritura fuera de límites detectada por ASan, no
  RCE, control del flujo ni explotabilidad completa.
