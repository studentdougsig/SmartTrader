package com.stocktrak;

import com.stocktrak.ticker.TickerInfoBuffer;
import com.stocktrak.ticker.TickerMap;
import com.stocktrak.transactional.HoldingInfo;
import com.stocktrak.transactional.Transaction;

import java.util.Calendar;

/**
 * Created by Douglas on 3/9/2015.
 */
public class FinvizProcess extends Thread {
    private static final String LOG_TAG = "FinvizProcess";
    private static final long PERIOD_SIZE = 900000;
    private static final int MOVING_AVG_LENGTH = 10;
    private static final double DEFAULT_TRADE_PRICE = 800000;

    private ReadyToUpdatePortfolioListener readyToUpdatePortfolioListener;

    public FinvizProcess(ReadyToUpdatePortfolioListener readyToUpdatePortfolioListener) {
        this.readyToUpdatePortfolioListener = readyToUpdatePortfolioListener;
    }

    @Override
    public synchronized void start() {
        super.start();
    }

    @Override
    public void run() {
        super.run();
        long startOfBusinessDay = startOfBusinessDay();
        long endOfBusinessDay = endOfBusinessDay();
        FinvizInterface finvizInterface = new FinvizInterface(MOVING_AVG_LENGTH);
        finvizInterface.login();
        long currentTime;
        //finvizInterface.initializeTickerMapWithPortfolioSix();
        while((currentTime = currentTime()) < startOfBusinessDay) {
            try {
                sleep(1000);
            } catch(InterruptedException e) {
                System.out.println(e);
            }
        }
        System.out.println("Business day has begun.");
        while((currentTime = currentTime()) < endOfBusinessDay) {
            finvizInterface.downloadPortfolioSixData();
            TickerMap tickerMap = finvizInterface.getTickerMap();
            System.out.println(tickerMap);
            determineTransactions(tickerMap);
            postAnalysisSleep();
        }
    }

    public void postAnalysisSleep() {
        AnalysisProcess.transactionQueue.add(new Transaction(0, "", Transaction.Type.REFRESH));
        try {
            sleep(PERIOD_SIZE);
        } catch(InterruptedException e) {
            log(e);
        }
    }

    public long currentTime() {
        return Calendar.getInstance().getTimeInMillis();
    }

    public long startOfBusinessDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 11);
        cal.set(Calendar.MINUTE, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public long endOfBusinessDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 16);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

//    public long startOfBusinessDay() {
//        return currentTime();
//    }
//
//    public long endOfBusinessDay() {
//        return currentTime() + 200000;
//    }

    public void determineTransactions(TickerMap tickerMap) {
        for(String symbol : tickerMap.getTickers()) {
            TickerInfoBuffer tickerInfoBuffer = tickerMap.get(symbol);
            if(tickerInfoBuffer.atMaxSize()) {
                double previousMovingAverage = tickerInfoBuffer.getPreviousAnalytics().getAverage();
                double currentMovingAverage = tickerInfoBuffer.getCurrentAnalytics().getAverage();
                double previousPrice = tickerInfoBuffer.getPreviousTickerInfo().getPrice();
                double currentPrice = tickerInfoBuffer.getCurrentTickerInfo().getPrice();
                if (!AnalysisProcess.holdings.containsKey(symbol) &&
                        previousPrice < previousMovingAverage && currentPrice > currentMovingAverage) {
                    int quantity = (int) (DEFAULT_TRADE_PRICE / currentPrice);
                    double totalSalePrice = quantity * currentPrice;
                    Transaction transaction = new Transaction(quantity, symbol, Transaction.Type.BUY);
                    HoldingInfo holdingInfo = new HoldingInfo(totalSalePrice, quantity, currentPrice);
                    AnalysisProcess.transactionQueue.add(transaction);
                    AnalysisProcess.holdings.put(symbol, holdingInfo);
                    AnalysisProcess.accountCash.decreaseExpectedBy(totalSalePrice);
                } else if (AnalysisProcess.holdings.containsKey(symbol)) {
                    HoldingInfo holdingInfo = AnalysisProcess.holdings.get(symbol);
                    boolean crossedToSell = previousPrice > previousMovingAverage && currentPrice < currentMovingAverage;
                    boolean profitHighEnough = holdingInfo.getPriceSpent() < 0.999 * currentPrice;
                    if (crossedToSell || profitHighEnough) {
                        Transaction transaction = new Transaction(holdingInfo.getQuantity(), symbol, Transaction.Type.SELL);
                        AnalysisProcess.transactionQueue.add(transaction);
                        AnalysisProcess.holdings.remove(symbol);
                    }
                }
            }
        }
    }

    public void closeAllTrades() {
        if(AnalysisProcess.holdings != null) {
            for(String ticker : AnalysisProcess.holdings.keySet()) {
                HoldingInfo holdingInfo = AnalysisProcess.holdings.get(ticker);
                Transaction transaction = new Transaction(holdingInfo.getQuantity(), ticker, Transaction.Type.SELL);
                AnalysisProcess.transactionQueue.add(transaction);
                AnalysisProcess.holdings.remove(ticker);
            }
        }
    }

    public void log(Object str) {
        System.out.println(LOG_TAG + ": " + (str != null ? str.toString() : null));
    }

}
