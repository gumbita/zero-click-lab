# Seguridad y uso responsable

EchoCall Lab es un laboratorio controlado que contiene un parser C
deliberadamente vulnerable. Utiliza exclusivamente infraestructura propia,
aislada y autorizada; no expongas el receptor UDP ni las herramientas a redes o
sistemas de terceros.

## Alcance operativo

- ECLB es un formato sintético creado para el laboratorio.
- El quickstart y la CI ejecutan únicamente la ruta Patched segura.
- No automatices la muestra oversized contra Vulnerable ni repitas el crash
  ASan documentado.
- Compilar Vulnerable no ejecuta ninguna entrada, pero sus APK y CLI deben
  tratarse como componentes de laboratorio.
- La evidencia demuestra una escritura fuera de límites concreta; no demuestra
  RCE, control del flujo ni compromiso completo.

Consulta [Reproducción segura](docs/reproduction.md) para comandos permitidos y
[Limitaciones](docs/limitations.md) para el alcance metodológico.

## Comunicación privada

Si detectas una vulnerabilidad accidental distinta del comportamiento
deliberado, no publiques detalles explotables en un issue. Comunícala mediante
el canal privado por el que recibiste acceso al repositorio; si no dispones de
uno, solicita primero un canal privado sin incluir información sensible.

Este archivo no concede una licencia de uso. La licencia se decidirá antes de
una eventual publicación pública.
