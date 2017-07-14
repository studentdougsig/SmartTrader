package com.stocktrak.ticker;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Douglas on 2/16/2015.
 */
public class TickerMap {

    private HashMap<String, TickerInfoBuffer> map;
    private int bufferSize;

    public TickerMap(int bufferSize) {
        map = new HashMap();
        this.bufferSize = bufferSize;
    }

    public void add(String key, TickerInfo info) {
        if(map.get(key) == null) {
            map.put(key, new TickerInfoBuffer(bufferSize));
        }
        map.get(key).add(info);
    }

    public TickerInfoBuffer get(String symbol) {
        return map.get(symbol);
    }

    public Set<String> getTickers() {
        return map.keySet();
    }

    @Override
    public String toString() {
        return "TickerMap{" +
                "map=" + map +
                ", bufferSize=" + bufferSize +
                '}';
    }
}
