if(NOT DEFINED CLI)
    message(FATAL_ERROR "CLI is required")
endif()
if(NOT DEFINED EXPECTED_EXIT)
    message(FATAL_ERROR "EXPECTED_EXIT is required")
endif()
if(NOT DEFINED EXPECTED_PATTERN)
    message(FATAL_ERROR "EXPECTED_PATTERN is required")
endif()

if(NO_INPUT)
    execute_process(
        COMMAND "${CLI}"
        RESULT_VARIABLE actual_exit
        OUTPUT_VARIABLE standard_output
        ERROR_VARIABLE standard_error
    )
else()
    if(NOT DEFINED INPUT)
        message(FATAL_ERROR "INPUT is required")
    endif()
    execute_process(
        COMMAND "${CLI}" "${INPUT}"
        RESULT_VARIABLE actual_exit
        OUTPUT_VARIABLE standard_output
        ERROR_VARIABLE standard_error
    )
endif()

set(combined_output "${standard_output}${standard_error}")
if(NOT "${actual_exit}" STREQUAL "${EXPECTED_EXIT}")
    message(FATAL_ERROR
        "Expected exit ${EXPECTED_EXIT}, got ${actual_exit}. Output: ${combined_output}"
    )
endif()
if(NOT combined_output MATCHES "${EXPECTED_PATTERN}")
    message(FATAL_ERROR
        "Pattern '${EXPECTED_PATTERN}' not found. Output: ${combined_output}"
    )
endif()

message(STATUS "exit=${actual_exit} output=${combined_output}")
