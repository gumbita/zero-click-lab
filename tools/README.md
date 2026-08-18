# Herramientas auxiliares

Las dos utilidades usan únicamente la biblioteca estándar de Python. Ayudan a
preparar o transportar entradas ECLB; no implementan otro parser de EchoCall.

## Generar las muestras

`generate_samples.py` codifica la cabecera ECLB en big-endian y genera de forma
determinista las cinco entradas versionadas:

```text
python tools/generate_samples.py
```

Úsalo cuando quieras estudiar cómo se construyen los bytes o verificar que las
muestras pueden regenerarse. Después compara tamaños y SHA-256 con
[`samples/README.md`](../samples/README.md). No sustituyas evidencia custodiada
sin registrar la nueva identidad.

## Enviar un datagrama controlado

`send_udp_packet.py` lee un archivo sin interpretarlo y lo envía como un único
datagrama UDP:

```text
python tools/send_udp_packet.py --file <ruta> [--host <host>] [--port <puerto>]
```

| Argumento | Función |
|---|---|
| `--file` | Archivo binario obligatorio |
| `--host` | Destino; por defecto `127.0.0.1` |
| `--port` | Puerto `1..65535`; por defecto `43568` |

Comprobación inicial recomendada, con Android Patched en un entorno propio:

```text
python tools/send_udp_packet.py --host <IP_AUTORIZADA> --port 43568 --file samples/benign/valid_call_control.bin
```

Una salida como esta confirma el envío, no la recepción ni el resultado del
parser:

```text
destination=<host>:43568 file=valid_call_control.bin bytes_sent=17
```

Contrasta la recepción en la pantalla técnica de EchoCall o en el log Android.
No envíes la muestra oversized canónica a Vulnerable fuera del procedimiento
experimental ya cerrado, y no uses la herramienta contra terceros.
