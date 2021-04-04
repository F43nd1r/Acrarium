package com.faendir.acra.hibernate

import com.google.auto.service.AutoService
import org.hibernate.boot.MetadataBuilder
import org.hibernate.boot.spi.MetadataBuilderContributor
import org.hibernate.dialect.function.StandardSQLFunction
import org.hibernate.type.StandardBasicTypes

@AutoService(MetadataBuilderContributor::class)
class FunctionDefinitions : MetadataBuilderContributor {
    override fun contribute(metadataBuilder: MetadataBuilder) {
        metadataBuilder.applySqlFunction("json_extract", StandardSQLFunction("json_extract", StandardBasicTypes.STRING))
        metadataBuilder.applySqlFunction("json_unquote", StandardSQLFunction("json_unquote", StandardBasicTypes.STRING))
    }
}