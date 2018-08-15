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

package com.faendir.acra.util;

/**
 * @author lukas
 * @since 15.08.18
 */
public class ImportResult {
    private final PlainTextUser user;
    private final int totalCount;
    private final int successCount;

    public ImportResult(PlainTextUser user, int totalCount, int successCount) {
        this.user = user;
        this.totalCount = totalCount;
        this.successCount = successCount;
    }

    public PlainTextUser getUser() {
        return user;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getSuccessCount() {
        return successCount;
    }
}
