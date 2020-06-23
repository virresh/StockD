package common;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.logging.Level;

import main.FxApp;

public class Constants {
	public static String version = "4.0.0";	
	public static String appname = "StockD";
	public static String dbname = "stockdb";
	public static String base = "BASELINK";
	public static String settings = "SETTINGS";
	public static String links = "LINKS";	

	public static final String DDMMYYYYhifenFormat = "dd-MM-yyyy";
	public static final String DDMMMYYYYhifenFormat = "dd-MMM-yyyy";
	public static final String YYYYMMDDhifenFormat = "yyyy-MM-dd";
	public static final String YYYYMMDDformat = "yyyyMMdd";
	public static final String DDMMYYFormat="ddMMyy";
	public static final String DDMMMYYYYFormat="ddMMMyyyy";
	public static final String DDMMYYYYFormat ="ddMMyyyy";
	public static final String MMMFormat ="MMM";
	public static final String YYYYFormat ="yyyy";

	public static String convertDDMMMYYYYtoYYYYMMDD(String input) {
		String ans = "";
		try {
			Date inputDate = new SimpleDateFormat(Constants.DDMMMYYYYhifenFormat).parse(input);
			ans = new SimpleDateFormat(Constants.YYYYMMDDformat).format(inputDate);
		}
		catch(Exception ex) {
			FxApp.logger.log(Level.SEVERE, "Couldn't parse date!");
			FxApp.logger.log(Level.SEVERE, ex.getMessage());
			ex.printStackTrace();
		}
		return ans;
	}
	
	public static String convertDate(String input, String from, String to) {
		String ans = "";
		try {
			Date inputDate = new SimpleDateFormat(from).parse(input);
			ans = new SimpleDateFormat(to).format(inputDate);
		}
		catch(Exception ex) {
			FxApp.logger.log(Level.SEVERE, "Couldn't parse date!");
			FxApp.logger.log(Level.SEVERE, ex.getMessage());
			ex.printStackTrace();
		}
		return ans;
	}
	
	public static Date getStringToDate(String date,String inputDateFormat) throws ParseException{
		return new SimpleDateFormat(inputDateFormat).parse(date);
	}
	
	public static String getDDMMYYYYDate(Date date) {
		return new SimpleDateFormat(DDMMYYYYhifenFormat).format(date);
	}

	public static String getDateFormat(Date date, String expectedFormat) {
		return new SimpleDateFormat(expectedFormat).format(date);
	}

	public static String getDateFormat(String inputDate,
			String inputDateFormat, String outputDateFormat)
					throws ParseException {
		Date date = getStringToDate(inputDate,inputDateFormat);
		return new SimpleDateFormat(outputDateFormat).format(date);
	}

	public static Date getCurrentDateTime() {
		return new GregorianCalendar().getTime();
	}

	public static String getIndexCode(String indexName) {
		if (indexName.equalsIgnoreCase("NIFTY")) {
			return "NIFTY 50";
		} else if (indexName.equalsIgnoreCase("NIFTY JUNIOR"))
			return "NIFTY NEXT 50";
		else if (indexName.equalsIgnoreCase("DEFTY"))
			return "NIFTY DEFTY";
		else if (indexName.equalsIgnoreCase("CNX 500"))
			return "NIFTY 500";
		else if (indexName.equalsIgnoreCase("CNX MIDCAP"))
			return "NIFTY MIDCAP 100";
		else if (indexName.equalsIgnoreCase("CNX IT"))
			return "NIFTY IT";
		else if (indexName.equalsIgnoreCase("BANK NIFTY"))
			return "NIFTY BANK";
		else if (indexName.equalsIgnoreCase("MIDCAP 50"))
			return "NIFTY MIDCAP 50";
		else if (indexName.equalsIgnoreCase("ESG INDIA IDX"))
			return "S&P ESG INDIA INDEX";
		else if (indexName.equalsIgnoreCase("SHARIAH"))
			return "NIFTY50 SHARIAH";
		else if (indexName.equalsIgnoreCase("500 SHARIAH"))
			return "NIFTY500 SHARIAH";
		else if (indexName.equalsIgnoreCase("CNX INFRA"))
			return "NIFTY INFRA";
		else if (indexName.equalsIgnoreCase("CNX REALTY"))
			return "NIFTY REALTY";
		else if (indexName.equalsIgnoreCase("CNX ENERGY"))
			return "NIFTY ENERGY";
		else if (indexName.equalsIgnoreCase("CNX MNC"))
			return "NIFTY MNC";
		else if (indexName.equalsIgnoreCase("CNX PHARMA"))
			return "NIFTY PHARMA";
		else if (indexName.equalsIgnoreCase("CNX PSE"))
			return "NIFTY PSE";
		else if (indexName.equalsIgnoreCase("CNX PSU BANK"))
			return "NIFTY PSU BANK";
		else if (indexName.equalsIgnoreCase("CNX SERVICE"))
			return "NIFTY SERV SECTOR";
		else if (indexName.equalsIgnoreCase("CNX FMCG"))
			return "NIFTY FMCG";
		else if (indexName.equalsIgnoreCase("CNX 100"))
			return "NIFTY 100";
		else if (indexName.equalsIgnoreCase("CNX AUTO"))
			return "NIFTY AUTO";
		else if (indexName.equalsIgnoreCase("CNX FINANCE"))
			return "NIFTY FIN SERVICE";
		else if (indexName.equalsIgnoreCase("CNX METAL"))
			return "NIFTY METAL";
		else if (indexName.equalsIgnoreCase("VIX"))
			return "INDIA VIX";
		return indexName;
	}
	
	public static String getOldIndexCode(String indexName) {
		if (indexName.equalsIgnoreCase("NIFTY")) {
			return "CNX NIFTY";
		} else if (indexName.equalsIgnoreCase("NIFTY JUNIOR"))
			return "CNX NIFTY JUNIOR";
		else if (indexName.equalsIgnoreCase("DEFTY"))
			return "CNX DEFTY";
		else if (indexName.equalsIgnoreCase("CNX 500"))
			return "CNX 500";
		else if (indexName.equalsIgnoreCase("CNX MIDCAP"))
			return "CNX MIDCAP";
		else if (indexName.equalsIgnoreCase("CNX IT"))
			return "CNX IT";
		else if (indexName.equalsIgnoreCase("BANK NIFTY"))
			return "BANK NIFTY";
		else if (indexName.equalsIgnoreCase("MIDCAP 50"))
			return "NIFTY MIDCAP 50";
		else if (indexName.equalsIgnoreCase("ESG INDIA IDX"))
			return "S&P ESG INDIA INDEX";
		else if (indexName.equalsIgnoreCase("SHARIAH"))
			return "CNX NIFTY SHARIAH";
		else if (indexName.equalsIgnoreCase("500 SHARIAH"))
			return "CNX 500 SHARIAH";
		else if (indexName.equalsIgnoreCase("CNX INFRA"))
			return "CNX INFRA";
		else if (indexName.equalsIgnoreCase("CNX REALTY"))
			return "CNX REALTY";
		else if (indexName.equalsIgnoreCase("CNX ENERGY"))
			return "CNX ENERGY";
		else if (indexName.equalsIgnoreCase("CNX MNC"))
			return "CNX MNC";
		else if (indexName.equalsIgnoreCase("CNX PHARMA"))
			return "CNX PHARMA";
		else if (indexName.equalsIgnoreCase("CNX PSE"))
			return "CNX PSE";
		else if (indexName.equalsIgnoreCase("CNX PSU BANK"))
			return "CNX PSU BANK";
		else if (indexName.equalsIgnoreCase("CNX SERVICE"))
			return "CNX SERVICE";
		else if (indexName.equalsIgnoreCase("CNX FMCG"))
			return "CNX FMCG";
		else if (indexName.equalsIgnoreCase("CNX 100"))
			return "CNX 100";
		else if (indexName.equalsIgnoreCase("CNX AUTO"))
			return "CNX AUTO";
		else if (indexName.equalsIgnoreCase("CNX FINANCE"))
			return "CNX FINANCE";
		else if (indexName.equalsIgnoreCase("CNX METAL"))
			return "CNX METAL";
		else if (indexName.equalsIgnoreCase("VIX"))
			return "INDIA VIX";
		return indexName;
	}
	
	public static String convertIndexSymbol(String indexSymbol){
		if (indexSymbol.equalsIgnoreCase("CNX NIFTY") || indexSymbol.equalsIgnoreCase("NIFTY 50"))
			return "NIFTY";
		else if (indexSymbol.equalsIgnoreCase("CNX NIFTY JUNIOR") || indexSymbol.equalsIgnoreCase("NIFTY NEXT 50"))
			return "JUNIOR";
		else if (indexSymbol.equalsIgnoreCase( "BANK NIFTY") || indexSymbol.equalsIgnoreCase( "NIFTY BANK"))
			return "BANKNIFTY";	 
		else if (indexSymbol.equalsIgnoreCase("CNX 100") || indexSymbol.equalsIgnoreCase("NIFTY 100"))
			return "NSE100";
		else if (indexSymbol.equalsIgnoreCase("CNX MIDCAP") || indexSymbol.equalsIgnoreCase("NIFTY MIDCAP 100"))
			return "NSEMIDCAP";
		else if (indexSymbol.equalsIgnoreCase("CNX IT") || indexSymbol.equalsIgnoreCase("NIFTY IT"))
			return "NSEIT";
		else if (indexSymbol.equalsIgnoreCase("CNX 500") || indexSymbol.equalsIgnoreCase("NIFTY 500"))
			return "NSE500";
		else if (indexSymbol.equalsIgnoreCase("CNX DEFTY"))
			return "NSEDEFTY";
		else if (indexSymbol.equalsIgnoreCase("NIFTY MIDCAP 50"))
			return "MIDCAP50";
		else if (indexSymbol.equalsIgnoreCase("S&P ESG INDIA INDEX"))
			return "NSEESG";
		else if (indexSymbol.equalsIgnoreCase("CNX NIFTY SHARIAH") || indexSymbol.equalsIgnoreCase("NIFTY50 SHARIAH"))
			return "NSESHARIAH";
		else if (indexSymbol.equalsIgnoreCase("CNX 500 SHARIAH") || indexSymbol.equalsIgnoreCase("NIFTY500 SHARIAH"))
			return "SHARIAH500";
		else if (indexSymbol.equalsIgnoreCase("CNX INFRA") || indexSymbol.equalsIgnoreCase("NIFTY INFRA"))
			return "NSEINFRA";
		else if (indexSymbol.equalsIgnoreCase("CNX REALTY") || indexSymbol.equalsIgnoreCase("NIFTY REALTY"))
			return "NSEREALTY";
		else if (indexSymbol.equalsIgnoreCase("CNX ENERGY") || indexSymbol.equalsIgnoreCase("NIFTY ENERGY"))
			return "NSEENERGY";
		else if (indexSymbol.equalsIgnoreCase("CNX FMCG") || indexSymbol.equalsIgnoreCase("NIFTY FMCG"))
			return "NSEFMCG";
		else if (indexSymbol.equalsIgnoreCase("CNX MNC") || indexSymbol.equalsIgnoreCase("NIFTY MNC"))
			return "NSEMNC";
		else if (indexSymbol.equalsIgnoreCase("CNX PHARMA") || indexSymbol.equalsIgnoreCase("NIFTY PHARMA"))
			return "NSEPHARMA";
		else if (indexSymbol.equalsIgnoreCase("CNX PSE") || indexSymbol.equalsIgnoreCase("NIFTY PSE"))
			return "NSEPSE";
		else if (indexSymbol.equalsIgnoreCase("CNX PSU BANK") || indexSymbol.equalsIgnoreCase("NIFTY PSU BANK"))
			return "NSEPSUBANK";
		else if (indexSymbol.equalsIgnoreCase("CNX SERVICE") || indexSymbol.equalsIgnoreCase("NIFTY SERV SECTOR"))
			return "NSESERVICE";
		else if (indexSymbol.equalsIgnoreCase("CNX AUTO") || indexSymbol.equalsIgnoreCase("NIFTY AUTO"))
			return "NSEAUTO";
		else if (indexSymbol.equalsIgnoreCase("CNX FINANCE") || indexSymbol.equalsIgnoreCase("NIFTY FINANCE"))
			return "NSEFINANCE";
		else if (indexSymbol.equalsIgnoreCase("CNX METAL") || indexSymbol.equalsIgnoreCase("NIFTY METAL"))
			return "NSEMETAL";
		else if (indexSymbol.equalsIgnoreCase("INDIA VIX"))
			return "VIX";
		return indexSymbol;
	}
}
