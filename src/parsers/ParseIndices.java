package parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import common.Constants;
import common.RunContext;
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
		Table df = Table
				   .read()
				   .csv(instream)
				   .retainColumns("Index Name", "Index Date", "Open Index Value", "High Index Value", "Low Index Value", "Closing Index Value", "Volume");
		
		List<Integer> rowIdx = new ArrayList<Integer>();
		
		df.replaceColumn("Open Index Value", df.column("Open Index Value").asStringColumn().replaceAll("-", "0"));
		df.replaceColumn("High Index Value", df.column("High Index Value").asStringColumn().replaceAll("-", "0"));
		df.replaceColumn("Low Index Value", df.column("Low Index Value").asStringColumn().replaceAll("-", "0"));
		df.replaceColumn("Closing Index Value", df.column("Closing Index Value").asStringColumn().replaceAll("-", "0"));
		df.replaceColumn("Volume", df.column("Volume").asStringColumn().replaceAll("-", "0"));
		
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
		
		CsvWriteOptions opts = CsvWriteOptions
								.builder(res)
								.separator(',')
								.header(false)
								.build();

		df.write().usingOptions(opts);
		return df;
	}
}
