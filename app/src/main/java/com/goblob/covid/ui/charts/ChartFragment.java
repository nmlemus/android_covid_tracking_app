package com.goblob.covid.ui.charts;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.goblob.covid.R;

import java.util.ArrayList;
import java.util.List;

public class ChartFragment extends Fragment {

    private View root;
    private LineChart casesByDay, totalCases;
    private BarChart sexDistribution, ageDistribution;
    private HorizontalBarChart chart;
    protected Typeface tfRegular;
    protected Typeface tfLight;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        root = inflater.inflate(R.layout.fragment_chart, container, false);

        tfRegular = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Regular.ttf");
        tfLight = Typeface.createFromAsset(getActivity().getAssets(), "OpenSans-Light.ttf");

        totalCases = (LineChart) root.findViewById(R.id.cases_total);
        casesByDay = (LineChart) root.findViewById(R.id.cases_by_day);
        chart = root.findViewById(R.id.chart1);
        sexDistribution = root.findViewById(R.id.sex_distribution);
        ageDistribution = root.findViewById(R.id.age_distribution);

        distributionBySex();

        casesByCorregimiento();

        distributionByAge();

        List<Entry> totalCasos = new ArrayList<Entry>();
        totalCasos.add(new Entry(1, 69));
        totalCasos.add(new Entry(2, 86));
        totalCasos.add(new Entry(3, 109));
        totalCasos.add(new Entry(4, 137));
        totalCasos.add(new Entry(5, 200));
        totalCasos.add(new Entry(6, 245));
        totalCasos.add(new Entry(7, 313));
        totalCasos.add(new Entry(8, 345));
        totalCasos.add(new Entry(9, 443));
        totalCasos.add(new Entry(10, 558));
        totalCasos.add(new Entry(11, 674));
        totalCasos.add(new Entry(12, 786));
        totalCasos.add(new Entry(13, 901));
        totalCasos.add(new Entry(14, 989));
        totalCasos.add(new Entry(15, 1075));
        totalCasos.add(new Entry(16, 1181));
        totalCasos.add(new Entry(17, 1317));
        totalCasos.add(new Entry(18, 1475));
        totalCasos.add(new Entry(19, 1673));
        totalCasos.add(new Entry(20, 1801));
        totalCasos.add(new Entry(21, 1988));
        totalCasos.add(new Entry(22, 2100));


        List<Entry> casosNuevos = new ArrayList<Entry>();
        casosNuevos.add(new Entry(1, 14));
        casosNuevos.add(new Entry(2, 17));
        casosNuevos.add(new Entry(3, 23));
        casosNuevos.add(new Entry(4, 28));
        casosNuevos.add(new Entry(5, 63));
        casosNuevos.add(new Entry(6, 45));
        casosNuevos.add(new Entry(7, 68));
        casosNuevos.add(new Entry(8, 32));
        casosNuevos.add(new Entry(9, 98));
        casosNuevos.add(new Entry(10, 115));
        casosNuevos.add(new Entry(11, 116));
        casosNuevos.add(new Entry(12, 112));
        casosNuevos.add(new Entry(13, 115));
        casosNuevos.add(new Entry(14, 88));
        casosNuevos.add(new Entry(15, 86));
        casosNuevos.add(new Entry(16, 106));
        casosNuevos.add(new Entry(17, 136));
        casosNuevos.add(new Entry(18, 158));
        casosNuevos.add(new Entry(19, 198));
        casosNuevos.add(new Entry(20, 128));
        casosNuevos.add(new Entry(21, 187));
        casosNuevos.add(new Entry(22, 120));



        LineDataSet totalCasosDataSet = new LineDataSet(totalCasos, "Total de Casos"); // add totalCasos to dataset
        totalCasosDataSet.setCircleRadius(3f);
        totalCasosDataSet.setFillAlpha(110);
        totalCasosDataSet.setColor(Color.BLACK);
        totalCasosDataSet.setCircleColor(Color.BLACK);
        totalCasosDataSet.setLineWidth(1f);
        totalCasosDataSet.setValueTextSize(9f);
        totalCasosDataSet.setDrawFilled(true);

        Description desc = new Description();
        desc.setText("");

        LineData totalCasosLineData = new LineData(totalCasosDataSet);
        totalCases.setData(totalCasosLineData);
        totalCases.setDescription(desc);
        totalCases.invalidate(); // refresh

        LineDataSet newCasosDataSet = new LineDataSet(casosNuevos, "Nuevos casos por dia"); // add totalCasos to dataset
        newCasosDataSet.setCircleRadius(3f);
        newCasosDataSet.setFillAlpha(110);
        newCasosDataSet.setColor(Color.BLACK);
        newCasosDataSet.setCircleColor(Color.BLACK);
        newCasosDataSet.setLineWidth(1f);
        newCasosDataSet.setValueTextSize(9f);
        newCasosDataSet.setDrawFilled(true);

        LineData newCasosLineData = new LineData(newCasosDataSet);

        casesByDay.setData(newCasosLineData);
        casesByDay.setDescription(desc);
        casesByDay.invalidate();


        return root;
    }

    private void distributionByAge() {

        ageDistribution.setDrawBarShadow(false);

        ageDistribution.setDrawValueAboveBar(true);

        ageDistribution.getDescription().setEnabled(false);

        // if more than 60 totalCasos are displayed in the chart, no values will be
        // drawn
        ageDistribution.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        ageDistribution.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        ageDistribution.setDrawGridBackground(false);

        XAxis xl = ageDistribution.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTypeface(tfLight);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        // xl.setGranularity(10f);

        YAxis yl = ageDistribution.getAxisLeft();
        yl.setTypeface(tfLight);
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yl.setInverted(true);

        YAxis yr = ageDistribution.getAxisRight();
        yr.setTypeface(tfLight);
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yr.setInverted(true);

        ageDistribution.setFitBars(true);
        ageDistribution.animateY(2500);

        Legend l = ageDistribution.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);

        float barWidth = 9f;
        float spaceForBar = 1f;

        ArrayList<BarEntry> values = new ArrayList<>();

        values.add(new BarEntry(0f, 4.71f));
        values.add(new BarEntry(1f, 37.38f));
        values.add(new BarEntry(2f, 40.71f));
        values.add(new BarEntry(3f, 15.14f));
        values.add(new BarEntry(4f, 2.05f));


        BarDataSet set1 = new BarDataSet(values, "Distribución por edades");
        set1.setColors(new int[] { android.R.color.holo_red_dark, android.R.color.holo_blue_bright,
                android.R.color.holo_green_dark, android.R.color.holo_red_light, android.R.color.holo_blue_light }, getContext());

        // set1.addColor(Color.parseColor("#878686"));

        ArrayList<String> labels = new ArrayList<>();
        labels.add("< 20");
        labels.add("20-39");
        labels.add("40-59");
        labels.add("60-79");
        labels.add("> 80");

        set1.setDrawIcons(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(tfLight);
        // data.setBarWidth(barWidth);
        ageDistribution.setData(data);
        ageDistribution.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        ageDistribution.setFitBars(true);
        ageDistribution.invalidate();
    }

    private void distributionBySex() {

        sexDistribution.setDrawBarShadow(false);

        sexDistribution.setDrawValueAboveBar(true);

        sexDistribution.getDescription().setEnabled(false);

        // if more than 60 totalCasos are displayed in the chart, no values will be
        // drawn
        sexDistribution.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        sexDistribution.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        sexDistribution.setDrawGridBackground(false);

        XAxis xl = sexDistribution.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTypeface(tfLight);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        // xl.setGranularity(10f);

        YAxis yl = sexDistribution.getAxisLeft();
        yl.setTypeface(tfLight);
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yl.setInverted(true);

        YAxis yr = sexDistribution.getAxisRight();
        yr.setTypeface(tfLight);
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yr.setInverted(true);

        sexDistribution.setFitBars(true);
        sexDistribution.animateY(2500);

        Legend l = sexDistribution.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);

        float barWidth = 9f;
        float spaceForBar = 1f;

        ArrayList<BarEntry> values = new ArrayList<>();

        values.add(new BarEntry(0f, 1262));
        values.add(new BarEntry(1f, 838));


        BarDataSet set1 = new BarDataSet(values, "Distribución por sexo");
        set1.setColors(new int[] { android.R.color.holo_red_dark, android.R.color.holo_blue_bright }, getContext());

        // set1.addColor(Color.parseColor("#878686"));

        ArrayList<String> labels = new ArrayList<>();
        labels.add("Masculino");
        labels.add("Femenino");

        set1.setDrawIcons(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(tfLight);
        // data.setBarWidth(barWidth);
        sexDistribution.setData(data);
        sexDistribution.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));

        sexDistribution.setFitBars(true);
        sexDistribution.invalidate();

    }

    private void casesByCorregimiento() {

        chart.setDrawBarShadow(false);

        chart.setDrawValueAboveBar(true);

        chart.getDescription().setEnabled(false);

        // if more than 60 totalCasos are displayed in the chart, no values will be
        // drawn
        chart.setMaxVisibleValueCount(60);

        // scaling can now only be done on x- and y-axis separately
        chart.setPinchZoom(false);

        // draw shadows for each bar that show the maximum value
        // chart.setDrawBarShadow(true);

        chart.setDrawGridBackground(false);

        XAxis xl = chart.getXAxis();
        xl.setPosition(XAxis.XAxisPosition.BOTTOM);
        xl.setTypeface(tfLight);
        xl.setDrawAxisLine(true);
        xl.setDrawGridLines(false);
        // xl.setGranularity(10f);

        YAxis yl = chart.getAxisLeft();
        yl.setTypeface(tfLight);
        yl.setDrawAxisLine(true);
        yl.setDrawGridLines(true);
        yl.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yl.setInverted(true);

        YAxis yr = chart.getAxisRight();
        yr.setTypeface(tfLight);
        yr.setDrawAxisLine(true);
        yr.setDrawGridLines(false);
        yr.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        yr.setInverted(true);

        chart.setFitBars(true);
        // chart.animateY(2500);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setFormSize(8f);
        l.setXEntrySpace(4f);


        float barWidth = 9f;
        float spaceForBar = 1f;

        ArrayList<BarEntry> values = new ArrayList<>();

        values.add(new BarEntry(8 * spaceForBar, 127));
        values.add(new BarEntry(7 * spaceForBar, 86));
        values.add(new BarEntry(6 * spaceForBar, 82));
        values.add(new BarEntry(5 * spaceForBar, 69));
        values.add(new BarEntry(4 * spaceForBar, 68));
        values.add(new BarEntry(3 * spaceForBar, 61));
        values.add(new BarEntry(2 * spaceForBar, 59));
        values.add(new BarEntry(1 * spaceForBar, 52));
        values.add(new BarEntry(0 * spaceForBar, 42));

        BarDataSet set1 = new BarDataSet(values, "Corregimientos con más casos");

        ArrayList<String> labels = new ArrayList<>();
        labels.add("Alcalde Diaz");
        labels.add("Bella Vista");
        labels.add("Betania");
        labels.add("Tocumen");
        labels.add("Vista Alegre");
        labels.add("Santa Ana");
        labels.add("Arraijan");
        labels.add("Juan Diaz");
        labels.add("San Francisco");


        set1.setDrawIcons(false);

        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);

        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setValueTypeface(tfLight);
        // data.setBarWidth(barWidth);
        chart.setData(data);
        chart.getXAxis().setValueFormatter(new LabelFormatter(labels, barWidth));

        chart.setFitBars(true);
        chart.invalidate();
    }
}
