package fxcontrollers;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTextArea;

import common.RunContext;
import downloads.Callback;
import downloads.FileDownloader;
import downloads.PerformDay;
import downloads.PerformOnRange;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import logging.TextFieldHandler;
import main.FxApp;
import parsers.ParseFO;

public class MainWindowController implements Initializable, Callback {

    @FXML
    private AnchorPane mainroot;

    @FXML
    private Label UpdateLabel;

    @FXML
    private Label InternetStatus;
    
    @FXML
    private JFXTextArea messages;

    @FXML
    private MenuItem preference_button;
    
    @FXML
    private JFXDatePicker fromDate;

    @FXML
    private JFXDatePicker toDate;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
//		FxApp.logger.addHandler(new TextFieldHandler(messages));
		fromDate.setValue(LocalDate.now());
		toDate.setValue(LocalDate.now());
	}
	
	public void openSettings(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/SettingsWindow.fxml"));
	        Parent otherroot = loader.load();
	        Stage stage = new Stage();
	        Stage primaryStage = (Stage) mainroot.getScene().getWindow();
	        stage.setScene(new Scene(otherroot));
	        
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(primaryStage);
	        
	        SettingWindowController prefController = loader.getController();
	        
	        // reference: https://stackoverflow.com/a/52234104/9374197
	        stage.getScene().getWindow().addEventFilter(WindowEvent.WINDOW_CLOSE_REQUEST, prefController::shutdown);
	        
	        stage.show();

	    } catch(Exception e) {
	    	e.printStackTrace();
	        FxApp.logger.log(Level.SEVERE, "Cannot open Preferences Window!");
	        FxApp.logger.log(Level.FINEST, e.getMessage(), e);
	    }
	}
	
	public void startdownload(ActionEvent event) {
		preference_button.setDisable(true);
		try {
			RunContext.getContext().updateContext();
			Thread t = new Thread(new PerformOnRange(fromDate.getValue(), toDate.getValue(), this));
			t.start();
		} catch (Exception e) {
			FxApp.logger.log(Level.SEVERE, "Could not parse given link.");
			FxApp.logger.log(Level.FINEST, e.getMessage(), e);
			e.printStackTrace();
		}
	}

	@Override
	public void callback() {
		preference_button.setDisable(false);
		FxApp.logger.log(Level.INFO, "Download Completed");
	}

}
