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
package com.faendir.acra.rest;

import com.faendir.acra.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lukas
 * @since 23.08.18
 */
@RestController
@RequestMapping(RestApiInterface.API_PATH)
@PreAuthorize("hasRole(T(com.faendir.acra.model.User$Role).API)")
public class RestApiInterface {
    public static final String API_PATH = "api";
    @NonNull private final DataService dataService;

    @Autowired
    public RestApiInterface(@NonNull DataService dataService) {
        this.dataService = dataService;
    }
}
