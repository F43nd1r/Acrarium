import {html, PolymerElement} from '@polymer/polymer/polymer-element.js';

class AcrariumCard extends PolymerElement {
    static get template() {
        // language=HTML
        return html`
            <style>
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
                    display: inline-block;
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
            </style>
            <slot name="header" class="acrarium-card-header" on-click="handleClick"></slot>
            <div class$="{{getContentClass(collapse, divider)}}">
                <slot class="acrarium-card-content"></slot>
            </div>
        `
    }

    getContentClass(collapse, divider) {
        let classes = "acrarium-card-content-wrapper";
        if (collapse) classes += " collapse";
        if (divider) classes += " divider";
        return classes;
    }

    static get properties() {
        return {
            canCollapse: Boolean,
            collapse: Boolean,
            divider: Boolean
        }
    }

    handleClick() {
        if (this.canCollapse) {
            this.collapse = !this.collapse;
        }
    }
}

customElements.define("acrarium-card", AcrariumCard);