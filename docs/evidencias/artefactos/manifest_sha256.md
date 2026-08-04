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
| `../../../.gitattributes` | 325 bytes | `1720FCB479CB2F420A79DE2B7D6CC4A03164D756EA6038772EDBB9F40929F462` | Conservación byte a byte de artefactos de evidencia |
| `../../../README.md` | 3 980 bytes | `957301ACA37A5532FA9B1128E4BDC9E387B6E47119C1293060497011373A9DE1` | Enlace raíz al registro de evidencias |
| `../README.md` | 2 558 bytes | `8BC2AC5DD09014209A1F72FD6BA06EA8522B14151DC9BFB52B213E636AD51883` | Índice y taxonomía |
| `../registro_validacion_experimental.md` | 37 263 bytes | `8E53AC558A8DE26AA7A46B5E429925B57FBD84898786A4D7DA41C215C3524B56` | Matriz y análisis consolidado |
| `README.md` | 2 516 bytes | `BEBCB3C5EA7A7A673B85D5DFD8C9095D7046B52480A3C498FEF007A01E1FE4AE` | Política de artefactos y saneamiento |
| `contexto/echocall-vulnerable-20260731-134143.png` | 204 949 bytes | `EE7DDFD7804D18DB51F1C415934B0E57EF98DE485E4EB79EDE94D45CF634A8E0` | Captura contextual; no concluyente para E-021/E-022 |
| `E-022/asan-udp-vulnerable-20260731-140415.log` | 64 698 bytes | `F59B0BCCC33F2B9E6BCCA28DA80145F59C04A2E93B9F101A999A042185EDED7D` | Log primario E-022 |
| `E-021_E-024_sesion_powershell_sanitizada.txt` | 369 039 bytes | `92C0CF67B87D2D126504FA9FDD59455753EA56363B40AFD1865EB25866595496` | Derivado saneado de la transcripción primaria |
| `E-025/e025-safe-before-20260804-121742.xml` | 10 642 bytes | `2FD09851FAD75DC0E1AFB2D313321BB2CE48B100D2EB7543B4BA9FF396263FF3` | UI anterior E-025 |
| `E-025/e025-safe-after-20260804-121742.xml` | 15 139 bytes | `96552323FA40F7C6C79414197933865DDA189BA8704F5371B7BEFF416B7DD6AC` | UI posterior E-025 |
| `E-025/e025-safe-udp-oversized-20260804-121742-metadata.txt` | 1 361 bytes | `163250586C651FAEAAA4226A06197FD0D395CD84B8933B08AD436DB3FD873E45` | Metadatos y cadena de custodia E-025 |
| `E-025/e025-safe-udp-oversized-20260804-121742-sender.txt` | 79 bytes | `C0EAA651A54FA70296A7AEE873358FEA1584094D46053EC0D713F54538BDA4F5` | Salida del emisor E-025 |
| `E-025/e025-safe-udp-oversized-20260804-121742.log` | 2 774 bytes | `D06D6FABF8E7333011CB51C66019BBAA7F14564EE96FD8AFED3B5AE44A664EDB` | Log primario E-025 |

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
