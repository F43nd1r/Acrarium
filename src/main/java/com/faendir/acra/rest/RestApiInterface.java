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

import com.faendir.acra.model.App;
import com.faendir.acra.model.Bug;
import com.faendir.acra.model.Report;
import com.faendir.acra.model.Stacktrace;
import com.faendir.acra.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    @RequestMapping(value = "apps", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<Integer> listApps() {
        return dataService.getAppIds();
    }

    @RequestMapping(value = "apps/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Optional<App> getApp(@PathVariable int id) {
        return dataService.findApp(id);
    }

    @RequestMapping(value = "apps/{id}/bugs", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<Integer> listBugs(@PathVariable int id) {
        return dataService.findApp(id).map(dataService::getBugIds).orElse(Collections.emptyList());
    }

    @RequestMapping(value = "bugs/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Optional<Bug> getBug(@PathVariable int id) {
        return dataService.findBug(id);
    }

    @RequestMapping(value = "bugs/{id}/stacktraces", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<Integer> listStacktraces(@PathVariable int id) {
        return dataService.findBug(id).map(dataService::getStacktraceIds).orElse(Collections.emptyList());
    }

    @RequestMapping(value = "stacktraces/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Optional<Stacktrace> getStacktrace(@PathVariable int id) {
        return dataService.findStacktrace(id);
    }

    @RequestMapping(value = "stacktraces/{id}/reports", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<String> listReports(@PathVariable int id) {
        return dataService.findStacktrace(id).map(dataService::getReportIds).orElse(Collections.emptyList());
    }

    @RequestMapping(value = "apps/{id}/reports", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public List<String> listReportsOfApp(@PathVariable int id, @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime after) {
        return dataService.findApp(id).map(app -> dataService.getReportIds(app, after)).orElse(Collections.emptyList());
    }

    @RequestMapping(value = "reports/{id}", produces = MediaType.APPLICATION_JSON_VALUE, method = RequestMethod.GET)
    public Optional<Report> getReport(@PathVariable String id) {
        return dataService.findReport(id);
    }
}
