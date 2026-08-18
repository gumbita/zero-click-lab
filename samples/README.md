# Muestras ECLB

Las muestras convierten las reglas de ECLB en entradas concretas y
reproducibles. Cada archivo controla tres propiedades: tamaño total, longitud
declarada en la cabecera y longitud real del payload.

## Qué cambia en cada muestra

| Muestra | Bytes | `declared_length` | `actual_length` | Propósito | Resultado Patched |
|---|---:|---:|---:|---|---|
| `benign/valid_call_control.bin` | 17 | 4 | 4 | Flujo normal con payload `CALL` | `accepted/ok` |
| `malformed/length_mismatch.bin` | 18 | 10 | 5 | Declaración y contenido no coinciden | `length_mismatch` |
| `malformed/oversized_payload.bin` | 17 | 64 | 4 | Longitud declarada excesiva e incoherente | `payload_too_large` |
| `malformed/oversized_complete_payload.bin` | 77 | 64 | 64 | Supera el máximo de 32 manteniendo coherencia | `payload_too_large` |
| `malformed/truncated_packet.bin` | 5 | ND | ND | Cabecera incompleta | `truncated_header` |

La muestra canónica del experimento es `oversized_complete_payload.bin`:
`declared_length` coincide con `actual_length`, por lo que Vulnerable alcanza la
copia gobernada por 64. Patched rechaza antes porque 64 supera el máximo 32.

## Integridad

| Muestra | SHA-256 |
|---|---|
| `valid_call_control.bin` | `912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E` |
| `length_mismatch.bin` | `B7B3E3D267CA313B943147A83C7461FB3E0553EF17A0B595AC986611A1B83584` |
| `oversized_complete_payload.bin` | `516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA` |
| `oversized_payload.bin` | `3C3CD136FFB223449F226FE22061922371E8B8C11EC60F336E293F41F4047D30` |
| `truncated_packet.bin` | `9F46C77E1F2857E4E8D2A1C62403EF15275A664B20CF70ACB2922F083CF1F18C` |

[`tools/generate_samples.py`](../tools/generate_samples.py) regenera exactamente
este conjunto. La [especificación ECLB](../docs/02_packet_format.md) explica los
offsets y el orden big-endian.

## Uso seguro

1. Empieza por `valid_call_control.bin`.
2. Usa las entradas malformadas únicamente contra Patched para comprobar
   rechazos rutinarios.
3. No envíes `oversized_complete_payload.bin` a Vulnerable como quickstart,
   demostración o CI: puede alcanzar la escritura fuera de límites.
4. Calcula el SHA-256 antes de atribuir un resultado a una muestra.

Los archivos son construcciones propias; no contienen tráfico capturado ni
datos de terceros.
