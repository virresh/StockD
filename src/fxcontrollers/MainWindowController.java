package fxcontrollers;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.jfoenix.controls.JFXTextArea;

import common.RunContext;
import downloads.FileDownloader;
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

public class MainWindowController implements Initializable {

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

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		FxApp.logger.addHandler(new TextFieldHandler(messages));
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
		try {
			RunContext rt = RunContext.getContext();
//			ParseEQ p = new ParseEQ("./Equity/");
//			ParseIndices p = new ParseIndices("./Indices");
			ParseFO p = new ParseFO("./Futures");
			FileDownloader fd = new FileDownloader(rt.getTemp().getAbsolutePath());
//			String dlink = "https://archives.nseindia.com/content/historical/EQUITIES/2020/MAY/cm05MAY2020bhav.csv.zip";
			String dlink = "https://archives.nseindia.com/content/historical/DERIVATIVES/2020/JUN/fo24JUN2020bhav.csv.zip";
//			String dlink = "https://archives.nseindia.com/content/indices/ind_close_all_23062020.csv";
//			String dlink = "https://www.nseindia.com/api/reports?archives=%5B%7B%22name%22%3A%22F%26O%20-%20Bhavcopy(csv)%22%2C%22type%22%3A%22archives%22%2C%22category%22%3A%22derivatives%22%2C%22section%22%3A%22equity%22%7D%5D&date=03-Jun-2020&type=equity&mode=single";
			fd.DownloadFile(dlink, p);
		} catch (Exception e) {
			FxApp.logger.log(Level.SEVERE, "Could not parse given link.");
			FxApp.logger.log(Level.FINEST, e.getMessage(), e);
			e.printStackTrace();
		}
		FxApp.logger.log(Level.INFO, "Download Completed");
	}

}
