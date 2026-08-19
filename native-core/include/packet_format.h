/*
 * Copyright (C) 2026 Àngels Gumbau Granero
 * SPDX-License-Identifier: GPL-3.0-only
 * See LICENSE in the repository root.
 */

#ifndef ZERO_CLICK_LAB_PACKET_FORMAT_H
#define ZERO_CLICK_LAB_PACKET_FORMAT_H

#include <stddef.h>
#include <stdint.h>

#define PACKET_HEADER_SIZE ((size_t)13u)
#define PACKET_MAX_PAYLOAD_SIZE ((size_t)32u)

#define PACKET_OFFSET_MAGIC ((size_t)0u)
#define PACKET_OFFSET_VERSION ((size_t)4u)
#define PACKET_OFFSET_FLAGS ((size_t)5u)
#define PACKET_OFFSET_TYPE ((size_t)6u)
#define PACKET_OFFSET_LENGTH ((size_t)7u)
#define PACKET_OFFSET_SSRC ((size_t)9u)
#define PACKET_OFFSET_PAYLOAD ((size_t)13u)

#define PACKET_MAGIC_0 ((uint8_t)0x45u)
#define PACKET_MAGIC_1 ((uint8_t)0x43u)
#define PACKET_MAGIC_2 ((uint8_t)0x4cu)
#define PACKET_MAGIC_3 ((uint8_t)0x42u)

#define PACKET_VERSION ((uint8_t)1u)
#define PACKET_TYPE_CALL_CONTROL ((uint8_t)1u)

#endif
