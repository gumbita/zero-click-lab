/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>

#include "packet_format.h"
#include "parser_result.h"
#include "vulnerable_parser.h"

#define CLI_MAX_FILE_SIZE ((long)1048576L)

enum cli_exit_code {
    CLI_EXIT_ACCEPTED = 0,
    CLI_EXIT_REJECTED = 2,
    CLI_EXIT_FILE_ERROR = 3,
    CLI_EXIT_ALLOCATION_ERROR = 4,
    CLI_EXIT_USAGE = 64
};

static int print_rejection(
    const parser_result *result,
    const size_t file_size
)
{
    switch (result->status) {
    case PARSER_TRUNCATED_HEADER:
        printf(
            "status=rejected code=truncated_header actual_size=%zu "
            "required_size=%zu\n",
            file_size,
            PACKET_HEADER_SIZE
        );
        break;
    case PARSER_INVALID_MAGIC:
        printf("status=rejected code=invalid_magic\n");
        break;
    case PARSER_UNSUPPORTED_VERSION:
        printf(
            "status=rejected code=unsupported_version version=%u\n",
            (unsigned int)result->version
        );
        break;
    case PARSER_INVALID_PACKET_TYPE:
        printf(
            "status=rejected code=invalid_packet_type type=%u\n",
            (unsigned int)result->packet_type
        );
        break;
    case PARSER_PAYLOAD_TOO_LARGE:
        printf(
            "status=rejected code=payload_too_large declared_length=%u "
            "actual_length=%zu maximum=%zu\n",
            (unsigned int)result->declared_length,
            result->actual_length,
            PACKET_MAX_PAYLOAD_SIZE
        );
        break;
    case PARSER_LENGTH_MISMATCH:
        printf(
            "status=rejected code=length_mismatch declared_length=%u "
            "actual_length=%zu\n",
            (unsigned int)result->declared_length,
            result->actual_length
        );
        break;
    case PARSER_ALLOCATION_FAILED:
        fprintf(stderr, "status=error code=allocation_failed\n");
        return CLI_EXIT_ALLOCATION_ERROR;
    default:
        printf(
            "status=rejected code=%s\n",
            parser_status_name(result->status)
        );
        break;
    }

    return CLI_EXIT_REJECTED;
}

static int parse_and_print(const uint8_t *data, const size_t file_size)
{
    parser_result result;
    const parser_status status = vulnerable_parse_packet(data, file_size, &result);

    if (status == PARSER_OK) {
        printf(
            "status=accepted code=ok version=%u flags=%u type=%u "
            "declared_length=%u actual_length=%zu ssrc=0x%08" PRIx32
            " checksum=%u\n",
            (unsigned int)result.version,
            (unsigned int)result.flags,
            (unsigned int)result.packet_type,
            (unsigned int)result.declared_length,
            result.actual_length,
            result.ssrc,
            (unsigned int)result.checksum
        );
        return CLI_EXIT_ACCEPTED;
    }

    return print_rejection(&result, file_size);
}

int main(const int argc, char *argv[])
{
    FILE *input = NULL;
    long raw_size = 0L;
    size_t file_size = 0u;
    uint8_t *data = NULL;
    int exit_code = CLI_EXIT_FILE_ERROR;

    if (argc != 2) {
        fprintf(stderr, "usage: receiver_vuln <sample.bin>\n");
        return CLI_EXIT_USAGE;
    }

    input = fopen(argv[1], "rb");
    if (input == NULL) {
        fprintf(stderr, "status=error code=file_open_failed\n");
        return CLI_EXIT_FILE_ERROR;
    }
    if (fseek(input, 0L, SEEK_END) != 0) {
        fprintf(stderr, "status=error code=file_read_failed\n");
        (void)fclose(input);
        return CLI_EXIT_FILE_ERROR;
    }
    raw_size = ftell(input);
    if (raw_size < 0L) {
        fprintf(stderr, "status=error code=file_read_failed\n");
        (void)fclose(input);
        return CLI_EXIT_FILE_ERROR;
    }
    if (raw_size > CLI_MAX_FILE_SIZE) {
        fprintf(
            stderr,
            "status=error code=file_too_large maximum=%ld\n",
            CLI_MAX_FILE_SIZE
        );
        (void)fclose(input);
        return CLI_EXIT_FILE_ERROR;
    }
    if (fseek(input, 0L, SEEK_SET) != 0) {
        fprintf(stderr, "status=error code=file_read_failed\n");
        (void)fclose(input);
        return CLI_EXIT_FILE_ERROR;
    }

    file_size = (size_t)raw_size;
    if (file_size > 0u) {
        data = malloc(file_size);
        if (data == NULL) {
            fprintf(stderr, "status=error code=allocation_failed\n");
            (void)fclose(input);
            return CLI_EXIT_ALLOCATION_ERROR;
        }
        if (fread(data, 1u, file_size, input) != file_size) {
            fprintf(stderr, "status=error code=file_read_failed\n");
            free(data);
            (void)fclose(input);
            return CLI_EXIT_FILE_ERROR;
        }
    }
    if (fclose(input) != 0) {
        fprintf(stderr, "status=error code=file_read_failed\n");
        free(data);
        return CLI_EXIT_FILE_ERROR;
    }

    exit_code = parse_and_print(data, file_size);
    free(data);
    return exit_code;
}
