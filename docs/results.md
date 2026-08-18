# Resultados experimentales

La comparación final utilizó la misma muestra ECLB canónica y dos candidatos
Android ASan congelados cuyo commit fuente fue
`7bbb5ba984c55edfe2d0c6254253fb0ed9f2065d`.

`Patched ASan` y `Vulnerable ASan` designan, respectivamente, la variante
Patched o Vulnerable construida con instrumentación AddressSanitizer. ASan no
es una variante lógica del parser ni altera la diferencia de validación que se
compara.

## Entrada

| Propiedad | Valor |
|---|---|
| Archivo | `samples/malformed/oversized_complete_payload.bin` |
| Tamaño total | 77 bytes |
| `declared_length` | 64 |
| `actual_length` | 64 |
| Máximo Patched | 32 |
| SHA-256 | `516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA` |

## Resultado comparado

| Propiedad | Patched ASan | Vulnerable ASan |
|---|---|---|
| Parser | `safe_parse_packet` | `vulnerable_parse_packet` |
| Resultado principal | `payload_too_large` | `heap-buffer-overflow` |
| Operación | Rechazo antes del sink | `WRITE of size 64` mediante `__asan_memcpy` |
| Región afectada | No aplica | Heap de 32 bytes |
| Proceso | Permaneció vivo | Terminó |
| ASan/señal | Sin informe ASan observado en la ventana documentada | `ABORTING`, `SIGABRT` |

Patched devolvió exactamente:

```text
status=rejected code=payload_too_large declared_length=64 actual_length=64 maximum=32
```

En Vulnerable, ASan atribuyó la escritura a `vulnerable_parse_packet`. La
ejecución oversized Vulnerable fue única, está cerrada y no debe repetirse como
parte del onboarding o la CI.

## Qué demuestra

- La entrada coherente de 64 bytes alcanza una escritura fuera de los límites
  de una reserva heap de 32 bytes en el parser Vulnerable de EchoCall.
- El parser Patched rechaza esa misma condición mediante el límite semántico de
  32 bytes en la ejecución documentada.
- La evidencia estática E-028/E-029 y la evidencia dinámica convergen sobre la
  misma diferencia lógica del laboratorio.

## Qué no demuestra

- RCE, ejecución arbitraria o control del flujo.
- Explotabilidad completa, persistencia o compromiso del dispositivo.
- Seguridad general de Patched o ausencia de otras vulnerabilidades.
- Identidad binaria entre los ELF Debug analizados en E-028/E-029 y los APK
  ASan de la comparación dinámica.
- Equivalencia exacta con WhatsApp, RTCP o CVE-2019-3568.

La matriz histórica y la cadena de custodia disponible se conservan en el
[registro experimental](evidencias/registro_validacion_experimental.md).
