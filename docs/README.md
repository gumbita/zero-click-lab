# Mapa documental

La documentación del repositorio combina el estado vigente de EchoCall con
material histórico que explica la evolución del laboratorio. Este mapa indica
qué fuente usar para cada propósito.

## Documentación actual

- [Arquitectura vigente](architecture.md): flujo UDP → Kotlin → JNI → parser C
  y selección fija por build.
- [Guía de reproducción segura](reproduction.md): niveles Python, Native Core,
  Android Patched y separación de la ruta Vulnerable/ASan.
- [Resultados experimentales](results.md): comparación final sobre la muestra
  canónica de 77 bytes.
- [Reversing estático](reversing.md): síntesis comparada de E-028/E-029.
- [Limitaciones](limitations.md): alcance, interpretación y custodia.
- [Diseño de interfaz de EchoCall](../documentacion/android/diseno-interfaz-echocall.md):
  arquitectura de producto y decisiones vigentes de la aplicación Android.
- [Plan de implementación de EchoCall](../documentacion/android/plan-implementacion-echocall.md):
  fases, validaciones y estado consolidado del desarrollo Android.

Estos dos documentos son la referencia autoritativa para el estado actual de
EchoCall.

## Especificación

- [Formato binario ECLB](02_packet_format.md): contrato sintético compartido
  por el prototipo histórico, Native Core y la integración Android.

## Diseño histórico

- [Diseño inicial del laboratorio de emulación](diseno_laboratorio_emulacion.md):
  decisiones y alternativas consideradas al inicio. No debe interpretarse como
  descripción exacta de la implementación actual.

## Evidencia histórica versionada

- [Guía de evidencias](evidencias/README.md): alcance, clasificación y custodia.
- [Registro de validación experimental](evidencias/registro_validacion_experimental.md):
  matriz histórica de resultados y carencias documentales.
- [E-028 — reversing estático Vulnerable](evidencias/artefactos/E-028/README.md).
- [E-029 — reversing estático Patched](evidencias/artefactos/E-029/README.md).

Los resultados finales están resumidos en [`results.md`](results.md). Parte de
la evidencia primaria de las Fases 8A/8B se mantiene bajo custodia externa
selectiva; E-028/E-029 preservan capturas estáticas, con la limitación de
custodia de sus ELF documentada explícitamente.
