# Herramientas

Estas utilidades auxiliares usan únicamente la biblioteca estándar de Python;
no constituyen otra implementación de EchoCall ni requieren dependencias
externas.

## `generate_samples.py`

Genera de forma determinista las cinco muestras ECLB versionadas bajo
`samples/`. Conserva la utilidad vigente del prototipo histórico sin mantener
sus parsers ni su procesador paralelo.

```text
python tools/generate_samples.py
```

Antes de sustituir muestras con valor probatorio, compara tamaños y SHA-256 con
[`samples/README.md`](../samples/README.md) y los manifiestos de evidencia.

## `send_udp_packet.py`

Envía un archivo binario como un único datagrama UDP. La utilidad no interpreta
ni modifica el contenido: valida la ruta, el puerto y el máximo teórico de un
payload UDP, envía los bytes y muestra el destino y la cantidad transmitida.

```text
python tools/send_udp_packet.py --file <ruta> [--host <host>] [--port <puerto>]
```

Argumentos:

- `--file`: archivo obligatorio que se enviará;
- `--host`: destino, por defecto `127.0.0.1`;
- `--port`: puerto UDP, por defecto `43568`, con rango `1..65535`.

Desde la raíz del repositorio, la comprobación recomendada es la muestra
benigna:

```text
python tools/send_udp_packet.py --host <IP_DEL_ENTORNO_AUTORIZADO> --port 43568 --file samples/benign/valid_call_control.bin
```

Si el envío termina correctamente, la herramienta presenta una línea similar
a esta:

```text
destination=<host>:43568 file=valid_call_control.bin bytes_sent=17
```

Ese mensaje acredita el envío local, no la recepción ni el resultado del
parser. Confirma el retorno en Modo Lab o en el log de la aplicación.

No uses esta herramienta contra sistemas de terceros. No envíes la muestra
oversized a una variante Vulnerable fuera del procedimiento experimental
controlado; el [inicio rápido](../docs/reproduction.md) no lo hace.
