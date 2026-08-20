# Alcance y limitaciones

Estas limitaciones acotan la interpretación del experimento; no sustituyen la
explicación técnica de [arquitectura](architecture.md) y
[resultados](experimental-results.md).

## Validez interna

- La comparación usa la misma muestra ECLB, con payload declarado y real de 64
  bytes, sobre candidatos Android congelados.
- La ruta Vulnerable contiene deliberadamente una reserva de 32 bytes seguida
  de una copia gobernada por la longitud declarada.
- Patched aplica el máximo semántico de 32 antes del procesamiento.
- El diagnóstico dinámico procede de ASan, logs, señal, tombstone e información
  de terminación; el marcador pre-JNI es evidencia auxiliar.
- La ausencia de un informe ASan en la ventana Patched documentada no demuestra
  ausencia de otros errores o rutas vulnerables.

## Validez externa

- EchoCall, su flujo de llamada, el receptor y ECLB son construcciones propias.
- ECLB no es un protocolo real de VoIP y el tamaño 32/64 es una decisión del
  diseño experimental.
- CVE-2019-3568 motiva la pregunta sobre procesamiento automático y memoria,
  pero EchoCall no comparte su código, protocolo ni identidad binaria.
- Los resultados no deben extrapolarse a la seguridad de productos reales ni a
  todas las vulnerabilidades zero-click.

## Qué no se ha demostrado

- Ejecución remota de código (RCE) o ejecución arbitraria.
- Secuestro del flujo de control, shellcode o persistencia.
- Exfiltración, compromiso completo del dispositivo o explotabilidad práctica.
- Seguridad general de Patched o ausencia de otras vulnerabilidades.
- Equivalencia exacta con CVE-2019-3568.

## Límites de AddressSanitizer

ASan observa determinadas clases de errores de memoria durante las rutas que se
ejecutan; no prueba que las rutas no ejercitadas estén libres de fallos. La
[documentación Android NDK](https://developer.android.com/ndk/guides/asan)
indica además que ASan está obsoleto/no soportado desde 2023 y recomienda HWASan
en entornos ARM64 compatibles. EchoCall conserva ASan para reproducir la cadena
experimental `x86_64` ya documentada, no como recomendación general para nuevos
proyectos.

## Custodia y binarios

- Los candidatos finales y parte de la evidencia primaria permanecen bajo
  custodia externa selectiva; sus hashes están en
  [procedencia Android](evidence/android-experiment-provenance.md).
- Las capturas E-028/E-029 están versionadas, pero sus ELF Debug exactos con los
  hashes registrados no están disponibles actualmente.
- No se afirma identidad entre esos ELF y los APK ASan de la comparación
  dinámica.
- Versiones futuras del SDK, NDK, Gradle o sistema anfitrión pueden afectar la
  reconstrucción de builds históricos.

## Uso autorizado

El repositorio contiene código deliberadamente vulnerable. Las comprobaciones
rutinarias deben limitarse a Native Core Patched y Android Patched. Consulta
[`SECURITY.md`](../SECURITY.md) antes de ejecutar herramientas o muestras.
