package server.models.game;

import server.models.exceptions.ClientException;
import server.models.account.Account;
import server.models.account.MatchHistory;
import server.models.card.Card;
import server.models.card.CardType;
import server.models.card.Deck;
import server.models.map.Cell;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

public class Player {
    private String userName;
    private int currentMP;
    private Deck deck;
    private Troop hero;
    private ArrayList<Card> hand = new ArrayList<>();
    private ArrayList<Troop> troops = new ArrayList<>();
    private ArrayList<Card> graveyard = new ArrayList<>();
    private Card nextCard;
    private ArrayList<Card> collectedItems = new ArrayList<>();
    private ArrayList<Troop> flagCarriers = new ArrayList<>();
    private int playerNumber;
    private int numberOfCollectedFlags;
    private MatchHistory matchHistory;

    public Player(Account account, int playerNumber) {
        this.playerNumber = playerNumber;
        userName = account.getUsername();
        deck = new Deck(account.getMainDeck());
        setNextCard();
        for (int i = 0; i < 5; i++) {
            addNextCardToHand();
        }
        hero = new Troop(deck.getHero(), playerNumber);
        troops.add(hero);
    }

    public Troop insert(String cardId) throws ClientException {
        Card card = null;
        Iterator iterator = hand.iterator();
        while (iterator.hasNext()) {
            Card card1 = (Card) iterator.next();
            if (card1.getCardId().equals(cardId)) {
                card = card1;
                iterator.remove();
                break;
            }
        }

        if (card == null)
            throw new ClientException("card id is not valid");

        if (card.getMannaPoint() > currentMP)
            throw new ClientException("not enough manna point");


        currentMP -= card.getMannaPoint();

        if (card.getType() == CardType.MINION) {
            Troop troop = new Troop(card, playerNumber);
            troops.add(troop);
            return troop;
        }
        return null;
    }

    public void setNextCard() {
        int index = new Random().nextInt(deck.getOthers().size());
        nextCard = deck.getOthers().get(index);
        deck.getOthers().remove(nextCard);
    }

    public void addNextCardToHand() {
        if (hand.size() <= 5) {
            hand.add(nextCard);
            setNextCard();
        }
    }

    public String getUserName() {
        return this.userName;
    }

    public int getCurrentMP() {
        return this.currentMP;
    }

    public void setCurrentMP(int currentMP) {
        this.currentMP = currentMP;
    }

    public ArrayList<Troop> getFlagCarriers() {
        return flagCarriers;
    }

    public void addFlagCarrier(Troop troop) {
        if (!this.flagCarriers.contains(troop))
            this.flagCarriers.add(troop);
    }

    public void removeFlagCarrier(Troop troop) {
        flagCarriers.remove(troop);
    }

    public void changeCurrentMP(int change) {

    }

    public int getPlayerNumber() {
        return playerNumber;
    }

    public Deck getDeck() {
        return this.deck;
    }

    public Card collectFromDeck() {
        return null;
    }


    public void addToHand(Card card) {

    }


    public Card collectFromHand(String cardId) {
        return null;
    }

    public ArrayList<Troop> getTroops() {
        return this.troops;
    }

    public ArrayList<Card> getGraveyard() {
        return this.graveyard;
    }


    public void addToGraveYard(Card card) {

    }

    public Card getNextCard() {
        return this.nextCard;
    }


    public void setNextCard(Card nextCard) {
        this.nextCard = nextCard;
    }

    public ArrayList<Card> getCollectedItems() {
        return this.collectedItems;
    }

    public void addCollectibleItems(Card card){

    }

    public Troop getTroop(Cell cell) {
        for (Troop troop : troops) {
            if (troop.getCell().equals(cell)) {
                return troop;
            }
        }
        return null;
    }

    public Troop getTroop(String cardId) {
        for (Troop troop : troops) {
            if (troop.getCard().getCardId().equalsIgnoreCase(cardId)) {
                return troop;
            }
        }
        return null;
    }

    public Troop getHero() {
        return hero;
    }

    public void setHero(Troop hero) {
        this.hero = hero;
    }

    public void killTroop(Troop troop) {
        addToGraveYard(troop.getCard());
        troops.remove(troop);
    }

    public int getNumberOfCollectedFlags() {
        return numberOfCollectedFlags;
    }

    public void increaseNumberOfCollectedFlags() {
        this.numberOfCollectedFlags++;
    }

    public void decreaseNumberOfCollectedFlags() {
        this.numberOfCollectedFlags--;
    }

    public void setMatchHistory(MatchHistory matchHistory) {
        this.matchHistory = matchHistory;
    }
}