package parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;

import common.Constants;
import common.RunContext;
import main.FxApp;
import tech.tablesaw.api.DoubleColumn;
import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;

public class ParseIndices extends BaseConverter {

	public ParseIndices(String save_dir) throws IOException {
		super(save_dir, "IN");
	}

	@Override
	public Table parse(String filePath) throws Exception {
		File f = Paths.get(filePath).toFile();
		InputStream instream = new FileInputStream(f);
		if(f.isFile() && f.length()==0) {
			FxApp.logger.log(Level.INFO, "Data Unavailable.");
			instream.close();
			f.delete();
			return null;
		}
		Table df = Table
				   .read()
				   .csv(instream)
				   .retainColumns("Index Name", "Index Date", "Open Index Value", "High Index Value", "Low Index Value", "Closing Index Value", "Volume");
		
		List<Integer> rowIdx = new ArrayList<Integer>();

		df.replaceColumn(df.column("Open Index Value").asStringColumn().replaceAll("-", "0").setName("Open Index Value"));
		df.replaceColumn(df.column("High Index Value").asStringColumn().replaceAll("-", "0").setName("High Index Value"));
		df.replaceColumn(df.column("Low Index Value").asStringColumn().replaceAll("-", "0").setName("Low Index Value"));
		df.replaceColumn(df.column("Closing Index Value").asStringColumn().replaceAll("-", "0").setName("Closing Index Value"));
		df.replaceColumn(df.column("Volume").asStringColumn().replaceAll("-", "0").setName("Volume"));
		
		HashSet<String> symbols = RunContext.getContext().getIndexesInUse();

		for(int i=0; i<df.rowCount(); i++) {
			String in = df.row(i).getString("Index Name").toUpperCase();
			String newcode = Constants.getIndexCode(in);
			if(symbols.contains(newcode)) {
				rowIdx.add(i);
				df.row(i).setString("Index Name", newcode);
			}
		}
		
		df = df.rows(rowIdx.stream().mapToInt(i->i).toArray());
		df.addColumns(DoubleColumn.create("OPEN INTEREST", df.rowCount()).setMissingTo(0.0));

		StringColumn x = df.column("Index Date")
						   .asStringColumn()
						   .map((idt)->{ return Constants.convertDate(idt, Constants.DDMMYYYYhifenFormat, Constants.YYYYMMDDformat);});
		df.replaceColumn("Index Date", x);

		String suffix = Constants.convertDate(
								filePath.substring(filePath.length()-12, filePath.length()-4), 
								Constants.DDMMYYYYFormat, Constants.DDMMMYYYYFormat).toUpperCase();
		String res = Paths
					  .get(this.directory, 
						   System.getProperty("file.separator"), 
						   this.prodcode + "_" + suffix + ".txt")
					  .toString();

		df.column("Index Name").setName("SYMBOL");
		df.column("Index Date").setName("TIMESTAMP");
		df.column("Open Index Value").setName("OPEN");
		df.column("High Index Value").setName("HIGH");
		df.column("Low Index Value").setName("LOW");
		df.column("Closing Index Value").setName("CLOSE");
		df.column("Volume").setName("VOLUME");
		
		CsvWriteOptions opts = CsvWriteOptions
								.builder(res)
								.separator(',')
								.header(true)
								.build();

		df.write().usingOptions(opts);
		return df;
	}
}
