/*
 * (C) Copyright 2022-2023 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.persistence.version

import com.faendir.acra.dataprovider.AcrariumDataProvider
import com.faendir.acra.dataprovider.AcrariumSort
import com.faendir.acra.jooq.generated.tables.references.VERSION
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.asOrderFields
import com.faendir.acra.persistence.fetchListInto
import com.faendir.acra.persistence.fetchValue
import com.faendir.acra.persistence.fetchValueInto
import org.jooq.DSLContext
import org.jooq.impl.DSL.max
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Stream

@Repository
class VersionRepository(private val jooq: DSLContext) {

    @Transactional
    @PreAuthorize("isReporter() || hasEditPermission(#appId)")
    fun ensureExists(appId: AppId, code: Int, flavor: String?, name: String) {
        jooq.insertInto(VERSION)
            .set(VERSION.APP_ID, appId)
            .set(VERSION.CODE, code)
            .set(VERSION.FLAVOR, flavor ?: "")
            .set(VERSION.NAME, name)
            .onDuplicateKeyIgnore()
            .execute()
    }

    @Transactional
    @PreAuthorize("hasEditPermission(#appId)")
    fun setMappings(appId: AppId, code: Int, flavor: String?, name: String?, mappings: String?) {
        jooq.insertInto(VERSION)
            .set(VERSION.APP_ID, appId)
            .set(VERSION.CODE, code)
            .set(VERSION.FLAVOR, flavor ?: "")
            .set(VERSION.NAME, name ?: code.toString())
            .set(VERSION.MAPPINGS, mappings)
            .onDuplicateKeyUpdate()
            .apply { if (name != null) set(VERSION.NAME, name) }
            .set(VERSION.MAPPINGS, mappings)
            .execute()
    }

    @Transactional
    @PreAuthorize("hasEditPermission(#version.appId)")
    fun delete(version: Version) {
        jooq.deleteFrom(VERSION).where(VERSION.APP_ID.eq(version.appId), VERSION.CODE.eq(version.code), VERSION.FLAVOR.eq(version.flavor)).execute()
    }

    @PreAuthorize("hasViewPermission(#appId)")
    fun find(appId: AppId, key: VersionKey): Version? =
        find(appId, key.code, key.flavor)

    @PreAuthorize("hasViewPermission(#appId)")
    fun find(appId: AppId, versionCode: Int, flavor: String): Version? =
        jooq.selectFrom(VERSION).where(VERSION.APP_ID.eq(appId), VERSION.CODE.eq(versionCode), VERSION.FLAVOR.eq(flavor)).fetchValueInto()

    @PreAuthorize("hasViewPermission(#appId)")
    fun getProvider(appId: AppId): AcrariumDataProvider<Version, Nothing, Version.Sort> = object : AcrariumDataProvider<Version, Nothing, Version.Sort>() {
        override fun fetch(filters: Set<Nothing>, sort: List<AcrariumSort<Version.Sort>>, offset: Int, limit: Int): Stream<Version> = jooq.selectFrom(VERSION)
            .where(VERSION.APP_ID.eq(appId))
            .orderBy(sort.asOrderFields())
            .offset(offset)
            .limit(limit)
            .fetchListInto<Version>()
            .stream()

        override fun size(filters: Set<Nothing>): Int = jooq.selectCount().from(VERSION).where(VERSION.APP_ID.eq(appId)).fetchValue() ?: 0

    }


    @PreAuthorize("hasViewPermission(#appId)")
    fun getMaxVersionCode(appId: AppId): Int? = jooq.select(max(VERSION.CODE)).from(VERSION).where(VERSION.APP_ID.eq(appId)).fetchValue()

    @PreAuthorize("hasViewPermission(#appId)")
    fun getVersionNames(appId: AppId): List<VersionName> =
        jooq.select(VERSION.CODE, VERSION.FLAVOR, VERSION.NAME).from(VERSION).where(VERSION.APP_ID.eq(appId)).fetchInto(VersionName::class.java)
}