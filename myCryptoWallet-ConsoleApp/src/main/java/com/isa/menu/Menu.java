package com.isa.menu;

import com.isa.CoinSearch;
import com.isa.CoinsList;
import com.isa.Data;
import com.isa.Endpoints;

import java.util.InputMismatchException;
import java.util.Scanner;

public enum Menu {
    ADD_COIN(1, "Dodaj nowy coin"),
    SEARCH_COIN(2, "Wyszukaj coin z listy"),
    LIST_COINS(3, "Wyświetl listę coin-ów"),
    UPDATE_COIN_LIST (4, "Zaktualizuj listę coin–ów"),
    ADD_FAVOURITE_COIN (5, "Dodaj coin-a do listy ulubionych"),
    EXPORT_FILE (6, "Exportuj bibliotekę do pliku"),
    IMPORT_FILE (7, "Importuj bibliotekę z pliku"),
    EXIT (8, "Zakończ działanie programu \n  Wybierz opcję");



    private final int position;

    private final String description;

    Menu(final int position, final String description) {
        this.description = description;
        this.position = position;
    }

   public int getPosition() {
        return position;
    }

    String getDescription() {
        return description;
    }

    public static void printMenu() {
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n" +
                "myCryptoWallet \n" +
                "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@\n");
        for (Menu menu : Menu.values()) {
            System.out.println(menu);
        }

    }
    public static Menu getMenuItem(int number) {
        Menu menuToReturn = null;
        for (Menu menu : Menu.values()) {
            if (menu.getPosition() == number) {
                menuToReturn = menu;
            }
        }
        return menuToReturn;
    }

    public static void getMenu() {
        boolean flag = true;
        while (flag)
            try {
                Scanner sc = new Scanner(System.in);
                Menu.printMenu();
                int chooseOption = sc.nextInt();
                switch (chooseOption) {
                    case 1:
                        //System.out.println(Menu.ADD_COIN);
                        Endpoints.addCoin();
                        break;
                    case 2:
                        System.out.println(Menu.SEARCH_COIN);
                        CoinSearch.findYourToken();
                        break;
                    case 3:
                        // System.out.println(Menu.LIST_COINS);
                        CoinsList cL  = new CoinsList(2);
                        break;
                    case 4:
                        //System.out.println(Menu.UPDATE_COIN_LIST);
                        // aktualizuje plik coin.json
                        System.out.println("Aktualizauje listę kryptowalut...");
                        Data.updateCoinList();
                        break;
                    case 5:
                        System.out.println(Menu.ADD_FAVOURITE_COIN);
                        break;
                    case 6:
                        System.out.println(Menu.EXPORT_FILE);
                        //wczytuje zapisane wcześniej endpointy z pliku endpoints.json
                        Data.serializer(Endpoints.getEndpoints(), "endpoints.json");
                        break;
                    case 7:
                        System.out.println(Menu.IMPORT_FILE);
                        //importuje listę endpoints do pliku endpoints.json
                        Endpoints.setEndpoints();
                        break;
                    case 8:
                        flag = false;
                        break;
                    default:
                        System.out.println("Nie ma takiej opcji, spróbuj ponownie");
                }
            } catch (InputMismatchException e) {
                System.out.println("Podaj liczbę całkowitą");
            }

    }

    @Override
    public String toString() {
        return position +  " - " + description;
    }
}