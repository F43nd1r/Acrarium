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

package com.faendir.acra.ui.component;

import com.vaadin.flow.component.customfield.CustomField;
import com.vaadin.flow.component.textfield.NumberField;

/**
 * @author lukas
 * @since 07.05.19
 */
public class RangeField extends CustomField<Double> {
    private final RangeInput input;
    private final NumberField field;

    public RangeField() {
        input = new RangeInput();
        field = new NumberField();
        field.setHasControls(true);
        input.addValueChangeListener(e -> {
            if(e.isFromClient()) {
                field.setValue(e.getValue());
            }
        });
        field.addValueChangeListener(e -> {
            if(e.isFromClient()) {
                input.setValue(e.getValue());
            }
        });
        add(input, field);
    }

    @Override
    protected Double generateModelValue() {
        return input.getValue();
    }

    @Override
    protected void setPresentationValue(Double newPresentationValue) {
        input.setValue(newPresentationValue);
        field.setValue(newPresentationValue);
    }

    public void setMin(double min) {
        input.setMin(min);
        field.setMin(min);
    }

    public double getMin() {
        return input.getMin();
    }

    public void setMax(double max) {
        input.setMax(max);
        field.setMax(max);
    }

    public double getMax() {
        return input.getMax();
    }

    public void setStep(double step) {
        input.setStep(step);
        field.setStep(step);
    }

    public double getStep() {
        return input.getStep();
    }
}
