E-029 — Análisis estático de la variante PATCHED mediante Ghidra

1. Objetivo

Analizar estáticamente la biblioteca nativa de la variante PATCHED de EchoCall y determinar qué diferencias defensivas pueden recuperarse directamente del binario respecto a la variante VULNERABLE analizada en E-028.

El objetivo específico es comprobar si el parser PATCHED aplica un límite máximo antes del procesamiento y cómo se representa el rechazo payload_too_large en el flujo nativo.

2. Artefacto analizado

Biblioteca extraída del APK PATCHED:

lib/x86_64/libechocall_native.so

La copia de trabajo se importó en Ghidra con un nombre diferenciado para evitar confusión con la variante VULNERABLE:

libechocall_native_patched.so

Identidad registrada durante la sesión:

SHA-256: 229C26DBEB9110B22ED08F75B2A4F171900412389D7440B506EC5E9301507D60
Tamaño en disco: 7984 bytes

Estos valores proceden de los comandos de identificación ejecutados durante la sesión. Las capturas adjuntas del parser PATCHED no sustituyen esa evidencia de provenance.

Estado de custodia comprobado el 2026-08-17:

- el ELF exacto con ese SHA-256 no se ha localizado físicamente en este
  repositorio, ni como `.so` independiente ni dentro de los APK locales;
- la salida local `patchedDebug` x86_64 presente en
  `android-app/app/build/intermediates/stripped_native_libs/patchedDebug/stripPatchedDebugDebugSymbols/out/lib/x86_64/libechocall_native.so`
  y dentro de `app-patched-debug.apk` también mide 7984 bytes, pero su
  SHA-256 es
  `44EC1B920652ED402481EC16050C5223F4F5AEEEAF794F0A0E4599E9A783B2AF`;
- la coincidencia de tamaño no establece identidad entre ambos artefactos.

La identidad anterior permanece como identidad registrada durante el análisis;
la custodia física y el provenance verificable desde el árbol local quedan
`PENDIENTES`. No se atribuye este ELF a los binarios ASan finales de 8A/8B.

3. Herramienta

Ghidra 12.0.4

Se aplicó la misma metodología que en E-028: importación ELF, Auto Analysis, navegación por Symbol Tree, inspección de Listing y decompilación de las funciones relevantes.

4. Símbolos y parser PATCHED

El Symbol Tree muestra los símbolos JNI comunes y permite identificar:

Java_com_echocall_lab_NativeBridge_parsePacket
safe_parse_packet

En la variante PATCHED, el handler JNI interno reconstruido durante la sesión es:

FUN_00100c50

El flujo observado es:

NativeBridge.parsePacket(byte[])
        ↓
Java_com_echocall_lab_NativeBridge_parsePacket
        ↓
FUN_00100c50
        ↓
safe_parse_packet

5. Validación del límite máximo

En safe_parse_packet aparece la condición:

if (*(ushort *)(param_3 + 1) < 0x21) {

Como:

0x21 = 33

la rama aceptada equivale a:

declared_length <= 32

Solo dentro de esa rama se alcanza posteriormente la comparación de coherencia:

declared_length == actual_length

Por tanto, el orden relevante recuperado del binario PATCHED es:

declared_length <= 32 ?
        ↓ sí
 declared_length == actual_length ?
        ↓ sí
    procesamiento

Cuando el límite no se cumple, la función entra en la rama alternativa y asigna:

*(undefined4 *)param_3 = 6;

Interpretación estructural:

declared_length > 32
        ↓
status = 6

6. Procesamiento después de validar

En el camino admitido, safe_parse_packet procesa el contenido mediante un bucle sobre la longitud ya aceptada:

for (local_58 = 0; local_58 < (ulong)param_3[2]; local_58 = local_58 + 1) {
    cVar2 = param_1[local_58 + 0xd] + cVar2;
}

Dentro de safe_parse_packet no se observa la secuencia crítica identificada en VULNERABLE:

malloc(0x20)
+
__memcpy_chk(... declared_length ...)

Por tanto, PATCHED no solo introduce el límite máximo sino que elimina, dentro del parser analizado, el sink concreto malloc(32) → copy(declared_length) observado en E-028.

La afirmación se limita al flujo de safe_parse_packet analizado; no implica ausencia de cualquier otra operación de memoria o vulnerabilidad en el binario completo.

7. Correlación de status = 6

El análisis del handler JNI PATCHED permite vincular el código 6 con el rechazo textual.

Después de la llamada a:

iVar1 = safe_parse_packet(...);

se observa una rama:

else if (iVar1 == 6) {
    ...
    "status=rejected code=payload_too_large declared_length=%u actual_length=%zu maximum=%zu"
}

La cadena queda así:

declared_length > 32
        ↓
safe_parse_packet
        ↓
status = 6
        ↓
handler JNI
        ↓
status=rejected
code=payload_too_large

Esta correlación queda confirmada dentro del binario PATCHED.

8. Resultado estático

Flujo defensivo recuperado:

APK PATCHED
        ↓
lib/x86_64/libechocall_native.so
        ↓
Java_com_echocall_lab_NativeBridge_parsePacket
        ↓
FUN_00100c50
        ↓
safe_parse_packet
        ↓
declared_length <= 32 ?
        ├── no → status = 6 → payload_too_large
        └── sí → declared_length == actual_length ?
                    ↓
                procesamiento acotado

9. Qué demuestra y qué no demuestra

Hechos observados directamente en el binario

existencia de safe_parse_packet;

entrada JNI común mediante Java_com_echocall_lab_NativeBridge_parsePacket;

límite recuperado como < 0x21, equivalente a aceptar como máximo 32 bytes;

conservación de la comprobación declared_length == actual_length en el camino admitido;

status = 6 cuando se supera el máximo;

mapeo del código 6 a status=rejected code=payload_too_large en el handler JNI;

ausencia, dentro de safe_parse_packet, de la secuencia malloc(0x20) + __memcpy_chk(...) observada en VULNERABLE.

Interpretación respaldada

La variante PATCHED añade un control explícito de límites antes del procesamiento y elimina del parser analizado el sink concreto asociado a la condición vulnerable de E-028.

No demostrado por E-029

seguridad general de PATCHED;

ausencia de cualquier otra vulnerabilidad;

imposibilidad absoluta de corrupción de memoria en otras rutas;

comportamiento de todas las entradas posibles;

identidad con los binarios ASan utilizados en las ejecuciones dinámicas finales salvo provenance adicional;

RCE o cualquier afirmación de explotabilidad.

10. Evidencias gráficas

Las capturas PATCHED aportadas en esta sesión pueden organizarse así:

| ID | Captura | Finalidad |
|---|---|---|
| E-029-01 | `e029-01-ghidra-patched-safe-parser-overview.png` | Vista general de `safe_parse_packet` y su contexto en Ghidra |
| E-029-02 | `e029-02-ghidra-patched-symbol-tree.png` | Identificación de `safe_parse_packet` y los símbolos JNI relevantes |
| E-029-03 | `e029-03-ghidra-patched-length-check.png` | Condición `< 0x21`, comprobación de coherencia y generación de `status = 6` |
| E-029-04 | `e029-04-ghidra-patched-status6-payload-too-large.png` | Correlación `safe_parse_packet` → `iVar1 == 6` → `payload_too_large` |

Material auxiliar conservado: `e029-aux-02-ghidra-patched-safe-parser-entry.png`.

La cuarta captura principal ya documenta la cadena `safe_parse_packet` →
`status = 6` → handler JNI → `payload_too_large`; no hace falta otra captura.

11. Conclusión

El análisis estático de PATCHED identifica directamente en el binario una comprobación declared_length < 0x21, equivalente a limitar la entrada a un máximo de 32 bytes, antes de la comprobación de coherencia y del procesamiento. Si el límite se supera, safe_parse_packet genera el estado 6, y el handler JNI lo transforma en payload_too_large. Además, en safe_parse_packet no se observa la secuencia malloc(32) + copy(declared_length) presente en VULNERABLE. Estos hallazgos muestran estáticamente una estrategia defensiva basada en validación temprana y eliminación del sink concreto analizado, sin demostrar seguridad general.

Fuentes

Evidencia primaria interna

libechocall_native.so PATCHED analizada con Ghidra.

SHA-256 registrado: 229C26DBEB9110B22ED08F75B2A4F171900412389D7440B506EC5E9301507D60.

Capturas E-029.

Comparación diferencial con E-028.

Fuentes externas

NSA / Ghidra — Introduction to Ghidra Student Guide: https://ghidra.re/ghidra_docs/GhidraClass/Beginner/Introduction_to_Ghidra_Student_Guide.html

NSA / Ghidra — Releases, Ghidra 12.0.4: https://github.com/NationalSecurityAgency/ghidra/releases

Android Developers — Android ABIs: https://developer.android.com/ndk/guides/abis

MITRE — CWE-122, Heap-based Buffer Overflow: https://cwe.mitre.org/data/definitions/122.html
