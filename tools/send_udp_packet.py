# Copyright (C) 2026 Àngels Gumbau Granero
# SPDX-License-Identifier: GPL-3.0-only
# See LICENSE in the repository root.

"""Send one binary packet as a single UDP datagram."""

from __future__ import annotations

import argparse
from pathlib import Path
import socket
import sys


DEFAULT_HOST = "127.0.0.1"
DEFAULT_PORT = 43568
MAX_UDP_PAYLOAD_SIZE = 65_507


def valid_port(value: str) -> int:
    """Return a valid UDP port parsed from an argument."""
    port = int(value)
    if not 1 <= port <= 65_535:
        raise argparse.ArgumentTypeError("port must be between 1 and 65535")
    return port


def parse_args() -> argparse.Namespace:
    """Parse command-line arguments."""
    parser = argparse.ArgumentParser(
        description="Send a binary EchoCall Lab packet over UDP.",
    )
    parser.add_argument("--host", default=DEFAULT_HOST)
    parser.add_argument("--port", type=valid_port, default=DEFAULT_PORT)
    parser.add_argument("--file", required=True, type=Path)
    return parser.parse_args()


def main() -> int:
    """Read and send one file as one UDP datagram."""
    args = parse_args()
    packet_path: Path = args.file

    if not packet_path.exists():
        print(
            f"error: packet file does not exist: {packet_path}",
            file=sys.stderr,
        )
        return 2
    if not packet_path.is_file():
        print(
            f"error: packet path is not a regular file: {packet_path}",
            file=sys.stderr,
        )
        return 2

    try:
        packet = packet_path.read_bytes()
    except OSError as error:
        print(f"error: unable to read {packet_path}: {error}", file=sys.stderr)
        return 3

    if len(packet) > MAX_UDP_PAYLOAD_SIZE:
        print(
            "error: packet exceeds the maximum UDP payload size "
            f"({len(packet)} > {MAX_UDP_PAYLOAD_SIZE})",
            file=sys.stderr,
        )
        return 4

    try:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as udp_socket:
            bytes_sent = udp_socket.sendto(packet, (args.host, args.port))
    except (OSError, OverflowError) as error:
        print(
            f"error: unable to send to {args.host}:{args.port}: {error}",
            file=sys.stderr,
        )
        return 5

    if bytes_sent != len(packet):
        print(
            f"error: incomplete datagram send ({bytes_sent}/{len(packet)} bytes)",
            file=sys.stderr,
        )
        return 6

    print(
        f"destination={args.host}:{args.port} "
        f"file={packet_path.name} bytes_sent={bytes_sent}",
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
