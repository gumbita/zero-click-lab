# Procedencia del experimento Android final

Este documento conserva la información de procedencia y los hashes únicos que
antes estaban repartidos entre el diseño y el plan de implementación Android.
Complementa el [registro de validación](experimental-validation-log.md):
no sustituye los artefactos primarios ni convierte en pública la custodia
externa.

## Cadena de código fuente

| Hito | Commit | Función histórica |
|---|---|---|
| Recepción UDP endurecida | `3bcceb36748aaf385dfa6c4b8e43b0a213767de4` | Cola y recuperación del receptor |
| Línea base Android/ASan | `8b20ffed4ef3ef5fb4b4f22c67e8853ebef1065c` | Evidencia Android anterior al rediseño |
| Separación de variantes | `26b0638442a5f31b134ba259a8afcbfc0d40d35d` | Parser fijado por flavor/CMake |
| Modelos y navegación | `ece2e13584838d1e56da117a634ff53b51faa17b` | Estado y navegación de la app simulada |
| Mensajería y llamadas | `aa69cba406fa78fd088019ec75dcd33a0ff05856` | Flujo visual de comunicaciones |
| Integración Patched | `8d7add26aa22b5884b1ae401e5abe6c4429fd5d6` | Rechazo controlado de la entrada oversized |
| Marcador pre-JNI | `e1da09eaea29a1f9f2ab0e395a6bb5c829c478f1` | Persistencia antes de entrar en JNI |
| Fuente de los APK finales | `7bbb5ba984c55edfe2d0c6254253fb0ed9f2065d` | Código exacto usado para construir candidatos |
| Cierre documental previo | `12ad66a486f4a24870ed7728570256fd0f65cf3e` | Documentación; no es fuente de los APK |

Los documentos completos retirados del árbol principal pueden consultarse en
el historial de `707e93c218b68cd7eaac58b903754d1917aea9ca`:

- [`diseno-interfaz-echocall.md`](https://github.com/gumbita/zero-click-lab/blob/707e93c218b68cd7eaac58b903754d1917aea9ca/documentacion/android/diseno-interfaz-echocall.md)
- [`plan-implementacion-echocall.md`](https://github.com/gumbita/zero-click-lab/blob/707e93c218b68cd7eaac58b903754d1917aea9ca/documentacion/android/plan-implementacion-echocall.md)

## Candidatos Android congelados

Los cuatro APK se construyeron desde `7bbb5ba…`, se fijaron por tamaño y
SHA-256 y se preservaron en:

```text
%USERPROFILE%\Documents\EchoCall-TFM-Evidence\phase7-frozen-candidates\echocall-phase7-20260810T162009Z
```

SHA-256 de `candidate-manifest.txt`:
`59E04A43D1170DF9DD2D50E4346A464CF1900CE0822B9CF339508D82A5B97B7E`.
El experimento posterior usó esos mismos bytes, sin recompilar, modificar,
resignar, reempaquetar ni regenerar los APK.

| Variante | Build | `applicationId` | ABI | Bytes | SHA-256 |
|---|---|---|---|---:|---|
| Vulnerable | Debug | `com.echocall.lab.vulnerable` | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` | 28.141.106 | `B3E6F8EABACE1B1FE66E5559996098196AAB2207537B2054BDA11263A1BB4953` |
| Patched | Debug | `com.echocall.lab.patched` | `arm64-v8a`, `armeabi-v7a`, `x86`, `x86_64` | 28.140.722 | `1A3A8C7860594E8BE344B1E3ED1AC6D490E0828B71BFE1E1F3CBBDB853A780F0` |
| Vulnerable | ASan | `com.echocall.lab.vulnerable.asan` | `x86_64` | 26.933.964 | `DD8018E5D4B31AB778E479087C51E5D23DBC41F927D6D2F9F615255959B74BE5` |
| Patched | ASan | `com.echocall.lab.patched.asan` | `x86_64` | 26.933.752 | `0F5DC5B9DE28FB26DEF2F8A97CA8EA2F89F305EFCECCC42952D4FF13D5B01F4C` |

Dos APK Debug provisionales de una validación visual anterior medían
33.104.622 y 33.104.242 bytes y tenían, respectivamente, los SHA-256
`9C173998CF4E4B85712923AE9FABB321D1BE2753D0B2A267682A29AAF35C5135` y
`ABE656B5BD96F55377B718555D6031485C09383DACBAB8BA1B23642AEF11D16D`.
No son los candidatos finales de la tabla anterior.

## Entrada común y resultados

Ambas ejecuciones finales usaron una vez la muestra
`oversized_complete_payload.bin`: 77 bytes totales, payload declarado y real de
64 bytes y SHA-256
`516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA`.

### Patched con instrumentación ASan

El APK Patched ASan devolvió:

```text
status=rejected code=payload_too_large declared_length=64 actual_length=64 maximum=32
```

El marcador pre-JNI se limpió, el proceso mantuvo el mismo PID y no se observó
un informe ASan en la ventana documentada. La custodia externa está en:

```text
%USERPROFILE%\Documents\EchoCall-TFM-Evidence\phase8a-patched-asan-20260810T172319Z
```

| Artefacto | SHA-256 |
|---|---|
| Manifest | `910642CAA5E428A4DF1FA201E2EF3E3F699AC60391E4A27E9124B09AE5E161A8` |
| `artifact-hashes.txt` | `3A1364EBF7BE5E5D7D32792E501CF242E8C0139DE9155B39C258698193FFE255` |
| Addendum | `6FA461F18E59910BF0F989638038C58E73D0B8FB3759B96584B21D02BEDEC4E5` |
| `incidents.txt` | `F93C2A41BCC122E417E100A079A9CF7A8A0BCE472D767FCA915E40D4B8B77313` |

### Vulnerable con instrumentación ASan

ASan informó `heap-buffer-overflow`, un `WRITE` de 64 bytes situado
inmediatamente después de una región heap de 32 bytes y terminó el proceso con
`SIGABRT`. Tras el relanzamiento, el marcador persistente indicó que la llamada
JNI no había alcanzado su limpieza normal; ese marcador es evidencia auxiliar,
no prueba por sí solo el overflow o la señal. La custodia externa está en:

```text
%USERPROFILE%\Documents\EchoCall-TFM-Evidence\phase8b-vulnerable-asan-20260810T174243Z
```

| Artefacto | SHA-256 |
|---|---|
| Manifest | `A33E17F4574509FD81AE53EA86C88763B5F6FA82CDBA5CA6D069261E17666F7B` |
| `artifact-hashes.txt` | `E7CE06F333551A6C084E1855BC6DAB6FDC2EC1A03E934242CF547522A0F77803` |
| Log RAW | `55094B74451A1CF86D8E61FD7BBA47BB67ED3C72324019084284ED0230BA56EA` |
| Informe ASan RAW | `CD17F66CF4219A14EF26DA6219B9692923E07C732916D75CCF6A1AA43FBEA7E7` |
| `tombstone_09` | `688416EA8E9149C4C3B63620E7D8051F93690BE6A656492495C9417382EC0071` |
| Exit information | `9113DAE00858180C0305C59C346C5F342AF6B9E032334E36A5D1A31C08A1B4E0` |

La simbolización resolvió la reserva y la copia en
`vulnerable_parser.c:83:15` y `vulnerable_parser.c:93:11` para el candidato
documentado. Se usó una biblioteca nativa de 98.568 bytes, SHA-256
`5E254E39CF252D4E6C70FC4966FD6933CCE1C76C70724651410B68F0EE41655B`,
Build ID `6dbcbaecdc5dfd981b60e91f334a6bc451bc36a5`, configuración `5kc70511`, NDK
`27.0.12077973` y LLVM `18.0.1`. Su procedencia quedó clasificada como
`PROVENANCE_RESOLVED`.

## Límites de atribución

- Los resultados acreditan una escritura fuera de límites en EchoCall Lab; no
  acreditan RCE ni control del flujo.
- El rechazo de Patched acredita esa validación concreta, no seguridad general.
- Los ELF Debug analizados en E-028/E-029 no están disponibles actualmente con
  sus hashes registrados; no se equiparan a los candidatos ASan.
- Las repeticiones del mismo diagnóstico en log, debuggerd y tombstone cuentan
  como un incidente, no como ejecuciones independientes.
