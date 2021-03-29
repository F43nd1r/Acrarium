package com.faendir.acra.model

import com.faendir.acra.util.NoArgConstructor
import com.univocity.parsers.annotations.Parsed
import java.io.Serializable
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.IdClass

@Entity
@IdClass(Device.ID::class)
class Device(
    @Id
    @Parsed(field = ["Device"])
    val device: String,

    @Id
    @Parsed(field = ["Model"])
    val model: String,

    @Parsed(field = ["Marketing Name"])
    val marketingName: String?
) {
    @NoArgConstructor
    internal data class ID(val device: String, val model: String) : Serializable
}