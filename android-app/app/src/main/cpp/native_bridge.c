#include <jni.h>

#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>

#include "packet_format.h"

#if defined(ECHOCALL_PARSER_VULNERABLE) && \
    defined(ECHOCALL_PARSER_PATCHED)
#error "Multiple EchoCall parser implementations configured"
#elif defined(ECHOCALL_PARSER_VULNERABLE)
#include "vulnerable_parser.h"
#define ECHOCALL_PARSE_PACKET vulnerable_parse_packet
#define ECHOCALL_PARSER_NAME "VULNERABLE"
#elif defined(ECHOCALL_PARSER_PATCHED)
#include "safe_parser.h"
#define ECHOCALL_PARSE_PACKET safe_parse_packet
#define ECHOCALL_PARSER_NAME "PATCHED"
#else
#error "No EchoCall parser implementation configured"
#endif

JNIEXPORT jstring JNICALL
Java_com_echocall_lab_NativeBridge_nativeStatus(
    JNIEnv *env,
    jobject receiver
)
{
    (void)receiver;
    return (*env)->NewStringUTF(env, "Native JNI connected");
}

JNIEXPORT jstring JNICALL
Java_com_echocall_lab_NativeBridge_getCompiledParserImplementation(
    JNIEnv *env,
    jobject receiver
)
{
    (void)receiver;
    return (*env)->NewStringUTF(env, ECHOCALL_PARSER_NAME);
}

static jstring parse_packet_to_string(
    JNIEnv *env,
    jbyteArray packet
)
{
    char output[256] = {0};
    parser_result result;
    parser_status status;
    jsize packet_size;
    jbyte *packet_data;
    int written;

    if (packet == NULL) {
        return (*env)->NewStringUTF(
            env,
            "status=error code=invalid_argument"
        );
    }

    packet_size = (*env)->GetArrayLength(env, packet);
    packet_data = (*env)->GetByteArrayElements(env, packet, NULL);
    if (packet_data == NULL) {
        return NULL;
    }

    status = ECHOCALL_PARSE_PACKET(
        (const uint8_t *)packet_data,
        (size_t)packet_size,
        &result
    );
    (*env)->ReleaseByteArrayElements(env, packet, packet_data, JNI_ABORT);

    if (status == PARSER_OK) {
        written = snprintf(
            output,
            sizeof(output),
            "status=accepted code=ok version=%u flags=%u type=%u "
            "declared_length=%u actual_length=%zu ssrc=0x%08" PRIx32
            " checksum=%u",
            (unsigned int)result.version,
            (unsigned int)result.flags,
            (unsigned int)result.packet_type,
            (unsigned int)result.declared_length,
            result.actual_length,
            result.ssrc,
            (unsigned int)result.checksum
        );
    } else if (status == PARSER_PAYLOAD_TOO_LARGE) {
        written = snprintf(
            output,
            sizeof(output),
            "status=rejected code=payload_too_large declared_length=%u "
            "actual_length=%zu maximum=%zu",
            (unsigned int)result.declared_length,
            result.actual_length,
            PACKET_MAX_PAYLOAD_SIZE
        );
    } else {
        written = snprintf(
            output,
            sizeof(output),
            "status=rejected code=%s",
            parser_status_name(status)
        );
    }

    if (written < 0 || (size_t)written >= sizeof(output)) {
        return (*env)->NewStringUTF(env, "status=error code=format_failed");
    }

    return (*env)->NewStringUTF(env, output);
}

JNIEXPORT jstring JNICALL
Java_com_echocall_lab_NativeBridge_parsePacket(
    JNIEnv *env,
    jobject receiver,
    jbyteArray packet
)
{
    (void)receiver;
    return parse_packet_to_string(env, packet);
}
