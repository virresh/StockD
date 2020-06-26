package downloads;

import java.time.LocalDate;
import java.util.logging.Level;

import main.FxApp;

public class PerformOnRange implements Runnable{
	
	Callback c;
	LocalDate start;
	LocalDate end;
	
	public PerformOnRange(LocalDate start, LocalDate end, Callback c) {
		this.c = c;
		this.start = start;
		this.end = end;
	}

	@Override
	public void run() {
		try {
			for(LocalDate ld = this.start; ld.isBefore(this.end); ld=ld.plusDays(1) ) {
				PerformDay p = new PerformDay(ld);
				p.perform();
			}
		}
		catch(Exception e){
			FxApp.logger.log(Level.SEVERE, "An error ocurred. Please check internet connectivity. Please report the error if it persists.");
			FxApp.logger.log(Level.FINEST, e.getMessage(), e);
		}
		c.callback();
	}

}
