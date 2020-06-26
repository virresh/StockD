package parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

import common.Constants;
import common.RunContext;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;

public class ParseFO extends BaseConverter {

	public ParseFO(String save_dir) throws IOException {
		super(save_dir, "FU");
	}
	
	public String add_prefixes(String input, int count) {
		String v = "";
		for(int i=0; i<count; i++) {
			v = v + "I";
		}
		if(RunContext.getContext().FO_add_I_predix()) {
			return v + "-" + input;
		}
		else {
			return input + "-" + v;
		}
	}

	@Override
	public Table parse(String filePath) throws Exception {
		File f = Paths.get(filePath).toFile();
		InputStream instream = new FileInputStream(f);
		Table df = Table
				   .read()
				   .csv(instream)
				   .retainColumns("SYMBOL", "TIMESTAMP", "OPEN", "HIGH", "LOW", "CLOSE", "VAL_INLAKH", "OPEN_INT", "INSTRUMENT");

		df = df.dropWhere(df.column("INSTRUMENT")
				.asStringColumn()
				.upperCase()
				.isNotIn("FUTIDX", "FUTSTK"));
		
		df.removeColumns(df.columnIndex("INSTRUMENT"));
		
		StringColumn x = df.column("TIMESTAMP")
						   .asStringColumn()
						   .map(Constants::convertDDMMMYYYYtoYYYYMMDD);
		df.replaceColumn("TIMESTAMP", x);
		
		// add prefixes/suffixes to FO Symbols
		String previous = null;
		int count = 0;
		for(int i=0; i<df.rowCount(); i++) {
			String in = df.row(i).getString("SYMBOL");
			if(previous==null || !previous.equals(in)) {
				previous = in;
				count = 0;
			}
			count++;
			df.row(i).setString("SYMBOL", add_prefixes(in, count));
		}

		String suffix = filePath.substring(filePath.length()-17, filePath.length()-8);
		String res = Paths
					  .get(this.directory, 
							  System.getProperty("file.separator"), 
							  this.prodcode + "_" + suffix + ".txt")
					  .toString();
		
		df.column("VAL_INLAKH").setName("VOLUME");
		df.column("OPEN_INT").setName("OPEN INTEREST");
		
		CsvWriteOptions opts = CsvWriteOptions
								.builder(res)
								.separator(',')
								.header(false)
								.build();
		
//		System.out.println(df.structure());
//		System.out.println(df.first(4));
//		System.out.println(res);
		df.write().usingOptions(opts);
		return df;
	}
}
