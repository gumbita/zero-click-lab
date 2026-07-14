"""Emula validacion insuficiente en Python, sin corrupcion de memoria nativa."""

from __future__ import annotations

import struct


HEADER = struct.Struct(">4sBBBHI")


def parse_packet(data: bytes) -> dict[str, object]:
    """Procesa confiando en la longitud declarada (patron inseguro intencional)."""
    magic, version, flags, packet_type, declared_length, ssrc = (
        HEADER.unpack_from(data)
    )
    payload = data[HEADER.size:]

    # Fallo didactico: accede a todos los bytes declarados sin comprobar cuantos
    # llegaron realmente. Python lo convierte en IndexError; no hay corrupcion.
    checksum = 0
    for index in range(declared_length):
        checksum = (checksum + payload[index]) % 256

    return {
        "magic": magic.decode("ascii", errors="replace"),
        "version": version,
        "flags": flags,
        "packet_type": packet_type,
        "declared_length": declared_length,
        "actual_length": len(payload),
        "ssrc": ssrc,
        "checksum": checksum,
    }
