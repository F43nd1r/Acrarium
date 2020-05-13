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

import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import '@vaadin/vaadin-dialog/vaadin-dialog.js';
import '@vaadin/vaadin-button/vaadin-button.js';

class AcrariumDialogContent extends PolymerElement {
    static get template() {
        // language=HTML
        return html`
            <style>
                :host {
                    display: block;
                    max-width: 1000px;
                }

                .footer {
                    background-color: var(--lumo-contrast-5pct);
                    display: flex;
                    margin: calc(var(--lumo-space-l) * -1);
                    margin-top: var(--lumo-space-l);
                    padding: var(--lumo-space-s) var(--lumo-space-l);
                }

                .spacer {
                    flex: 1;
                }

                slot[name="header"]::slotted(*) {
                    margin-top: var(--lumo-space-s) !important;
                }
            </style>
            <div class="header">
                <slot name="header"></slot>
            </div>
            <slot></slot>
            <div class="footer">
                <slot name="negative"></slot>
                <div class="spacer"></div>
                <slot name="positive"></slot>
            </div>
        `
    }
}

customElements.define("acrarium-dialog-content", AcrariumDialogContent);