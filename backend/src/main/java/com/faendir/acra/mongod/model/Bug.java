package com.faendir.acra.mongod.model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author Lukas
 * @since 13.05.2017
 */
@Document
public class Bug implements AppScoped{
    private Identification id;
    @Indexed
    private String app;
    private boolean solved;
    private String stacktrace;

    public Bug(){
    }

    public Bug(String app, String stacktrace, int versionCode){
        this.id = new Identification(stacktrace.hashCode(), versionCode);
        this.app = app;
        this.stacktrace = stacktrace;
    }

    public boolean is(ReportInfo report){
        return report.getStacktrace().hashCode() == id.stacktraceHash && report.getVersionCode() == id.versionCode;
    }

    public boolean isSolved() {
        return solved;
    }

    public void setSolved(boolean solved) {
        this.solved = solved;
    }

    public String getStacktrace() {
        return stacktrace;
    }

    public int getVersionCode() {
        return id.versionCode;
    }

    @Override
    public String getApp() {
        return app;
    }

    public static class Identification implements Serializable {
        private int stacktraceHash;
        private int versionCode;

        public Identification(int stacktraceHash, int versionCode) {
            this.stacktraceHash = stacktraceHash;
            this.versionCode = versionCode;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Identification that = (Identification) o;

            return stacktraceHash == that.stacktraceHash && versionCode == that.versionCode;
        }

        @Override
        public int hashCode() {
            int result = stacktraceHash;
            result = 31 * result + versionCode;
            return result;
        }
    }
}
