package application;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Application extends javafx.application.Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Application.class.getResource("mapview.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        //stage.setResizable(false);
        stage.setTitle("Dijkstra Route Finder");
        stage.setScene(scene);
//        stage.getIcons().add(new Image("file:icon.png"));
//        stage.getIcons().add(new Image(Application.class.getResourceAsStream("icon.png")));
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}