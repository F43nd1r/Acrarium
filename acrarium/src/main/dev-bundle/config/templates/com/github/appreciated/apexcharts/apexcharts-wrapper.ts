import {html, LitElement} from 'lit';
import {customElement} from 'lit/decorators.js';
// @ts-ignore
import ApexCharts from 'apexcharts/dist/apexcharts.esm';
import '@webcomponents/shadycss/apply-shim.min.js';
import color from "onecolor";
import {PropertyValues} from "@lit/reactive-element/development/reactive-element";

declare global {
    interface Window {
        ShadyCSS?: any;
    }
}

@customElement('apex-charts-wrapper')
export class ApexChartsWrapper extends LitElement {
    config: any = {};
    chartComponent?: ApexCharts;
    annotations?: string;
    chart?: string;
    series?: string;
    labels?: string;
    colors?: string;
    dataLabels?: string;
    fill?: string;
    forecastDataPoints?: string;
    grid?: string;
    legend?: string;
    markers?: string;
    noData?: string;
    plotOptions?: string;
    responsive?: string;
    states?: string;
    stroke?: string;
    subtitle?: string;
    theme?: string;
    chartTitle?: string;
    tooltip?: string;
    xaxis?: string;
    yaxis?: string;
    width?: string;
    height?: string;
    debug?: string;

    render() {
        return html`
            <style include="apex-charts-style"></style>
            <slot></slot>
        `;
    }

    static get properties() {
        return {
            annotations: {
                type: Object
            }, // ApexAnnotations;
            chart: {
                type: Object
            }, // ApexChart;
            colors: {
                type: Object
            }, // string[];
            dataLabels: {
                type: Object
            }, // ApexDataLabels;
            fill: {
                type: Object
            }, // ApexFill;
            forecastDataPoints: {
                type: Object
            }, // ApexForecastDataPoints;
            grid: {
                type: Object
            }, // ApexGrid;
            labels: {
                type: Object
            }, // string[];
            legend: {
                type: Object
            }, // ApexLegend;
            markers: {
                type: Object
            }, // ApexMarkers;
            noData: {
                type: Object
            }, // ApexNoData;
            plotOptions: {
                type: Object
            }, // ApexPlotOptions;
            responsive: {
                type: Object
            }, // ApexResponsive[];
            series: {
                type: Object
            }, // ApexAxisChartSeries | ApexNonAxisChartSeries;
            states: {
                type: Object
            }, // ApexStates;
            stroke: {
                type: Object
            }, // ApexStroke;
            subtitle: {
                type: Object
            }, // ApexTitleSubtitle;
            theme: {
                type: Object
            }, // ApexTheme;
            chartTitle: {
                type: Object
            }, // ApexTitleSubtitle;
            tooltip: {
                type: Object
            }, // ApexTooltip;
            xaxis: {
                type: Object
            }, // ApexXAxis;
            yaxis: {
                type: Object
            }, // ApexYAxis | ApexYAxis[];
            debug: {
                type: Boolean
            },
            width: {
                type: String
            },
            height: {
                type: String
            }
        };
    }

    firstUpdated(_changedProperties: PropertyValues) {
        super.firstUpdated(_changedProperties);
        const div = document.createElement('div');
        this.appendChild(div);
        this.updateConfig();
        this.chartComponent = new ApexCharts(div, this.config);
        this.beginRender();
    }

    async beginRender() {
        try {
            await this.chartComponent.render();
        } catch (e) {
            console.error("An exception occurred during the rendering of the chart with the following configuration:");
            console.error(this.config);
            console.error(e);
        }
    }

    updateConfig() {
        let primaryColor;
        if (window.ShadyCSS) {
            primaryColor = window.ShadyCSS.getComputedStyleValue(this, '--apex-charts-primary-color');
        } else {
            primaryColor = getComputedStyle(this).getPropertyValue('--apex-charts-primary-color');
        }
        let backgroundColor;
        if (window.ShadyCSS) {
            backgroundColor = window.ShadyCSS.getComputedStyleValue(this, '--apex-charts-background-color');
        } else {
            backgroundColor = getComputedStyle(this).getPropertyValue('--apex-charts-background-color');
        }
        this.config = {};
        if (this.annotations) {
            this.config.annotations = JSON.parse(this.annotations);
        }
        if (this.chart) {
            this.config.chart = JSON.parse(this.chart);
            if (this.config.chart && this.config.chart.events) {
                if (this.config.chart.events.animationEnd) {
                    this.config.chart.events.animationEnd = this.evalFunction(this.config.chart.events.animationEnd);
                }
                if (this.config.chart.events.beforeMount) {
                    this.config.chart.events.beforeMount = this.evalFunction(this.config.chart.events.beforeMount);
                }
                if (this.config.chart.events.mounted) {
                    this.config.chart.events.mounted = this.evalFunction(this.config.chart.events.mounted);
                }
                if (this.config.chart.events.updated) {
                    this.config.chart.events.updated = this.evalFunction(this.config.chart.events.updated);
                }
                if (this.config.chart.events.click) {
                    this.config.chart.events.click = this.evalFunction(this.config.chart.events.click);
                }
                if (this.config.chart.events.mouseMove) {
                    this.config.chart.events.mouseMove = this.evalFunction(this.config.chart.events.mouseMove);
                }
                if (this.config.chart.events.legendClick) {
                    this.config.chart.events.legendClick = this.evalFunction(this.config.chart.events.legendClick);
                }
                if (this.config.chart.events.markerClick) {
                    this.config.chart.events.markerClick = this.evalFunction(this.config.chart.events.markerClick);
                }
                if (this.config.chart.events.selection) {
                    this.config.chart.events.selection = this.evalFunction(this.config.chart.events.selection);
                }
                if (this.config.chart.events.dataPointSelection) {
                    this.config.chart.events.dataPointSelection = this.evalFunction(this.config.chart.events.dataPointSelection);
                }
                if (this.config.chart.events.dataPointMouseEnter) {
                    this.config.chart.events.dataPointMouseEnter = this.evalFunction(this.config.chart.events.dataPointMouseEnter);
                }
                if (this.config.chart.events.dataPointMouseLeave) {
                    this.config.chart.events.dataPointMouseLeave = this.evalFunction(this.config.chart.events.dataPointMouseLeave);
                }
                if (this.config.chart.events.beforeZoom) {
                    this.config.chart.events.beforeZoom = this.evalFunction(this.config.chart.events.beforeZoom);
                }
                if (this.config.chart.events.beforeResetZoom) {
                    this.config.chart.events.beforeResetZoom = this.evalFunction(this.config.chart.events.beforeResetZoom);
                }
                if (this.config.chart.events.zoomed) {
                    this.config.chart.events.zoomed = this.evalFunction(this.config.chart.events.zoomed);
                }
                if (this.config.chart.events.scrolled) {
                    this.config.chart.events.scrolled = this.evalFunction(this.config.chart.events.scrolled);
                }
            }
        }
        if (this.series) {
            this.config.series = JSON.parse(this.series);
        }
        if (this.labels) {
            this.config.labels = this.labels;
            if (this.config.labels.formatter) {
                this.config.labels.formatter = this.evalFunction(this.config.labels.formatter);
            }
        }
        if (this.colors) {
            this.config.colors = JSON.parse(this.colors);
        }
        if (this.dataLabels) {
            this.config.dataLabels = JSON.parse(this.dataLabels);
            if (this.config.dataLabels.formatter) {
                this.config.dataLabels.formatter = this.evalFunction(this.config.dataLabels.formatter);
            }
        }
        if (this.fill) {
            this.config.fill = JSON.parse(this.fill);
        }
        if (this.forecastDataPoints) {
            this.config.forecastDataPoints = JSON.parse(this.forecastDataPoints);
        }
        if (this.grid) {
            this.config.grid = JSON.parse(this.grid);
        }
        if (this.legend) {
            this.config.legend = JSON.parse(this.legend);
            if (this.config.legend.formatter) {
                this.config.legend.formatter = this.evalFunction(this.config.legend.formatter);
            }
        }
        if (this.markers) {
            this.config.markers = JSON.parse(this.markers);
        }
        if (this.noData) {
            this.config.noData = this.noData;
        }
        if (this.plotOptions) {
            this.config.plotOptions = JSON.parse(this.plotOptions);
        }
        if (this.responsive) {
            this.config.responsive = JSON.parse(this.responsive);
        }
        if (this.states) {
            this.config.states = JSON.parse(this.states);
        }
        if (this.stroke) {
            this.config.stroke = JSON.parse(this.stroke);
        }
        if (this.subtitle) {
            this.config.subtitle = JSON.parse(this.subtitle);
        }
        if (this.theme) {
            this.config.theme = JSON.parse(this.theme);
        } else if (!this.config.fill || !this.config.fill.type || !Array.isArray(this.config.fill.type) || this.config.fill.type[0] !== "gradient") {
            if (backgroundColor && color(backgroundColor)) {
                this.config.theme = {
                    mode: ((color(backgroundColor).lightness() > 0.5) ? 'light' : 'dark')
                };
            }
            if (!this.colors && primaryColor && color(primaryColor)) {
                this.config.theme.monochrome = {
                    enabled: true,
                    color: color(primaryColor).hex(),
                    shadeTo: 'light',
                    shadeIntensity: 0.65
                };
            }
        }
        if (this.chartTitle) {
            this.config.title = JSON.parse(this.chartTitle);
        }
        if (this.tooltip) {
            this.config.tooltip = JSON.parse(this.tooltip);
            if (this.config.tooltip.x && this.config.tooltip.x.formatter) {
                this.config.tooltip.x.formatter = this.evalFunction(this.config.tooltip.x.formatter);
            }
            if (this.config.tooltip.y && this.config.tooltip.y.formatter) {
                this.config.tooltip.y.formatter = this.evalFunction(this.config.tooltip.y.formatter);
            }
            if (this.config.tooltip.z && this.config.tooltip.z.formatter) {
                this.config.tooltip.z.formatter = this.evalFunction(this.config.tooltip.z.formatter);
            }
            if (this.config.tooltip.custom) {
                this.config.tooltip.custom = this.evalFunction(this.config.tooltip.custom);
            }
        }
        if (this.xaxis) {
            this.config.xaxis = JSON.parse(this.xaxis);
            if (this.config.xaxis.labels && this.config.xaxis.labels.formatter) {
                this.config.xaxis.labels.formatter = this.evalFunction(this.config.xaxis.labels.formatter);
            }
            if (this.config.xaxis.title && this.config.xaxis.title.formatter) {
                this.config.xaxis.title.formatter = this.evalFunction(this.config.xaxis.title.formatter);
            }
        }
        if (this.yaxis) {
            this.config.yaxis = JSON.parse(this.yaxis);
            for (let i = 0; i < this.config.yaxis.length; i++) {
                if (this.config.yaxis[i].labels && this.config.yaxis[i].labels.formatter) {
                    this.config.yaxis[i].labels.formatter = this.evalFunction(this.config.yaxis[i].labels.formatter);
                }
                if (this.config.yaxis[i].title && this.config.yaxis[i].title.formatter) {
                    this.config.yaxis[i].title.formatter = this.evalFunction(this.config.yaxis[i].title.formatter);
                }
            }
        }
        if (!this.config.chart) {
            this.config.chart = {};
        }
        if (this.width) {
            this.config.chart.width = this.width;
        }
        if (this.height) {
            this.config.chart.height = this.height;
        }
        if (!this.config.chart.background && backgroundColor && color(backgroundColor)) {
            this.config.chart.background = backgroundColor;
        }
        if (!this.config.stroke) {
            this.config.stroke = {};
        }
        if (this.config.chart && this.config.chart.type === "radar") {
            if (!this.config.plotOptions && backgroundColor && color(backgroundColor)) {
                this.config.plotOptions = {
                    radar: {
                        polygons: {
                            fill: {
                                colors: [color(backgroundColor).hex()]
                            }
                        }
                    }
                };
            }
        }
        if (this.config.plotoptions && this.config.plotoptions.radialbar && this.config.plotoptions.radialbar.datalabels && this.config.plotoptions.radialbar.datalabels.value && this.config.plotoptions.radialbar.datalabels.value.formatter) {
            this.config.plotoptions.radialbar.datalabels.value.formatter = this.evalFunction(this.config.plotoptions.radialbar.datalabels.value.formatter);
        }
        if (this.config.plotoptions && this.config.plotoptions.radialbar && this.config.plotoptions.radialbar.datalabels && this.config.plotoptions.radialbar.datalabels.total && this.config.plotoptions.radialbar.datalabels.total.formatter) {
            this.config.plotoptions.radialbar.datalabels.total.formatter = this.evalFunction(this.config.plotoptions.radialbar.datalabels.total.formatter);
        }
        if (this.config.plotoptions && this.config.plotoptions.pie && this.config.plotoptions.pie.datalabels && this.config.plotoptions.pie.datalabels.total && this.config.plotoptions.pie.datalabels.total.formatter) {
            this.config.plotoptions.pie.datalabels.total.formatter = this.evalFunction(this.config.plotoptions.pie.datalabels.total.formatter);
        }
        if (this.config.plotoptions && this.config.plotoptions.pie && this.config.plotoptions.pie.datalabels && this.config.plotoptions.pie.datalabels.value && this.config.plotoptions.pie.datalabels.value.formatter) {
            this.config.plotoptions.pie.datalabels.value.formatter = this.evalFunction(this.config.plotoptions.pie.datalabels.value.formatter);
        }
    }

    /**
     * This is due to the way the eval function works eval("function (){return \"test\"}") will throw an
     * Uncaught SyntaxError: Function statements require a function name.
     *
     * If the string is wrapped with brackets, as for example eval("(function (){return \"test\"})") the function
     * returns ƒ (){return "test"} which is what is needed.
     * @param string for example "function (){return \"test\"}"
     * @returns {function} returns an actual JavaScript instance of the passed string function
     */
    evalFunction(string: string) {
        return eval("(" + string + ")");
    }

    updateData() {
        if (this.chartComponent && this.series) {
            this.chartComponent.updateSeries(JSON.parse(this.series));
        }
        if (this.debug) {
            console.log(this.chartComponent);
        }
    }

    reRender() {
        if (this.chartComponent) {
            this.updateConfig();
            this.chartComponent.render();
        }
    }

    dataURI() {
        if (this.chartComponent) {
            this.updateConfig();
            return this.chartComponent.dataURI();
        }
    }

    toggleSeries(seriesName: string) {
        if (this.chartComponent) {
            this.updateConfig();
            return this.chartComponent.toggleSeries(seriesName);
        }
    }

    hideSeries(seriesName: string) {
        if (this.chartComponent) {
            this.updateConfig();
            return this.chartComponent.hideSeries(seriesName);
        }
    }

    showSeries(seriesName: string) {
        if (this.chartComponent) {
            this.updateConfig();
            return this.chartComponent.showSeries(seriesName);
        }
    }

    resetSeries(shouldUpdateChart: boolean, shouldResetZoom: boolean) {
        if (this.chartComponent) {
            this.updateConfig();
            return this.chartComponent.resetSeries(shouldUpdateChart, shouldResetZoom);
        }
    }

}