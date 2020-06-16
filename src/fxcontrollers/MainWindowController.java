package fxcontrollers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.FxApp;

public class MainWindowController implements Initializable {

    @FXML
    private AnchorPane mainroot;

    @FXML
    private Label UpdateLabel;

    @FXML
    private Label InternetStatus;
    

    @FXML
    private MenuItem preference_button;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
	}
	
	public void openSettings(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/SettingsWindow.fxml"));
	        Parent otherroot = loader.load();
	        Stage stage = new Stage();
	        stage.setScene(new Scene(otherroot));
	        stage.show();
	        SettingWindowController prefController = loader.getController();
	        
	        // reference: https://stackoverflow.com/a/52234104/9374197
	        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, prefController::shutdown);
	    } catch(Exception e) {
	    	e.printStackTrace();
	        FxApp.logger.log(Level.SEVERE, "Cannot open Preferences Window!");
	        FxApp.logger.log(Level.SEVERE, e.getMessage());
	    }
	}

}
