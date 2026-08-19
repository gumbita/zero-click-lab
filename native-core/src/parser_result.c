/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

#include "parser_result.h"

const char *parser_status_name(const parser_status status)
{
    switch (status) {
    case PARSER_OK:
        return "ok";
    case PARSER_INVALID_ARGUMENT:
        return "invalid_argument";
    case PARSER_TRUNCATED_HEADER:
        return "truncated_header";
    case PARSER_INVALID_MAGIC:
        return "invalid_magic";
    case PARSER_UNSUPPORTED_VERSION:
        return "unsupported_version";
    case PARSER_INVALID_PACKET_TYPE:
        return "invalid_packet_type";
    case PARSER_PAYLOAD_TOO_LARGE:
        return "payload_too_large";
    case PARSER_LENGTH_MISMATCH:
        return "length_mismatch";
    case PARSER_ALLOCATION_FAILED:
        return "allocation_failed";
    default:
        return "unknown_status";
    }
}
