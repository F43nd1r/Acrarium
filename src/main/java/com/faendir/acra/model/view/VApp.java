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

package com.faendir.acra.model.view;

import com.faendir.acra.model.App;
import com.querydsl.core.annotations.QueryProjection;
import org.springframework.lang.NonNull;

/**
 * @author lukas
 * @since 29.05.18
 */
public class VApp {
    private final App app;
    private final long reportCount;
    private final long bugCount;

    @QueryProjection
    public VApp(App app, long bugCount, long reportCount) {
        this.app = app;
        this.reportCount = reportCount;
        this.bugCount = bugCount;
    }

    public App getApp() {
        return app;
    }

    public long getReportCount() {
        return reportCount;
    }

    public long getBugCount() {
        return bugCount;
    }

    public int getId() {
        return app.getId();
    }

    @NonNull
    public String getName() {
        return app.getName();
    }
}
