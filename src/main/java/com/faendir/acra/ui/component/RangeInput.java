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

import com.vaadin.flow.component.AbstractSinglePropertyField;
import com.vaadin.flow.component.Focusable;
import com.vaadin.flow.component.PropertyDescriptor;
import com.vaadin.flow.component.PropertyDescriptors;
import com.vaadin.flow.component.Tag;

/**
 * @author lukas
 * @since 29.11.18
 */
@Tag(Tag.INPUT)
public class RangeInput extends AbstractSinglePropertyField<RangeInput, Double> implements Focusable<RangeInput>, HasSize, HasStyle {
    private static final PropertyDescriptor<Double, Double> MIN_DESCRIPTOR = PropertyDescriptors.propertyWithDefault("min", 0d);
    private static final PropertyDescriptor<Double, Double> MAX_DESCRIPTOR = PropertyDescriptors.propertyWithDefault("max", 100d);
    private static final PropertyDescriptor<Double, Double> STEP_DESCRIPTOR = PropertyDescriptors.propertyWithDefault("step", 1d);
    private static final PropertyDescriptor<String, String> TYPE_DESCRIPTOR = PropertyDescriptors.attributeWithDefault("type", "text");

    public RangeInput() {
        super("value", 0d, false);
        setSynchronizedEvent("change");
        setType("range");
    }

    public RangeInput(double min, double max, double defaultValue) {
        this();
        setSynchronizedEvent("change");
        setMin(min);
        setMax(max);
        setValue(defaultValue);
    }

    public void setMin(double min) {
        set(MIN_DESCRIPTOR, min);
    }

    public double getMin() {
        return get(MIN_DESCRIPTOR);
    }

    public void setMax(double max) {
        set(MAX_DESCRIPTOR, max);
    }

    public double getMax() {
        return get(MAX_DESCRIPTOR);
    }

    public void setStep(double step) {
        set(STEP_DESCRIPTOR, step);
    }

    public double getStep() {
        return get(STEP_DESCRIPTOR);
    }

    public void setType(String type) {
        set(TYPE_DESCRIPTOR, type);
    }

    public String getType() {
        return get(TYPE_DESCRIPTOR);
    }
}
