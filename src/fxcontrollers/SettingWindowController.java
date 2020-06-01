package fxcontrollers;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXTabPane;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import models.BaseLink;
import models.ConfigurationWrapper;
import models.Setting;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

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
		
		// setup index grid. Maybe replacing with flowpane would be a nice thing??
		if(categories.get("index") != null) {
			List<Integer> panel_content = categories.get("index");
			int N_cols = categories.get("index").size() / N_rows + 1;
			GridPane container = new GridPane();
			int content_used = 0;
			for(int r=0; r<N_rows; r++) {
				for(int c=0; c<N_cols; c++) {
					Setting s = settings.get(panel_content.get(content_used));
					JFXCheckBox chkbox = new JFXCheckBox(s.getSETTING_NAME());
					chkbox.setTooltip(new Tooltip(s.getSETTING_NAME()));
					chkbox.setPadding(new Insets(10));
					chkbox.setSelected(s.getSETTING_VALUE().equals("true"));
					container.add(chkbox, c, r);
					content_used++;
					if(content_used >= panel_content.size()) {
						break;
					}
				}
				if(content_used >= panel_content.size()) {
					break;
				}
			}
			index.getChildren().add(container);
		}
		
		if(categories.get("equity") != null) {
			List<Integer> panel_content = categories.get("equity");
			int N_cols = categories.get("equity").size() / N_rows + 1;
			GridPane container = new GridPane();
			int content_used = 0;
			for(int r=0; r<N_rows; r++) {
				for(int c=0; c<N_cols; c++) {
					Setting s = settings.get(panel_content.get(content_used));
					if(s.getSETTING_TYPE().equals("checkbox")){
						JFXCheckBox chkbox = new JFXCheckBox(s.getSETTING_NAME());
						chkbox.setTooltip(new Tooltip(s.getSETTING_NAME()));
						chkbox.setPadding(new Insets(10));
						chkbox.setSelected(s.getSETTING_VALUE().equals("true"));
						container.add(chkbox, c, r);
					}
					else if(s.getSETTING_TYPE().equals("directory")) {
						///// TODO: Setup a file chooser here
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
			index.getChildren().add(container);
		}
		
	}
    
}
