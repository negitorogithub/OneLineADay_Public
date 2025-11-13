package net.unifar.mydiary.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.Date

@Serializable
@Entity
data class Diary(
    @PrimaryKey val id: String,
    val content: String,
    @Contextual val date: Date
)