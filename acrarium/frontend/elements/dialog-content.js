import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';
import '@vaadin/vaadin-dialog/vaadin-dialog.js';
import '@vaadin/vaadin-button/vaadin-button.js';

class AcrariumDialogContent extends PolymerElement {
    static get template() {
        // language=HTML
        return html`
            <style>

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