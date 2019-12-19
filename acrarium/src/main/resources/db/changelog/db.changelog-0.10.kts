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

import liquibase.precondition.core.PreconditionContainer
import org.liquibase.kotlin.databaseChangeLog

databaseChangeLog {
    include("db.changelog-0.10-user.kts", true)
    include("db.changelog-0.10-roles.kts", true)
    include("db.changelog-0.10-app.kts", true)
    include("db.changelog-0.10-permissions.kts", true)
    include("db.changelog-0.10-mail.kts", true)
    include("db.changelog-0.10-version.kts", true)
    include("db.changelog-0.10-bug.kts", true)
    include("db.changelog-0.10-stacktrace.kts", true)
    include("db.changelog-0.10-stacktrace-match.kts", true)
    include("db.changelog-0.10-report.kts", true)
    include("db.changelog-0.10-attachment.kts", true)
}