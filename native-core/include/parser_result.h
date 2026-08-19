/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

#ifndef ZERO_CLICK_LAB_PARSER_RESULT_H
#define ZERO_CLICK_LAB_PARSER_RESULT_H

#include <stddef.h>
#include <stdint.h>

typedef enum parser_status {
    PARSER_OK = 0,
    PARSER_INVALID_ARGUMENT,
    PARSER_TRUNCATED_HEADER,
    PARSER_INVALID_MAGIC,
    PARSER_UNSUPPORTED_VERSION,
    PARSER_INVALID_PACKET_TYPE,
    PARSER_PAYLOAD_TOO_LARGE,
    PARSER_LENGTH_MISMATCH,
    PARSER_ALLOCATION_FAILED
} parser_status;

typedef struct parser_result {
    parser_status status;
    uint8_t version;
    uint8_t flags;
    uint8_t packet_type;
    uint16_t declared_length;
    size_t actual_length;
    uint32_t ssrc;
    uint8_t checksum;
} parser_result;

const char *parser_status_name(parser_status status);

#endif
