/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

#include "safe_parser.h"

#include "packet_format.h"

static uint16_t read_be16(const uint8_t *data, const size_t offset)
{
    const uint16_t high = (uint16_t)data[offset];
    const uint16_t low = (uint16_t)data[offset + 1u];

    return (uint16_t)((uint16_t)(high << 8u) | low);
}

static uint32_t read_be32(const uint8_t *data, const size_t offset)
{
    const uint32_t byte0 = (uint32_t)data[offset];
    const uint32_t byte1 = (uint32_t)data[offset + 1u];
    const uint32_t byte2 = (uint32_t)data[offset + 2u];
    const uint32_t byte3 = (uint32_t)data[offset + 3u];

    return (byte0 << 24u) | (byte1 << 16u) | (byte2 << 8u) | byte3;
}

static int has_valid_magic(const uint8_t *data)
{
    return data[PACKET_OFFSET_MAGIC] == PACKET_MAGIC_0
        && data[PACKET_OFFSET_MAGIC + 1u] == PACKET_MAGIC_1
        && data[PACKET_OFFSET_MAGIC + 2u] == PACKET_MAGIC_2
        && data[PACKET_OFFSET_MAGIC + 3u] == PACKET_MAGIC_3;
}

parser_status safe_parse_packet(
    const uint8_t *data,
    const size_t data_size,
    parser_result *result
)
{
    size_t index = 0u;
    uint32_t checksum = 0u;

    if (result == NULL) {
        return PARSER_INVALID_ARGUMENT;
    }

    *result = (parser_result){0};

    if (data == NULL && data_size > 0u) {
        result->status = PARSER_INVALID_ARGUMENT;
        return result->status;
    }
    if (data_size < PACKET_HEADER_SIZE) {
        result->status = PARSER_TRUNCATED_HEADER;
        return result->status;
    }

    result->version = data[PACKET_OFFSET_VERSION];
    result->flags = data[PACKET_OFFSET_FLAGS];
    result->packet_type = data[PACKET_OFFSET_TYPE];
    result->declared_length = read_be16(data, PACKET_OFFSET_LENGTH);
    result->actual_length = data_size - PACKET_OFFSET_PAYLOAD;
    result->ssrc = read_be32(data, PACKET_OFFSET_SSRC);

    if (!has_valid_magic(data)) {
        result->status = PARSER_INVALID_MAGIC;
        return result->status;
    }
    if (result->version != PACKET_VERSION) {
        result->status = PARSER_UNSUPPORTED_VERSION;
        return result->status;
    }
    if (result->packet_type != PACKET_TYPE_CALL_CONTROL) {
        result->status = PARSER_INVALID_PACKET_TYPE;
        return result->status;
    }
    if ((size_t)result->declared_length > PACKET_MAX_PAYLOAD_SIZE) {
        result->status = PARSER_PAYLOAD_TOO_LARGE;
        return result->status;
    }
    if ((size_t)result->declared_length != result->actual_length) {
        result->status = PARSER_LENGTH_MISMATCH;
        return result->status;
    }

    for (index = 0u; index < result->actual_length; ++index) {
        checksum += (uint32_t)data[PACKET_OFFSET_PAYLOAD + index];
    }
    result->checksum = (uint8_t)(checksum % 256u);
    result->status = PARSER_OK;
    return result->status;
}
