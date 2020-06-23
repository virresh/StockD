package parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import common.Constants;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;

public class ParseEQ extends BaseConverter {

	public ParseEQ(String save_dir) throws IOException {
		super(save_dir, "EQ");
	}

	@Override
	public Table parse(String filePath) throws Exception {
		File f = Paths.get(filePath).toFile();
		InputStream instream = new FileInputStream(f);
		Table df = Table
				   .read()
				   .csv(instream)
				   .retainColumns("SYMBOL", "TIMESTAMP", "OPEN", "HIGH", "LOW", "CLOSE", "TOTTRDQTY", "SERIES");
		
		df.column("TOTTRDQTY").setName("VOLUME");
		df.addColumns(DoubleColumn.create("OPEN INTEREST", df.rowCount()).setMissingTo(0.0));
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
