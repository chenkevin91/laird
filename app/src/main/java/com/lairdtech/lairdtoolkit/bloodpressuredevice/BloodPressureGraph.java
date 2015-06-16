/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.bloodpressuredevice;

import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.lairdtech.lairdtoolkit.bases.BaseGraph;

public class BloodPressureGraph extends BaseGraph{
    // graph line
    private XYSeries mCurrentSeries;
    public XYSeries mCurrentSeries2;
    public XYSeries mCurrentSeries3;
    private XYSeriesRenderer mCurrentRenderer;
    private XYSeriesRenderer mCurrentRenderer2;
    private XYSeriesRenderer mCurrentRenderer3;
    
    
    public BloodPressureGraph(Context context, View view){
        super(context, view);
        initChart();
        setStartingPositions(mRenderer.getXAxisMin(), mRenderer.getXAxisMax(), mRenderer.getYAxisMin(), mRenderer.getYAxisMax());
        paintChart();
    }
    
    @Override
    protected void initChart() {
        super.initChart();
        mCurrentSeries = new XYSeries("Systolic");
        mCurrentSeries2 = new XYSeries("Dystolic");
        mCurrentSeries3 = new XYSeries("Arterial Pressure");
        mDataset.addSeries(mCurrentSeries);
        mDataset.addSeries(mCurrentSeries2);
        mDataset.addSeries(mCurrentSeries3);
        
        mRenderer.setYTitle("Blood Pressure");
        mRenderer.setYAxisMax(210);
        mRenderer.setYAxisMin(0);
        mRenderer.setXAxisMax(1.1);
        mRenderer.setXAxisMin(0);
        mRenderer.setPanEnabled(IS_GRAPH_MOVEABLE_BY_TOUCH_HORIZONTAL, IS_GRAPH_MOVEABLE_BY_TOUCH_VERTICAL);
        mRenderer.setZoomEnabled(IS_GRAPH_ZOOMABLE_BY_TOUCH_HORIZONTAL, IS_GRAPH_ZOOMABLE_BY_TOUCH_VERTICAL);
        
        // line styling
        // first line systolic values
        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setColor(Color.parseColor("#FF0000")); // FF0000
        mCurrentRenderer.setLineWidth(LINE_WEIGHT);
        // second line dystolic values
        mCurrentRenderer2 = new XYSeriesRenderer();
        mCurrentRenderer2.setColor(Color.parseColor("#0034C5")); // 0034C5
        mCurrentRenderer2.setLineWidth(LINE_WEIGHT);
        // third line arterial pressure values
        mCurrentRenderer3 = new XYSeriesRenderer();
        mCurrentRenderer3.setColor(Color.parseColor("#008000")); // 008000
        mCurrentRenderer3.setLineWidth(LINE_WEIGHT);   
        
        mRenderer.addSeriesRenderer(mCurrentRenderer);
        mRenderer.addSeriesRenderer(mCurrentRenderer2);
        mRenderer.addSeriesRenderer(mCurrentRenderer3);
    }
    
    public void addNewData(double systolic, double dystolic, double arterialPressure) {
        // x, y
        mCurrentSeries.add(calculateElapsedTime(), systolic);
        mCurrentSeries2.add(calculateElapsedTime(), dystolic);
        mCurrentSeries3.add(calculateElapsedTime(), arterialPressure);
        paintChart();
    }
    
    @Override
    public void clearGraph() {
        mCurrentSeries.clear();
        mCurrentSeries2.clear();
        mCurrentSeries3.clear();
        setAxisStartingPoints();
        paintChart();
    }
}