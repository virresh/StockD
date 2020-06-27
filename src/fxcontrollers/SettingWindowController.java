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

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.logging.Level;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXTabPane;

import common.JSONUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import main.FxApp;
import models.BaseLink;
import models.ConfigurationWrapper;
import models.Link;
import models.Setting;

public class SettingWindowController implements Initializable {
	
	private static Boolean changed=false;

    @FXML
    private JFXTabPane configuration_tab;
    
    @FXML
    private FlowPane index;

    @FXML
    private FlowPane equity;

    @FXML
    private FlowPane futures;

    @FXML
    private FlowPane currency;

    @FXML
    private FlowPane options;
    
    @FXML
    private FlowPane others;
    
    @FXML
    private JFXComboBox<String> profileurl;

    @FXML
    private Button loadlinkprofile;

    @FXML
    private Button resetall;

    @FXML
    private VBox currentprofile;

    
    private Node make_chkbox(Setting s) {
		JFXCheckBox chkbox = new JFXCheckBox(s.getSETTING_NAME());
		chkbox.setTooltip(new Tooltip(s.getSETTING_NAME()));
		chkbox.setPadding(new Insets(10));
		chkbox.setSelected(s.getSETTING_VALUE().equals("true"));
		chkbox.setOnAction(
				new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						SettingWindowController.changed = true;
						if(chkbox.isSelected()) {
							s.setSETTING_VALUE("true");
						}
						else {
							s.setSETTING_VALUE("false");
						}
					}
				});
		return chkbox;
    }
    
    private Node make_directory(Setting s) {
    	FlowPane labelCombo = new FlowPane(20.0, 5.0);
    	
    	TextField txtfield = new TextField(s.getSETTING_VALUE());
    	txtfield.setTooltip(new Tooltip(s.getSETTING_NAME()));
    	txtfield.setPadding(new Insets(10));
    	Label fieldname = new Label(s.getSETTING_NAME());
    	DirectoryChooser fchoose = new DirectoryChooser();
    	Button bchoose = new Button("Browse");
    	
    	bchoose.setOnAction(
    			new EventHandler<ActionEvent>() {
					@Override
					public void handle(ActionEvent event) {
						File f = fchoose.showDialog(
									((Node)event.getSource()).getParent().getScene().getWindow()
								);
						if(f!=null) {
							txtfield.setText(f.getAbsolutePath());
							SettingWindowController.changed = true;
						}
					}
				});
    	
    	labelCombo.getChildren().addAll(fieldname, txtfield, bchoose);
		return labelCombo;
    }
    
    @FXML
    public void check_all(ActionEvent event) {
    	if(event.getSource() instanceof Button) {
    		Button b = (Button) event.getSource();
    		Node parent = b;
    		while(!(parent instanceof FlowPane)) {
    			parent = parent.getParent();
    		}
    		FlowPane parentpane = (FlowPane)parent;
    		for(Node ch: parentpane.getChildren()) {
    			if(ch instanceof GridPane) {
    				for(Node c: ((GridPane) ch).getChildren()) {
    	    			if(c instanceof JFXCheckBox) {
    	    				JFXCheckBox chkbx = (JFXCheckBox) c;
    	    				if(!chkbx.isSelected()) {
    	    					chkbx.fire();
    	    				}
    	    			}
    				}
    			}
    		}
    	}
    }
    
    @FXML
    public void uncheck_all(ActionEvent event) {
    	if(event.getSource() instanceof Button) {
    		Button b = (Button) event.getSource();
    		Node parent = b;
    		while(!(parent instanceof FlowPane)) {
    			parent = parent.getParent();
    		}
    		FlowPane parentpane = (FlowPane)parent;
    		for(Node ch: parentpane.getChildren()) {
    			if(ch instanceof GridPane) {
    				for(Node c: ((GridPane) ch).getChildren()) {
    	    			if(c instanceof JFXCheckBox) {
    	    				JFXCheckBox chkbx = (JFXCheckBox) c;
    	    				if(chkbx.isSelected()) {
    	    					chkbx.fire();
    	    				}
    	    			}
    				}
    			}
    		}
    	}
    }
    
    public void shutdown(WindowEvent event) {
        if(SettingWindowController.changed) {  
        	// if the configuration has changed, alert the user with a popup
        	// referenced from: https://stackoverflow.com/a/52234104/9374197
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.getButtonTypes().remove(ButtonType.OK);
            alert.getButtonTypes().add(ButtonType.NO);
            alert.getButtonTypes().add(ButtonType.YES);
            alert.setTitle("Save Changes?");
            alert.setContentText(
            		String.format("There are some unsaved changes. " + 
            					  "Do you wish to save them? If you don't save them, " +
            					  "they will be applied to only this session. " + 
            					  "Restarting will reset them to original values. " + 
            					  "Saving to disk might take a while, so please be patient."
            					  ));
//            alert.initOwner(primaryStage.getOwner());
            Optional<ButtonType> res = alert.showAndWait();

            if(res.isPresent()) {
                if(res.get().equals(ButtonType.YES)) {
                    try {
            			ConfigurationWrapper.getInstance().override_and_save_to_db();
            			SettingWindowController.changed = false;
            		} catch (SQLException e) {
            			e.printStackTrace();
            			FxApp.logger.log(Level.SEVERE, "Failed to save configuration");
            			FxApp.logger.log(Level.SEVERE, e.getMessage());
            		}
                }
            }
        }
    }
    
    public void updateConfigurationUI() {
		profileurl.getItems().clear();
		profileurl.getItems().addAll(
				  "https://virresh.github.io/StockD/nse_live.json",
				  "https://virresh.github.io/StockD/nse_archives.json",
				  "https://virresh.github.io/StockD/nse_old.json");
		List<BaseLink> baselinks = ConfigurationWrapper.getInstance().get_base_links();
		List<Link> links = ConfigurationWrapper.getInstance().get_all_links();
		if(baselinks.size() == 0) {
			FxApp.logger.log(Level.INFO, "Corrupted Internal database. Re-Initialise by deleting stockdb folder.");
		}
		BaseLink b = baselinks.get(0);
		currentprofile.getChildren().clear();
		currentprofile.getChildren().add(new Label("Current Profile: " + b.getSTOCK_TYPE()));
		for(Link l: links) {
			currentprofile.getChildren().add(new Label(l.getPRODUCT_DESCRIPTION() + ": " + l.getPRODUCT_LINK()));
		}
    }
    
    @FXML
    public void loadFromUrl() {
    	String url = profileurl.getValue();
    	try {
	    	if(url != "") {
	    		JSONUtils.reset_links_from_URL(url);
	    		updateConfigurationUI();
	    		Stage s = (Stage)(loadlinkprofile.getScene().getWindow());
	    		s.close();
	    	}
    	}
    	catch(Exception ex) {
    		FxApp.logger.log(Level.FINEST, ex.getMessage(), ex);
    	}
    }
    
    @FXML
    public void loadDefaults(ActionEvent event) {
    	try {
    		FxApp.firstTimeLoad();
    		updateConfigurationUI();
    		Stage s = (Stage)(loadlinkprofile.getScene().getWindow());
    		s.close();
	    }
    	catch(Exception ex) {
    		FxApp.logger.log(Level.FINEST, ex.getMessage(), ex);
    	}
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		int N_rows = 9;
		List<Setting> settings = ConfigurationWrapper.getInstance().get_all_settings();
		try {
			updateConfigurationUI();	
		}
		catch(Exception ex){
			// do nothing
			FxApp.logger.log(Level.FINEST, ex.getMessage(), ex);
		}
		
		Map<String, List<Integer>> categories = new HashMap<String, List<Integer>>();
		
		for(int i=0; i<settings.size(); i++) {
			Setting s = settings.get(i);
			if(categories.containsKey(s.getSUBCATEGORY())) {
				categories.get(s.getSUBCATEGORY()).add(i);
			}
			else {
				categories.put(s.getSUBCATEGORY(), new ArrayList<Integer>(List.of(i)));
			}
		}
		
		Map<String, FlowPane> interfaces = new HashMap<String, FlowPane>();
		interfaces.put("index", index);
		interfaces.put("equity", equity);
		interfaces.put("futures", futures);
		interfaces.put("currencyfutures", currency);
		interfaces.put("options", options);
		interfaces.put("", others);
		
		
		// setup all root panes with grid. Maybe replacing with flowpane would be a nice thing??
		for(String entry: interfaces.keySet()) {
			if(categories.get(entry) != null) {
				List<Integer> panel_content = categories.get(entry);
				int N_cols = panel_content.size() / N_rows + 1;
				GridPane container = new GridPane();
				int content_used = 0;
				for(int r=0; r<N_rows; r++) {
					for(int c=0; c<N_cols; c++) {
						Setting s = settings.get(panel_content.get(content_used));
						if(s.getSETTING_TYPE().equals("checkbox")) {
							Node chkbox = make_chkbox(s);
							container.add(chkbox, c, r);
						}
						else if(s.getSETTING_TYPE().equals("directory")) {
							Node jtf = make_directory(s);
							container.add(jtf, c, r);
						}
						content_used++;
						if(content_used >= panel_content.size()) {
							break;
						}
					}
					if(content_used >= panel_content.size()) {
						break;
					}
				}
				interfaces.get(entry).getChildren().add(container);
			}
		}		
	}
    
}
