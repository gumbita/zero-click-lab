#include <stdint.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "packet_format.h"
#include "parser_result.h"
#include "safe_parser.h"

typedef struct test_buffer {
    uint8_t *data;
    size_t size;
} test_buffer;

static int failures = 0;

#define CHECK(condition, message) \
    do { \
        if (!(condition)) { \
            fprintf(stderr, "FAIL: %s\n", (message)); \
            ++failures; \
        } \
    } while (0)

static test_buffer read_file(const char *path)
{
    test_buffer buffer = {0};
    FILE *input = fopen(path, "rb");
    long raw_size = 0L;

    if (input == NULL) {
        return buffer;
    }
    if (fseek(input, 0L, SEEK_END) != 0) {
        (void)fclose(input);
        return buffer;
    }
    raw_size = ftell(input);
    if (raw_size <= 0L || fseek(input, 0L, SEEK_SET) != 0) {
        (void)fclose(input);
        return buffer;
    }

    buffer.size = (size_t)raw_size;
    buffer.data = malloc(buffer.size);
    if (buffer.data == NULL) {
        buffer.size = 0u;
        (void)fclose(input);
        return buffer;
    }
    if (fread(buffer.data, 1u, buffer.size, input) != buffer.size) {
        free(buffer.data);
        buffer.data = NULL;
        buffer.size = 0u;
    }
    (void)fclose(input);
    return buffer;
}

static void test_versioned_samples(char *argv[])
{
    const parser_status expected[] = {
        PARSER_OK,
        PARSER_PAYLOAD_TOO_LARGE,
        PARSER_LENGTH_MISMATCH,
        PARSER_TRUNCATED_HEADER
    };
    size_t index = 0u;

    for (index = 0u; index < 4u; ++index) {
        test_buffer buffer = read_file(argv[index + 1u]);
        parser_result result;
        parser_status status;

        CHECK(buffer.data != NULL, "versioned sample could not be read");
        if (buffer.data == NULL) {
            continue;
        }
        status = safe_parse_packet(buffer.data, buffer.size, &result);
        CHECK(status == expected[index], "unexpected sample status");

        if (index == 0u) {
            CHECK(result.declared_length == 4u, "valid declared length");
            CHECK(result.actual_length == 4u, "valid actual length");
            CHECK(result.ssrc == UINT32_C(0x10203040), "valid SSRC");
            CHECK(result.checksum == 28u, "valid checksum");
        }
        free(buffer.data);
    }
}

static void test_mutated_headers(const char *valid_path)
{
    test_buffer valid = read_file(valid_path);
    parser_result result;

    CHECK(valid.data != NULL, "valid sample required for mutations");
    if (valid.data == NULL) {
        return;
    }

    valid.data[PACKET_OFFSET_MAGIC] = (uint8_t)0u;
    CHECK(
        safe_parse_packet(valid.data, valid.size, &result) == PARSER_INVALID_MAGIC,
        "invalid magic"
    );
    valid.data[PACKET_OFFSET_MAGIC] = PACKET_MAGIC_0;

    valid.data[PACKET_OFFSET_VERSION] = (uint8_t)2u;
    CHECK(
        safe_parse_packet(valid.data, valid.size, &result)
            == PARSER_UNSUPPORTED_VERSION,
        "unsupported version"
    );
    valid.data[PACKET_OFFSET_VERSION] = PACKET_VERSION;

    valid.data[PACKET_OFFSET_TYPE] = (uint8_t)2u;
    CHECK(
        safe_parse_packet(valid.data, valid.size, &result)
            == PARSER_INVALID_PACKET_TYPE,
        "invalid packet type"
    );

    free(valid.data);
}

static void test_arguments_and_names(void)
{
    static const struct {
        parser_status status;
        const char *name;
    } status_names[] = {
        {PARSER_OK, "ok"},
        {PARSER_INVALID_ARGUMENT, "invalid_argument"},
        {PARSER_TRUNCATED_HEADER, "truncated_header"},
        {PARSER_INVALID_MAGIC, "invalid_magic"},
        {PARSER_UNSUPPORTED_VERSION, "unsupported_version"},
        {PARSER_INVALID_PACKET_TYPE, "invalid_packet_type"},
        {PARSER_PAYLOAD_TOO_LARGE, "payload_too_large"},
        {PARSER_LENGTH_MISMATCH, "length_mismatch"}
    };
    const uint8_t empty_buffer = (uint8_t)0u;
    parser_result result;
    size_t index = 0u;

    CHECK(
        safe_parse_packet(&empty_buffer, 0u, &result) == PARSER_TRUNCATED_HEADER,
        "empty input"
    );
    CHECK(
        safe_parse_packet(NULL, 1u, &result) == PARSER_INVALID_ARGUMENT,
        "null data with nonzero size"
    );
    CHECK(
        safe_parse_packet(&empty_buffer, 1u, NULL) == PARSER_INVALID_ARGUMENT,
        "null result"
    );
    for (index = 0u; index < sizeof(status_names) / sizeof(status_names[0]); ++index) {
        CHECK(
            strcmp(parser_status_name(status_names[index].status), status_names[index].name)
                == 0,
            "parser status name"
        );
    }
}

int main(const int argc, char *argv[])
{
    if (argc != 5) {
        fprintf(stderr, "usage: test_safe_parser <valid> <oversized> <mismatch> <truncated>\n");
        return 2;
    }

    test_versioned_samples(argv);
    test_mutated_headers(argv[1]);
    test_arguments_and_names();

    if (failures != 0) {
        fprintf(stderr, "native_parser_tests=failed count=%d\n", failures);
        return 1;
    }
    printf("native_parser_tests=passed\n");
    return 0;
}
