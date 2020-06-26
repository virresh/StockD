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
package parsers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import tech.tablesaw.api.Table;

public abstract class BaseConverter {
	String directory;
	String prodcode;
	protected String convertToDate(String date) throws Exception{
		try{
			return common.Constants.getDateFormat(
					date,
					common.Constants.DDMMMYYYYFormat,
					common.Constants.YYYYMMDDformat
					);
		}
		catch(Exception e){
			throw e;
		}
	}
	
	/* *
	 * Core parser for csv files
	 * Inherit and override this class
	 * */
	public abstract Table parse(String filePath) throws Exception;
	
	public BaseConverter(String save_dir, String prodcode) throws IOException {
		this.directory = save_dir;
		this.prodcode = prodcode;
		if(!Files.exists(Paths.get(this.directory))) {
			Files.createDirectories(Paths.get(this.directory));
		}
	}
}
