/*
 * (C) Copyright 2019 Lukas Morawietz (https://github.com/F43nd1r)
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

import {css, html, LitElement} from 'lit-element'
import {customElement, property} from "lit/decorators.js"

@customElement("acrarium-card")
export default class Card extends LitElement {
    @property() canCollapse: Boolean = false
    @property() isCollapsed: Boolean = false
    @property() divider: Boolean = false

    static get styles() {
        return css`
            :host {
                box-shadow: 0 3px 6px rgba(0, 0, 0, 0.16), 0 3px 6px rgba(0, 0, 0, 0.23);
                border-radius: 2px;
                margin: 1rem;
                display: inline-flex;
                flex-direction: column;
            }

            .acrarium-card-header {
                padding: 1rem;
                box-sizing: border-box;
                background-color: var(--acrarium-card-header-color, var(--lumo-contrast-5pct));
                color: var(--acrarium-card-header-text-color);
                display: inline-block;
                width: 100%;
            }

            .acrarium-card-content {
                padding: 1rem;
                box-sizing: border-box;
                display: inline-flex;
                flex-direction: column;
                align-content: center;
                width: 100%;
                height: 100%;
            }

            .acrarium-card-content-wrapper {
                width: 100%;
                flex: 1;
                min-height: 0;
            }

            .collapse {
                display: none;
            }

            .divider > ::slotted(:not(:first-of-type)) {
                border-top: 1px solid var(--lumo-contrast-20pct);
                margin-top: 0.5em;
            }
        `
    }

    render() {
        return html`
            <slot name="header" class="acrarium-card-header" @click="${this.handleClick}"></slot>
            <div class="acrarium-card-content-wrapper ${this.isCollapsed ? "collapse" : this.divider ? "divider" : ""}">
                <slot class="acrarium-card-content"></slot>
            </div>
        `
    }

    handleClick() {
        if (this.canCollapse) {
            this.isCollapsed = !this.isCollapsed;
        }
    }
}