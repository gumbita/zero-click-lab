# Mapa de aprendizaje

`technical-documentation/` es el centro documental técnico de EchoCall Lab. El
recorrido está
organizado por preguntas, no por las fases internas que condujeron al resultado.

## Quiero montar el laboratorio

- [Getting Started](getting-started.md): prepara las herramientas y lleva
  EchoCall Patched hasta la primera ejecución segura.

## Quiero entender el sistema

- [Arquitectura](architecture.md): sigue una entrada desde UDP hasta el parser C
  y explica la selección Vulnerable/Patched, JNI y el marcador pre-JNI.
- [EchoCall Android](../android-app/README.md): compila la aplicación y localiza
  los puntos de observación en Android.

## Quiero entender la entrada

- [Formato binario ECLB](packet-format.md): especificación canónica de la
  cabecera, semántica de `declared_length`, máximo defensivo y muestras.
- [Muestras](../samples/README.md): compara los bytes relevantes, su finalidad y
  el riesgo de ejecución.

## Quiero localizar la diferencia Vulnerable/Patched

- [Native Core](../native-core/README.md): relaciona `vulnerable_parser.c` y
  `safe_parser.c` con los tests y frontends CLI.
- [`vulnerable_parser.c`](../native-core/src/vulnerable_parser.c): reserva fija y
  copia gobernada por la entrada.
- [`safe_parser.c`](../native-core/src/safe_parser.c): validación temprana usada
  por Patched.

## Quiero estudiar los resultados

- [Resultados experimentales](experimental-results.md): misma entrada, dos variantes y resultados
  observables con instrumentación ASan.
- [Ingeniería inversa](reverse-engineering.md): contraste estático de la reserva/copia Vulnerable y
  la validación Patched.

## Quiero reproducir los experimentos

- [Reproducción experimental](experimental-reproduction.md): Native Core seguro, Android Patched y builds
  instrumentadas sin automatizar la ejecución peligrosa.
- [Herramientas](../tools/README.md): generación determinista y envío UDP
  controlado.

## Quiero contrastar la evidencia

- [Guía de evidencias](evidence/README.md): qué prueba respalda cada clase de
  afirmación.
- [Registro experimental](evidence/experimental-validation-log.md): matriz
  histórica con estados y carencias.
- [Procedencia Android](evidence/android-experiment-provenance.md): commits,
  candidatos, tamaños, hashes y custodia externa.
- [E-028 Vulnerable](evidence/artifacts/E-028/README.md) y
  [E-029 Patched](evidence/artifacts/E-029/README.md): reversing estático.

## Quiero entender hasta dónde llegan las conclusiones

- [Limitaciones](limitations.md): validez interna/externa, límites de ASan,
  custodia y afirmaciones no demostradas.
- [Uso seguro](../SECURITY.md): reglas operativas y comunicación privada.
