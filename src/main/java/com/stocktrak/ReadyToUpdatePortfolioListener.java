package com.stocktrak;

/**
 * Created by Douglas on 4/11/2015.
 */
public interface ReadyToUpdatePortfolioListener {

    public void setFinvizReadyToUpdatePortfolio(boolean ready);

    public void setStocktrakReadyToUpdatePortfolio(boolean ready);

    public Boolean getFinvizReadyToUpdatePortfolio();

    public Boolean getStocktrakReadyToUpdatePortfolio();
}
