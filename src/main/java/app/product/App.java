package app.product;
 
import app.product.util.DatabaseManager;
import app.product.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;
 
public class App extends Application {
 
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Initialise DB FIRST — creates tables and seeds data
        DatabaseManager.getInstance();
 
        SceneManager sm = SceneManager.getInstance();
        sm.init(primaryStage);
        sm.loadAllScenes();
 
        primaryStage.setTitle("NOCH");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        sm.switchTo(SceneManager.LOGIN);
        primaryStage.show();
    }
 
    @Override
    public void stop() {
        // Close DB connection cleanly on app exit
        DatabaseManager.getInstance().close();
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}