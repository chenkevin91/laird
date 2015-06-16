/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.healththermometerdevice;

import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.view.View;

import com.lairdtech.lairdtoolkit.bases.BaseGraph;

public class TempGraph extends BaseGraph{  
    // graph line
    private XYSeries mCurrentSeries;
    private XYSeriesRenderer mCurrentRenderer;
    
    public TempGraph(Context context, View view){
        super(context, view);
        initChart();
        setStartingPositions(mRenderer.getXAxisMin(), mRenderer.getXAxisMax(), mRenderer.getYAxisMin(), mRenderer.getYAxisMax());
        paintChart();
    }
    
    @Override
    protected void initChart() {
        super.initChart();
        mCurrentSeries = new XYSeries("Temperature Per Minute");
        mRenderer.setYTitle("Temperature");
        mRenderer.setPanEnabled(IS_GRAPH_MOVEABLE_BY_TOUCH_HORIZONTAL, IS_GRAPH_MOVEABLE_BY_TOUCH_VERTICAL);
        mRenderer.setZoomEnabled(IS_GRAPH_ZOOMABLE_BY_TOUCH_HORIZONTAL, IS_GRAPH_ZOOMABLE_BY_TOUCH_VERTICAL);
        mRenderer.setYAxisMax(43);
        mRenderer.setYAxisMin(-10);
        mDataset.addSeries(mCurrentSeries);
        // graph line
        mCurrentRenderer = new XYSeriesRenderer();
        mCurrentRenderer.setColor(Color.parseColor("#FF0000")); // FF0000
        mCurrentRenderer.setLineWidth(LINE_WEIGHT);        
        mRenderer.addSeriesRenderer(mCurrentRenderer);
    }

    public void addNewData(double bpm) {
        // x, y
        mCurrentSeries.add(calculateElapsedTime(), bpm);
        paintChart();
    }
    
    @Override
    public void clearGraph(){
        mCurrentSeries.clear();
        setAxisStartingPoints();
        paintChart();
    }
}