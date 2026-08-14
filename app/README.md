# MVP Python inicial — componente histórico

> **COMPONENTE HISTÓRICO**
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

El código Python se conserva sin cambios por trazabilidad y para mostrar la
evolución metodológica del proyecto. Para conocer el estado vigente, empieza
por el [README del repositorio](../README.md) y la
[documentación Android autoritativa](../documentacion/android/).
