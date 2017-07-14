package com.stocktrak;
import com.stocktrak.transactional.AccountCash;
import com.stocktrak.transactional.HoldingsMap;
import com.stocktrak.transactional.Transaction;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Douglas on 2/10/2015.
 */
public class AnalysisProcess {

    public static final LinkedBlockingQueue<Transaction> transactionQueue = new LinkedBlockingQueue();
    public static final AccountCash accountCash = new AccountCash();
    public static HoldingsMap holdings = new HoldingsMap();
    private static Boolean finvizReadyToUpdatePortfolio = false;
    private static Boolean stocktrakReadyToUpdatePortfolio = false;
    AnalysisReadyToUpdatePortfolioListener analysisReadyToUpdatePortfolioListener;

    private AnalysisProcess analysisProcess;
    private StockTrackerProcess stockTrackerProcess;

    public AnalysisProcess() {
        analysisReadyToUpdatePortfolioListener = new AnalysisReadyToUpdatePortfolioListener();
    }

    public static void main(String[] args) {
        AnalysisProcess analysisProcess = new AnalysisProcess();
        analysisProcess.start();
    }

    private void start() {
        stockTrackerProcess = new StockTrackerProcess(new StockTrakReadyToTradeListener(), analysisReadyToUpdatePortfolioListener);
        stockTrackerProcess.start();
    }

    private class StockTrakReadyToTradeListener implements ReadyToTradeListener {
        @Override
        public void readyToTrade() {
            Thread finvizProcess = new FinvizProcess(analysisReadyToUpdatePortfolioListener);
            finvizProcess.start();
        }
    }

    private class AnalysisReadyToUpdatePortfolioListener implements ReadyToUpdatePortfolioListener {
        private Object stocktrakPortfolioReadyLock = new Object();
        private Object finvizPortfolioReadyLock = new Object();

        private void updatePortfolioIfReady() {
            if (getFinvizReadyToUpdatePortfolio() && getStocktrakReadyToUpdatePortfolio()) {
                stockTrackerProcess.updateHoldings();
                setStocktrakReadyToUpdatePortfolio(false);
            }
        }

        public void setFinvizReadyToUpdatePortfolio(boolean ready) {
            synchronized (finvizPortfolioReadyLock) {
                finvizReadyToUpdatePortfolio = ready;
            }
            updatePortfolioIfReady();
        }

        public void setStocktrakReadyToUpdatePortfolio(boolean ready) {
            synchronized (stocktrakPortfolioReadyLock) {
                stocktrakReadyToUpdatePortfolio = ready;
            }
            updatePortfolioIfReady();
        }

        public Boolean getFinvizReadyToUpdatePortfolio() {
            synchronized (finvizPortfolioReadyLock) {
                return finvizReadyToUpdatePortfolio;
            }
        }

        public Boolean getStocktrakReadyToUpdatePortfolio() {
            synchronized (stocktrakPortfolioReadyLock) {
                return stocktrakReadyToUpdatePortfolio;
            }
        }
    }
}
