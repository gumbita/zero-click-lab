E-028 — Análisis estático de la variante VULNERABLE mediante Ghidra

1. Objetivo

Analizar estáticamente la biblioteca nativa incluida en la variante VULNERABLE de EchoCall y comprobar si la condición que permite la escritura fuera de límites puede identificarse directamente en el binario compilado, sin utilizar el código fuente como punto de partida del análisis.

El análisis se realizó con Ghidra 12.0.4, importando la biblioteca ELF, ejecutando Auto Analysis y utilizando Symbol Tree, Listing y Decompiler para reconstruir el flujo relevante.

2. Artefacto analizado

APK de procedencia registrado durante la sesión:

app-vulnerable-debug.apk

Biblioteca extraída:

lib/x86_64/libechocall_native.so

Ruta de trabajo registrada en la captura de la sesión:

C:\Users\gumbita\Documents\TFM-reversing-work\ghidra\vulnerable\lib\x86_64\libechocall_native.so

Identidad registrada mediante PowerShell durante la sesión:

SHA-256: A14467B2377A73FAEB83143564269E0E16D6494047F89C30819921B76BF26723
Tamaño en disco: 8360 bytes

Estado de custodia comprobado el 2026-08-17:

- el ELF exacto con ese SHA-256 no se ha localizado físicamente en este
  repositorio, ni como `.so` independiente ni dentro de los APK locales;
- la salida local `vulnerableDebug` x86_64 presente en
  `android-app/app/build/intermediates/stripped_native_libs/vulnerableDebug/stripVulnerableDebugDebugSymbols/out/lib/x86_64/libechocall_native.so`
  y dentro de `app-vulnerable-debug.apk` también mide 8360 bytes, pero su
  SHA-256 es
  `149CBC733497694EC14C87F1817CE38C4207DEF46F0CB7D8B7A11F29521FFE89`;
- la coincidencia de tamaño no establece identidad entre ambos artefactos.

Por tanto, la identidad anterior se conserva como identidad registrada durante
el análisis, mientras que su custodia física y su provenance verificable desde
el árbol local quedan `PENDIENTES`. Esta limitación no invalida las capturas ni
el análisis documentado, pero impide atribuir el ELF a las ejecuciones ASan
finales 8A/8B.

Nota de trazabilidad. La ventana Import Results Summary de Ghidra muestra # of Bytes: 10220. Ese contador pertenece al resumen interno de importación de Ghidra y no se utiliza aquí como sustituto del tamaño del archivo en disco obtenido con Get-Item.

Android documenta que las bibliotecas nativas de un APK se almacenan bajo el patrón /lib/<abi>/lib<name>.so y que x86_64 es una ABI soportada por el NDK.

3. Herramienta

Ghidra 12.0.4

El ZIP utilizado se verificó previamente mediante SHA-256:

C3B458661D69E26E203D739C0C82D143CC8A4A29D9E571F099C2CF4BDA62A120

Ese valor coincide con el publicado por la NSA para Ghidra 12.0.4.

4. Importación y reconocimiento del ELF

Ghidra reconoció la biblioteca como un ELF x86-64 little-endian de 64 bits y tipo shared object.

La captura de resumen de importación muestra, entre otros datos:

Language ID: x86:LE:64:default
Processor: x86
Endian: Little
Address Size: 64
ELF File Type: shared object
Created With Ghidra Version: 12.0.4
ELF Comment: Android toolchain / clang 18.0.1
Linker: LLD 18.0.1
Executable SHA256: a14467b2377a73faeb83143564269e0e16d6494047f89c30819921b76bf26723

Durante la importación Ghidra dejó sin resolver referencias externas a libm.so, libdl.so y libc.so porque dichas bibliotecas no estaban cargadas en el proyecto. Esta circunstancia no impidió localizar el punto de entrada JNI ni analizar el parser propio.

5. Entrada JNI

El Symbol Tree permite identificar el símbolo exportado:

Java_com_echocall_lab_NativeBridge_parsePacket

La función exportada actúa como wrapper y delega el procesamiento efectivo en una función interna reconstruida por Ghidra como:

FUN_00100d10

En esa función interna se observa una llamada a:

iVar1 = vulnerable_parse_packet(pcVar2, (long)iVar1, &local_148);

Flujo recuperado:

NativeBridge.parsePacket(byte[])
        ↓
Java_com_echocall_lab_NativeBridge_parsePacket
        ↓
FUN_00100d10
        ↓
vulnerable_parse_packet

Este flujo constituye evidencia estática del paso JNI hacia el parser vulnerable dentro del binario analizado.

6. Tratamiento de longitudes

En vulnerable_parse_packet se recupera una longitud declarada del paquete y se calcula la longitud efectiva del contenido restando 0x0d bytes a la longitud total:

uVar3 = FUN_001014d0((long)param_1, 7);
*(undefined2 *)(param_3 + 1) = uVar3;
param_3[2] = param_2 - 0xd;

Posteriormente se observa una comprobación equivalente a:

if ((ulong)*(ushort *)(param_3 + 1) == param_3[2]) {

Interpretación:

declared_length == actual_length

Por tanto, una entrada debe ser internamente coherente para alcanzar la parte posterior del flujo analizado.

7. Condición vulnerable y causa técnica

Tras superar la comprobación de coherencia aparece:

__ptr = malloc(0x20);

0x20 equivale a 32 bytes.

A continuación se observa:

__memcpy_chk(
    __ptr,
    param_1 + 0xd,
    *(undefined2 *)(param_3 + 1),
    0xffffffffffffffff
);

La cantidad de bytes solicitada para la copia depende de la longitud declarada por la entrada.

En el flujo decompilado previo a esta operación no se identifica una comprobación equivalente a:

declared_length <= 32

La condición reconstruida es, por tanto:

declared_length == actual_length
        ↓
malloc(32)
        ↓
copy(declared_length)
        ↓
sin límite <= 32 identificado antes de la copia

Una entrada coherente con declared_length > 32 puede alcanzar una operación de copia cuyo tamaño supera la capacidad de la región reservada.

Esta estructura es compatible con CWE-122 — Heap-based Buffer Overflow, ya que la región susceptible de ser sobrescrita se reserva dinámicamente en heap mediante malloc().

8. Observación sobre __memcpy_chk

Ghidra recupera como cuarto argumento:

0xffffffffffffffff

En un contexto de 64 bits este valor equivale a SIZE_MAX.

Bionic define __memcpy_chk(dst, src, count, dst_len) y comprueba count frente a dst_len antes de llamar a memcpy.

Interpretación razonable: el valor SIZE_MAX es compatible con que la comprobación fortificada no disponga en ese punto del tamaño real de 32 bytes de la reserva dinámica. Por ello, esa llamada no aporta el límite de 32 bytes que debería imponer explícitamente el parser.

No se concluye a partir de este dato que FORTIFY esté globalmente desactivado.

9. Resultado estático

Cadena reconstruida desde el ELF:

APK VULNERABLE
        ↓
lib/x86_64/libechocall_native.so
        ↓
Java_com_echocall_lab_NativeBridge_parsePacket
        ↓
FUN_00100d10
        ↓
vulnerable_parse_packet
        ↓
declared_length == actual_length
        ↓
malloc(32)
        ↓
copy(declared_length)
        ↓
sin upper bound <= 32 identificado
        ↓
condición compatible con escritura fuera de límites en heap

10. Qué demuestra y qué no demuestra

Hechos observados directamente en el binario

entrada JNI Java_com_echocall_lab_NativeBridge_parsePacket;

delegación a una función interna;

llamada a vulnerable_parse_packet;

cálculo de la longitud efectiva usando una cabecera de 0x0d bytes;

comparación declared_length == actual_length;

malloc(0x20);

copia cuyo tamaño deriva de declared_length;

no identificación, en el flujo decompilado previo a la copia, de un límite equivalente a <= 32.

Interpretación respaldada

La combinación de una reserva heap fija de 32 bytes, una cantidad de copia derivada de la entrada y la ausencia de un límite superior identificado antes del sink constituye una condición compatible con CWE-122.

No demostrado por E-028

ocurrencia efectiva del overflow en una ejecución concreta;

contenido exacto de memoria adyacente sobrescrita;

control del flujo;

modificación de una dirección de retorno;

ejecución de código;

RCE.

La ocurrencia dinámica del fallo corresponde a la evidencia ASan separada del laboratorio.

11. Evidencias gráficas

| ID | Archivo conservado | Finalidad |
|---|---|---|
| E-028-01 | `e028-01-ghidra-vulnerable-import-summary.png` | Resumen de importación, arquitectura, toolchain y SHA-256 registrado por Ghidra |
| E-028-02 | `e028-02-ghidra-jni-to-vulnerable-parser.png` | Handler JNI interno y llamada a `vulnerable_parse_packet` |
| E-028-03 | `e028-03-ghidra-vulnerable-parse-packet-context.png` | Contexto del parser y tratamiento de longitudes |
| E-028-04 | `e028-04-ghidra-vulnerable-root-cause.png` | `malloc(0x20)` y `__memcpy_chk(...)`, evidencia principal de la condición vulnerable |

Material auxiliar conservado:

- `e028-aux-04-ghidra-vulnerable-symbol-tree.png`;
- `e028-aux-05-ghidra-jni-export.png`.

12. Conclusión

El análisis estático de la biblioteca nativa VULNERABLE permite recuperar desde el binario la condición que explica el comportamiento inseguro del laboratorio: vulnerable_parse_packet exige coherencia entre longitud declarada y real, reserva posteriormente 32 bytes en heap y alcanza una copia cuyo tamaño depende de la longitud declarada. En el flujo decompilado anterior a esa copia no se identifica un control equivalente a limitar la longitud a 32 bytes. El resultado constituye una condición compatible con CWE-122, sin implicar por sí mismo control de flujo ni RCE.

Fuentes

Evidencia primaria interna

libechocall_native.so VULNERABLE analizada en Ghidra.

SHA-256 registrado: A14467B2377A73FAEB83143564269E0E16D6494047F89C30819921B76BF26723.

Capturas E-028-01 a E-028-04.

Salidas Get-Item / Get-FileHash registradas durante la sesión.

Fuentes externas

NSA / Ghidra — Introduction to Ghidra Student Guide: https://ghidra.re/ghidra_docs/GhidraClass/Beginner/Introduction_to_Ghidra_Student_Guide.html

NSA / Ghidra — Releases, Ghidra 12.0.4: https://github.com/NationalSecurityAgency/ghidra/releases

Android Developers — Android ABIs: https://developer.android.com/ndk/guides/abis

AOSP Bionic — __memcpy_chk.cpp: https://android.googlesource.com/platform/bionic/+/1e52871/libc/bionic/__memcpy_chk.cpp

MITRE — CWE-122, Heap-based Buffer Overflow: https://cwe.mitre.org/data/definitions/122.html
