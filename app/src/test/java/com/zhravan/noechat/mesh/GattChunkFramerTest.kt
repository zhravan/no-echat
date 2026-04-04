package com.zhravan.noechat.mesh

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GattChunkFramerTest {

    @Test
    fun splitThenParse_reassemblesPayload() {
        val original = ByteArray(123) { (it * 7 and 0xFF).toByte() }
        val msgId = 0x11223344.toInt()
        val maxBody = 40
        val frames = GattChunkFramer.split(original, maxBody, msgId)
        assertEquals(4, frames.size)

        val out = java.io.ByteArrayOutputStream()
        frames.forEachIndexed { index, frame ->
            val parsed = GattChunkFramer.parse(frame)
            assertNotNull(parsed)
            assertEquals(msgId, parsed!!.msgId)
            assertEquals(frames.size, parsed.totalChunks)
            assertEquals(index, parsed.chunkIndex)
            out.write(parsed.body)
        }
        assertArrayEquals(original, out.toByteArray())
    }

    @Test
    fun maxBodyForMtu_clamped() {
        val body = GattChunkFramer.maxBodyForMtu(185)
        assertTrue(body >= MeshConstants.GATT_MIN_CHUNK_BODY)
        assertTrue(body <= MeshConstants.GATT_MAX_CHUNK_BODY)
    }
}
