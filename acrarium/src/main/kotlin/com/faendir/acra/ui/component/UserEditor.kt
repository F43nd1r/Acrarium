/*
 * (C) Copyright 2020-2023 Lukas Morawietz (https://github.com/F43nd1r)
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
package com.faendir.acra.ui.component

import com.faendir.acra.domain.MailService
import com.faendir.acra.i18n.Messages
import com.faendir.acra.persistence.user.Role
import com.faendir.acra.persistence.user.UserRepository
import com.faendir.acra.ui.component.Translatable.ValidatedValue
import com.faendir.acra.ui.ext.setMaxWidthFull
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.Composite
import com.vaadin.flow.component.HasValidation
import com.vaadin.flow.component.HasValue
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.data.binder.Binder
import com.vaadin.flow.data.binder.Setter
import com.vaadin.flow.data.binder.ValidationResult
import com.vaadin.flow.data.binder.ValueContext
import com.vaadin.flow.data.validator.EmailValidator
import com.vaadin.flow.dom.ElementFactory
import java.util.*
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * @author lukas
 * @since 28.02.19
 */
class UserEditor(userRepository: UserRepository, mailService: MailService?, existingUsername: String? = null, grantRoles: Set<Role> = emptySet(), onSuccess: () -> Unit) :
    Composite<FlexLayout>() {
    private val isExistingUser = existingUsername != null
    private val user = MutableUser(existingUsername ?: "", "", existingUsername?.let { userRepository.find(existingUsername)?.mail } ?: "")

    init {
        setId(EDITOR_ID)
        val binder = Binder<MutableUser>()
        val username = Translatable.createTextField(Messages.USERNAME)
        exposeInput(username)
        username.setWidthFull()
        username.setId(USERNAME_ID)
        val usernameBindingBuilder = binder.forField(username)
        if (!isExistingUser) {
            usernameBindingBuilder.asRequired(getTranslation(Messages.USERNAME_REQUIRED))
        }
        usernameBindingBuilder.withValidator({ it == user.username || !userRepository.exists(it) }, getTranslation(Messages.USERNAME_TAKEN))
            .bind({ it.username }, if (!isExistingUser) Setter { u, value -> u.username = value.lowercase(Locale.getDefault()) } else null)
        content.add(username)
        val mail = Translatable.createTextField(Messages.EMAIL)
        mail.setWidthFull()
        mail.setId(MAIL_ID)
        val emailValidator = EmailValidator(getTranslation(Messages.INVALID_MAIL))
        binder.forField(mail).withValidator { m: String, c: ValueContext? -> if (m.isEmpty()) ValidationResult.ok() else emailValidator.apply(m, c) }
            .bind({ it.mail }) { u, value -> u.mail = value }
        content.add(mail)
        val newPassword = Translatable.createPasswordField(Messages.NEW_PASSWORD)
        exposeInput(newPassword)
        newPassword.setWidthFull()
        newPassword.setId(PASSWORD_ID)
        val repeatPassword = Translatable.createPasswordField(Messages.REPEAT_PASSWORD)
        exposeInput(repeatPassword)
        repeatPassword.setWidthFull()
        repeatPassword.setId(REPEAT_PASSWORD_ID)
        if (isExistingUser) {
            val oldPassword = Translatable.createPasswordField(Messages.OLD_PASSWORD)
            exposeInput(oldPassword)
            oldPassword.setWidthFull()
            oldPassword.setId(OLD_PASSWORD_ID)
            val oldPasswordBinding = binder.forField(oldPassword).withValidator({
                if (user.username.isNotBlank() && (newPassword.value.isNotEmpty() || oldPassword.value.isNotEmpty())) userRepository.checkPassword(user.username, it) else true
            }, getTranslation(Messages.INCORRECT_PASSWORD)).bind({ "" }) { _, _ -> }
            content.add(oldPassword)
            newPassword.addValueChangeListener { oldPasswordBinding.validate() }
            repeatPassword.addValueChangeListener { oldPasswordBinding.validate() }
        }
        val newPasswordBindingBuilder = binder.forField(newPassword)
            .withValidator { p, _ -> if (p.isNotBlank()) ValidationResult.ok() else ValidationResult.error(getTranslation(Messages.INVALID_PASSWORD)) }
        val repeatPasswordBindingBuilder = binder.forField(repeatPassword)
        if (!isExistingUser) {
            newPasswordBindingBuilder.asRequired(getTranslation(Messages.PASSWORD_REQUIRED))
            repeatPasswordBindingBuilder.asRequired(getTranslation(Messages.PASSWORD_REQUIRED))
        }
        newPasswordBindingBuilder.bind({ "" }) { u, rawPassword ->
            if (rawPassword.isNotBlank()) {
                u.rawPassword = rawPassword
            }
        }
        content.add(newPassword)
        val repeatPasswordBinding = repeatPasswordBindingBuilder.withValidator({ it == newPassword.value }, getTranslation(Messages.PASSWORDS_NOT_MATCHING))
            .bind({ "" }) { _, _ -> }
        newPassword.addValueChangeListener { if (repeatPassword.value.isNotEmpty()) repeatPasswordBinding.validate() }
        content.add(repeatPassword)
        binder.readBean(user)
        val button = Translatable.createButton(Messages.CONFIRM) {
            if (binder.writeBeanIfValid(user)) {
                if (isExistingUser) {
                    userRepository.update(user.username, user.rawPassword.takeIf { it.isNotBlank() }, user.mail.takeIf { it.isNotBlank() })
                } else {
                    userRepository.create(user.username, user.rawPassword, user.mail.takeIf { it.isNotBlank() }, roles = grantRoles.toTypedArray())
                }
                onSuccess()
            }
        }
        button.setWidthFull()
        button.setId(SUBMIT_ID)
        button.content.isEnabled = false
        binder.addStatusChangeListener { button.content.isEnabled = it.binder.hasChanges() }
        content.add(button)
        val testMailButton = Translatable.createButton(Messages.SEND_TEST_MAIL) {
            if (canSendTestMail(mailService)) {
                mailService.testMessage(user.mail)
            }
        }
        testMailButton.setWidthFull()
        testMailButton.content.isEnabled = canSendTestMail(mailService)
        binder.addStatusChangeListener { testMailButton.content.isEnabled = canSendTestMail(mailService) }
        content.add(testMailButton)
        content.flexDirection = FlexLayout.FlexDirection.COLUMN
        content.setMaxWidthFull()
        content.width = "calc(var(--lumo-size-m) * 10)"
    }

    @OptIn(ExperimentalContracts::class)
    private fun canSendTestMail(mailService: MailService?): Boolean {
        contract {
            returns(true) implies (mailService != null)
        }
        return user.mail.isNotBlank() == true && mailService != null
    }

    /**
     * password managers need an input outside the shadow dom, which we add here.
     * @param field the field which should be visible to password managers
     * @param <T> the type of the field
     **/
    private fun <T, V> exposeInput(field: ValidatedValue<T, *, V>) where T : Component, T : HasValue<ComponentValueChangeEvent<T, V>, V>, T : HasValidation {
        val input = ElementFactory.createInput()
        input.setAttribute("slot", "input")
        field.getElement().appendChild(input)
    }

    companion object {
        const val EDITOR_ID = "userEditor"
        const val USERNAME_ID = "editorUsername"
        const val MAIL_ID = "editorMail"
        const val PASSWORD_ID = "editorPassword"
        const val OLD_PASSWORD_ID = "editorOldPassword"
        const val REPEAT_PASSWORD_ID = "editorRepeatPassword"
        const val SUBMIT_ID = "editorSubmit"
    }
}

private class MutableUser(var username: String, var rawPassword: String, var mail: String)