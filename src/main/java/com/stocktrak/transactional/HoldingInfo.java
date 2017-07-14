package com.stocktrak.transactional;

/**
 * Created by Douglas on 3/22/2015.
 */
public class HoldingInfo {
    private double priceSpent;
    private double totalSaleCost;
    private int quantity;

    public HoldingInfo(double totalSaleCost, int quantity, double priceSpent) {
        this.priceSpent = priceSpent;
        this.totalSaleCost = totalSaleCost;
        this.quantity = quantity;
    }

    public double getPriceSpent() {
        return priceSpent;
    }

    public void setPriceSpent(double priceSpent) {
        this.priceSpent = priceSpent;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalSaleCost() {
        return totalSaleCost;
    }

    public void setTotalSaleCost(double totalSaleCost) {
        this.totalSaleCost = totalSaleCost;
    }

    @Override
    public String toString() {
        return "HoldingInfo{" +
                "priceSpent=" + priceSpent +
                ", totalSaleCost=" + totalSaleCost +
                ", quantity=" + quantity +
                '}';
    }
}
