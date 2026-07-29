# Formato binario del prototipo Python

## Alcance

La Fase 0.5 usa un formato sintético inspirado en paquetes de control. Permite
comparar una implementación con validación insuficiente y otra defensiva, pero
Python no reproduce un buffer overflow, corrupción de memoria ni RCE.

## Cabecera

El formato de `struct` es `>4sBBBHI`. El prefijo `>` establece orden de bytes
*big-endian*, tamaños estándar y ausencia de alineación nativa. La cabecera mide
13 bytes.

| Offset | Tamaño | Campo | Valor o semántica |
| ---: | ---: | --- | --- |
| 0 | 4 | `MAGIC` | `ECLB` (`45 43 4C 42`) |
| 4 | 1 | `VERSION` | `1` |
| 5 | 1 | `FLAGS` | `0` en las muestras |
| 6 | 1 | `PACKET_TYPE` | `1` (*call control*) |
| 7 | 2 | `LENGTH` | Longitud declarada del payload |
| 9 | 4 | `SSRC` | `0x10203040` en las muestras |
| 13 | N | `PAYLOAD` | Bytes restantes del archivo |

El parser seguro acepta únicamente `MAGIC=b"ECLB"`, `VERSION=1` y tipos del
conjunto `ALLOWED_TYPES={1}`. `LENGTH` no incluye la cabecera: debe ser igual al
número real de bytes desde el offset 13. Además, `LENGTH` no puede superar
`MAX_PAYLOAD_SIZE=32`.

La condición de consistencia es:

```text
tamaño_total = 13 + LENGTH
LENGTH = tamaño_real_del_payload
LENGTH <= 32
```

## Muestras reproducibles

### `valid_call_control.bin`

```text
45 43 4C 42 01 00 01 00 04 10 20 30 40 43 41 4C 4C
```

17 bytes; declara 4 bytes y contiene el payload `CALL` de 4 bytes.

SHA-256: `912b5f7f858a790d4c49ae2860cd421f0b70c8dd8e582abe99ab6d6640965b8e`

- Vulnerable: aceptada.
- Seguro: aceptada.

### `oversized_payload.bin`

```text
45 43 4C 42 01 00 01 00 40 10 20 30 40 54 49 4E 59
```

17 bytes; declara 64 bytes, contiene `TINY` (4 bytes) y supera el máximo de 32.

SHA-256: `3c3cd136ffb223449f226fe22061922371e8b8c11ec60f336e293f41f4047d30`

- Vulnerable: `IndexError` al acceder fuera del payload real.
- Seguro: rechazo `payload_too_large`; esta validación precede a la discrepancia.

### `length_mismatch.bin`

```text
45 43 4C 42 01 00 01 00 0A 10 20 30 40 53 48 4F 52 54
```

18 bytes; declara 10 bytes y contiene `SHORT` (5 bytes).

SHA-256: `b7b3e3d267ca313b943147a83c7461fb3e0553ef17a0b595ac986611a1b83584`

- Vulnerable: `IndexError` al acceder fuera del payload real.
- Seguro: rechazo `length_mismatch`.

### `truncated_packet.bin`

```text
45 43 4C 42 01
```

5 bytes; no contiene la cabecera completa de 13 bytes.

SHA-256: `9f46c77e1f2857e4e8d2a1c62403ef15275a664b20cf70acb2922f083cf1f18c`

- Vulnerable: `struct.error` al desempaquetar la cabecera incompleta.
- Seguro: rechazo `truncated_header` antes de desempaquetar.
