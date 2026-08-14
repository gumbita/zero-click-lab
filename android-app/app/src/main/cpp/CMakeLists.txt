cmake_minimum_required(VERSION 3.22.1)

project(echocall_native LANGUAGES C)

set(CMAKE_C_STANDARD 17)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_EXTENSIONS OFF)

option(
    ENABLE_ANDROID_ASAN
    "Instrument the Android native library with AddressSanitizer"
    OFF
)

get_filename_component(
    NATIVE_CORE_ROOT
    "${CMAKE_CURRENT_LIST_DIR}/../../../../../native-core"
    ABSOLUTE
)

if(
    NOT DEFINED ECHOCALL_PARSER_IMPLEMENTATION
    OR ECHOCALL_PARSER_IMPLEMENTATION STREQUAL ""
)
    message(FATAL_ERROR "ECHOCALL_PARSER_IMPLEMENTATION is required")
endif()

if(ECHOCALL_PARSER_IMPLEMENTATION STREQUAL "VULNERABLE")
    set(
        ECHOCALL_PARSER_SOURCE
        "${NATIVE_CORE_ROOT}/src/vulnerable_parser.c"
    )
    set(ECHOCALL_PARSER_DEFINE ECHOCALL_PARSER_VULNERABLE)
elseif(ECHOCALL_PARSER_IMPLEMENTATION STREQUAL "PATCHED")
    set(
        ECHOCALL_PARSER_SOURCE
        "${NATIVE_CORE_ROOT}/src/safe_parser.c"
    )
    set(ECHOCALL_PARSER_DEFINE ECHOCALL_PARSER_PATCHED)
else()
    message(
        FATAL_ERROR
        "Invalid ECHOCALL_PARSER_IMPLEMENTATION: '${ECHOCALL_PARSER_IMPLEMENTATION}'. Expected VULNERABLE or PATCHED"
    )
endif()

add_library(
    echocall_native
    SHARED
    native_bridge.c
    "${NATIVE_CORE_ROOT}/include/packet_format.h"
    "${NATIVE_CORE_ROOT}/include/parser_result.h"
    "${NATIVE_CORE_ROOT}/src/parser_result.c"
    "${ECHOCALL_PARSER_SOURCE}"
)

target_include_directories(
    echocall_native
    PRIVATE
    "${NATIVE_CORE_ROOT}/include"
)

target_compile_definitions(
    echocall_native
    PRIVATE
    ${ECHOCALL_PARSER_DEFINE}=1
)

target_compile_options(echocall_native PRIVATE
    -Wall
    -Wextra
    -Wpedantic
    -Werror
)

if(ENABLE_ANDROID_ASAN)
    target_compile_options(echocall_native PRIVATE
        -fsanitize=address
        -fno-omit-frame-pointer
    )
    target_link_options(echocall_native PRIVATE
        -fsanitize=address
    )
endif()
