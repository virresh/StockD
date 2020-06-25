package logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import com.jfoenix.controls.JFXTextArea;

public class TextFieldHandler extends Handler{
	JFXTextArea ta;

	public TextFieldHandler(JFXTextArea tarea) {
		this.ta = tarea;
	}

	@Override
	public void publish(LogRecord record) {
		if(record.getMessage() != null && record.getLevel() != Level.FINEST) {
			this.ta.appendText(record.getMessage() + "\n");
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
		this.ta.appendText("Thanks for using StockD!");
	}
}
