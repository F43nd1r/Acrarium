package com.faendir.acra.ui.base;

import com.faendir.acra.rest.RestReportInterface;
import com.faendir.acra.ui.view.Overview;
import com.faendir.acra.util.PlainTextUser;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.UI;

/**
 * @author lukas
 * @since 09.11.18
 */
public class ConfigurationLabel extends Text {
    public ConfigurationLabel(PlainTextUser user) {
        super(String.format("Take note of the following ACRA configuration. It cannot be viewed later:<br><code>\n" +
                "  @AcraCore(reportFormat = StringFormat.JSON)<br>\n" +
                "  @AcraHttpSender(uri = \"%s%s\",<br>\n" +
                "  basicAuthLogin = \"%s\",<br>\n" +
                "  basicAuthPassword = \"%s\",<br>\n" +
                "  httpMethod = HttpSender.Method.POST)<br></code>", UI.getCurrent().getRouter().getUrl(Overview.class), RestReportInterface.REPORT_PATH, user.getUsername(), user.getPlaintextPassword()));
    }
}
