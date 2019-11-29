import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

class AcrariumBox extends PolymerElement {
    static get template() {
        // language=HTML
        return html`
            <style>
                :host {
                    display: flex;
                    flex-direction: row;
                }

                .acrarium-box-title {
                    font-weight: bold;
                }

                .acrarium-box-text {
                    display: flex;
                    flex-direction: column;
                    flex: 1;
                }
            </style>
            <div class="acrarium-box-text">
                <slot name="title" class="acrarium-box-title"></slot>
                <slot name="details"></slot>
            </div>
            <slot name="action"></slot>
        `
    }
}

customElements.define("acrarium-box", AcrariumBox);