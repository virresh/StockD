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
