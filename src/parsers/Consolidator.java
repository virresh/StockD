package parsers;

import java.io.IOException;
import java.nio.file.Paths;

import tech.tablesaw.api.StringColumn;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvWriteOptions;

public class Consolidator extends BaseConverter {
	Table eq;
	Table fu;
	Table ind;
	public Consolidator(String save_dir, Table eq, Table fu, Table ind) throws IOException {
		super(save_dir, "ALL");
		this.eq = eq;
		this.fu = fu;
		this.ind = ind;
	}

	@Override
	public Table parse(String suffix) throws Exception {
		Table df = Table.create("consolidated");
		df.addColumns(
				StringColumn.create("SYMBOL"),
				StringColumn.create("TIMESTAMP"),
				StringColumn.create("OPEN"),
				StringColumn.create("HIGH"),
				StringColumn.create("LOW"),
				StringColumn.create("CLOSE"),
				StringColumn.create("VOLUME"),
				StringColumn.create("OPEN INTEREST"));
		
//		System.out.println(this.ind.structure());
		
		if(this.eq != null){
			for(int i=0; i<this.eq.rowCount(); i++) {
				df.addRow(this.eq.row(i));
			}
		}
		if(this.fu != null){
			for(int i=0; i<this.fu.rowCount(); i++) {
				df.addRow(this.fu.row(i));
			}
		}
		if(this.ind != null){
			for(int i=0; i<this.ind.rowCount(); i++) {
				df.addRow(this.ind.row(i));
			}
		}
		
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
