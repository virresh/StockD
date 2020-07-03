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
