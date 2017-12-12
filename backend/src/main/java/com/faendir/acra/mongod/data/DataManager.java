package com.faendir.acra.mongod.data;

import com.faendir.acra.mongod.model.App;
import com.faendir.acra.mongod.model.Bug;
import com.faendir.acra.mongod.model.ParsedException;
import com.faendir.acra.mongod.model.ProguardMapping;
import com.faendir.acra.mongod.model.Report;
import com.faendir.acra.mongod.model.ReportInfo;
import com.faendir.acra.mongod.util.BufferedMongoDataProvider;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.vaadin.data.provider.DataProvider;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Component;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;
import proguard.retrace.ReTrace;

import java.io.File;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 21.05.2017
 */
@Component
public class DataManager {
    @NotNull private final MappingRepository mappingRepository;
    @NotNull private final ReportRepository reportRepository;
    @NotNull private final AppRepository appRepository;
    @NotNull private final BugRepository bugRepository;
    @NotNull private final List<ListenerHolder<?>> listeners;
    @NotNull private final GridFsTemplate gridFsTemplate;
    @NotNull private final Logger logger;
    @NotNull private final SecureRandom secureRandom;
    @NotNull private final ExecutorService listenerExecutor;

    @Autowired
    public DataManager(@NotNull SecureRandom secureRandom, @NotNull AppRepository appRepository, @NotNull GridFsTemplate gridFsTemplate,
                       @NotNull MappingRepository mappingRepository, @NotNull ReportRepository reportRepository, @NotNull BugRepository bugRepository) {
        this.secureRandom = secureRandom;
        this.appRepository = appRepository;
        this.bugRepository = bugRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.mappingRepository = mappingRepository;
        this.reportRepository = reportRepository;
        this.logger = LoggerFactory.getLogger(DataManager.class);
        this.listeners = new ArrayList<>();
        this.listenerExecutor = Executors.newSingleThreadExecutor();
    }

    public synchronized void createNewApp(@NotNull String name) {
        byte[] bytes = new byte[12];
        secureRandom.nextBytes(bytes);
        appRepository.save(new App(name, Base64Utils.encodeToString(bytes)));
    }

    @NotNull
    public List<App> getApps() {
        return appRepository.findAll();
    }

    @Nullable
    public App getApp(@NotNull String id) {
        return appRepository.findById(id).orElse(null);
    }

    public synchronized void deleteApp(@NotNull String id) {
        appRepository.deleteById(id);
        getReportsForApp(id).forEach(this::deleteReport);
        mappingRepository.deleteAll(getMappings(id));
    }

    @NotNull
    public List<Pair<GridFSFile, Supplier<GridFsResource>>> getAttachments(@NotNull String report) {
        return gridFsTemplate.find(new Query(Criteria.where("metadata.reportId").is(report)))
                .map(file -> Pair.of(file, (Supplier<GridFsResource>) () -> gridFsTemplate.getResource(file.getFilename()))).into(new ArrayList<>());
    }

    public synchronized void addMapping(@NotNull String app, int version, @NotNull String mappings) {
        mappingRepository.save(new ProguardMapping(app, version, mappings));
    }

    @Nullable
    private ProguardMapping getMapping(@NotNull String app, int version) {
        return mappingRepository.findById(new ProguardMapping.MetaData(app, version)).orElse(null);
    }

    @NotNull
    public List<ProguardMapping> getMappings(@NotNull String app) {
        return mappingRepository.findAllByIdApp(app);
    }

    public void newReport(@NotNull String app, @NotNull JSONObject content) {
        newReport(app, content, Collections.emptyList());
    }

    public synchronized void newReport(@NotNull String app, @NotNull JSONObject content, @NotNull List<MultipartFile> attachments) {
        Report report = reportRepository.save(new Report(content, app));
        for (MultipartFile a : attachments) {
            try {
                gridFsTemplate.store(a.getInputStream(), a.getOriginalFilename(), a.getContentType(), new BasicDBObjectBuilder().add("reportId", report.getId()).get());
            } catch (IOException e) {
                logger.warn("Failed to load attachment", e);
            }
        }
        ReportInfo info = new ReportInfo(report);
        notifyListeners(info);
        Bug bug = getBugs(app).stream().filter(b -> matches(b, info)).findAny().orElseGet(() -> new Bug(info.getApp(), info.getStacktrace(), info.getVersionCode()));
        bug.getReportIds().add(info.getId());
        bug.setLastReport(info.getDate());
        saveBug(bug);
    }

    @NotNull
    public List<ReportInfo> getReportsForApp(@NotNull String app) {
        try (Stream<Report> stream = reportRepository.streamAllByApp(app)) {
            return stream.map(ReportInfo::new).collect(Collectors.toList());
        }
    }

    public DataProvider<ReportInfo, Void> getLazyReportsForApp(@NotNull String app) {
        return BufferedMongoDataProvider.of(pageable -> reportRepository.findAllByApp(app, pageable), ReportInfo::new);
    }

    public long reportCountForApp(@NotNull String app) {
        return reportRepository.countByApp(app);
    }

    @Nullable
    public Report getReport(@NotNull String id) {
        return reportRepository.findById(id).orElse(null);
    }

    public synchronized void deleteReport(@NotNull ReportInfo report) {
        reportRepository.deleteById(report.getId());
        gridFsTemplate.delete(new Query(Criteria.where("metadata.reportId").is(report.getId())));
        bugRepository.findByReportIdsContains(report.getId()).forEach(bug -> {
            bug.getReportIds().remove(report.getId());
            if (bug.getReportIds().isEmpty()) {
                deleteBug(bug);
            } else {
                saveBug(bug);
            }
        });
        notifyListeners(report);
    }

    @SuppressWarnings("WeakerAccess")
    public void deleteBug(@NotNull Bug bug) {
        bugRepository.delete(bug);
        notifyListeners(bug);
    }

    @SuppressWarnings("WeakerAccess")
    public void saveBug(@NotNull Bug bug) {
        Bug b = bugRepository.save(bug);
        notifyListeners(bug);
    }

    @NotNull
    public List<Bug> getBugs(@NotNull String app) {
        return bugRepository.findByApp(app);
    }

    public DataProvider<Bug, Void> getLazyBugs(@NotNull String app, boolean includeSolved) {
        if (includeSolved) {
            return BufferedMongoDataProvider.of(pageable -> bugRepository.findAllByApp(app, pageable));
        } else {
            return BufferedMongoDataProvider.of(pageable -> bugRepository.findAllByAppAndSolvedIsFalse(app, pageable));
        }
    }

    public DataProvider<ReportInfo, Void> getLazyReportsForBug(@NotNull Bug bug) {
        return BufferedMongoDataProvider.of(pageable -> reportRepository.findAllByIdIn(bug.getReportIds(), pageable), ReportInfo::new);
    }

    public long reportCountForBug(@NotNull Bug bug) {
        return bug.getReportIds().size();
    }

    public void rebuildBugs(@NotNull String app) {
        bugRepository.deleteAll();
        Map<Bug, List<ReportInfo>> mapping = new HashMap<>();
        getReportsForApp(app).forEach(reportInfo -> mapping.entrySet().stream().filter(entry -> matches(entry.getKey(), reportInfo)).map(Map.Entry::getValue).findAny()
                .orElseGet(() -> {
                    List<ReportInfo> list = new ArrayList<>();
                    mapping.put(new Bug(reportInfo.getApp(), reportInfo.getStacktrace(), reportInfo.getVersionCode()), list);
                    return list;
                })
                .add(reportInfo));
        mapping.forEach((bug, reports) -> {
            reports.forEach(report->bug.getReportIds().add(report.getId()));
            bug.setLastReport(ReportUtils.getLastReportDate(reports));
        });
        bugRepository.insert(mapping.keySet());
        mapping.keySet().stream().findAny().ifPresent(this::notifyListeners);
    }

    public boolean matches(@NotNull Bug bug, @NotNull ReportInfo info) {
        return bug.getVersionCode() == info.getVersionCode() && new ParsedException(bug.getStacktrace()).equals(new ParsedException(info.getStacktrace()));
    }

    @NotNull
    public String retrace(@NotNull Report report) {
        ProguardMapping mapping = getMapping(report.getApp(), report.getVersionCode());
        if (mapping != null) {
            try {
                File file = File.createTempFile("mapping", ".txt");
                FileUtils.writeStringToFile(file, mapping.getMappings());
                StringWriter writer = new StringWriter();
                new ReTrace(ReTrace.STACK_TRACE_EXPRESSION, false, file).retrace(new LineNumberReader(new StringReader(report.getStacktrace())), new PrintWriter(writer));
                //noinspection ResultOfMethodCallIgnored
                file.delete();
                return writer.toString();
            } catch (IOException ignored) {
            }
        }
        return report.getStacktrace();
    }

    public void setBugSolved(@NotNull Bug bug, boolean solved) {
        bug.setSolved(solved);
        saveBug(bug);
    }

    public <T> void addListener(@NotNull Listener<T> listener, @NotNull Class<T> clazz) {
        listeners.add(new ListenerHolder<>(listener, clazz));
    }

    public void removeListener(@NotNull Listener<?> listener) {
        listeners.removeIf(h -> h.listener.equals(listener));
    }

    private void notifyListeners(@NotNull Object change) {
        listeners.forEach(h -> listenerExecutor.submit(() -> h.callIfAssignable(change)));
    }

    public interface Listener<T> {
        void onChange(@NotNull T t);
    }

    private static class ListenerHolder<T> {
        @NotNull private final Listener<T> listener;
        @NotNull private final Class<T> clazz;

        private ListenerHolder(@NotNull Listener<T> listener, @NotNull Class<T> clazz) {
            this.listener = listener;
            this.clazz = clazz;
        }

        private void callIfAssignable(@NotNull Object change) {
            if (clazz.isInstance(change)) {
                listener.onChange(clazz.cast(change));
            }
        }
    }
}
