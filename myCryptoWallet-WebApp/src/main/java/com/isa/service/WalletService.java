package com.isa.service;

import com.isa.control.Coin;
import com.isa.control.CoinSearch;
import com.isa.control.Data;
import com.isa.control.Wallet;
import com.isa.control.transactions.ActiveTransaction;
import com.isa.entity.User;
import com.isa.entity.WalletEntity;
import com.isa.mapper.WalletEntityMapper;
import com.isa.model.ActiveTransactionDto;
import com.isa.model.ClosedTransactionDto;
import com.isa.model.MapperToDto;
import com.isa.repository.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class WalletService {

    private Wallet wallet;
    private Coin coinForBuy = new Coin();
    private List<Coin> searchResult = new ArrayList<>();
    private ActiveTransaction transactionForClose;
    private ActiveTransaction transactionForChangeAttributes;
    private String userMail;

    private final WalletRepository walletRepository;
    private final ActiveTransactionRepository activeTransactionRepository;
    private final ClosedTransactionRepository closedTransactionRepository;


    public WalletService(WalletRepository walletRepository,ActiveTransactionRepository activeTransactionRepository, ClosedTransactionRepository closedTransactionRepository ){
       // Wallet wallet = Data.deserializeWallet();
        this.walletRepository = walletRepository;
        this.activeTransactionRepository = activeTransactionRepository;
        this.closedTransactionRepository = closedTransactionRepository;
    }

    public void findWalletByPrincipalName(String name){
        this.userMail = name;
        WalletEntity walletEntitiesByUserEmail = this.walletRepository.findWalletEntitiesByUserEmail(this.userMail);
        if (walletEntitiesByUserEmail == null) this.wallet = null;
        else this.wallet = WalletEntityMapper.mapWalletEntityToWallet(walletEntitiesByUserEmail);
    }

    public Set<ActiveTransactionDto> mapActiveTransactionsToDto(){
        return this.wallet.getActiveTransactions().stream()
                .map(MapperToDto::mapActiveTransactionToActiveTransactionDto).collect(Collectors.toSet());
    }

    public Set<ClosedTransactionDto> mapClosedTransactionsToDto(){
        return this.wallet.getTransactionsHistory().stream()
                .map(MapperToDto::mapClosedTransactionToClosedTransactionDto).collect(Collectors.toSet());
    }

    public void buyNewTokenForWallet(Coin coin, double volume){
        this.wallet.buyNewToken(coin, volume);
    }
    public void searchCoin(String coinSymbol){
        CoinSearch coinSearch = new CoinSearch();
        this.searchResult = coinSearch.search(coinSymbol);
    }

    public void addCoinForBuy(String coinSymbol){
        this.coinForBuy = searchResult.stream().filter(n->n.getSymbol().equals(coinSymbol)).findFirst().orElseThrow();
    }

    public void searchTransactionForClose(long transactionId){
        this.transactionForClose = wallet.searchActiveTransaction(transactionId);
    }

    public void searchTransactionForChangeAttributes(long transactionId){
        this.transactionForChangeAttributes = wallet.searchActiveTransaction(transactionId);
    }

    public void closeTransaction(double volume){
        this.wallet.closeActiveTransaction(transactionForClose, volume);
        this.transactionForClose = new ActiveTransaction();
    }

    public void setSlAndTpAlarm(double stopLoss, double takeProfit){
        transactionForChangeAttributes.setSLAlarm(stopLoss, true);
        transactionForChangeAttributes.setTPAlarm(takeProfit, true);
        wallet.getActiveTransactions().removeIf(n -> n.getIdTransaction() == transactionForChangeAttributes.getIdTransaction());
        wallet.getActiveTransactions().add(transactionForChangeAttributes);
        transactionForChangeAttributes = new ActiveTransaction();
    }

    public void topUpWallet(double amount){
        wallet.loadWalletBalance(amount);
        wallet.updateWallet();
    }

    public void withdrawalFoundsFromWallet(double amount){
        wallet.withdrawalFunds(amount);
        wallet.updateWallet();
    }

    public boolean checkIsPossibleToWithdrawalAmount(Double amount){
        return amount <= wallet.getWalletBalance() && amount > 0;
    }

    public void saveWalletToFile(){
        if (this.wallet != null) {
            wallet.updateWallet();
            WalletEntity walletEntity = WalletEntityMapper.mapWalletToEntity(wallet);


            WalletEntity currentWalletEntity = walletRepository.findWalletEntitiesByUserEmail(this.userMail);
        //Data.serializer(wallet, "wallet.json");

            currentWalletEntity.setPaymentCalc(walletEntity.getPaymentCalc());
            currentWalletEntity.setClosedTransactionEntities(walletEntity.getClosedTransactionEntities());
            currentWalletEntity.setHistoricalProfitLoss(walletEntity.getHistoricalProfitLoss());
            currentWalletEntity.setActiveTransactionEntityList(walletEntity.getActiveTransactionEntityList());
            currentWalletEntity.setWalletId(walletEntity.getWalletId());


            walletRepository.save(currentWalletEntity);
        }
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public Coin getCoinForBuy() {
        return coinForBuy;
    }

    public void setCoinForBuy(Coin coinForBuy) {
        this.coinForBuy = coinForBuy;
    }

    public List<Coin> getSearchResult() {
        return searchResult;
    }

    public void setSearchResult(List<Coin> searchResult) {
        this.searchResult = searchResult;
    }

    public ActiveTransaction getTransactionForClose() {
        return transactionForClose;
    }

    public void setTransactionForClose(ActiveTransaction transactionForClose) {
        this.transactionForClose = transactionForClose;
    }

    public ActiveTransaction getTransactionForChangeAttributes() {
        return transactionForChangeAttributes;
    }

    public void setTransactionForChangeAttributes(ActiveTransaction transactionForChangeAttributes) {
        this.transactionForChangeAttributes = transactionForChangeAttributes;
    }
}
