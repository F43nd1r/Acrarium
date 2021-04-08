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

import {css, customElement, html, LitElement, property} from 'lit-element'

@customElement("acrarium-image-with-label")
export default class ImageWithLabel extends LitElement {
    @property() image: String = ""
    @property() label: String = ""

    static get styles() {
        return css`
                :host {
                    display: flex;
                    flex-direction: row;
                    align-items: center;
                }

                img {
                    width: 32px;
                    height: 32px;
                }

                label {
                    padding-left: var(--lumo-space-s);
                }
        `
    }

    render() {
        return html`
            <img src="${this.image}" alt="${this.label}">
            <label>${this.label}</label>
        `
    }
}