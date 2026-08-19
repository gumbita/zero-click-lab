# Manifest SHA-256 de evidencias Android

Manifest recalculado durante la consolidación de E-025 el 04/08/2026. En la
sección A, tamaño y SHA-256 corresponden a los bytes preparados en el índice de
Git. La política `.gitattributes` hace que los artefactos primarios coincidan
byte por byte con sus blobs; los Markdown pueden adoptar finales de línea del
entorno al extraerse. Estos valores no convierten por sí solos una
reconstrucción documental en evidencia experimental primaria.

## A. Artefactos versionados

| Archivo | Tamaño | SHA-256 | Función |
|---|---:|---|---|
| `../../../.gitattributes` | 339 bytes | `0BF96FC7CC180580057A29EDB2D8D60297C911A3BFF76D89375F8BB3F03C9AAF` | Conservación byte a byte de artefactos de evidencia |
| `../../../README.md` | 9 132 bytes | `548983F405D12F5CB8648F36E4B610D0BDC9EAD797AF663CF105B6AD9C62589B` | Enlace raíz al registro de evidencias |
| `../README.md` | 3 998 bytes | `2AFC9A62D7847C35E16A922B8BD5532BB557F30E78680F261103511F1107CB99` | Índice y taxonomía |
| `../registro_validacion_experimental.md` | 39 526 bytes | `8BF4878AECAC5722EE165D109A91B81BF46A34D6AFEE1B73A1B3A2A6B2A910D4` | Matriz y análisis consolidado |
| `../procedencia-experimento-android.md` | 6 626 bytes | `275F55336D605D9FA886926EE8A114282D32054B2E76F1E778FCEDBE896A82AE` | Identidad, cronología y custodia externa del experimento Android |
| `README.md` | 3 076 bytes | `CA7175DCACD724E1E3B9C497AC44343B9AFF26935F324E5377F027A7331FEC8D` | Política de artefactos y saneamiento |
| `contexto/echocall-vulnerable-20260731-134143.png` | 204 949 bytes | `EE7DDFD7804D18DB51F1C415934B0E57EF98DE485E4EB79EDE94D45CF634A8E0` | Captura contextual; no concluyente para E-021/E-022 |
| `E-022/asan-udp-vulnerable-20260731-140415.log` | 64 698 bytes | `F59B0BCCC33F2B9E6BCCA28DA80145F59C04A2E93B9F101A999A042185EDED7D` | Log primario E-022 |
| `E-021_E-024_sesion_powershell_sanitizada.txt` | 369 039 bytes | `92C0CF67B87D2D126504FA9FDD59455753EA56363B40AFD1865EB25866595496` | Derivado saneado de la transcripción primaria |
| `E-025/e025-safe-before-20260804-121742.xml` | 10 642 bytes | `2FD09851FAD75DC0E1AFB2D313321BB2CE48B100D2EB7543B4BA9FF396263FF3` | UI anterior E-025 |
| `E-025/e025-safe-after-20260804-121742.xml` | 15 139 bytes | `96552323FA40F7C6C79414197933865DDA189BA8704F5371B7BEFF416B7DD6AC` | UI posterior E-025 |
| `E-025/e025-safe-udp-oversized-20260804-121742-metadata.txt` | 1 361 bytes | `163250586C651FAEAAA4226A06197FD0D395CD84B8933B08AD436DB3FD873E45` | Metadatos y cadena de custodia E-025 |
| `E-025/e025-safe-udp-oversized-20260804-121742-sender.txt` | 79 bytes | `C0EAA651A54FA70296A7AEE873358FEA1584094D46053EC0D713F54538BDA4F5` | Salida del emisor E-025 |
| `E-025/e025-safe-udp-oversized-20260804-121742.log` | 2 774 bytes | `D06D6FABF8E7333011CB51C66019BBAA7F14564EE96FD8AFED3B5AE44A664EDB` | Log primario E-025 |
| `E-028/README.md` | 10 003 bytes | `8A1BF72A5C823839624E4E86B6BDDCC1633B8D430DFBB22E23C49A05C74622B6` | Alcance, hallazgos y limitación de custodia E-028 |
| `E-028/e028-01-ghidra-vulnerable-import-summary.png` | 96 491 bytes | `A840572F38E8F216F2BB964027008A7A09531546B674A25F3F6C0A3CC906BCE6` | Importación Ghidra del ELF Vulnerable |
| `E-028/e028-02-ghidra-jni-to-vulnerable-parser.png` | 128 912 bytes | `AB6F160F842683534E5558AA4459A1931C53A0C22708EBA4E44A3C15423F882D` | Flujo JNI hacia el parser Vulnerable |
| `E-028/e028-03-ghidra-vulnerable-parse-packet-context.png` | 124 954 bytes | `7312EE1B2239FE94FF35ADED39F77AEC7F87DB5A75781F74E22463DC868346F1` | Contexto de longitudes Vulnerable |
| `E-028/e028-04-ghidra-vulnerable-root-cause.png` | 154 480 bytes | `4BD72EB4DFC6F22714AA3D61AE12629E01A469E76D5CEFEC4F343B2B356BC0D3` | Reserva de 32 bytes y copia gobernada por entrada |
| `E-028/e028-aux-04-ghidra-vulnerable-symbol-tree.png` | 21 139 bytes | `7B7A65E05CB4E465847432404CD965FD9EF6A5F26797D6C72CAF75413191C776` | Árbol auxiliar de símbolos Vulnerable |
| `E-028/e028-aux-05-ghidra-jni-export.png` | 12 053 bytes | `E8A580BAAFF8B6FFEC30CCEE756EF50DCE476A081A3089E4B5BD949894E0CF3D` | Export JNI auxiliar Vulnerable |
| `E-029/README.md` | 8 445 bytes | `762836C0AC87E1C74CB80B8F0B0E11D00BA07E15D9BEBE5448F3C5C42498AD95` | Alcance, hallazgos y limitación de custodia E-029 |
| `E-029/e029-01-ghidra-patched-safe-parser-overview.png` | 164 003 bytes | `E233863BCE0F527A3F3EBFB9CA661EECEC0EBCA72766EFFFE5A4E472C917719A` | Vista general del parser Patched |
| `E-029/e029-02-ghidra-patched-symbol-tree.png` | 16 637 bytes | `F71E50E48ACEB4D9AB1E5DD2B8D8896FDA2D5F04023E2F606611E4A52CC34707` | Árbol de símbolos Patched |
| `E-029/e029-03-ghidra-patched-length-check.png` | 162 399 bytes | `B9C650257D1948F5563CF6D04C255272FBBA89390B0CA3D96612F6C2A26AF637` | Límite `< 0x21` y estado 6 |
| `E-029/e029-04-ghidra-patched-status6-payload-too-large.png` | 145 711 bytes | `28AE8870C078C273F40CA234955096E1664CCB1426C2DF7DACC49AE6993103E1` | Mapeo de estado 6 a `payload_too_large` |
| `E-029/e029-aux-02-ghidra-patched-safe-parser-entry.png` | 150 286 bytes | `A88D7AE1CCFECDDCD887A35A24C9CD9EEEED8864117B05D7CF1F26B70249D470` | Entrada auxiliar de `safe_parse_packet` |

`manifest_sha256.md` también se versiona, pero no incluye su propio hash porque
ese valor cambiaría al escribirlo y produciría una referencia circular.

## B. Artefactos externos o excluidos

| Artefacto | Tamaño | SHA-256 o identidad | Estado y motivo |
|---|---:|---|---|
| `E-021_E-024_sesion_powershell_20260731_20260803.txt` | 372 699 bytes | `ECED591432B4783142303B530FD42AC41CFD6493722FA410B4C4DC0C9A860F14` | Original externo; excluido de Git por contener 915 rutas personales. El derivado saneado sustituye únicamente el prefijo de perfil personal de Windows por `<USER_HOME>`. |
| `E-022/libechocall_native-x86_64-unstripped.so` | 106 528 bytes | SHA-256 `BA86A1DDB9881A6BF22F07907DCE14995242C42F4B25981FC1F1DD6649490453`; Build ID `c455a1c576ff356de665e37770bd209913e6e7b2` | Binario de símbolos externo; excluido de Git normal y con rutas SDK personales incrustadas. |
| APK ASan usado en E-025 | 23 179 791 bytes | `964198FC0316E1FA149067523778097604B60D0E48E635A2545ACB266EDC5182` | Artefacto de build excluido. Está identificado por los metadatos E-025, no disponible mediante este commit. |
| APK ASan histórico de E-022 | ND | ND | No conservado; no se sustituye por el APK E-025 ni por un APK actual. |

## C. Artefactos pendientes de revisión

| Artefacto | Tamaño | SHA-256 | Estado |
|---|---:|---|---|
| `../registro_evidencias_zero_click.xlsx` | 134 036 bytes | `E423F751925BD4E144E103BF8478C788928A87B3FE033003481F2297D52D76AB` | Excluido temporalmente. Contenido interno, fórmulas y formato pendientes de revisión estructurada compatible con XLSX. |

## Muestras

| Muestra | Tamaño | SHA-256 | Alcance de custodia |
|---|---:|---|---|
| `samples/benign/valid_call_control.bin` | 17 bytes | `912B5F7F858A790D4C49AE2860CD421F0B70C8DD8E582ABE99AB6D6640965B8E` | Hash calculado durante la consolidación, no antes de E-021. |
| `samples/malformed/oversized_complete_payload.bin` | 77 bytes | `516F7C6A9B6237274F33F8AB01057DFDBD1137DF0C898F70B5AFB6B7DA742ABA` | Calculado antes del envío E-025; para E-022 solo existe el mismo hash calculado posprueba. |
