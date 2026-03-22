package app.product.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SceneManager {

    private static SceneManager instance;
    private Stage primaryStage;
    private final Map<String, Scene> scenes = new HashMap<>();

    public static final String LOGIN      = "login";
    public static final String SHOP       = "shop";
    public static final String DASHBOARD  = "dashboard";

    private SceneManager() {}

    public static SceneManager getInstance() {
        if (instance == null) {
            instance = new SceneManager();
        }
        return instance;
    }

    public void init(Stage stage) {
        this.primaryStage = stage;
    }

    public void loadAllScenes() throws IOException {
        loadScene(LOGIN,     "/AdminLogin.fxml",      900,  700);
        loadScene(SHOP,      "/CustomerShop.fxml",   1280,  900);
        loadScene(DASHBOARD, "/StaffDashboard.fxml", 1280,  900);
    }

    public void loadScene(String name, String fxmlPath, double w, double h) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
        Scene scene = new Scene(loader.load(), w, h);
        scene.getStylesheets().add(getClass().getResource("/styles/main.css").toExternalForm());
        scenes.put(name, scene);
    }

    public void switchTo(String name) {
        Scene scene = scenes.get(name);
        if (scene != null && primaryStage != null) {
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void reload(String name, double w, double h) throws IOException {
        String path = switch (name) {
            case LOGIN     -> "/AdminLogin.fxml";
            case SHOP      -> "/CustomerShop.fxml";
            case DASHBOARD -> "/StaffDashboard.fxml";
            default        -> throw new IllegalArgumentException("Unknown scene: " + name);
        };
        loadScene(name, path, w, h);
    }
}
