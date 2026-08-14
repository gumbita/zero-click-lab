# Seguridad y uso responsable

## Alcance del repositorio

Este repositorio contiene código deliberadamente vulnerable creado con fines
académicos. Debe utilizarse únicamente en sistemas propios, emuladores y
entornos controlados para los que exista autorización expresa.

EchoCall es una aplicación de laboratorio independiente. No contiene código de
WhatsApp, no implementa RTCP real y no proporciona un exploit contra WhatsApp
ni contra terceros. Sus entradas emplean ECLB, un formato sintético diseñado
para este proyecto.

La demostración reproduce de forma controlada una operación de copia insegura
y su contraste con una validación defensiva. No constituye una reproducción
exacta de CVE-2019-3568 y no demuestra ejecución remota de código (RCE), control
del flujo ni compromiso de un dispositivo.

## Uso permitido

- Revisión académica y análisis estático del código.
- Ejecución en infraestructura propia y aislada siguiendo los procedimientos
  documentados.
- Reproducción de resultados únicamente dentro del alcance autorizado del
  laboratorio.

No debe utilizarse el código, las herramientas ni las muestras contra sistemas
de terceros, servicios reales o dispositivos sin autorización. Las muestras
malformadas requieren especial cautela y solo deben ejecutarse en fases y
entornos expresamente preparados para ello.

## Comunicación de problemas

Si detectas una vulnerabilidad accidental distinta del comportamiento
deliberadamente incluido, evita publicar detalles explotables antes de que
pueda revisarse. Comunícala de forma privada a la persona responsable del
repositorio mediante los canales privados asociados al proyecto.

Este archivo no concede una licencia de uso. La licencia del repositorio se
decidirá por separado antes de una eventual publicación pública.
