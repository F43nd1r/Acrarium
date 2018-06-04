/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
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

package com.faendir.acra.ui.view.base;

import com.faendir.acra.util.PlainTextUser;
import com.faendir.acra.util.Utils;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Label;

/**
 * @author Lukas
 * @since 18.12.2017
 */
public class ConfigurationLabel extends Label {
    public ConfigurationLabel(PlainTextUser user) {
        super(String.format("Take note of the following ACRA configuration. It cannot be viewed later:<br><code>"
                            + "@AcraCore(reportFormat = StringFormat.JSON)<br>"
                            + "@AcraHttpSender(uri = \"%sreport\",<br>"
                            + "basicAuthLogin = \"%s\",<br>"
                            + "basicAuthPassword = \"%s\",<br>"
                            + "httpMethod = HttpSender.Method.POST)<br></code>", Utils.getUrlWithFragment(null), user.getUsername(), user.getPlaintextPassword()), ContentMode.HTML);
    }
}
