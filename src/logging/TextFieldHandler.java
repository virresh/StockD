package logging;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class TextFieldHandler extends Handler{
	DisplayMessageListener dml;

	public TextFieldHandler(DisplayMessageListener dml) {
		this.dml = dml;
	}

	@Override
	public void publish(LogRecord record) {
		if(record.getMessage() != null && record.getLevel() != Level.FINEST) {
			dml.newMessage(record.getMessage() + "\n");
		}
	}

	@Override
	public void flush() {
	}

	@Override
	public void close() throws SecurityException {
		dml.newMessage("Thanks for using StockD!");
	}
}
