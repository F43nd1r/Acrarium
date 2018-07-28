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

package com.faendir.acra.model;

import org.springframework.data.annotation.PersistenceConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.ManyToOne;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author lukas
 * @since 28.07.18
 */
@Entity
@IdClass(StacktraceMatch.ID.class)
public class StacktraceMatch {
    @Id
    @ManyToOne
    private Stacktrace left;
    @Id
    @ManyToOne
    private Stacktrace right;
    private int score;

    @PersistenceConstructor
    StacktraceMatch(){
    }

    public StacktraceMatch(Stacktrace left, Stacktrace right, int score) {
        this.left = left;
        this.right = right;
        this.score = score;
    }

    public Stacktrace getLeft() {
        return left;
    }

    public Stacktrace getRight() {
        return right;
    }

    public List<Stacktrace> getBoth() {
        return Arrays.asList(left, right);
    }

    public int getScore() {
        return score;
    }

    static class ID implements Serializable {
        private Stacktrace left;
        private Stacktrace right;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ID id = (ID) o;
            return Objects.equals(left, id.left) && Objects.equals(right, id.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, right);
        }
    }
}
