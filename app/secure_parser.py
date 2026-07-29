"""Parser defensivo del formato binario sintetico del laboratorio."""

from __future__ import annotations

import struct


HEADER = struct.Struct(">4sBBBHI")
MAGIC = b"ECLB"
VERSION = 1
ALLOWED_TYPES = {1}
MAX_PAYLOAD_SIZE = 32


class PacketValidationError(ValueError):
    """Rechazo esperado de una entrada que no cumple el formato."""


def parse_packet(data: bytes) -> dict[str, object]:
    if len(data) < HEADER.size:
        raise PacketValidationError(
            f"truncated_header actual={len(data)} required={HEADER.size}"
        )

    magic, version, flags, packet_type, declared_length, ssrc = (
        HEADER.unpack_from(data)
    )
    payload = data[HEADER.size:]

    if magic != MAGIC:
        raise PacketValidationError("invalid_magic")
    if version != VERSION:
        raise PacketValidationError(f"unsupported_version value={version}")
    if packet_type not in ALLOWED_TYPES:
        raise PacketValidationError(f"invalid_packet_type value={packet_type}")
    if declared_length > MAX_PAYLOAD_SIZE:
        raise PacketValidationError(
            f"payload_too_large declared={declared_length} maximum={MAX_PAYLOAD_SIZE}"
        )
    if declared_length != len(payload):
        raise PacketValidationError(
            f"length_mismatch declared={declared_length} actual={len(payload)}"
        )

    return {
        "magic": magic.decode("ascii"),
        "version": version,
        "flags": flags,
        "packet_type": packet_type,
        "declared_length": declared_length,
        "actual_length": len(payload),
        "ssrc": ssrc,
        "checksum": sum(payload) % 256,
    }
