# Artefactos de evidencia

Los artefactos que se incorporen en esta ruta deben usar el ID de la matriz y un
nombre descriptivo. Ejemplos:

```text
E-005_asan_receiver_vuln_oversized.txt
E-006_receiver_safe_oversized.txt
E-013_android_flujo_pre_interaccion_logcat.txt
E-013_android_llamada_pre_interaccion.png
manifest_sha256.md
```

Cada log debe conservar, siempre que sea posible:

1. fecha y entorno de ejecución;
2. comando exacto;
3. entrada y SHA-256;
4. stdout y stderr sin recortes relevantes;
5. exit code;
6. versión de la herramienta empleada.

Las capturas son material complementario. Para ASan, Gradle y Logcat se debe
conservar también la salida textual completa. No se añadirán APK, volcados
masivos ni datos personales. Una biblioteca nativa o paquete de símbolos solo
se conservará de forma excepcional cuando su Build ID esté vinculado a una
evidencia principal y sea necesario para su simbolización.

La presencia de código, un commit o un APK actual permite reconstruir una
configuración, pero no convierte por sí sola un resultado histórico en
evidencia experimental primaria. Para futuras pruebas se calculará el hash de
la entrada y, cuando proceda, del APK antes de ejecutar; se conservarán el
comando, la salida íntegra y el estado final del proceso.

## Derivados saneados

La transcripción E-021–E-024 versionada es un derivado documental del original
externo. La única transformación aplicada sustituye las 915 apariciones del
prefijo de perfil personal de Windows por el marcador:

```text
<PREFIJO_DE_PERFIL_ORIGINAL>  ->  <USER_HOME>
```

No se cambian fechas, comandos, resultados, PIDs, hashes, mensajes de error ni
ningún otro byte del contenido técnico. El original tiene
372 699 bytes, 4 258 líneas y SHA-256
`ECED591432B4783142303B530FD42AC41CFD6493722FA410B4C4DC0C9A860F14`.
Permanece fuera de Git por contener rutas personales. El derivado saneado tiene
369 039 bytes, las mismas 4 258 líneas y SHA-256
`92C0CF67B87D2D126504FA9FDD59455753EA56363B40AFD1865EB25866595496`.

La política [`.gitattributes`](../../../.gitattributes) desactiva la
normalización de finales de línea para logs, TXT y XML de este directorio y
trata los PNG como binarios. Así, el blob versionado conserva los mismos bytes
que el artefacto cuyo SHA-256 figura en el manifest. Los espacios finales ya
presentes en el log E-022 y en los metadatos E-025 se preservan; la regla de
whitespace solo evita que `git diff --check` los confunda con defectos del
contenido documental.
