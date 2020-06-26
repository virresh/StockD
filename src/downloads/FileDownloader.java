package downloads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import main.FxApp;
import parsers.BaseConverter;
import tech.tablesaw.api.Table;

/**
 * 
 * @author viresh
 * 
 * Official Website uses below js function
 * 
function SingledownloadReports(id, type, _this) {
    var downloadData = [];
    //#cr_equity_daily
    var $this = $(_this).parents('.reportsDownload');
    var obj = { "name": $this.find('label.chk_container').text().trim(), "type": $this.data("type"), "category": $this.data("cat"), "section": $this.data("section"), "link": $this.data("link") };
    downloadData.push(obj);
    fileDownload(downloadData, type, id, "single");
}
 *
 * So a possible future improvement would be to switch to json based api for data
 * However no documentation on this api is available, so I'm just making a special purpose HTTP client
 * which can deal with get links of this api
 */

public class FileDownloader {
	String tempdir;
	
	public FileDownloader(String templocation) {
		this.tempdir = templocation;
	}
	
	public Table DownloadFile(String link, BaseConverter parser) throws IOException {
		boolean unzip = false;
		String localname = "test";
		
		// No timeout on this client
		// could potentially take forever on say 1byte/sec connection

		HttpGet hget = new HttpGet(link);
		hget.addHeader("Accept", "*/*");
		HttpClient hclient = HttpClientBuilder.create()
											  .setRedirectStrategy(new LaxRedirectStrategy())
											  .setUserAgent("Java Client")
											  .build();
		
		HttpResponse hresp = hclient.execute(hget);
		HttpEntity entity = hresp.getEntity();
		
		if(entity == null || hresp.getStatusLine().getStatusCode() != 200) {
			FxApp.logger.log(Level.INFO, "Cannot fetch file.");
			return null;
		}
		else {
			unzip = entity.getContentType().getValue().equalsIgnoreCase("application/zip");
			if(hresp.getFirstHeader("Content-Disposition") != null) {
				Optional<String> opt= Arrays.stream(
						hresp.getFirstHeader("Content-Disposition")
						     .getElements())
						.map(ele -> ele.getParameterByName("filename"))
						.filter(Objects::nonNull)
						.map(NameValuePair::getValue)
						.findFirst();
				if(opt.isPresent()) {
					localname = opt.get();
				}			
				if(unzip) {
					localname = localname.substring(0, localname.length()-4);
				}
			}
			else {
				int lastSlashIndex = link.lastIndexOf('/');
		        if (lastSlashIndex >= 0 && lastSlashIndex < link.length() - 1) {
		        	if(unzip == true) {
		        		localname = link.substring(lastSlashIndex+1, link.length()-4);
		        	}
		        	else {
		        		localname = link.substring(lastSlashIndex+1);
		        	}
				}
			}
		}

        InputStream inp = null;
        File outfile = Paths.get(this.tempdir, localname).toFile();
		FileOutputStream out = new FileOutputStream(outfile);
		FxApp.logger.log(Level.INFO, "Downloading " + localname);
		
		inp = hresp.getEntity().getContent();

        if(unzip) {
        	boolean didExtraction = false;
			ZipInputStream zipstream = new ZipInputStream(inp);
			ZipEntry zent = null;
			while((zent = zipstream.getNextEntry())!=null) {
				if(zent.isDirectory()) {
					continue;
				}
				String fileName = zent.getName();
				if(fileName.contains("/")) {
					//If the path is like /PR/abc.zip
					String[] splittedNames = zent.getName().split("/");
					fileName=splittedNames[splittedNames.length-1];
				}
				if(fileName.equals(localname)) {
				    int count=0;
				    byte[] b1 = new byte[1000];
				    while((count = zipstream.read(b1)) != -1) {
				    	out.write(b1, 0, count);
				    }
				    didExtraction = true;
				    break;
				}
			}
			if(!didExtraction) {
				FxApp.logger.log(Level.SEVERE, "Unsupported ZIP format. Skipping.");
				return null;
			}
		}
		else {
		    int count=0;
		    byte[] b1 = new byte[1000];
		    while((count = inp.read(b1)) != -1) {
		    	out.write(b1, 0, count);
		    }
		}
		out.close();
		
		Table t = null;
		
		try {
			t = parser.parse(outfile.getAbsolutePath());
		} catch (Exception e) {
			FxApp.logger.log(Level.SEVERE, "Cannot process file " + outfile.getAbsolutePath());
			FxApp.logger.log(Level.FINEST, e.getMessage(), e);
			e.printStackTrace();
		}
		if(outfile.exists()) {
			outfile.delete();
		}
		return t;
	}
}
