from __future__ import annotations

import logging
from pathlib import Path
import shutil
import tempfile
import unittest
import uuid

from app.processor import process_inbox


ROOT_DIR = Path(__file__).resolve().parents[1]
BENIGN = ROOT_DIR / "samples" / "benign"
MALFORMED = ROOT_DIR / "samples" / "malformed"
LOG_FORMAT = "%(asctime)s level=%(levelname)s event=%(message)s"


class ProcessorTests(unittest.TestCase):
    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary.name)
        self.inbox = self.root / "inbox"
        self.processed = self.root / "processed"
        self.log_dir = self.root / "logs"
        self.log_dir.mkdir(parents=True)
        self.log_file = self.log_dir / "processing.log"

        self.logger = logging.getLogger(f"zero_click_lab_test.{uuid.uuid4()}")
        self.logger.setLevel(logging.INFO)
        self.logger.propagate = False
        handler = logging.FileHandler(self.log_file, encoding="utf-8")
        handler.setFormatter(logging.Formatter(LOG_FORMAT))
        self.logger.addHandler(handler)

    def tearDown(self) -> None:
        for handler in list(self.logger.handlers):
            handler.close()
            self.logger.removeHandler(handler)
        self.temporary.cleanup()

    def read_log(self) -> str:
        for handler in self.logger.handlers:
            handler.flush()
        return self.log_file.read_text(encoding="utf-8")

    def test_vulnerable_mode_continues_moves_and_preserves_destination(self) -> None:
        self.inbox.mkdir()
        self.processed.mkdir()
        shutil.copy2(MALFORMED / "length_mismatch.bin", self.inbox)
        shutil.copy2(BENIGN / "valid_call_control.bin", self.inbox)
        existing = self.processed / "valid_call_control.bin"
        existing.write_bytes(b"existing")

        process_inbox(
            "vulnerable",
            inbox_dir=self.inbox,
            processed_dir=self.processed,
            logger=self.logger,
        )

        self.assertEqual(list(self.inbox.glob("*.bin")), [])
        self.assertEqual(existing.read_bytes(), b"existing")
        self.assertTrue((self.processed / "length_mismatch.bin").is_file())
        self.assertTrue((self.processed / "valid_call_control_1.bin").is_file())

        log = self.read_log()
        for event in (
            "ZERO_CLICK_LAB_STARTED",
            "CONTROL_FILE_RECEIVED",
            "PACKET_READ",
            "PACKET_ACCEPTED",
            "PACKET_PROCESSING_FAILED",
            "FILE_MOVED_TO_PROCESSED",
        ):
            self.assertIn(event, log)
        self.assertIn("outcome=controlled_failure", log)
        self.assertLess(
            log.index("file=length_mismatch.bin"),
            log.index("file=valid_call_control.bin"),
        )

    def test_secure_mode_records_rejection_and_moves_file(self) -> None:
        self.inbox.mkdir()
        shutil.copy2(MALFORMED / "oversized_payload.bin", self.inbox)

        process_inbox(
            "secure",
            inbox_dir=self.inbox,
            processed_dir=self.processed,
            logger=self.logger,
        )

        self.assertEqual(list(self.inbox.glob("*.bin")), [])
        self.assertTrue((self.processed / "oversized_payload.bin").is_file())
        log = self.read_log()
        self.assertIn("PACKET_PROCESSING_FAILED", log)
        self.assertIn("outcome=rejected", log)
        self.assertIn("FILE_MOVED_TO_PROCESSED", log)

    def test_empty_inbox_is_created_and_logged(self) -> None:
        process_inbox(
            "secure",
            inbox_dir=self.inbox,
            processed_dir=self.processed,
            logger=self.logger,
        )

        self.assertTrue(self.inbox.is_dir())
        self.assertTrue(self.processed.is_dir())
        log = self.read_log()
        self.assertIn("ZERO_CLICK_LAB_STARTED", log)
        self.assertIn("INBOX_EMPTY", log)


if __name__ == "__main__":
    unittest.main()
