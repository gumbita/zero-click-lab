# Native Core — Fase 1A

Parser seguro en C para el formato binario documentado en
`docs/02_packet_format.md`. Esta fase solo lee archivos locales y no contiene
código vulnerable, red ni dependencias externas.

## Contrato

La cabecera ocupa 13 bytes y todos los enteros multibyte se leen explícitamente
en big-endian. El parser valida argumentos, cabecera, magic `ECLB`, versión 1,
tipo 1, payload máximo de 32 bytes y coincidencia entre longitud declarada y
real. No convierte el buffer a una estructura C.

`receiver_safe` aplica además un límite defensivo de lectura de 1 MiB. Este es
un límite propio de la CLI para evitar reservas arbitrarias: no pertenece al
formato binario y no modifica el máximo semántico de 32 bytes del payload seguro.

## Build en WSL

Desde la raíz del repositorio:

```powershell
wsl -- bash -lc "cd /mnt/c/Users/Angels/Documents/GitHub/zero-click-lab && cmake -S native-core -B native-core/build -G 'Unix Makefiles' -DCMAKE_BUILD_TYPE=Debug -DCMAKE_C_COMPILER=cc"
wsl -- bash -lc "cd /mnt/c/Users/Angels/Documents/GitHub/zero-click-lab && cmake --build native-core/build --parallel"
wsl -- bash -lc "cd /mnt/c/Users/Angels/Documents/GitHub/zero-click-lab && ctest --test-dir native-core/build --output-on-failure"
```

## Uso

```text
receiver_safe <sample.bin>
```

Ejemplo:

```powershell
wsl -- bash -lc "cd /mnt/c/Users/Angels/Documents/GitHub/zero-click-lab && ./native-core/build/receiver_safe samples/benign/valid_call_control.bin"
```

Códigos de salida:

- `0`: paquete aceptado;
- `2`: paquete rechazado por el parser;
- `3`: error de archivo o límite de lectura;
- `4`: error de asignación de memoria;
- `64`: uso incorrecto.
