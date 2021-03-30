package com.faendir.acra.ui

import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.UserEditor
import com.faendir.acra.ui.testbench.BaseVaadinTest
import com.faendir.acra.ui.testbench.KBrowserWebDriverContainer
import com.faendir.acra.ui.testbench.PASSWORD
import com.faendir.acra.ui.testbench.USERNAME
import com.faendir.acra.annotation.VaadinTest
import com.faendir.acra.ui.testbench.getPage
import com.vaadin.flow.component.button.testbench.ButtonElement
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement
import com.vaadin.flow.component.textfield.testbench.TextFieldElement
import com.vaadin.testbench.TestBenchElement
import com.vaadin.testbench.annotations.Attribute
import com.vaadin.testbench.elementsbase.Element
import org.junit.platform.commons.annotation.Testable
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

class InitialSetupTest : BaseVaadinTest() {

    @Autowired
    private lateinit var userService: UserService

    @Testable
    @VaadinTest
    fun `perform initial user creation`(container: KBrowserWebDriverContainer, browserName: String) {
        driver.getPage(port)
        val editor = `$`(UserEditorElement::class.java).first()
        editor.username.value = USERNAME
        editor.password.value = PASSWORD
        editor.repeatPassword.value = PASSWORD
        editor.submit.click()
        waitForVaadin()
        expectThat(userService) {
            get { hasAdmin() }.isTrue()
            get { getUser(USERNAME) }.isNotNull()
            get { checkPassword(getUser(USERNAME), PASSWORD) }.isTrue()
        }
    }
}

@Element("div")
@Attribute(name = "id", value = UserEditor.EDITOR_ID)
class UserEditorElement : TestBenchElement() {
    val username: TextFieldElement get() = `$`(TextFieldElement::class.java).id(UserEditor.USERNAME_ID)
    val password: PasswordFieldElement get() = `$`(PasswordFieldElement::class.java).id(UserEditor.PASSWORD_ID)
    val repeatPassword: PasswordFieldElement get() = `$`(PasswordFieldElement::class.java).id(UserEditor.REPEAT_PASSWORD_ID)
    val submit: ButtonElement get() = `$`(ButtonElement::class.java).id(UserEditor.SUBMIT_ID)
}