# Evidencias experimentales

> **REGISTRO HISTÓRICO VERSIONADO**
>
> Esta ruta conserva evidencia y reconstrucciones de etapas anteriores. La
> evidencia primaria final de las Fases 8A y 8B se mantiene bajo custodia
> externa selectiva; sus hashes, protocolo y conclusiones están documentados en
> el [diseño Android](../../documentacion/android/diseno-interfaz-echocall.md)
> y el
> [plan de implementación](../../documentacion/android/plan-implementacion-echocall.md).

Esta ruta contiene el registro versionado de la validación y la reconstrucción
documental del laboratorio zero-click. Cada resultado debe
distinguir si procede de un artefacto primario, del código o historial Git, de
un resultado reportado sin artefacto, o de una prueba aún pendiente. El código
de terminación se registra cuando aplica y está disponible; si no se obtuvo,
permanece explícitamente como `ND`.

La custodia es selectiva: no todo lo presente localmente en `docs/evidencias/`
se incorpora a Git. El [manifest](artefactos/manifest_sha256.md) distingue
artefactos versionados, externos/excluidos y pendientes de revisión.

## Contenido

- [Registro de validación experimental](registro_validacion_experimental.md):
  matriz maestra, carencias y criterio de capturas y logs.
- `registro_evidencias_zero_click.xlsx`: archivo XLSX local excluido
  temporalmente de Git. Solo se han confirmado existencia, tamaño y hash; su
  contenido interno no se certifica.
- [`artefactos/`](artefactos/README.md): convención para incorporar logs,
  capturas, manifiestos y salidas de herramientas sin perder trazabilidad.
- [E-028](artefactos/E-028/README.md): análisis estático Ghidra de Vulnerable;
  capturas preservadas y custodia física del ELF exacto no disponible.
- [E-029](artefactos/E-029/README.md): análisis estático Ghidra de Patched;
  capturas preservadas y custodia física del ELF exacto no disponible.
- [Comparativa técnica](../reversing.md): convergencia de evidencia estática y
  dinámica sin afirmar identidad binaria con los candidatos ASan finales.
- [Manifest SHA-256](artefactos/manifest_sha256.md): identidad de los
  artefactos actualmente custodiados.

## Clasificación de evidencia

- `PRIMARIA`: existe log, captura, transcripción, salida original o artefacto
  conservado que respalda directamente el resultado.
- `RECONSTRUIDA DOCUMENTALMENTE`: código, commits, recursos o artefactos
  actuales confirman la funcionalidad o configuración, pero no sustituyen la
  salida experimental original.
- `REPORTADA` o `REPORTADA SIN ARTEFACTO PRIMARIO`: el resultado se realizó y
  fue comunicado, pero no se conserva en este conjunto una salida primaria
  suficiente.
- `PENDIENTE`: todavía requiere una prueba o artefacto.

La recomendación de repetición es independiente del estado. E-022 se conserva
como evidencia primaria de una única ejecución concluyente y se clasifica como
`NO REPETIR`. E-025 conserva la ejecución comparativa SAFE por UDP con cadena
de custodia completa y también se clasifica como `PRIMARIA` y `NO REPETIR`.

E-021, E-022 y E-025 son evidencia histórica versionada previa a la matriz
final de variantes EchoCall. Los binarios, logs y capturas primarios de 8A/8B
no se incorporaron a esta carpeta: su ausencia del repositorio responde a la
política de custodia externa selectiva y no implica que los artefactos no
existan.

No deben registrarse como hechos resultados esperados, hipótesis ni resultados
históricos que no se hayan vuelto a comprobar.

El libro `registro_evidencias_zero_click.xlsx` existe, tiene 134 036 bytes y
SHA-256
`E423F751925BD4E144E103BF8478C788928A87B3FE033003481F2297D52D76AB`.
Contenido interno, fórmulas y formato pendientes de revisión con una herramienta
estructurada compatible con XLSX.
