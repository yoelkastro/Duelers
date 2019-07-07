package models.message;

public enum MessageType {
    //SENDER:SERVER
    OPPONENT_INFO,
    SEND_EXCEPTION,
    ACCOUNT_COPY,
    GAME_COPY,
    ORIGINAL_CARDS_COPY,
    LEADERBOARD_COPY,
    STORIES_COPY,
    CARD_POSITION,
    TROOP_UPDATE,
    GAME_UPDATE,
    Game_FINISH,
    DONE,
    ANIMATION,

    //SENDER:CLIENT
    GET_DATA,
    MOVE_TROOP,
    CREATE_DECK,
    REMOVE_DECK,
    ADD_TO_DECK,
    REMOVE_FROM_DECK,
    SELECT_DECK,
    BUY_CARD,
    SELL_CARD,
    INSERT,
    ATTACK,
    COMBO,
    USE_SPECIAL_POWER,
    END_TURN,
    LOG_IN,
    LOG_OUT,
    REGISTER,
    NEW_MULTIPLAYER_GAME,
    NEW_DECK_GAME,
    NEW_STORY_GAME,
    SELECT_USER,
    SUDO,

    //SENDER:DUAL
    CHAT,

    ADD_CARD,
    IMPORT_DECK,
    FORCE_FINISH
}