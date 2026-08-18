# Muestras ECLB

Esta carpeta contiene entradas binarias del formato sintético ECLB usado por
EchoCall Lab. No son paquetes RTCP, tráfico de WhatsApp ni muestras capturadas
de terceros.

## Inventario

| Muestra | Bytes | SHA-256 | Resultado esperado en Patched | Uso recomendado |
|---|---:|---|---|---|
| `benign/valid_call_control.bin` | 17 | `912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E` | `status=accepted code=ok` | Comprobación segura inicial |
| `malformed/length_mismatch.bin` | 18 | `B7B3E3D267CA313B943147A83C7461FB3E0553EF17A0B595AC986611A1B83584` | `length_mismatch` | Test defensivo local |
| `malformed/oversized_complete_payload.bin` | 77 | `516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA` | `payload_too_large`; `declared_length=64`; `actual_length=64`; `maximum=32` | Solo Patched o procedimiento experimental autorizado |
| `malformed/oversized_payload.bin` | 17 | `3C3CD136FFB223449F226FE22061922371E8B8C11EC60F336E293F41F4047D30` | Rechazo por longitud | Test defensivo local |
| `malformed/truncated_packet.bin` | 5 | `9F46C77E1F2857E4E8D2A1C62403EF15275A664B20CF70ACB2922F083CF1F18C` | Rechazo por paquete truncado | Test defensivo local |

Los hashes anteriores se calculan sobre los archivos versionados. La
especificación de campos y endianness está en
[`docs/02_packet_format.md`](../docs/02_packet_format.md).
La utilidad estándar [`tools/generate_samples.py`](../tools/generate_samples.py)
permite regenerar exactamente este conjunto desde la raíz del repositorio.

## Ejecución segura

Empieza siempre por `valid_call_control.bin`. Las muestras malformadas se
incluyen para validar rechazos en el parser Patched y para conservar la
trazabilidad del laboratorio.

No envíes automáticamente `oversized_complete_payload.bin` a una variante
Vulnerable. Esa ruta puede provocar una escritura fuera de límites y queda
fuera del inicio rápido. Consulta [`SECURITY.md`](../SECURITY.md) y la
[guía de reproducción](../docs/reproduction.md).
