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

import {css, customElement, html, LitElement} from 'lit-element'

@customElement("acrarium-box")
export default class Box extends LitElement {
    static get styles() {
        return css`
          :host {
            display: flex;
            flex-direction: row;
            align-items: center;
          }

          .acrarium-box-title {
            font-weight: bold;
          }

          .acrarium-box-text {
            display: flex;
            flex-direction: column;
            flex: 1;
          }
        `
    }

    render() {
        return html`
            <div class="acrarium-box-text">
                <slot name="title" class="acrarium-box-title"></slot>
                <slot name="details"></slot>
            </div>
            <slot name="action"></slot>
        `
    }
}