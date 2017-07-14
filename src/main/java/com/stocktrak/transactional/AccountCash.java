package com.stocktrak.transactional;

/**
 * Created by Douglas on 2/16/2015.
 */
public class AccountCash {

    private Cash currentCash;
    private Cash expectedCash;

    public AccountCash() {
        this(new Cash(), new Cash());
    }

    public AccountCash(Cash currentCash, Cash expectedCash) {
        this.currentCash = currentCash;
        this.expectedCash = expectedCash;
    }

    public synchronized Double getExpectedCash() {
        return expectedCash.getCash();
    }

    public synchronized void decreaseExpectedBy(double decrease) {
        expectedCash.decreaseBy(decrease);
    }

    public synchronized void increaseExpectedBy(double increase) {
        expectedCash.increaseBy(increase);
    }

    public synchronized void decreaseCurrentBy(double decrease) {
        currentCash.decreaseBy(decrease);
    }

    public synchronized void increaseCurrentBy(double increase) {
        currentCash.increaseBy(increase);
    }

    public synchronized void setCurrentCash(double amount) {
        currentCash = new Cash(amount);
    }

    public synchronized void setExpectedCash(double amount) {
        expectedCash = new Cash(amount);
    }

    @Override
    public String toString() {
        return "AccountCash{" +
                "currentCash=" + currentCash +
                ", expectedCash=" + expectedCash +
                '}';
    }
}
