/*******************************************************************************
 * StockD fetches EOD stock market data from Offical Stock exchange sites
 *     Copyright (C) 2020  Viresh Gupta
 *     More at https://github.com/virresh/StockD/
 * 
 *     This program is free software; you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation; either version 2 of the License, or
 *     (at your option) any later version.
 * 
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc.,
 *     51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 ******************************************************************************/
package fxcontrollers;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.logging.Level;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.controls.JFXTextArea;

import common.Constants;
import common.RunContext;
import downloads.Callback;
import downloads.PerformOnRange;
import downloads.ProgressUpdate;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import logging.DisplayMessageListener;
import logging.TextFieldHandler;
import main.FxApp;

public class MainWindowController implements Initializable, Callback, DisplayMessageListener, ProgressUpdate {
	
	private PerformOnRange thread;

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
    
    @FXML
    private JFXProgressBar pbar;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		FxApp.logger.addHandler(new TextFieldHandler(this));
		fromDate.setValue(LocalDate.now());
		toDate.setValue(LocalDate.now());
		internetCheck();
	}
	
	public void internetCheck() {
		String link = RunContext.getContext().getVersionCheckLink();
		try {
			URL u = new URL(link);
			Scanner s = new Scanner(u.openStream());
			if(s.hasNext()) {
				String x = s.next();
				if(Constants.isnewer(x)) {
					FxApp.logger.log(Level.INFO, "A newer version is available. Consider updating to get newest features.");
					UpdateLabel.setText("Available");
				}
				else {
					UpdateLabel.setText("Latest");
				}
				InternetStatus.setText("OK");
			}
			else {
				FxApp.logger.log(Level.INFO, "Could not check internet connectivity and version!");
				throw new IOException();
			}
			s.close();
		} catch (IOException e) {
			InternetStatus.setText("ERROR");
			FxApp.logger.log(Level.INFO, "Could not check internet connectivity and version!");
			e.printStackTrace();
		}
	}
	
	@FXML
	public void openAbout(ActionEvent event) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("/About.fxml"));
	        Parent otherroot = loader.load();
	        Stage stage = new Stage();
	        Stage primaryStage = (Stage) mainroot.getScene().getWindow();
	        stage.setScene(new Scene(otherroot));
	        
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(primaryStage);
	        stage.show();			
		}
        catch(Exception ex) {
        	FxApp.logger.log(Level.FINEST, "Unable to open about!");
        	ex.printStackTrace();
        }
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
		if(!preference_button.isDisable()) {
			preference_button.setDisable(true);
			try {
				RunContext.getContext().updateContext();
				thread = new PerformOnRange(fromDate.getValue(), toDate.getValue(), this, this);
				Thread t = new Thread(this.thread);
				t.start();
			} catch (Exception e) {
				FxApp.logger.log(Level.SEVERE, "Could not parse given link.");
				FxApp.logger.log(Level.FINEST, e.getMessage(), e);
				e.printStackTrace();
			}
		}
		else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("StockD - Download in Progress!");
			alert.setHeaderText("Another download is already in progress!");
			alert.setContentText("Please wait for it to finish or close the program to interrupt download.");
			alert.showAndWait();
		}
	}

	@Override
	public void callback() {
		preference_button.setDisable(false);
		FxApp.logger.log(Level.INFO, "Download Completed");
	}

	@Override
	public void newMessage(String message) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				messages.appendText(message);				
			}
		});		
	}
	
	public void shutdown(WindowEvent event) {
		if(preference_button.isDisable()) {
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("A download is in progress!");
			alert.setHeaderText("A download is in progress. Do you really want to quit?");
			alert.setContentText("If you confirm, the download will be terminated after it is done with current task.");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){
				this.thread.signalStop();
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				Platform.exit();
			} else {
			    event.consume();
			}
		}
		else {
			Platform.exit();
		}
	}

	@Override
	public void updateProgress(double current, double total) {
		this.pbar.setProgress(current / total);		
	}
}
