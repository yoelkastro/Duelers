package server.gameCenter;

import server.Server;
import server.clientPortal.models.message.Message;
import server.dataCenter.DataCenter;
import server.dataCenter.models.account.Account;
import server.dataCenter.models.account.MatchHistory;
import server.dataCenter.models.card.Deck;
import server.exceptions.ClientException;
import server.exceptions.LogicException;
import server.exceptions.ServerException;
import server.gameCenter.models.GlobalRequest;
import server.gameCenter.models.UserInvitation;
import server.gameCenter.models.game.*;
import server.gameCenter.models.map.GameMap;

import java.util.HashMap;
import java.util.LinkedList;

public class GameCenter extends Thread {//synchronize
    private static GameCenter ourInstance = new GameCenter();

    public static GameCenter getInstance() {
        return ourInstance;
    }

    private HashMap<Account, Game> onlineGames = new HashMap<>();//Account -> Game
    private final LinkedList<GlobalRequest> globalRequests = new LinkedList<>();
    private final LinkedList<UserInvitation> userInvitations = new LinkedList<>();

    private GameCenter() {
    }


    @Override
    public void run() {
        Server.getInstance().serverPrint("Starting GameCenter...");
    }

    private Game getGame(String clientName) throws ClientException {
        Account account = DataCenter.getInstance().getClients().get(clientName);
        if (account == null) {
            throw new ClientException("your client hasn't logged in!");
        }
        Game game = onlineGames.get(account);
        if (game == null) {
            throw new ClientException("you don't have online game!");
        }
        return game;
    }

    public void getMultiPlayerGameRequest(Message message) throws LogicException {
        DataCenter.getInstance().loginCheck(message.getSender());
        Account account1 = DataCenter.getInstance().getClients().get(message.getSender());
        if (onlineGames.get(account1) != null)
            throw new ClientException("You have online game!");
        if (!account1.hasValidMainDeck())
            throw new ClientException("You don't have valid main deck");
        if (message.getNewGameFields() == null || message.getNewGameFields().getGameType() == null)
            throw new ClientException("Invalid Request");
        if (message.getNewGameFields().getOpponentUsername() == null) {
            addGlobalRequest(account1, message.getNewGameFields().getGameType(), message.getNewGameFields().getNumberOfFlags());
        } else {
            Account account2 = DataCenter.getInstance().getAccount(message.getNewGameFields().getOpponentUsername());
            checkOpponentAccountValidation(account2);
            addUserInvitation(account1, account2, message.getNewGameFields().getGameType(), message.getNewGameFields().getNumberOfFlags());
        }
    }

    private void addGlobalRequest(Account account, GameType gameType, int numberOfFlags) {
        removeAllGameRequests(account);
        synchronized (globalRequests) {
            for (GlobalRequest globalRequest : globalRequests) {
                if (globalRequest.getNumberOfFlags() == numberOfFlags && globalRequest.getGameType() == gameType) {
                    newMultiplayerGame(globalRequest.getRequester(), account, gameType, numberOfFlags);
                    globalRequests.remove(globalRequest);
                    return;
                }
            }
            globalRequests.addLast(new GlobalRequest(account, gameType, numberOfFlags));
        }
    }

    private void addUserInvitation(Account inviter, Account invited, GameType gameType, int numberOfFlags) {
        removeAllGameRequests(inviter);
        synchronized (userInvitations) {
            userInvitations.addLast(new UserInvitation(inviter, invited, gameType, numberOfFlags));
            Server.getInstance().addToSendingMessages(Message.makeInvitationMessage(
                    Server.getInstance().serverName, DataCenter.getInstance().getAccounts().get(invited), inviter.getUsername(),
                    gameType, numberOfFlags));
        }
    }

    public void removeAllGameRequests(Account account) {
        synchronized (globalRequests) {
            for (GlobalRequest globalRequest : globalRequests) {
                if (globalRequest.getRequester() == account) {
                    globalRequests.remove(globalRequest);
                    break;
                }
            }
        }
        synchronized (userInvitations) {
            for (UserInvitation userInvitation : userInvitations) {
                if (userInvitation.getInviter() == account) {
                    userInvitations.remove(userInvitation);
                    break;
                }
            }
        }
    }

    private UserInvitation getUserInvitation(Account inviter) {
        for (UserInvitation userInvitation : userInvitations) {
            if (userInvitation.getInviter() == inviter)
                return userInvitation;
        }
        return null;
    }

    public void getAcceptRequest(Message message) throws LogicException {
        DataCenter.getInstance().loginCheck(message.getSender());
        Account invited = DataCenter.getInstance().getClients().get(message.getSender());
        synchronized (userInvitations) {
            if (message.getNewGameFields() == null || message.getNewGameFields().getOpponentUsername() == null)
                throw new ClientException("invalid accept message!");
            Account inviter = DataCenter.getInstance().getAccount(message.getNewGameFields().getOpponentUsername());
            if (inviter == null)
                throw new ClientException("invalid opponent username!");
            UserInvitation invitation = getUserInvitation(inviter);
            if (invitation == null)
                throw new ClientException("The Invitation was not found!");
            userInvitations.remove(invitation);
            Server.getInstance().addToSendingMessages(Message.makeAcceptRequestMessage(
                    Server.getInstance().serverName, DataCenter.getInstance().getAccounts().get(inviter)));
            newMultiplayerGame(inviter, invited, invitation.getGameType(), invitation.getNumberOfFlags());

        }
    }


    public void getDeclineRequest(Message message) throws LogicException {
        DataCenter.getInstance().loginCheck(message.getSender());
        Account account = DataCenter.getInstance().getClients().get(message.getSender());
        synchronized (userInvitations) {

        }
    }

    public void getCancelRequest(Message message) throws LogicException {
        DataCenter.getInstance().loginCheck(message.getSender());
        Account account = DataCenter.getInstance().getClients().get(message.getSender());
        removeAllGameRequests(account);
    }

    private void checkOpponentAccountValidation(Account opponentAccount) throws LogicException {
        if (opponentAccount == null) {
            throw new ClientException("invalid opponent username!");
        }
        if (DataCenter.getInstance().getAccounts().get(opponentAccount) == null) {
            throw new ClientException("opponentAccount is not online!");
        }
        if (!opponentAccount.hasValidMainDeck()) {
            throw new ClientException("opponent doesn't have valid main deck");
        }
        if (onlineGames.get(opponentAccount) != null) {
            throw new ClientException("opponentAccount has online game!");
        }
        //TODO:Validation
    }

    public void newDeckGame(Message message) throws LogicException {
        DataCenter.getInstance().loginCheck(message);
        Account myAccount = DataCenter.getInstance().getClients().get(message.getSender());
        removeAllGameRequests(myAccount);
        if (!myAccount.hasValidMainDeck()) {
            throw new ClientException("you don't have valid main deck!");
        }
        if (onlineGames.get(myAccount) != null) {
            throw new ClientException("you have online game!");
        }
        Deck deck = new Deck(myAccount.getDeck(message.getNewGameFields().getCustomDeckName()));
        deck.makeCustomGameDeck();
        if (deck == null || !deck.isValid()) {
            throw new ClientException("selected deck is not valid");
        }
        Game game = null;
        GameMap gameMap = new GameMap(DataCenter.getInstance().getCollectibleItems(), message.getNewGameFields().getNumberOfFlags(), DataCenter.getInstance().getOriginalFlag());
        switch (message.getNewGameFields().getGameType()) {
            case KILL_HERO:
                game = new KillHeroBattle(myAccount, deck, gameMap);
                break;
            case A_FLAG:
                game = new SingleFlagBattle(myAccount, deck, gameMap);
                break;
            case SOME_FLAG:
                game = new MultiFlagBattle(myAccount, deck, gameMap, message.getNewGameFields().getNumberOfFlags());
                break;
        }
        game.setReward(Game.getDefaultReward());
        onlineGames.put(myAccount, game);
        Server.getInstance().addToSendingMessages(Message.makeGameCopyMessage
                (Server.getInstance().serverName, message.getSender(), game, 0));
        game.startGame();
    }

    public void newStoryGame(Message message) throws LogicException {
        DataCenter.getInstance().loginCheck(message);
        Account myAccount = DataCenter.getInstance().getClients().get(message.getSender());
        removeAllGameRequests(myAccount);
        if (!myAccount.hasValidMainDeck()) {
            throw new ClientException("you don't have valid main deck!");
        }
        if (onlineGames.get(myAccount) != null) {
            throw new ClientException("you have online game!");
        }
        Game game = null;
        Story story = DataCenter.getInstance().getStories().get(message.getNewGameFields().getStage());
        GameMap gameMap = new GameMap(DataCenter.getInstance().getCollectibleItems(), story.getNumberOfFlags(), DataCenter.getInstance().getOriginalFlag());
        switch (story.getGameType()) {
            case KILL_HERO:
                game = new KillHeroBattle(myAccount, story.getDeck(), gameMap);
                break;
            case A_FLAG:
                game = new SingleFlagBattle(myAccount, story.getDeck(), gameMap);
                break;
            case SOME_FLAG:
                game = new MultiFlagBattle(myAccount, story.getDeck(), gameMap, story.getNumberOfFlags());
                break;
        }
        game.setReward(story.getReward());
        onlineGames.put(myAccount, game);
        Server.getInstance().addToSendingMessages(Message.makeGameCopyMessage
                (Server.getInstance().serverName, message.getSender(), game, 0));
        game.startGame();
    }

    private void newMultiplayerGame(Account account1, Account account2, GameType gameType, int numberOfFlags) {

        removeAllGameRequests(account1);
        removeAllGameRequests(account2);
        Game game = null;
        GameMap gameMap = new GameMap(DataCenter.getInstance().getCollectibleItems(), numberOfFlags, DataCenter.getInstance().getOriginalFlag());
        switch (gameType) {
            case KILL_HERO:
                game = new KillHeroBattle(account1, account2, gameMap);
                break;
            case A_FLAG:
                game = new SingleFlagBattle(account1, account2, gameMap);
                break;
            case SOME_FLAG:
                game = new MultiFlagBattle(account1, account2, gameMap, numberOfFlags);
                break;
        }
        game.setReward(Game.getDefaultReward());
        onlineGames.put(account1, game);
        onlineGames.put(account2, game);
        Server.getInstance().addToSendingMessages(Message.makeGameCopyMessage
                (Server.getInstance().serverName, DataCenter.getInstance().getClientName(account1.getUsername()), game, 0));
        Server.getInstance().addToSendingMessages(Message.makeGameCopyMessage
                (Server.getInstance().serverName, DataCenter.getInstance().getClientName(account2.getUsername()), game, 0));
        try {
            game.startGame();
        } catch (ServerException e) {
            Server.getInstance().serverPrint("game start error in multiPlayer");
        }

    }

    private void removeGame(Game game) {
        if (!game.getPlayerOne().getUserName().equalsIgnoreCase("AI")) {
            Account account1 = DataCenter.getInstance().getAccount(game.getPlayerOne().getUserName());
            if (account1 != null) {
                onlineGames.remove(account1);
            }
        }
        if (!game.getPlayerTwo().getUserName().equalsIgnoreCase("AI")) {
            Account account2 = DataCenter.getInstance().getAccount(game.getPlayerTwo().getUserName());
            if (account2 != null) {
                onlineGames.remove(account2);
                //DataCenter.getInstance().getAccounts().replace(account2, null);
            }
        }
        // DataCenter.getInstance().getClients().replace(onlineClients.get(1).getClientName(), null);
    }

    public void insertCard(Message message) throws LogicException {
        Game game = getGame(message.getSender());
        try {
            game.insert(DataCenter.getInstance().getClients().get(message.getSender()).getUsername(), message.getOtherFields().getMyCardId(), message.getOtherFields().getPosition());
            Server.getInstance().sendGameUpdateMessage(game);
        } finally {
            checkGameFinish(game);
        }
    }

    public void attack(Message message) throws LogicException {
        Game game = getGame(message.getSender());
        try {
            game.attack(DataCenter.getInstance().getClients().get(message.getSender()).getUsername(), message.getOtherFields().getMyCardId(), message.getOtherFields().getOpponentCardId());
        } finally {
            checkGameFinish(game);
        }
    }

    public void combo(Message message) throws LogicException {
        Game game = getGame(message.getSender());
        try {
            game.comboAttack(DataCenter.getInstance().getClients().get(message.getSender()).getUsername(), message.getOtherFields().getMyCardIds(), message.getOtherFields().getOpponentCardId());

        } finally {
            checkGameFinish(game);

        }
    }

    public void useSpecialPower(Message message) throws LogicException {
        Game game = getGame(message.getSender());
        try {
            game.useSpecialPower(DataCenter.getInstance().getClients().get(message.getSender()).getUsername(), message.getOtherFields().getMyCardId(), message.getOtherFields().getPosition());
            Server.getInstance().sendGameUpdateMessage(game);
        } finally {
            checkGameFinish(game);

        }
    }

    public void moveTroop(Message message) throws LogicException {
        Game game = getGame(message.getSender());
        try {
            game.moveTroop(DataCenter.getInstance().getClients().get(message.getSender()).getUsername(), message.getOtherFields().getMyCardId(), message.getOtherFields().getPosition());

        } finally {
            checkGameFinish(game);

        }
    }

    public void endTurn(Message message) throws LogicException {
        Game game = getGame(message.getSender());
        try {
            game.changeTurn(DataCenter.getInstance().getClients().get(message.getSender()).getUsername());

        } finally {
            checkGameFinish(game);

        }
    }

    private void checkGameFinish(Game game) {
        if (game.finishCheck()) {
            finish(game);
        }
    }

    private void finish(Game game) {
        MatchHistory playerOneHistory = game.getPlayerOne().getMatchHistory();
        MatchHistory playerTwoHistory = game.getPlayerTwo().getMatchHistory();
        if (!game.getPlayerOne().getUserName().equalsIgnoreCase("AI")) {
            Account account = DataCenter.getInstance().getAccount(game.getPlayerOne().getUserName());
            if (account == null)
                Server.getInstance().serverPrint("Error");
            else {
                account.addMatchHistory(playerOneHistory, game.getReward());
                DataCenter.getInstance().saveAccount(account);
            }
        }
        if (!game.getPlayerTwo().getUserName().equalsIgnoreCase("AI")) {
            Account account = DataCenter.getInstance().getAccount(game.getPlayerTwo().getUserName());
            if (account == null)
                Server.getInstance().serverPrint("Error");
            else {
                account.addMatchHistory(playerTwoHistory, game.getReward());
                DataCenter.getInstance().saveAccount(account);
            }
        }
        Server.getInstance().sendGameFinishMessages(game);
        removeGame(game);
    }

    public void forceFinishGame(String sender) throws LogicException {
        Game game = getGame(sender);

        if (game == null) {
            Server.getInstance().serverPrint("Error forceGameFinish!");
            return;
        }
        DataCenter.getInstance().loginCheck(sender);
        String username = DataCenter.getInstance().getClients().get(sender).getUsername();

        game.forceFinish(username);
        finish(game);


    }
}
