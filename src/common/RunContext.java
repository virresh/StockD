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
package common;

import java.io.File;
import java.util.HashSet;

import models.BaseLink;
import models.ConfigurationWrapper;
import models.Link;
import models.Setting;

public class RunContext {
	
	boolean FO_add_I_prefix;
	boolean consolidateBhavCopy;
	boolean skip_weekends;
	
	private static RunContext sinstance;
	private HashSet<String> indexesInUse;
	private String eqDir;
	private String futDir;
	private String indicesDir;
	private String consolidatedDir;
	
	Link eqBhavCopy;
	Link fuBhavCopy;
	Link indicesBhavCopy;
	
	BaseLink base;
	
	File temp;
	
	boolean doEquity;
	boolean doFutures;
	boolean doIndices;
	
	public void updateContext() {
		indexesInUse = new HashSet<>();
		for(Setting s: ConfigurationWrapper.getInstance().get_all_settings()) {
			if(s.getSETTING_TYPE().equals("directory")) {
				if(s.getSETTING_NAME().equals("Equities Directory")) {
					eqDir = s.getSETTING_VALUE();
				}
				else if(s.getSETTING_NAME().equals("Futures Directory")) {
					futDir = s.getSETTING_VALUE();
				}
				else if(s.getSETTING_NAME().equals("Indices Directory")) {
					indicesDir = s.getSETTING_VALUE();
				}
				else if(s.getSETTING_NAME().equals("Consolidated Bhavcopy Directory")) {
					consolidatedDir = s.getSETTING_VALUE();
				}
			}
			else if(s.getSETTING_TYPE().equals("checkbox")) {
				if(s.getCATEGORY().equals("download") && s.getSUBCATEGORY().equals("index")) {
					// valid index name, add to desired ones
					if(s.getSETTING_VALUE().equals("true")) {
						this.indexesInUse.add(Constants.getIndexCode(s.getSETTING_NAME()));						
					}
				}
				else if(s.getSETTING_NAME().equals("Skip Weekends")) {
					this.skip_weekends = (s.getSETTING_VALUE().equals("true"))?true:false;
				}
				else if(s.getSETTING_NAME().equals("Equity Bhavcopy")) {
					this.doEquity = (s.getSETTING_VALUE().equals("true"))?true:false;
				}
				else if(s.getSETTING_NAME().equals("Futures Bhavcopy")) {
					this.doFutures = (s.getSETTING_VALUE().equals("true"))?true:false;
				}
				else if(s.getSETTING_NAME().equals("Create consolidated Bhavcopy")) {
					this.consolidateBhavCopy = (s.getSETTING_VALUE().equals("true"))?true:false;
				}
			}
		}
		
		for(Link l: ConfigurationWrapper.getInstance().get_all_links()) {
			if(l.getPRODUCT_CODE().equals("eqbhav")) {
				this.eqBhavCopy = l;
			}
			else if(l.getPRODUCT_CODE().equals("fobhav")) {
				this.fuBhavCopy = l;
			}
			else if(l.getPRODUCT_CODE().equals("indexbhav")) {
				this.indicesBhavCopy = l;
			}
		}
		
		for(BaseLink bl: ConfigurationWrapper.getInstance().get_base_links()) {
			this.base = bl;
			break;
		}
		
		if(indexesInUse.size() > 0) {
			doIndices = true;
		}
		else {
			doIndices = false;
		}
	}
	
	private RunContext() {
		this.FO_add_I_prefix = false;
		this.consolidateBhavCopy = false;
		this.skip_weekends = true;
		this.indexesInUse = new HashSet<String>();	
		updateContext();
		
    	//// Setup temporary directory
    	File dir = new File(System.getProperty("user.dir")+"/Temp");
    	if(!dir.exists()) {
    		dir.mkdir();
    	}
    	else {
			File[] tempFiles = dir.listFiles();
			for (int i = 0; i < tempFiles.length; i++) {
				tempFiles[i].delete();
			}
    	}
    	this.temp = dir;
	}
	
	public static RunContext getContext() {
		if(sinstance == null) {
			sinstance = new RunContext();
		}
		return sinstance;
	}
	
	public boolean FO_add_I_predix() {
		return this.FO_add_I_prefix;
	}
	
	public boolean isConsolidateBhavCopy() {
		return this.consolidateBhavCopy;
	}
	
	public boolean isSkipWeekends() {
		return this.skip_weekends;
	}
	
	public HashSet<String> getIndexesInUse() {
		return indexesInUse;
	}

	public String getEqDir() {
		return eqDir;
	}

	public String getFutDir() {
		return futDir;
	}

	public String getIndicesDir() {
		return indicesDir;
	}

	public String getConsolidatedDir() {
		return consolidatedDir;
	}
	
	public String getEqLink() {
		return this.eqBhavCopy.getPRODUCT_LINK();
	}
	
	public String getFuLink() {
		return this.fuBhavCopy.getPRODUCT_LINK();
	}
	
	public String getIndicesLink() {
		return this.indicesBhavCopy.getPRODUCT_LINK();
	}

	public File getTemp() {
		return temp;
	}

	public void setTemp(File temp) {
		this.temp = temp;
	}

	public boolean isDoEquity() {
		return doEquity;
	}

	public boolean isDoFutures() {
		return doFutures;
	}

	public boolean isDoIndices() {
		return doIndices;
	}	
}
