\# Zero-click Lab



Laboratorio educativo para demostrar, de forma segura y controlada, el patrón general de una vulnerabilidad zero-click:



```text

entrada recibida

→ procesamiento automático

→ parser vulnerable

→ fallo por falta de validación

→ parser corregido

→ rechazo controlado



Alcance



Este laboratorio no implementa malware, no explota software real y no interactúa con WhatsApp ni con infraestructura de terceros.



La demo procesa archivos locales .bin dentro de una carpeta inbox/ para simular la llegada automática de una entrada externa.



Estructura inicial

zero-click-lab/

├─ app/

├─ inbox/

├─ processed/

├─ samples/

│  ├─ benign/

│  └─ malformed/

├─ logs/

├─ docs/

├─ README.md

├─ requirements.txt

└─ .gitignore



Guarda y cierra.



\# Paso 6 — Primer commit local



Ejecuta:



```powershell id="y6qgwo"

git add .

git commit -m "Initial zero-click lab structure"

