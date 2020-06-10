package fxcontrollers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTabPane;
import com.jfoenix.controls.JFXTextField;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import models.BaseLink;
import models.ConfigurationWrapper;
import models.Setting;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

public class SettingWindowController implements Initializable {

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
    
    private Node make_chkbox(Setting s) {
		JFXCheckBox chkbox = new JFXCheckBox(s.getSETTING_NAME());
		chkbox.setTooltip(new Tooltip(s.getSETTING_NAME()));
		chkbox.setPadding(new Insets(10));
		chkbox.setSelected(s.getSETTING_VALUE().equals("true"));
		return chkbox;
    }
    
    private Node make_directory(Setting s) {
    	FlowPane labelCombo = new FlowPane(20.0, 20.0);
    	
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
						}
					}
				});
    	
    	labelCombo.getChildren().addAll(fieldname, txtfield, bchoose);
		return labelCombo;
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		int N_rows = 9;
		List<BaseLink> baselinks = ConfigurationWrapper.getInstance().get_base_links();
		List<Setting> settings = ConfigurationWrapper.getInstance().get_all_settings();
		BaseLink b = baselinks.get(0);
		
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
