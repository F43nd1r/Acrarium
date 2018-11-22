package com.faendir.acra.ui.base;

import com.faendir.acra.ui.component.HasSize;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.server.InputStreamFactory;
import com.vaadin.flow.server.StreamResource;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.JFreeChart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * @author lukas
 * @since 11.10.18
 */
@Tag("object")
public class JFreeChartWrapper extends Component implements HasSize, HasStyle {

    // 809x 500 ~g olden ratio
    private static final int DEFAULT_WIDTH = 809;
    private static final int DEFAULT_HEIGHT = 500;

    private final JFreeChart chart;
    private int graphWidthInPixels = -1;
    private int graphHeightInPixels = -1;
    private String aspectRatio = "none";

    public JFreeChartWrapper(JFreeChart chartToBeWrapped) {
        this.chart = chartToBeWrapped;
        getElement().setAttribute("type", "image/svg+xml");
        //getElement().getStyle().set("display", "block");
        getElement().setAttribute("data", new StreamResource("chart" + System.currentTimeMillis() + ".svg", (InputStreamFactory) () -> {
            int width = getGraphWidth();
            int height = getGraphHeight();
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;
            try {
                docBuilder = docBuilderFactory.newDocumentBuilder();
            } catch (ParserConfigurationException e1) {
                throw new RuntimeException(e1);
            }
            Document document = docBuilder.newDocument();
            Element svgelem = document.createElement("svg");
            document.appendChild(svgelem);
            // Create an instance of the SVG Generator
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);

            // draw the chart in the SVG generator
            chart.draw(svgGenerator, new Rectangle(width, height));
            Element el = svgGenerator.getRoot();
            el.setAttributeNS(null, "viewBox", "0 0 " + width + " " + height + "");
            el.setAttributeNS(null, "style", "width:100%;height:100%;");
            el.setAttributeNS(null, "preserveAspectRatio", getSvgAspectRatio());
            // Write svg to buffer
            try (ByteArrayOutputStream stream = new ByteArrayOutputStream();
                 Writer out = new OutputStreamWriter(stream, StandardCharsets.UTF_8)) {
                /*
                 * don't use css, FF3 can'd deal with the result perfectly: wrong font sizes
                 */
                boolean useCSS = false;
                svgGenerator.stream(el, out, useCSS, false);
                stream.flush();
                return new ByteArrayInputStream(stream.toByteArray());
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return null;
        }));
    }

    /**
     * This method may be used to tune rendering of the chart when using
     * relative sizes. Most commonly you should use just use common methods
     * inherited from {@link HasSize} interface.
     * <p>
     * Sets the pixel size of the area where the graph is rendered. Most commonly developer may need to fine tune the value when the {@link JFreeChartWrapper} has a relative size.
     *
     * @see JFreeChartWrapper#getGraphWidth()
     * @see #setSvgAspectRatio(String)
     */
    public void setGraphWidth(int width) {
        graphWidthInPixels = width;
    }

    /**
     * This method may be used to tune rendering of the chart when using
     * relative sizes. Most commonly you should use just use common methods
     * inherited from {@link HasSize} interface.
     * <p>
     * Sets the pixel size of the area where the graph is rendered. Most commonly developer may need to fine tune the value when the {@link JFreeChartWrapper} has a relative size.
     *
     * @see JFreeChartWrapper#getGraphHeight()
     * @see #setSvgAspectRatio(String)
     */
    public void setGraphHeight(int height) {
        graphHeightInPixels = height;
    }

    /**
     * Gets the pixel width into which the graph is rendered. Unless explicitly
     * set, the value is derived from the components size, except when the
     * component has relative size.
     */
    public int getGraphWidth() {
        if (graphWidthInPixels > 0) {
            return graphWidthInPixels;
        }
        return DEFAULT_WIDTH;
    }

    /**
     * Gets the pixel height into which the graph is rendered. Unless explicitly
     * set, the value is derived from the components size, except when the
     * component has relative size.
     */
    public int getGraphHeight() {
        if (graphHeightInPixels > 0) {
            return graphHeightInPixels;
        }
        return DEFAULT_HEIGHT;
    }

    public String getSvgAspectRatio() {
        return aspectRatio;
    }

    /**
     * See SVG spec from W3 for more information.
     * Default is "none" (stretch), another common value is "xMidYMid" (stretch
     * proportionally, align middle of the area).
     */
    public void setSvgAspectRatio(String svgAspectRatioSetting) {
        aspectRatio = svgAspectRatioSetting;
    }
}
