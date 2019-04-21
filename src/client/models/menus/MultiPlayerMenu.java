package client.models.menus;

import client.Client;
import client.models.account.Account;

public class MultiPlayerMenu implements Menu {
    private static final MultiPlayerMenu MULTI_PLAYER_MENU = new MultiPlayerMenu();
    private Account secondAccount;
    private MultiPlayerMenu() {

    }

    public static MultiPlayerMenu getInstance() {
        return MULTI_PLAYER_MENU;
    }

    public void selectUser(String userName) {

    }

    public void startGame(String mode,int numberOfFlags) {

    }

    @Override
    public void exit(Client client) {

    }
}
