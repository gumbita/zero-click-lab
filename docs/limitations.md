# Limitaciones

## Alcance técnico

- EchoCall, ECLB, el receptor UDP y ambos parsers son construcciones propias.
- No se utiliza código de WhatsApp ni se implementa RTCP/SRTCP real.
- CVE-2019-3568 aporta contexto público, no identidad de protocolo, código o
  exploit.
- El procesamiento preinteracción estudiado pertenece a esta aplicación de
  laboratorio y no prueba un ataque contra terceros.

## Interpretación de resultados

- ASan detectó una escritura fuera de límites concreta; no se demostró RCE,
  control del flujo ni ejecución arbitraria.
- Un crash no equivale a explotabilidad completa.
- El rechazo de una muestra por Patched no demuestra seguridad general.
- La ausencia de firmas ASan en una ventana concreta no prueba ausencia de
  errores en todas las rutas o entradas.

## Custodia y reproducibilidad

- Las capturas de E-028/E-029 están preservadas, pero los ELF exactos con los
  hashes registrados no están disponibles actualmente.
- No se afirma identidad entre esos ELF Debug y los binarios ASan usados en la
  comparación dinámica.
- Algunos artefactos primarios finales permanecen bajo custodia externa y no
  se publican como builds en Git.
- Versiones futuras del SDK, NDK o Gradle pueden afectar la reproducción de los
  builds Android históricos.

## Uso

El repositorio contiene código deliberadamente vulnerable. Debe emplearse solo
en infraestructura propia y aislada, conforme a [`SECURITY.md`](../SECURITY.md).
