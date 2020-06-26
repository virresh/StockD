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

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.logging.Level;

import common.Constants;
import common.RunContext;
import main.FxApp;
import parsers.Consolidator;
import parsers.ParseEQ;
import parsers.ParseFO;
import parsers.ParseIndices;
import tech.tablesaw.api.Table;

public class PerformDay {
	Date date;
	public PerformDay(LocalDate date) {
		this.date = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
	}
	
	public void perform() {
		RunContext rt = RunContext.getContext();
		FileDownloader fd = new FileDownloader(rt.getTemp().getAbsolutePath());
		LocalDate ld = LocalDate.ofInstant(this.date.toInstant(), ZoneId.systemDefault());
		
		FxApp.logger.log(Level.INFO, "Processing " + ld.toString());
		Table eq = null;
		Table fu = null;
		Table in = null;

		if(rt.isDoEquity()) {
			try {
				ParseEQ p = new ParseEQ(rt.getEqDir());
				String link = rt.getEqLink();
				String downloadlink = String.format(link, this.date);
		    	eq = fd.DownloadFile(downloadlink, p);
			} catch (Exception e) {
				FxApp.logger.log(Level.INFO, "Failed to find equity bhavcopy for " + ld.toString());
				FxApp.logger.log(Level.FINEST, e.getMessage(), e);
				e.printStackTrace();
			}
		}

		if(rt.isDoFutures()) {
			try {
				ParseFO p = new ParseFO(rt.getFutDir());
				String link = rt.getFuLink();
				String downloadlink = String.format(link, this.date);
		    	fu = fd.DownloadFile(downloadlink, p);
			} catch (Exception e) {
				FxApp.logger.log(Level.INFO, "Failed to find futures bhavcopy for " + ld.toString());
				FxApp.logger.log(Level.FINEST, e.getMessage(), e);
				e.printStackTrace();
			}
		}

		if(rt.isDoIndices()) {
			try {
				ParseIndices p = new ParseIndices(rt.getIndicesDir());
				String link = rt.getIndicesLink();
				String downloadlink = String.format(link, this.date);
		    	in = fd.DownloadFile(downloadlink, p);
			} catch (Exception e) {
				FxApp.logger.log(Level.INFO, "Failed to find indices file for " + ld.toString());
				FxApp.logger.log(Level.FINEST, e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
		if(rt.isConsolidateBhavCopy()) {
			try {
				Consolidator p = new Consolidator(rt.getConsolidatedDir(), eq, fu, in);
//		    	fd.DownloadFile(Constants.getDateFormat(this.date, Constants.YYYYMMDDformat), p);
				p.parse(Constants.getDateFormat(this.date, Constants.YYYYMMDDformat));
			} catch (Exception e) {
				FxApp.logger.log(Level.INFO, "Failed to consolidate files for " + ld.toString());
				FxApp.logger.log(Level.FINEST, e.getMessage(), e);
				e.printStackTrace();
			}
		}
		
		FxApp.logger.log(Level.INFO, "" + ld.toString() + " done.");
	}

}
