package com.example.waypointTp.model

import java.util.UUID

data class Waypoint(
    val id: UUID,
    val name: String,
    val world: UUID,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val creator: UUID
)