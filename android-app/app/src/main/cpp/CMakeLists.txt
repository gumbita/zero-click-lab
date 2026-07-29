cmake_minimum_required(VERSION 3.22.1)

project(echocall_native LANGUAGES C)

set(CMAKE_C_STANDARD 17)
set(CMAKE_C_STANDARD_REQUIRED ON)
set(CMAKE_C_EXTENSIONS OFF)

get_filename_component(
    NATIVE_CORE_ROOT
    "${CMAKE_CURRENT_LIST_DIR}/../../../../../native-core"
    ABSOLUTE
)

add_library(
    echocall_native
    SHARED
    native_bridge.c
    "${NATIVE_CORE_ROOT}/include/packet_format.h"
    "${NATIVE_CORE_ROOT}/include/parser_result.h"
    "${NATIVE_CORE_ROOT}/include/safe_parser.h"
    "${NATIVE_CORE_ROOT}/include/vulnerable_parser.h"
    "${NATIVE_CORE_ROOT}/src/parser_result.c"
    "${NATIVE_CORE_ROOT}/src/safe_parser.c"
    "${NATIVE_CORE_ROOT}/src/vulnerable_parser.c"
)

target_include_directories(
    echocall_native
    PRIVATE
    "${NATIVE_CORE_ROOT}/include"
)

target_compile_options(echocall_native PRIVATE
    -Wall
    -Wextra
    -Wpedantic
    -Werror
)
