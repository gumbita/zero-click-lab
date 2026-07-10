"""Procesa automaticamente todos los .bin presentes en inbox/."""

from __future__ import annotations

import argparse
from pathlib import Path
import shutil

from app.logger import get_logger
from app.secure_parser import PacketValidationError
from app import secure_parser, vulnerable_parser


ROOT_DIR = Path(__file__).resolve().parents[1]
INBOX = ROOT_DIR / "inbox"
PROCESSED = ROOT_DIR / "processed"


def destination_for(source: Path) -> Path:
    destination = PROCESSED / source.name
    counter = 1
    while destination.exists():
        destination = PROCESSED / f"{source.stem}_{counter}{source.suffix}"
        counter += 1
    return destination


def process_file(path: Path, mode: str) -> None:
    logger = get_logger()
    logger.info("CONTROL_FILE_RECEIVED file=%s mode=%s", path.name, mode)
    try:
        data = path.read_bytes()
        logger.info("PACKET_READ file=%s bytes=%d", path.name, len(data))
        parser = secure_parser.parse_packet if mode == "secure" else vulnerable_parser.parse_packet
        result = parser(data)
        logger.info(
            "PACKET_ACCEPTED file=%s mode=%s declared_length=%s actual_length=%s",
            path.name,
            mode,
            result["declared_length"],
            result["actual_length"],
        )
    except PacketValidationError as exc:
        logger.warning(
            "PACKET_PROCESSING_FAILED file=%s mode=%s outcome=rejected reason=%s",
            path.name,
            mode,
            exc,
        )
    except Exception as exc:  # frontera de seguridad: la demo no termina abruptamente
        logger.error(
            "PACKET_PROCESSING_FAILED file=%s mode=%s outcome=controlled_failure error=%s reason=%s",
            path.name,
            mode,
            type(exc).__name__,
            exc,
        )
    finally:
        if path.exists():
            destination = destination_for(path)
            shutil.move(str(path), destination)
            logger.info(
                "FILE_MOVED_TO_PROCESSED file=%s destination=%s",
                path.name,
                destination.name,
            )


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--mode", choices=("vulnerable", "secure"), required=True)
    args = parser.parse_args()

    INBOX.mkdir(parents=True, exist_ok=True)
    PROCESSED.mkdir(parents=True, exist_ok=True)
    logger = get_logger()
    logger.info("ZERO_CLICK_LAB_STARTED mode=%s inbox=%s", args.mode, INBOX)

    files = sorted(INBOX.glob("*.bin"))
    if not files:
        logger.info("INBOX_EMPTY mode=%s", args.mode)
    for path in files:
        process_file(path, args.mode)


if __name__ == "__main__":
    main()
