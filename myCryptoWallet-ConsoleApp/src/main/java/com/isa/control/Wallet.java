package com.isa.control;

import com.isa.control.transactions.ActiveTransaction;
import com.isa.control.transactions.ClosedTransaction;
import com.isa.menu.Balance;

import java.util.*;
import java.util.stream.Collectors;

public class Wallet {
    private String walletId;
    private Balance startBalance;
    private double walletSum;
    private double profitLoss;
    private double historicalProfitLoss;
    private double transactionsCosts;
    private double walletBalance;
    private Set<ClosedTransaction> transactionsHistory = new HashSet<>();
    private Set<ActiveTransaction> activeTransactions = new HashSet<>();
    public Wallet(){}

    public Wallet(String walletId, Balance startBalance){
        this.walletId = walletId;
        this.startBalance = startBalance;
       // setWalletTransactions(Data.deserializeWalletTransactions());
    }

    public void buyNewToken(Coin coin, double volume){
        ActiveTransaction activeTransaction = new ActiveTransaction(coin, volume);
        if(activeTransaction.countTransactionCost() < walletBalance) {
            activeTransactions.add(new ActiveTransaction(coin, volume));
            System.out.println("transakcja zawarta pomyślnie");
        }else System.out.println("wartość transakcji przekracza ilość środków dostępnych w portfelu");
    }
    public void closeActiveTransaction(ActiveTransaction transaction, double volume){
        if(transaction.getVolume()<=volume){
            ClosedTransaction closed = new ClosedTransaction(transaction);
            transactionsHistory.add(closed);
            activeTransactions.remove(transaction);

        } else if (transaction.getVolume()>volume && volume>0) {
            ClosedTransaction closed = new ClosedTransaction(transaction, volume);
            transactionsHistory.add(closed);
            activeTransactions.remove(transaction);
            activeTransactions.add(closed.getActivePartOfClosedTransaction());
        }
        else System.out.println("volumen musi być liczbą dodatnią");

    }
    public void updateWallet(){
        if (!activeTransactions.isEmpty()){
            activeTransactions.forEach(n->{
                n.refreshPrice();
                executeStopLossAlarm(n);
                executeTakeProfitAlarm(n);
            });
        }
        historyProfitCount();
        currentProfitCount();
        countActiveTransactionsCosts();
        countWalletBalance();
        countWalletSum();
    }
    public void currentProfitCount(){
        if(!activeTransactions.isEmpty()) {
            this.profitLoss = activeTransactions.stream().mapToDouble(n -> {
               // n.refreshPrice();
                return n.countProfit();
            }).sum();
        }else this.profitLoss = 0;
    }

    public void historyProfitCount(){
        if(!transactionsHistory.isEmpty()){
            this.historicalProfitLoss =  transactionsHistory.stream().mapToDouble(ClosedTransaction::countProfit).sum();
        }else this.historicalProfitLoss = 0;
    }


    public void countWalletBalance(){
        this.walletBalance = startBalance.getWorth() - transactionsCosts + historicalProfitLoss + profitLoss;
    }

    public void countWalletSum(){
        this.walletSum = startBalance.getWorth() + historicalProfitLoss + profitLoss;
    }
    public void countActiveTransactionsCosts() {
        if (!activeTransactions.isEmpty()) {
            this.transactionsCosts = activeTransactions.stream().mapToDouble(ActiveTransaction::countTransactionCost).sum();
        }else this.transactionsCosts = 0;
    }
    public void executeStopLossAlarm(ActiveTransaction activeTransaction){
        if(activeTransaction.isSLOn() && activeTransaction.getCurrentPrice() <= activeTransaction.getStopLoss()){
            closeActiveTransaction(activeTransaction, activeTransaction.getVolume());
        }
    }
    public void executeTakeProfitAlarm(ActiveTransaction activeTransaction){
        if(activeTransaction.isTPOn() && activeTransaction.getCurrentPrice() >= activeTransaction.getTakeProfit()){
            closeActiveTransaction(activeTransaction, activeTransaction.getVolume());
        }
    }
    public static Wallet createNewWalletFromKeyboard(Scanner scanner){
        System.out.println("podaj unikatową nazwę portfela");
        String idForNewWallet = scanner.nextLine();
        System.out.println("wybierz początkową wartość portfela:");
        Balance.printBalance();
        double walletBalance = scanner.nextDouble();
        Balance balance = Balance.getBalance(walletBalance);
        return new Wallet(idForNewWallet,balance);
    }
    public static Coin searchCoinForBuying(){
        System.out.println("wybierz token który chcesz kupić");
        CoinSearch coinSearch = new CoinSearch();
        List<Coin> yourToken = new ArrayList<>();
        while (yourToken.isEmpty()) {
            yourToken = coinSearch.findYourToken();
        }
        return yourToken.get(0);
    }
    public ActiveTransaction searchActiveTransaction(Scanner scanner){
        activeTransactions.forEach(ActiveTransaction::printDetails);
        List<Long> idTransactionList = new ArrayList<>();
        activeTransactions.forEach(n -> idTransactionList.add(n.getIdTransaction()));
        System.out.println("wpisz ID aby wybrać pozycję");
        long idActiveTransaction = 0;
        while (!idTransactionList.contains(idActiveTransaction)) {
            idActiveTransaction = scanner.nextLong();
        }
        long finalIdActiveTransaction = idActiveTransaction;
        List<ActiveTransaction> activeTransactionList = activeTransactions.stream()
                .filter(n -> n.getIdTransaction() == finalIdActiveTransaction).collect(Collectors.toList());
        return activeTransactionList.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wallet wallet = (Wallet) o;
        return Double.compare(wallet.walletSum, walletSum) == 0 && Double.compare(wallet.profitLoss, profitLoss) == 0 && Double.compare(wallet.historicalProfitLoss, historicalProfitLoss) == 0 && Double.compare(wallet.transactionsCosts, transactionsCosts) == 0 && Double.compare(wallet.walletBalance, walletBalance) == 0 && Objects.equals(walletId, wallet.walletId) && startBalance == wallet.startBalance && Objects.equals(transactionsHistory, wallet.transactionsHistory) && Objects.equals(activeTransactions, wallet.activeTransactions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(walletId, startBalance, walletSum, profitLoss, historicalProfitLoss, transactionsCosts, walletBalance, transactionsHistory, activeTransactions);
    }

    public String getWalletId() {
        return walletId;
    }

    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public Balance getStartBalance() {
        return startBalance;
    }

    public void setStartBalance(Balance startBalance) {
        this.startBalance = startBalance;
    }

    public double getWalletSum() {
        return walletSum;
    }

    public void setWalletSum(double walletSum) {
        this.walletSum = walletSum;
    }

    public double getProfitLoss() {
        return profitLoss;
    }

    public void setProfitLoss(double profitLoss) {
        this.profitLoss = profitLoss;
    }

    public double getHistoricalProfitLoss() {
        return historicalProfitLoss;
    }

    public void setHistoricalProfitLoss(double historicalProfitLoss) {
        this.historicalProfitLoss = historicalProfitLoss;
    }

    public double getTransactionsCosts() {
        return transactionsCosts;
    }

    public void setTransactionsCosts(double transactionsCosts) {
        this.transactionsCosts = transactionsCosts;
    }

    public double getWalletBalance() {
        return walletBalance;
    }

    public void setWalletBalance(double walletBalance) {
        this.walletBalance = walletBalance;
    }

    public Set<ClosedTransaction> getTransactionsHistory() {
        return transactionsHistory;
    }

    public void setTransactionsHistory(Set<ClosedTransaction> transactionsHistory) {
        this.transactionsHistory = transactionsHistory;
    }

    public Set<ActiveTransaction> getActiveTransactions() {
        return activeTransactions;
    }

    public void setActiveTransactions(Set<ActiveTransaction> activeTransactions) {
        this.activeTransactions = activeTransactions;
    }
}
