#ifndef ZERO_CLICK_LAB_SAFE_PARSER_H
#define ZERO_CLICK_LAB_SAFE_PARSER_H

#include <stddef.h>
#include <stdint.h>

#include "parser_result.h"

parser_status safe_parse_packet(
    const uint8_t *data,
    size_t data_size,
    parser_result *result
);

#endif
