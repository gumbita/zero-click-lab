from __future__ import annotations

import hashlib
from pathlib import Path
import tempfile
import unittest

from app.create_samples import generate_samples


ROOT_DIR = Path(__file__).resolve().parents[1]

EXPECTED = {
    "samples/benign/valid_call_control.bin": (
        17,
        "912b5f7f858a790d4c49ae2860cd421f0b70c8dd8e582abe99ab6d6640965b8e",
    ),
    "samples/malformed/oversized_payload.bin": (
        17,
        "3c3cd136ffb223449f226fe22061922371e8b8c11ec60f336e293f41f4047d30",
    ),
    "samples/malformed/oversized_complete_payload.bin": (
        77,
        "516f7c6a9b6237274f33f8ab01057dfdbd1137df0c898f70b5afb6b7da742aba",
    ),
    "samples/malformed/length_mismatch.bin": (
        18,
        "b7b3e3d267ca313b943147a83c7461fb3e0553ef17a0b595ac986611a1b83584",
    ),
    "samples/malformed/truncated_packet.bin": (
        5,
        "9f46c77e1f2857e4e8d2a1c62403ef15275a664b20cf70acb2922f083cf1f18c",
    ),
}


class SampleGenerationTests(unittest.TestCase):
    def test_generation_is_isolated_and_reproducible(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            generated = generate_samples(root)
            relative_paths = {
                path.relative_to(root).as_posix() for path in generated
            }

            self.assertEqual(relative_paths, set(EXPECTED))
            self.assertTrue((root / "samples" / "benign").is_dir())
            self.assertTrue((root / "samples" / "malformed").is_dir())

            for relative, (size, expected_hash) in EXPECTED.items():
                generated_bytes = (root / relative).read_bytes()
                versioned_bytes = (ROOT_DIR / relative).read_bytes()
                actual_hash = hashlib.sha256(generated_bytes).hexdigest()

                self.assertEqual(len(generated_bytes), size)
                self.assertEqual(generated_bytes, versioned_bytes)
                self.assertEqual(actual_hash, expected_hash)


if __name__ == "__main__":
    unittest.main()
