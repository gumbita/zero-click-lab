"""Configuracion de logging compartida por el laboratorio."""

from __future__ import annotations

import logging
from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parents[1]
LOG_FILE = ROOT_DIR / "logs" / "processing.log"


def get_logger() -> logging.Logger:
    """Devuelve el logger del laboratorio, escribiendo siempre en processing.log."""
    LOG_FILE.parent.mkdir(parents=True, exist_ok=True)
    logger = logging.getLogger("zero_click_lab")
    logger.setLevel(logging.INFO)
    logger.propagate = False

    target = str(LOG_FILE.resolve())
    if not any(
        isinstance(handler, logging.FileHandler)
        and handler.baseFilename == target
        for handler in logger.handlers
    ):
        handler = logging.FileHandler(LOG_FILE, encoding="utf-8")
        handler.setFormatter(
            logging.Formatter("%(asctime)s level=%(levelname)s event=%(message)s")
        )
        logger.addHandler(handler)

    return logger
