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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import common.Constants;
import tech.tablesaw.api.ColumnType;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;
import tech.tablesaw.io.csv.CsvWriteOptions;

public class ParseEQ extends BaseConverter {

	public ParseEQ(String save_dir) throws IOException {
		super(save_dir, "EQ");
	}

	@Override
	public Table parse(String filePath) throws Exception {
		File f = Paths.get(filePath).toFile();
		InputStream instream = new FileInputStream(f);
		
		ColumnType[] columnTypes = {
				ColumnType.STRING,     // 0     SYMBOL      
				ColumnType.STRING,     // 1     SERIES      
				ColumnType.STRING,     // 2     OPEN        
				ColumnType.STRING,     // 3     HIGH        
				ColumnType.STRING,     // 4     LOW         
				ColumnType.STRING,     // 5     CLOSE       
				ColumnType.SKIP,     // 6     LAST        
				ColumnType.SKIP,     // 7     PREVCLOSE   
				ColumnType.STRING,    // 8     TOTTRDQTY   
				ColumnType.SKIP,     // 9     TOTTRDVAL   
				ColumnType.STRING,     // 10    TIMESTAMP   
				ColumnType.SKIP,    // 11    TOTALTRADES 
				ColumnType.SKIP,     // 12    ISIN        
				ColumnType.SKIP,     // 13    C13         
			};

		Table df = Table
				   .read()
				   .usingOptions(
						   CsvReadOptions
						   .builder(instream)
						   .columnTypes(columnTypes))
				   .retainColumns("SYMBOL", "TIMESTAMP", "OPEN", "HIGH", "LOW", "CLOSE", "TOTTRDQTY", "SERIES");
		
		df.column("TOTTRDQTY").setName("VOLUME");
		df.addColumns(StringColumn.create("OPEN INTEREST", df.rowCount()).setMissingTo("0.0"));
		df = df.dropWhere(df.column("SERIES")
							.asStringColumn()
							.upperCase()
							.isNotIn("EQ", "BE"));
		
		df.removeColumns(df.columnIndex("SERIES"));
		
		StringColumn x = df.column("TIMESTAMP")
						   .asStringColumn()
						   .map(Constants::convertDDMMMYYYYtoYYYYMMDD);
		df.replaceColumn("TIMESTAMP", x);

		String suffix = filePath.substring(filePath.length()-17, filePath.length()-8);
		String res = Paths
					  .get(this.directory, 
						   System.getProperty("file.separator"), 
						   this.prodcode + "_" + suffix + ".txt")
					  .toString();
		
		CsvWriteOptions opts = CsvWriteOptions
								.builder(res)
								.separator(',')
								.header(false)
								.build();

		df.write().usingOptions(opts);
		return df;
	}
}
