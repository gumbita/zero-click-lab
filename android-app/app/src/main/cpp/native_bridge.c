#include <jni.h>

#include <inttypes.h>
#include <stdint.h>
#include <stdio.h>

#include "safe_parser.h"
#include "vulnerable_parser.h"

typedef parser_status (*packet_parser_fn)(
    const uint8_t *data,
    size_t data_size,
    parser_result *result
);

JNIEXPORT jstring JNICALL
Java_com_echocall_lab_NativeBridge_nativeStatus(
    JNIEnv *env,
    jobject receiver
)
{
    (void)receiver;
    return (*env)->NewStringUTF(env, "Native JNI connected");
}

static jstring parse_packet_to_string(
    JNIEnv *env,
    jbyteArray packet,
    packet_parser_fn parser
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

    status = parser(
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
    return parse_packet_to_string(env, packet, safe_parse_packet);
}

JNIEXPORT jstring JNICALL
Java_com_echocall_lab_NativeBridge_parsePacketVulnerable(
    JNIEnv *env,
    jobject receiver,
    jbyteArray packet
)
{
    (void)receiver;
    return parse_packet_to_string(env, packet, vulnerable_parse_packet);
}
