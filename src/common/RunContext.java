package common;

import java.util.HashSet;

import models.BaseLink;
import models.ConfigurationWrapper;
import models.Link;
import models.Setting;

public class RunContext {
	
	private boolean FO_add_I_prefix;
	private boolean consolidateBhavCopy;
	private boolean skip_weekends;
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
	
	public void updateContext() {
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
					consolidatedDir = s.getSETTING_NAME();
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
			}
		}
		
		for(Link l: ConfigurationWrapper.getInstance().get_all_links()) {
			if(l.getPRODUCT_NAME().equals("eqbhav")) {
				this.eqBhavCopy = l;
			}
			else if(l.getPRODUCT_NAME().equals("fobhav")) {
				this.fuBhavCopy = l;
			}
			else if(l.getPRODUCT_NAME().equals("indexbhav")) {
				this.indicesBhavCopy = l;
			}
		}
		
		for(BaseLink bl: ConfigurationWrapper.getInstance().get_base_links()) {
			this.base = bl;
			break;
		}		
	}
	
	private RunContext() {
		this.FO_add_I_prefix = false;
		this.consolidateBhavCopy = false;
		this.skip_weekends = true;
		this.indexesInUse = new HashSet<String>();	
		updateContext();
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
		return this.base.getBASE_URL() + this.eqBhavCopy.getPRODUCT_LINK();
	}
	
	public String getFuLink() {
		return this.base.getBASE_URL() + this.fuBhavCopy.getPRODUCT_LINK();
	}
	
	public String getIndicesLink() {
		return this.base.getBASE_URL() + this.indicesBhavCopy.getPRODUCT_LINK();
	}
}
