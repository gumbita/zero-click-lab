# Formato binario ECLB

## Alcance y evolución

ECLB es un formato sintético propio del laboratorio. Se originó en el MVP
Python y continúa como contrato compartido por Native Core y EchoCall Android.
No es RTCP real, no contiene datos de WhatsApp y no pretende reproducir el
protocolo privado asociado a CVE-2019-3568.

La especificación vigente se corresponde con
[`native-core/include/packet_format.h`](../native-core/include/packet_format.h)
y con las validaciones de
[`safe_parser.c`](../native-core/src/safe_parser.c) y
[`vulnerable_parser.c`](../native-core/src/vulnerable_parser.c).

## Cabecera

El formato histórico de Python se expresa como `>4sBBBHI`. El prefijo `>`
establece orden de bytes *big-endian*, tamaños estándar y ausencia de
alineación nativa. La implementación C decodifica explícitamente los enteros
multibyte con el mismo orden. La cabecera mide 13 bytes.

| Offset | Tamaño | Campo | Valor o semántica |
| ---: | ---: | --- | --- |
| 0 | 4 | `MAGIC` | `ECLB` (`45 43 4C 42`) |
| 4 | 1 | `VERSION` | `1` |
| 5 | 1 | `FLAGS` | `0` en las muestras actuales |
| 6 | 1 | `PACKET_TYPE` | `1` (*call control*) |
| 7 | 2 | `LENGTH` | Longitud declarada del payload |
| 9 | 4 | `SSRC` | `0x10203040` en las muestras actuales |
| 13 | N | `PAYLOAD` | Bytes restantes del paquete |

`LENGTH` no incluye la cabecera. Un paquete coherente cumple:

```text
tamaño_total = 13 + LENGTH
LENGTH = tamaño_real_del_payload
```

La ruta Patched añade el límite defensivo `LENGTH <= 32`. Valida argumentos,
cabecera, magic, versión y tipo; rechaza una longitud superior al máximo antes
de comprobar la igualdad entre longitud declarada y real. La ruta Vulnerable
comprueba la coherencia declarada/real, pero reserva un buffer fijo de 32 bytes
y copia `LENGTH` bytes sin imponer ese máximo.

## Muestras

### Muestra válida compartida

`samples/benign/valid_call_control.bin`:

```text
45 43 4C 42 01 00 01 00 04 10 20 30 40 43 41 4C 4C
```

17 bytes; declara 4 bytes y contiene el payload `CALL` de 4 bytes.

SHA-256:
`912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E`

Los parsers Vulnerable y Patched aceptan esta entrada.

### Muestra canónica oversized final

`samples/malformed/oversized_complete_payload.bin` contiene 77 bytes: una
cabecera de 13 bytes que declara 64 bytes y un payload real de 64 bytes.

SHA-256:
`516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA`

En las ejecuciones finales documentadas, Patched devolvió
`payload_too_large`; Vulnerable ASan detectó un `heap-buffer-overflow` al copiar
64 bytes sobre una región heap de 32 bytes. Estos resultados no demuestran RCE
ni equivalencia exacta con CVE-2019-3568.

### Muestras históricas del prototipo Python

`samples/malformed/oversized_payload.bin`:

```text
45 43 4C 42 01 00 01 00 40 10 20 30 40 54 49 4E 59
```

17 bytes; declara 64 bytes, contiene `TINY` (4 bytes) y supera el máximo de 32.

SHA-256:
`3C3CD136FFB223449F226FE22061922371E8B8C11EC60F336E293F41F4047D30`

En el MVP Python, la ruta insuficientemente validada producía `IndexError` y la
ruta defensiva devolvía `payload_too_large`. En Native Core, Vulnerable la
rechaza como `length_mismatch` antes de copiar, mientras Patched conserva la
prioridad de `payload_too_large`.

`samples/malformed/length_mismatch.bin`:

```text
45 43 4C 42 01 00 01 00 0A 10 20 30 40 53 48 4F 52 54
```

18 bytes; declara 10 bytes y contiene `SHORT` (5 bytes).

SHA-256:
`B7B3E3D267CA313B943147A83C7461FB3E0553EF17A0B595AC986611A1B83584`

En el MVP Python, la ruta insuficientemente validada producía `IndexError`; la
ruta defensiva devolvía `length_mismatch`. Los dos parsers C actuales también
rechazan la incoherencia sin alcanzar la copia vulnerable.

`samples/malformed/truncated_packet.bin`:

```text
45 43 4C 42 01
```

5 bytes; no contiene la cabecera completa de 13 bytes.

SHA-256:
`9F46C77E1F2857E4E8D2A1C62403EF15275A664B20CF70ACB2922F083CF1F18C`

En el MVP Python, la ruta insuficientemente validada producía `struct.error`;
la defensiva rechazaba `truncated_header`. Los parsers C actuales rechazan
igualmente la cabecera truncada.

Las muestras malformadas se conservan para experimentos expresamente
autorizados. Este documento describe su semántica, pero no constituye una
receta de ejecución.
