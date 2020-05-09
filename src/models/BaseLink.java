package models;

/**
 * Note that even though class variables are capital,
 * the json must contain small letters for jackson to recognize the key
 * 
 * @author viresh
 */
public class BaseLink {
	String BASE_URL;
	String STOCK_TYPE;
	public String getBASE_URL() {
		return BASE_URL;
	}
	public void setBASE_URL(String bASE_URL) {
		BASE_URL = bASE_URL;
	}
	public String getSTOCK_TYPE() {
		return STOCK_TYPE;
	}
	public void setSTOCK_TYPE(String sTOCK_TYPE) {
		STOCK_TYPE = sTOCK_TYPE;
	}
}
