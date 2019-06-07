import javafx.event.EventHandler;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;

import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;

class CustomGameMenu extends PlayMenu {
    private static final String BACKGROUND_URL = "resources/menu/background/custom_game_background.jpg";
    private static final EventHandler<? super MouseEvent> BACK_EVENT = event -> SinglePlayerMenu.getInstance().show();
    private static CustomGameMenu menu;
    private static final PlayButtonItem[] items = {
            new PlayButtonItem("resources/menu/playButtons/kill_hero.jpg", "KILL HERO",
                    "You must kill opponent's hero to win", event -> menu.showKillHeroDialog()),
            new PlayButtonItem("resources/menu/playButtons/single_flag.jpg", "SINGLE FLAG",
                    "You must keep the flag for 6 turns to win", event -> menu.showSingleFlagDialog()),
            new PlayButtonItem("resources/menu/playButtons/multi_flag.jpg", "MULTI FLAG",
                    "You must collect at least half the flags to win", event -> menu.showMultiFlagDialog())
    };

    private String[] deckNames = {
            "strong deck", "spell deck", "custom deck", "alo", "slm", "sdfa", "gfh", "yth", "pkm", "lskdf", "skdfm", "ljf", "dff"
    };

    private CustomGameMenu() throws FileNotFoundException {
        super(items, BACKGROUND_URL, BACK_EVENT);
    }

    static CustomGameMenu getInstance() {
        if (menu == null) {
            try {
                menu = new CustomGameMenu();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return menu;
    }

    private void showKillHeroDialog() {
        DialogText text = new DialogText("Please choose one of your decks to be as opponent's deck");
        DeckListView listView = new DeckListView(deckNames);
        DialogBox dialogBox = new DialogBox(text, listView);
        DialogContainer dialogContainer = new DialogContainer(dialogBox);

        dialogBox.setButtonMouseEvent(buttonEvent -> {
            if (listView.getSelectionModel().getSelectedItem() == null) return;
            System.out.println(listView.getSelectionModel().getSelectedItem());
            //send data to server
            dialogContainer.closeFrom(root);
        });
        showDialog(dialogContainer);
        makeDialogClosable(dialogBox, dialogContainer);
    }

    private void showSingleFlagDialog() {
        DialogText text = new DialogText("Please choose one of your decks to be as opponent's deck");
        DeckListView listView = new DeckListView(deckNames);
        DialogBox dialogBox = new DialogBox(text, listView);
        DialogContainer dialogContainer = new DialogContainer(dialogBox);

        dialogBox.setButtonMouseEvent(buttonEvent -> {
            if (listView.getSelectionModel().getSelectedItem() == null) return;
            System.out.println("hello");
            //send data to server
            dialogContainer.closeFrom(root);
        });
        showDialog(dialogContainer);
        makeDialogClosable(dialogBox, dialogContainer);
    }

    private void showMultiFlagDialog() {
        DialogText deckText = new DialogText("Please choose one of your decks to be as opponent's deck");
        DeckListView listView = new DeckListView(deckNames);
        DialogText flagNumText = new DialogText("Please set number of flags in the game");
        NormalSpinner flagNumSpinner = new NormalSpinner(5, 30, 10);

        DialogBox dialogBox = new DialogBox(deckText, listView, flagNumText, flagNumSpinner);
        DialogContainer dialogContainer = new DialogContainer(dialogBox);

        dialogBox.setButtonMouseEvent(buttonEvent -> {
            if (listView.getSelectionModel().getSelectedItem() == null) return;
            //send data to server
            dialogContainer.closeFrom(root);
        });
        showDialog(dialogContainer);
        makeDialogClosable(dialogBox, dialogContainer);
    }

    private void showDialog(DialogContainer dialogContainer) {
        root.getChildren().get(0).setEffect(new GaussianBlur(Constants.ON_DIALOG_BLUR));
        root.getChildren().add(dialogContainer);
    }

    private void makeDialogClosable(DialogBox dialogBox, DialogContainer dialogContainer) {
        AtomicBoolean shouldBeClosed = new AtomicBoolean(true);
        dialogContainer.exitOnMouseClick(shouldBeClosed, root);
        dialogBox.preventClosingOnClick(shouldBeClosed);
    }
}