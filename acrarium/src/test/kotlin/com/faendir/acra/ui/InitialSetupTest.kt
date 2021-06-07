package com.faendir.acra.ui

import com.faendir.acra.service.UserService
import com.faendir.acra.ui.component.UserEditor
import com.faendir.acra.ui.testbench.BaseVaadinTest
import com.faendir.acra.ui.testbench.HasChrome
import com.faendir.acra.ui.testbench.HasContainer
import com.faendir.acra.ui.testbench.HasFirefox
import com.faendir.acra.ui.testbench.PASSWORD
import com.faendir.acra.ui.testbench.USERNAME
import com.vaadin.flow.component.button.testbench.ButtonElement
import com.vaadin.flow.component.textfield.testbench.PasswordFieldElement
import com.vaadin.flow.component.textfield.testbench.TextFieldElement
import com.vaadin.testbench.TestBenchElement
import com.vaadin.testbench.annotations.Attribute
import com.vaadin.testbench.elementsbase.Element
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import strikt.api.expectThat
import strikt.assertions.isNotNull
import strikt.assertions.isTrue

abstract class InitialSetupTest : BaseVaadinTest() {

    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `perform initial user creation`() {
        getPage(port)
        waitForVaadin()
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

class FirefoxInitialSetupTest : InitialSetupTest(), HasContainer by HasFirefox()
class ChromeInitialSetupTest : InitialSetupTest(), HasContainer by HasChrome()

@Element("div")
@Attribute(name = "id", value = UserEditor.EDITOR_ID)
class UserEditorElement : TestBenchElement() {
    val username: TextFieldElement get() = `$`(TextFieldElement::class.java).id(UserEditor.USERNAME_ID)
    val password: PasswordFieldElement get() = `$`(PasswordFieldElement::class.java).id(UserEditor.PASSWORD_ID)
    val repeatPassword: PasswordFieldElement get() = `$`(PasswordFieldElement::class.java).id(UserEditor.REPEAT_PASSWORD_ID)
    val submit: ButtonElement get() = `$`(ButtonElement::class.java).id(UserEditor.SUBMIT_ID)
}