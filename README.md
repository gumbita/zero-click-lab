# Zero-click Lab

MVP educativo y local que muestra este patrón de forma segura:

```text
entrada recibida → procesamiento automático → parser vulnerable
→ fallo por falta de validación → parser seguro → rechazo controlado
```

No contiene malware, no explota software real, no usa red ni se conecta a
WhatsApp o a servicios externos. Los paquetes son un formato sintético propio y
Python convierte el fallo vulnerable en una excepción controlada, sin corrupción
de memoria.

## Formato de la muestra

Cada archivo contiene `MAGIC(4) | VERSION(1) | FLAGS(1) | TYPE(1) |
LENGTH(2) | SSRC(4) | PAYLOAD(N)`. El parser vulnerable confía deliberadamente
en `LENGTH`; el seguro valida cabecera, tipo, tamaño máximo y consistencia entre
la longitud declarada y la real.

## Ejecución en Windows PowerShell

Ejecuta los comandos desde la raíz del repositorio. No es necesario instalar
dependencias externas.

1. Genera las muestras:

   ```powershell
   python -m app.create_samples
   ```

   Se crean una muestra válida en `samples/benign/` y tres entradas de prueba en
   `samples/malformed/`.

2. Prueba válida con el parser vulnerable:

   ```powershell
   Copy-Item .\samples\benign\valid_call_control.bin .\inbox\
   python -m app.processor --mode vulnerable
   Get-Content .\logs\processing.log -Tail 10
   ```

   El log incluye `CONTROL_FILE_RECEIVED`, `PACKET_READ`, `PACKET_ACCEPTED` y
   `FILE_MOVED_TO_PROCESSED`.

3. Prueba malformada con el parser vulnerable:

   ```powershell
   Copy-Item .\samples\malformed\oversized_payload.bin .\inbox\
   python -m app.processor --mode vulnerable
   Get-Content .\logs\processing.log -Tail 10
   ```

   El acceso basado en la longitud no validada genera un `IndexError`, capturado
   como `PACKET_PROCESSING_FAILED outcome=controlled_failure`. El proceso sigue
   funcionando y mueve la muestra a `processed/`.

4. Prueba la misma entrada con el parser seguro:

   ```powershell
   Copy-Item .\samples\malformed\oversized_payload.bin .\inbox\
   python -m app.processor --mode secure
   Get-Content .\logs\processing.log -Tail 10
   ```

   El parser la rechaza antes de acceder al payload. El log muestra
   `PACKET_PROCESSING_FAILED outcome=rejected reason=payload_too_large`, sin
   excepción inesperada.

El procesador realiza una pasada sobre todos los `.bin` presentes en `inbox/` al
arrancar. Cada ejecución registra `ZERO_CLICK_LAB_STARTED`. Los archivos se
mueven a `processed/`; si un nombre ya existe, se añade un sufijo numérico.

## Archivos generados durante la ejecución

- Log comparable: `logs/processing.log`.
- Entradas ya tratadas: `processed/`.
- Entradas pendientes: `inbox/*.bin`.

Estas rutas están excluidas de Git mediante `.gitignore`.

