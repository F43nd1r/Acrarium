/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.ui.base

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasElement
import com.vaadin.flow.component.orderedlayout.FlexLayout
import com.vaadin.flow.router.RouterLayout

/**
 * @author lukas
 * @since 13.07.18
 */
open class ParentLayout() : FlexLayout(), RouterLayout {
    private var _content: Component? = null
    private var routerRoot: HasElement = this

    constructor(routerRoot: HasElement) : this() {
        this.routerRoot = routerRoot
    }

    private fun setContent(content: HasElement, root: HasElement) {
        if (root === this) this._content = if (content is Component) content else null
        if (root != this && root is RouterLayout) root.showRouterLayoutContent(content)
        else {
            root.element.removeAllChildren()
            root.element.appendChild(content.element)
        }
    }

    var content: HasElement?
        get() = _content
        set(value) {
            value?.let { setContent(it, this) }
        }

    fun setRouterRoot(routerRoot: HasElement) {
        this.routerRoot = routerRoot
    }

    fun removeRouterRoot(routerRoot: HasElement) {
        if (this.routerRoot == routerRoot) {
            this.routerRoot = this
        }
    }

    override fun showRouterLayoutContent(content: HasElement) = setContent(content, routerRoot)
}