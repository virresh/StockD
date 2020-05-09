package models;

public class Setting {
	int SETTING_ID;
	String SETTING_NAME;
	String SETTING_VALUE;
	String SETTING_TYPE;
	String CATEGORY;
	String SUBCATEGORY;
	public int getSETTING_ID() {
		return SETTING_ID;
	}
	public void setSETTING_ID(int sETTING_ID) {
		SETTING_ID = sETTING_ID;
	}
	public String getSETTING_NAME() {
		return SETTING_NAME;
	}
	public void setSETTING_NAME(String sETTING_NAME) {
		SETTING_NAME = sETTING_NAME;
	}
	public String getSETTING_VALUE() {
		return SETTING_VALUE;
	}
	public void setSETTING_VALUE(String sETTING_VALUE) {
		SETTING_VALUE = sETTING_VALUE;
	}
	public String getSETTING_TYPE() {
		return SETTING_TYPE;
	}
	public void setSETTING_TYPE(String sETTING_TYPE) {
		SETTING_TYPE = sETTING_TYPE;
	}
	public String getCATEGORY() {
		return CATEGORY;
	}
	public void setCATEGORY(String cATEGORY) {
		CATEGORY = cATEGORY;
	}
	public String getSUBCATEGORY() {
		return SUBCATEGORY;
	}
	public void setSUBCATEGORY(String sUBCATEGORY) {
		SUBCATEGORY = sUBCATEGORY;
	}	
}
