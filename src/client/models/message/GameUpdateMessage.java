package client.models.message;

public class GameUpdateMessage {
    private int turnNumber;
    private int player1CurrentMP;
    private int player1NumberOfCollectedFlags;
    private int player2CurrentMP;
    private int player2NumberOfCollectedFlags;

    public int getTurnNumber() {
        return turnNumber;
    }

    public int getPlayer1CurrentMP() {
        return player1CurrentMP;
    }

    public int getPlayer1NumberOfCollectedFlags() {
        return player1NumberOfCollectedFlags;
    }

    public int getPlayer2CurrentMP() {
        return player2CurrentMP;
    }

    public int getPlayer2NumberOfCollectedFlags() {
        return player2NumberOfCollectedFlags;
    }
}
