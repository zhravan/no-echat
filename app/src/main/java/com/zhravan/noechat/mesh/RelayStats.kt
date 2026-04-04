package com.zhravan.noechat.mesh

data class RelayStats(
    val peersTried: Int,
    val successes: Int,
    val timestampEpochMs: Long
)
