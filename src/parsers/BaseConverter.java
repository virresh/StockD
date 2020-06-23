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
