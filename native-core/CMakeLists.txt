cmake_minimum_required(VERSION 3.20)

project(zero_click_native_core VERSION 0.1.0 LANGUAGES C)

set(CMAKE_C_STANDARD 17)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_EXTENSIONS OFF)

option(ENABLE_ASAN "Enable AddressSanitizer for the vulnerable receiver" OFF)

function(enable_strict_warnings target_name)
    if(CMAKE_C_COMPILER_ID MATCHES "GNU|Clang")
        target_compile_options(${target_name} PRIVATE
            -Wall
            -Wextra
            -Wpedantic
            -Wconversion
            -Wsign-conversion
            -Wshadow
            -Wformat=2
            -Wundef
            -Werror
        )
    endif()
endfunction()

add_library(parser_result STATIC src/parser_result.c)
target_include_directories(parser_result PUBLIC include)
enable_strict_warnings(parser_result)

add_library(safe_parser STATIC src/safe_parser.c)
target_include_directories(safe_parser PUBLIC include)
target_link_libraries(safe_parser PUBLIC parser_result)
enable_strict_warnings(safe_parser)

add_executable(receiver_safe cli/receiver_safe.c)
target_link_libraries(receiver_safe PRIVATE safe_parser)
enable_strict_warnings(receiver_safe)

add_library(vulnerable_parser STATIC src/vulnerable_parser.c)
target_include_directories(vulnerable_parser PUBLIC include)
target_link_libraries(vulnerable_parser PUBLIC parser_result)
enable_strict_warnings(vulnerable_parser)

add_executable(receiver_vuln cli/receiver_vuln.c)
target_link_libraries(receiver_vuln PRIVATE vulnerable_parser)
enable_strict_warnings(receiver_vuln)

if(ENABLE_ASAN AND CMAKE_C_COMPILER_ID MATCHES "GNU|Clang")
    target_compile_options(vulnerable_parser PRIVATE
        -fsanitize=address
        -fno-omit-frame-pointer
    )
    target_compile_options(receiver_vuln PRIVATE
        -fsanitize=address
        -fno-omit-frame-pointer
    )
    target_link_options(receiver_vuln PRIVATE -fsanitize=address)
endif()

enable_testing()

add_executable(test_safe_parser tests/test_safe_parser.c)
target_link_libraries(test_safe_parser PRIVATE safe_parser)
enable_strict_warnings(test_safe_parser)

set(SAMPLES_ROOT "${CMAKE_CURRENT_SOURCE_DIR}/../samples")
set(VALID_SAMPLE "${SAMPLES_ROOT}/benign/valid_call_control.bin")
set(OVERSIZED_SAMPLE "${SAMPLES_ROOT}/malformed/oversized_payload.bin")
set(MISMATCH_SAMPLE "${SAMPLES_ROOT}/malformed/length_mismatch.bin")
set(TRUNCATED_SAMPLE "${SAMPLES_ROOT}/malformed/truncated_packet.bin")
set(MISSING_SAMPLE "${SAMPLES_ROOT}/malformed/does_not_exist.bin")
set(EMPTY_SAMPLE "${CMAKE_CURRENT_BINARY_DIR}/empty.bin")
set(TOO_LARGE_SAMPLE "${CMAKE_CURRENT_BINARY_DIR}/too_large.bin")
file(WRITE "${EMPTY_SAMPLE}" "")
string(REPEAT "x" 1048577 TOO_LARGE_SAMPLE_CONTENT)
file(WRITE "${TOO_LARGE_SAMPLE}" "${TOO_LARGE_SAMPLE_CONTENT}")
unset(TOO_LARGE_SAMPLE_CONTENT)

add_test(NAME safe_parser_unit COMMAND test_safe_parser
    "${VALID_SAMPLE}"
    "${OVERSIZED_SAMPLE}"
    "${MISMATCH_SAMPLE}"
    "${TRUNCATED_SAMPLE}"
)

function(add_cli_test name input expected_exit expected_pattern)
    add_test(NAME ${name} COMMAND ${CMAKE_COMMAND}
        "-DCLI=$<TARGET_FILE:receiver_safe>"
        "-DINPUT=${input}"
        "-DEXPECTED_EXIT=${expected_exit}"
        "-DEXPECTED_PATTERN=${expected_pattern}"
        -P "${CMAKE_CURRENT_SOURCE_DIR}/tests/assert_cli.cmake"
    )
endfunction()

add_cli_test(cli_valid "${VALID_SAMPLE}" 0 "status=accepted code=ok")
add_cli_test(
    cli_oversized
    "${OVERSIZED_SAMPLE}"
    2
    "status=rejected code=payload_too_large"
)
add_cli_test(
    cli_length_mismatch
    "${MISMATCH_SAMPLE}"
    2
    "status=rejected code=length_mismatch"
)
add_cli_test(
    cli_truncated
    "${TRUNCATED_SAMPLE}"
    2
    "status=rejected code=truncated_header"
)
add_cli_test(cli_missing "${MISSING_SAMPLE}" 3 "status=error code=file_open_failed")
add_cli_test(cli_empty "${EMPTY_SAMPLE}" 2 "status=rejected code=truncated_header")
add_cli_test(
    cli_file_too_large
    "${TOO_LARGE_SAMPLE}"
    3
    "status=error code=file_too_large"
)

add_test(NAME cli_usage COMMAND ${CMAKE_COMMAND}
    "-DCLI=$<TARGET_FILE:receiver_safe>"
    -DNO_INPUT=TRUE
    -DEXPECTED_EXIT=64
    "-DEXPECTED_PATTERN=usage: receiver_safe <sample.bin>"
    -P "${CMAKE_CURRENT_SOURCE_DIR}/tests/assert_cli.cmake"
)
