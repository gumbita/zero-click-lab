"""Genera entradas binarias inocuas y reproducibles para la demo local."""

from __future__ import annotations

from pathlib import Path
import struct


ROOT_DIR = Path(__file__).resolve().parents[1]
HEADER = struct.Struct(">4sBBBH I")
MAGIC = b"ECLB"
VERSION = 1
CALL_CONTROL = 1


def packet(payload: bytes, *, declared_length: int | None = None) -> bytes:
    length = len(payload) if declared_length is None else declared_length
    return HEADER.pack(MAGIC, VERSION, 0, CALL_CONTROL, length, 0x10203040) + payload


def main() -> None:
    benign = ROOT_DIR / "samples" / "benign"
    malformed = ROOT_DIR / "samples" / "malformed"
    benign.mkdir(parents=True, exist_ok=True)
    malformed.mkdir(parents=True, exist_ok=True)

    samples = {
        benign / "valid_call_control.bin": packet(b"CALL"),
        malformed / "oversized_payload.bin": packet(b"TINY", declared_length=64),
        malformed / "length_mismatch.bin": packet(b"SHORT", declared_length=10),
        malformed / "truncated_packet.bin": b"ECLB\x01",
    }
    for path, data in samples.items():
        path.write_bytes(data)
        print(f"Generated {path.relative_to(ROOT_DIR)} ({len(data)} bytes)")


if __name__ == "__main__":
    main()
