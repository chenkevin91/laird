/*****************************************************************************
* Copyright (c) 2014 Laird Technologies. All Rights Reserved.
* 
* The information contained herein is property of Laird Technologies.
* Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided. 
* This heading must NOT be removed from the file.
******************************************************************************/

package com.lairdtech.lairdtoolkit.bases;

import java.util.concurrent.TimeUnit;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;
import android.widget.LinearLayout;

import com.lairdtech.lairdtoolkit.R;

public abstract class BaseGraph{
    public static final float LINE_WEIGHT = 4f;
    public static final boolean IS_GRAPH_MOVEABLE_BY_TOUCH_HORIZONTAL = false;
    public static final boolean IS_GRAPH_MOVEABLE_BY_TOUCH_VERTICAL = false;
    public static final boolean IS_GRAPH_ZOOMABLE_BY_TOUCH_HORIZONTAL = false;
    public static final boolean IS_GRAPH_ZOOMABLE_BY_TOUCH_VERTICAL = false;
    public static final int MARGIN_LEFT = 110;
    public static final int MARGIN_RIGHT = 70;
    
    private Context context;
    private GraphicalView mChart;
    protected XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
    protected XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
    private long mStartTime = 0;
    private long mEndTime = 0;
    private long mElapsedTime = 0;
    
    private double mStartingXAxisMin;
    private double mStartingXAxisMax;
    private double mStartingYAxisMin;
    private double mStartingYAxisMax;
    
    
    protected void setAxisStartingPoints(){
        mRenderer.setXAxisMin(mStartingXAxisMin);
        mRenderer.setXAxisMax(mStartingXAxisMax);
        mRenderer.setYAxisMin(mStartingYAxisMin);
        mRenderer.setYAxisMax(mStartingYAxisMax);
    }
    
    protected void setStartingPositions(double xMin, double xMax, double yMin, double yMax){
        mStartingXAxisMin = xMin;
        mStartingXAxisMax = xMax;
        mStartingYAxisMin = yMin;
        mStartingYAxisMax = yMax;
    }
    
    protected BaseGraph(Context context, View view){
        this.context = context;
        addChartToLayout(view);
    }
    
    protected void initChart() {
        setCommonRendererValues();
    }
    
    protected void clearGraph() {
        setCommonRendererValues();
    }
    
    public long getStartTime(){
        return mStartTime;
    }
    
    public void setStartTime(long startTime){
        mStartTime = startTime;
    }
    
    public void startTimer(){
        if(mStartTime == 0){
            mStartTime = System.nanoTime();
        }
    }
    
    protected void addChartToLayout(View view){        
        LinearLayout chartLayout=(LinearLayout) view.findViewById(R.id.chartLayout);

        mChart = ChartFactory.getCubeLineChartView(context, mDataset, mRenderer, 0.2f);
        chartLayout.addView(mChart);
    }
    
    protected void paintChart(){
        checkGraphHorizontalAutoMovement();
        checkGraphVerticalAutoMovement();
        mChart.repaint();
    }
    
    protected void setCommonRendererValues(){
        // graph styling
        mRenderer.setLabelsColor(Color.BLUE); // labels colour
        mRenderer.setAxisTitleTextSize(30); // labels size
        mRenderer.setLabelsTextSize(25); // numbers size
        mRenderer.setAxesColor(Color.BLACK);
        mRenderer.setMargins(new int[] {0, MARGIN_LEFT, 75, MARGIN_RIGHT});
        mRenderer.setMarginsColor(Color.parseColor("#00FFA500"));
        // X Axis and label
        mRenderer.setXTitle("Time (Minutes)");
        mRenderer.setXAxisMax(1.1);
        mRenderer.setXAxisMin(0);
        mRenderer.setXLabelsColor(Color.BLACK); //color of numbers on label X
        mRenderer.setXLabels(7);
        // Y Axis and label
        mRenderer.setYAxisAlign(Align.LEFT, 0);   
        mRenderer.setYLabelsAlign(Align.RIGHT, 0);  
        mRenderer.setYLabelsPadding(15);
        mRenderer.setYLabelsVerticalPadding(-8);
        mRenderer.setYLabelsColor(0, Color.BLACK); //colour of numbers on label Y
        mRenderer.setYLabels(6);        
        // user interaction with chart
        mRenderer.setZoomButtonsVisible(false);
        mRenderer.setShowLegend(true);
        mRenderer.setFitLegend(true);
        mRenderer.setLegendTextSize(30f);
        mRenderer.setShowGrid(true);
        mRenderer.setGridColor(Color.parseColor("#00000000")); // CD7F32
    }
    
    public double calculateElapsedTime(){
        mEndTime = System.nanoTime();
        mElapsedTime = mEndTime - mStartTime;
        
        double sec = (double) TimeUnit.SECONDS.convert(mElapsedTime, TimeUnit.NANOSECONDS);
//        MyTarget.debugMsg("SECONDS: " + sec);

        sec = sec/60;
        return sec;
    }
    
    /*
     * Min --> graph minimum view point
     * Max --> graph maximum view point
     * newMax --> max point of where the line is right now
     * newMin --> where should the minimum point be moved
     */
    public void checkGraphHorizontalAutoMovement(){
        double max = mRenderer.getXAxisMax();
        double min = mRenderer.getXAxisMin();
        if (!((max == Double.MAX_VALUE || max == -Double.MAX_VALUE) && (min == Double.MAX_VALUE || min == -Double.MAX_VALUE))) {

            double newMax = mDataset.getSeriesAt(0).getMaxX(); // new max is the latest value from our series
//            double newMin = newMax - Math.abs(max - min); // new min value is the X range the graph zoomed into
            
            if(newMax >= max){
                // move graph to the right
                mRenderer.setXAxisMax(newMax);
                /*
                 * currently the graph extends from the lowest existing number to the greatest existing number.
                 * for the whole graph to move to the right comment out the following line from above:
                 * "double newMin = newMax - Math.abs(max - min);"
                 * and the line below
                 */
//                mRenderer.setXAxisMin(newMin);
            } else{
                
            }
        }
    }
    
    public void checkGraphVerticalAutoMovement(){
        double max = mRenderer.getYAxisMax(); // get renderer maximum value
        double min = mRenderer.getYAxisMin(); // get renderer minimum value

        // Check if the user scrolled/zoomed/panned the graph or if it's on 'auto'
        if (!((max == Double.MAX_VALUE || max == -Double.MAX_VALUE) && (min == Double.MAX_VALUE || min == -Double.MAX_VALUE))) {
            double newMax = mDataset.getSeriesAt(0).getMaxY(); // new max is the latest value from our series
//            double newMin = newMax - Math.abs(max - min); // new min value is the X range the graph zoomed into

            // check which graph line has the biggest value
            // in this way we can know the greatest number and display it on the graph Y axis
            for(int j=0; j<mDataset.getSeriesCount(); j++){
                if(newMax < mDataset.getSeriesAt(j).getMaxY()){
                    newMax = mDataset.getSeriesAt(j).getMaxY();
                }
            }
            
            if(newMax >= max){
                mRenderer.setYAxisMax(newMax);
                /*
                 * currently the graph extends from the lowest existing number to the greatest existing number.
                 * for the whole graph to move up, comment out the following line from above:
                 * "double newMin = newMax - Math.abs(max - min);"
                 * and the line below
                 */
//                mRenderer.setYAxisMin(newMin);
                
            } else{
                
            }
        }
    }
}
