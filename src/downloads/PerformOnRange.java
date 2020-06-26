package downloads;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;

import main.FxApp;

public class PerformOnRange implements Runnable{
	
	Callback c;
	ProgressUpdate p;
	LocalDate start;
	LocalDate end;
	boolean stop;
	
	public PerformOnRange(LocalDate start, LocalDate end, Callback c, ProgressUpdate p) {
		this.stop = false;
		this.c = c;
		this.start = start;
		this.end = end;
		this.p = p;
	}
	
	public void signalStop() {
		this.stop = true;
	}

	@Override
	public void run() {
		double total = ChronoUnit.DAYS.between(this.start, this.end);
		double current = 0.0;
		this.p.updateProgress(current, total);
		try {
			for(LocalDate ld = this.start; this.end.isAfter(ld) && !this.stop; ld=ld.plusDays(1) ) {
				PerformDay p = new PerformDay(ld);
				p.perform();
				current += 1;
				this.p.updateProgress(current, total);
			}
		}
		catch(Exception e){
			FxApp.logger.log(Level.SEVERE, "An error ocurred. Please check internet connectivity. Please report the error if it persists.");
			FxApp.logger.log(Level.FINEST, e.getMessage(), e);
			e.printStackTrace();
		}
		c.callback();
	}

}
