import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.io.FileNotFoundException;

class LoginMenu {
    private static final Background LOGIN_BOX_BACKGROUND = new Background(
            new BackgroundFill(
                    Color.rgb(100, 100, 100, 0.6), CornerRadii.EMPTY, Insets.EMPTY
            )
    );
    private static final Background ABABEEL_BACKGROUND = new Background(
            new BackgroundFill(Color.rgb(15, 16, 11), CornerRadii.EMPTY, Insets.EMPTY)
    );
    private static final Paint ABABEEL_TEXT_COLOR = Color.rgb(102, 166, 214);
    private static final String DUELYST_LOGO_URL = "resources/ui/brand_duelyst.png";
    private static final String ABABEEL_URL = "resources/ui/logo.png";
    private static AnchorPane root = new AnchorPane();
    private static Scene scene = new Scene(root, Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT);

    void show() throws FileNotFoundException {
        Main.setScene(scene);
        root.setBackground(Constants.ROOT_BACKGROUND);

        BorderPane background = BackgroundMaker.makeMenuBackground();
        background.setEffect(new GaussianBlur(Constants.BACKGROUND_BLUR));
        BorderPane container = makeContainer();

        root.getChildren().addAll(background, container);
    }

    private BorderPane makeContainer() throws FileNotFoundException {
        BorderPane container = new BorderPane();
        container.setMinSize(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT);
        container.setMaxSize(Constants.SCENE_WIDTH, Constants.SCENE_HEIGHT);
        BorderPane.setAlignment(container, Pos.CENTER);

        VBox verticalBox = makeVerticalBox();

        container.setCenter(verticalBox);
        return container;
    }

    private VBox makeVerticalBox() throws FileNotFoundException {
        VBox verticalBox = new VBox(Constants.DEFAULT_SPACING);
        verticalBox.setMaxWidth(Constants.LOGIN_BOX_SIZE * 2);
        verticalBox.setAlignment(Pos.CENTER);

        ImageView brandView = ImageLoader.loadImage(
                DUELYST_LOGO_URL, Constants.DUELYST_LOGO_WIDTH, Constants.DUELYST_LOGO_HEIGHT
        );
        HBox horizontalBox = makeHorizontalBox();

        verticalBox.getChildren().addAll(brandView, horizontalBox);
        return verticalBox;
    }

    private HBox makeHorizontalBox() throws FileNotFoundException {
        HBox horizontalBox = new HBox();

        VBox logoBox = makeAbabeelLogoBox();
        VBox loginBox = makeLoginBox();

        horizontalBox.getChildren().addAll(logoBox, loginBox);
        return horizontalBox;
    }

    private VBox makeLoginBox() {
        TextField usernameField = TextFieldMaker.makeDefaultTextField("username");
        PasswordField passwordField = TextFieldMaker.makePasswordField();

        Region space = new Region();
        space.setMinHeight(Constants.LOGIN_BOX_SIZE * 0.5);

        HBox buttons = makeButtonsBox();

        VBox loginBox = new VBox(Constants.DEFAULT_SPACING * 2, usernameField, passwordField, space, buttons);
        loginBox.setPadding(Constants.LOGIN_BOX_PADDING);
        loginBox.setBackground(LOGIN_BOX_BACKGROUND);
        loginBox.setMinSize(Constants.LOGIN_BOX_SIZE, Constants.LOGIN_BOX_SIZE);
        loginBox.setMaxSize(Constants.LOGIN_BOX_SIZE, Constants.LOGIN_BOX_SIZE);

        return loginBox;
    }

    private HBox makeButtonsBox() {
        Button loginButton = ButtonMaker.makeLoginButton("LOG IN");

        loginButton.setOnMouseClicked(event -> {
            try {
                new MainMenu().show();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });

        Button registerButton = ButtonMaker.makeLoginButton("REGISTER");
        return new HBox(Constants.DEFAULT_SPACING, loginButton, registerButton);
    }

    private VBox makeAbabeelLogoBox() throws FileNotFoundException {
        ImageView logoView = ImageLoader.loadImage(ABABEEL_URL, Constants.LOGO_WIDTH, Constants.LOGO_HEIGHT);
        Text text = makeAbabeelText();

        VBox logoBox = new VBox(Constants.DEFAULT_SPACING * 10, logoView, text);
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setBackground(ABABEEL_BACKGROUND);
        logoBox.setMinSize(Constants.LOGIN_BOX_SIZE, Constants.LOGIN_BOX_SIZE);
        logoBox.setMaxSize(Constants.LOGIN_BOX_SIZE, Constants.LOGIN_BOX_SIZE);
        return logoBox;
    }

    private Text makeAbabeelText() {
        Text text = new Text("LOGIN TO DUELYST USING YOUR ABABEEL ACCOUNT");
        text.setFill(ABABEEL_TEXT_COLOR);
        text.setWrappingWidth(Constants.LOGIN_BOX_SIZE - Constants.DEFAULT_SPACING * 5);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Constants.LOGO_TEXT_FONT);
        return text;
    }
}