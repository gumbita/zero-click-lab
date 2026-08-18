# Modelo/prototipo Python de referencia

> **MODELO HISTÓRICO DE REFERENCIA**
>
> Esta carpeta conserva la primera etapa del laboratorio. No describe la
> demostración principal actual de EchoCall.

El MVP inicial modeló en Python una validación insuficiente del formato
sintético ECLB. Al no existir una copia a memoria nativa, el caso problemático
se materializaba como una excepción controlada de Python: no producía
corrupción de memoria, un *heap buffer overflow* nativo ni ejecución remota de
código.

Esta implementación permitió fijar el formato de entrada y comparar una ruta
insuficientemente validada con otra defensiva. Más adelante fue sustituida como
demostración principal por el [Native Core](../native-core/README.md) en C y la
aplicación Android EchoCall, que integran recepción UDP, JNI y variantes cuyo
parser se selecciona al compilar.

El código Python se conserva por trazabilidad, sigue respaldando los tests
seguros de `tests/` y permite explicar la evolución del contrato ECLB. No es la
implementación principal ni evidencia de corrupción nativa. Para conocer el
estado vigente, empieza por el [README del repositorio](../README.md) y la
[arquitectura actual](../docs/architecture.md).
