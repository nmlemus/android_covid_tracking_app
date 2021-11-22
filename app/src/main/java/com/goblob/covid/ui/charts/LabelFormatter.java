package com.goblob.covid.ui.charts;

import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;

public class LabelFormatter extends ValueFormatter {
    private final ArrayList<String> mLabels;
    private float spaceForBar;

    public LabelFormatter(ArrayList<String> labels, float spaceForBar) {
        mLabels = labels;
        this.spaceForBar = spaceForBar;
    }

    @Override
    public String getFormattedValue(float value) {
        int index = Math.round(value);
        return mLabels.get(index);
    }
}