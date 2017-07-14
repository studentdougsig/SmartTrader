package com.stocktrak.transactional;

/**
 * Created by Douglas on 2/16/2015.
 */
public class Cash {
    private Double cash;

    public Cash(Double cash) {
        this.cash = cash;
    }

    public Cash() {
        this(null);
    }

    public synchronized Double getCash() {
        return cash;
    }

    public synchronized void decreaseBy(double decrease) {
        cash = cash - decrease;
    }

    public synchronized void increaseBy(double increase) {
        cash = cash + increase;
    }

    @Override
    public String toString() {
        return "Cash{" +
                "cash=" + cash +
                '}';
    }
}
