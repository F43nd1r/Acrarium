package com.faendir.acra.persistence

import org.jooq.Field

interface SortDefinition {
    val field: Field<out Any>
}