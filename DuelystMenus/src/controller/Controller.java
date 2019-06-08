package controller;

import models.Constants;
import view.LoginMenu;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class Controller extends Application {
    private static Stage stage;

    public static void setScene(Scene scene) {
        scene.setCursor(Constants.DEFAULT_CURSOR);
        stage.setScene(scene);
    }

    @Override
    public void start(Stage primaryStage) {
        stage = primaryStage;

        LoginMenu.getInstance().show();

        stage.setTitle("DUELYST");
        stage.setResizable(false);
        stage.show();

        System.out.println(Font.getFamilies());
    }
}