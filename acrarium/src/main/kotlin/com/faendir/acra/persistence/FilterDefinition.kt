package com.faendir.acra.persistence

import org.jooq.Condition

interface FilterDefinition {
    val condition: Condition
}