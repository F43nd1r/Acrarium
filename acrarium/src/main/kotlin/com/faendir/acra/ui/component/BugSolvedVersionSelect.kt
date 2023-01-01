package com.faendir.acra.ui.component

import com.faendir.acra.i18n.Messages
import com.faendir.acra.i18n.TranslatableText
import com.faendir.acra.persistence.app.AppId
import com.faendir.acra.persistence.bug.BugRepository
import com.faendir.acra.persistence.bug.BugVersionInfo
import com.faendir.acra.persistence.user.Permission
import com.faendir.acra.persistence.version.VersionName
import com.faendir.acra.persistence.version.toVersionKey
import com.faendir.acra.security.SecurityUtils
import com.vaadin.flow.component.select.Select
import com.vaadin.flow.i18n.LocaleChangeEvent
import com.vaadin.flow.i18n.LocaleChangeObserver

class BugSolvedVersionSelect(appId: AppId, bug: BugVersionInfo, versions: Collection<VersionName>, bugRepository: BugRepository) :
    Select<VersionName>(), LocaleChangeObserver {
    private var label: TranslatableText? = null

    init {
        setItems(versions)
        setTextRenderer { it.name }
        isEmptySelectionAllowed = true
        emptySelectionCaption = getTranslation(Messages.NOT_SOLVED)
        value = bug.solvedVersionKey?.let { versions.first { version -> it.code == version.code && it.flavor == version.flavor } }
        isEnabled = SecurityUtils.hasPermission(appId, Permission.Level.EDIT)
        addValueChangeListener { e: ComponentValueChangeEvent<Select<VersionName?>, VersionName?> ->
            bugRepository.setSolved(appId, bug.id, e.value?.toVersionKey())
            style["--select-background-color"] =
                if (bug.latestVersionKey.code > (e.value?.code ?: Int.MAX_VALUE)) "var(--lumo-error-color-50pct)" else null
        }
        if (bug.latestVersionKey.code > (bug.solvedVersionKey?.code ?: Int.MAX_VALUE)) {
            style["--select-background-color"] = "var(--lumo-error-color-50pct)"
        }
    }

    override fun localeChange(event: LocaleChangeEvent?) {
        label?.let { setLabel(it.translate()) }
    }

    fun setTranslatableLabel(captionId: String, vararg params: Any) {
        label = TranslatableText(captionId, *params).also { setLabel(it.translate()) }
    }
}