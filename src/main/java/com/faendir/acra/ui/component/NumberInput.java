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

package com.faendir.acra.ui.component;

/**
 * @author lukas
 * @since 29.11.18
 */
public class NumberInput extends AbstractNumberInput {
    public NumberInput() {
        setType("number");
    }

    public NumberInput(double value) {
        this();
        setValue(value);
    }

    public NumberInput(double value, double min, double max) {
        this();
        setValue(value);
        setMin(min);
        setMax(max);
    }
}
