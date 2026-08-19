/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

#ifndef ZERO_CLICK_LAB_VULNERABLE_PARSER_H
#define ZERO_CLICK_LAB_VULNERABLE_PARSER_H

#include <stddef.h>
#include <stdint.h>

#include "parser_result.h"

parser_status vulnerable_parse_packet(
    const uint8_t *data,
    size_t data_size,
    parser_result *result
);

#endif
