package downloads;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.zip.ZipInputStream;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;

import main.FxApp;
import parsers.BaseConverter;

/**
 * 
 * @author viresh
 * Official Website js
function SingledownloadReports(id, type, _this) {
    var downloadData = [];
    //#cr_equity_daily
    var $this = $(_this).parents('.reportsDownload');
    var obj = { "name": $this.find('label.chk_container').text().trim(), "type": $this.data("type"), "category": $this.data("cat"), "section": $this.data("section"), "link": $this.data("link") };
    downloadData.push(obj);
    fileDownload(downloadData, type, id, "single");
}

 *
 */

public class FileDownloader {
	String tempdir;
	
	public FileDownloader(String templocation) {
		this.tempdir = templocation;
	}
	
	public void DownloadFile(String link, BaseConverter parser) throws IOException {
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
		
		if(entity == null) {
			FxApp.logger.log(Level.SEVERE, "Cannot fetch file.");
			return;
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
		        		localname = link.substring(lastSlashIndex, link.length()-4);
		        	}
		        	else {
		        		localname = link.substring(lastSlashIndex);
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
			ZipInputStream zipstream = new ZipInputStream(inp);
		    int count=0;
		    byte[] b1 = new byte[1000];
		    while((count = zipstream.read(b1)) != -1) {
		    	out.write(b1, 0, count);
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
		
		try {
			parser.parse(outfile.getAbsolutePath());
		} catch (Exception e) {
			FxApp.logger.log(Level.SEVERE, "Cannot process file " + outfile.getAbsolutePath());
			FxApp.logger.log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
		}
	}
}
