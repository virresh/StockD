package fxcontrollers;

import java.net.URL;
import java.util.ResourceBundle;

import common.Constants;
import common.RunContext;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Hyperlink;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class AboutController implements Initializable{

    @FXML
    private TextFlow textarea;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		Hyperlink h1 = new Hyperlink("https://github.com/virresh/StockD/");
		Hyperlink h2 = new Hyperlink("https://gitter.im/virresh/StockD");
		Hyperlink h3 = new Hyperlink("https://github.com/virresh/StockD/wiki/Contributing");
		Hyperlink h4 = new Hyperlink("https://github.com/virresh/StockD/wiki/User-Guide");
		
		h1.setOnAction(t->{
			RunContext.getContext().getHs().showDocument(h1.getText());
		});
		
		h2.setOnAction(t->{
			RunContext.getContext().getHs().showDocument(h2.getText());
		});
		
		h3.setOnAction(t->{
			RunContext.getContext().getHs().showDocument(h3.getText());
		});
		
		h4.setOnAction(t->{
			RunContext.getContext().getHs().showDocument(h4.getText());
		});
		
		textarea.getChildren()
				.addAll(
						new Text("Hi There!\nThanks for using StockD v" + Constants.version + ".\n"),
						new Text("In case you encounter issues please report them at:\n"),
						h1,
						new Text("\nTo get help/discuss with other users, you can join the chatroom at:\n"),
						h2,
						new Text("\nTo contribute to this project, visit:\n"),
						h3,
						new Text("\nYou can find the User Documentation at:\n"),
						h4,
						new Text("\n\nIf you find this software useful, consider donating/contributing.\nViresh Gupta\n")
						);
	}

}
