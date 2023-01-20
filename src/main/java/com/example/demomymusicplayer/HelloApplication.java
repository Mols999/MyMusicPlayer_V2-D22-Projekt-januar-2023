package com.example.demomymusicplayer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import java.io.IOException;


public class HelloApplication extends Application {
    @Override

    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 911, 480);
        stage.setTitle("Music-Player");
        stage.setScene(scene);
        stage.show();
        stage.getIcons().add(new Image("C:\\Users\\tfm\\IdeaProjects\\REALMusicPlayer\\target\\classes\\com\\example\\demomymusicplayer\\Photos\\TJS.png"));
    }
    public static void main(String[] args) {
        launch();
    }
}