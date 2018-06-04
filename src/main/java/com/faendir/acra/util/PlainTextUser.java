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

import com.faendir.acra.model.User;

import java.util.Collection;

/**
 * @author lukas
 * @since 21.05.18
 */
public class PlainTextUser extends User {
    private final String plaintextPassword;

    public PlainTextUser(String username, String plaintextPassword, String encodedPassword, Collection<Role> roles) {
        super(username, encodedPassword, roles);
        this.plaintextPassword = plaintextPassword;
    }

    public String getPlaintextPassword() {
        return plaintextPassword;
    }
}
