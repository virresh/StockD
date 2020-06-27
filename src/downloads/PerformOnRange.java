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
package downloads;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;

import common.RunContext;
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
		boolean skipweekends = RunContext.getContext().isSkipWeekends();

		this.p.updateProgress(current, total);
		try {
			for(LocalDate ld = this.start; this.end.isAfter(ld) && !this.stop; ld=ld.plusDays(1) ) {
				if(skipweekends && (ld.getDayOfWeek().equals(DayOfWeek.SATURDAY) || ld.getDayOfWeek().equals(DayOfWeek.SUNDAY))){
					FxApp.logger.log(Level.INFO, "Skipping weekend " + ld.toString());
				}
				else {
					PerformDay p = new PerformDay(ld);
					p.perform();					
				}
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
