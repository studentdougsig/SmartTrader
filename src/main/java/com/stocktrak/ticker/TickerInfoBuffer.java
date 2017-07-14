package com.stocktrak.ticker;

import org.apache.commons.collections.buffer.CircularFifoBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Created by Douglas on 2/16/2015.
 */
public class TickerInfoBuffer implements Iterable<TickerInfo> {
    private CircularFifoBuffer tickerInfoBuffer;
    private TickerInfo currentTickerInfo;
    private TickerInfo previousTickerInfo;
    private Analytics currentAnalytics;
    private Analytics previousAnalytics;
    private int maxSize;

    public TickerInfoBuffer() {
        this(10);
    }

    public TickerInfoBuffer(int maxSize) {
        this.maxSize = maxSize;
        tickerInfoBuffer = new CircularFifoBuffer(maxSize);
    }

    public int getMaxSize() {
        return maxSize;
    }

    private double getAverage() {
        double sum = 0;
        for(TickerInfo tickerInfo : this) {
            sum += tickerInfo.getPrice();
        }
        double average = sum / tickerInfoBuffer.size();
        return average;
    }

    private double getStandardDeviation() {
        return 0.0;
    }

    public void updateAnalytics() {
        previousAnalytics = currentAnalytics;
        currentAnalytics = new Analytics(getAverage(), getStandardDeviation());
    }

    public Analytics getCurrentAnalytics() {
        return currentAnalytics;
    }

    public Analytics getPreviousAnalytics() {
        return previousAnalytics;
    }

    public boolean add(TickerInfo tickerInfo) {
        boolean ret = tickerInfoBuffer.add(tickerInfo);
        if(ret) {
            previousTickerInfo = currentTickerInfo;
            currentTickerInfo = tickerInfo;
            updateAnalytics();
        }
        return ret;
    }

    public boolean atMaxSize() {
        return tickerInfoBuffer.size() == tickerInfoBuffer.maxSize();
    }

    public TickerInfo get() {
        return (TickerInfo)tickerInfoBuffer.get();
    }

    public TickerInfo get(int index) {
        if(index < tickerInfoBuffer.size()) {
            int i = 0;
            for(TickerInfo tickerInfo : this) {
                if(i++ == index) {
                    return tickerInfo;
                }
            }
        }
        return null;
    }

    public TickerInfo getCurrentTickerInfo() {
        return currentTickerInfo;
    }

    public TickerInfo getPreviousTickerInfo() {
        return previousTickerInfo;
    }

    public TickerInfo remove() {
        return (TickerInfo)tickerInfoBuffer.remove();
    }

    @Override
    public Iterator iterator() {
        return tickerInfoBuffer.iterator();
    }

    @Override
    public String toString() {
        return "TickerInfoBuffer{" +
                "tickerInfoBuffer=" + tickerInfoBuffer +
                ", currentTickerInfo=" + currentTickerInfo +
                ", previousTickerInfo=" + previousTickerInfo +
                ", currentAnalytics=" + currentAnalytics +
                ", previousAnalytics=" + previousAnalytics +
                ", maxSize=" + maxSize +
                '}';
    }
}