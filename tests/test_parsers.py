from __future__ import annotations

from pathlib import Path
import struct
import unittest

from app import secure_parser, vulnerable_parser
from app.secure_parser import PacketValidationError


ROOT_DIR = Path(__file__).resolve().parents[1]
BENIGN = ROOT_DIR / "samples" / "benign"
MALFORMED = ROOT_DIR / "samples" / "malformed"


class ParserTests(unittest.TestCase):
    def test_valid_sample_is_accepted_by_both_parsers(self) -> None:
        data = (BENIGN / "valid_call_control.bin").read_bytes()

        for parser in (vulnerable_parser.parse_packet, secure_parser.parse_packet):
            with self.subTest(parser=parser.__module__):
                result = parser(data)
                self.assertEqual(result["declared_length"], 4)
                self.assertEqual(result["actual_length"], 4)

    def test_oversized_payload_results(self) -> None:
        data = (MALFORMED / "oversized_payload.bin").read_bytes()

        with self.assertRaises(IndexError):
            vulnerable_parser.parse_packet(data)
        with self.assertRaisesRegex(PacketValidationError, "payload_too_large"):
            secure_parser.parse_packet(data)

    def test_length_mismatch_results(self) -> None:
        data = (MALFORMED / "length_mismatch.bin").read_bytes()

        with self.assertRaises(IndexError):
            vulnerable_parser.parse_packet(data)
        with self.assertRaisesRegex(PacketValidationError, "length_mismatch"):
            secure_parser.parse_packet(data)

    def test_truncated_packet_results(self) -> None:
        data = (MALFORMED / "truncated_packet.bin").read_bytes()

        with self.assertRaises(struct.error):
            vulnerable_parser.parse_packet(data)
        with self.assertRaisesRegex(PacketValidationError, "truncated_header"):
            secure_parser.parse_packet(data)


if __name__ == "__main__":
    unittest.main()
