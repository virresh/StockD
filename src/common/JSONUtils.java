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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import main.FxApp;
import models.BaseLink;
import models.ConfigurationWrapper;
import models.Link;
import models.Setting;

public class JSONUtils {
	private static Logger logger = FxApp.logger;
	private static JsonNode read_file(String location) {
		JsonNode json = null;
		ObjectMapper om = new ObjectMapper();
		File f = new File(location);
		if(f.canRead() == false) {
			logger.log(Level.FINEST, "Do not have read permissions on file.");
			return null;
		}

		try {
			json = om.readTree(f);
		} catch (JsonProcessingException e) {
			logger.log(Level.FINEST, "Jackson failed to parse JSON.");
			logger.log(Level.FINEST, e.getMessage(), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.FINEST, "IO Exception");
			logger.log(Level.FINEST, e.getMessage());
			e.printStackTrace();
		}
		return json;
	}
	
	private static JsonNode read_inputstream(InputStream inp) {
		JsonNode json = null;
		ObjectMapper om = new ObjectMapper();
		try {
			json = om.readTree(inp);
		} catch (IOException e) {
			logger.log(Level.FINEST, "Unable to read input. Leaving configuration as-is.");
			logger.log(Level.FINEST, e.getMessage(), e);
			e.printStackTrace();
		}
		return json;
	}
	
	private static JsonNode read_url(String url) {
		JsonNode json = null;
		ObjectMapper om = new ObjectMapper();
		
		try {
			json = om.readTree(new URL(url));
		} catch (IOException e) {
			logger.log(Level.INFO, "Loading link profile from URL failed.");
			logger.log(Level.FINEST, "Unable to read url. Leaving configuration as-is.");
			logger.log(Level.FINEST, e.getMessage(), e);
			e.printStackTrace();
		}
		return json;
	}
	
	public static void reset_links_from_URL(String url) throws JsonMappingException, JsonProcessingException, SQLException {
		// Read link profile from URL and override in database
		JsonNode jnode = read_url(url);
		if(jnode == null) {
			return;
		}
		JsonNode baselink = jnode.get(Constants.base);
		JsonNode links = jnode.get(Constants.links);
		
		ObjectMapper om = new ObjectMapper();
		List<Link> links_list = null;
		List<BaseLink> baselinks_list = null;
		
		if(baselink != null) {			
			baselinks_list = om.readValue(baselink.toString(), new TypeReference<List<BaseLink>>(){});
		}
		if(links != null) {			
			links_list = om.readValue(links.toString(), new TypeReference<List<Link>>(){});
		}
		
		if(baselinks_list.size() >= 0 && links_list.size() > 0) {
			ConfigurationWrapper global_instance = ConfigurationWrapper.getInstance(true);
			global_instance.update_all_links(links_list);
			global_instance.update_all_baselinks(baselinks_list);
			
			global_instance.override_and_save_to_db();					
		}
		else {
			logger.log(Level.INFO, "Invalid Link Profile format. Not updating anything.");
		}
	}
	
	public static void reset_configuration_from_file(String location) throws JsonParseException, JsonMappingException, IOException, SQLException {
		JsonNode jnd = read_file(location);
		reset_configuration(jnd);
	}
	
	public static void reset_configuration_from_inputstream(InputStream ins) throws JsonParseException, JsonMappingException, IOException, SQLException {
		JsonNode jnd = read_inputstream(ins);
		reset_configuration(jnd);
	}

	public static void reset_configuration(JsonNode jnode) throws JsonParseException, JsonMappingException, IOException, SQLException {
		// Read all configurations from file and override in database
		JsonNode baselink = jnode.get(Constants.base);
		JsonNode settings = jnode.get(Constants.settings);
		JsonNode links = jnode.get(Constants.links);
		
		ObjectMapper om = new ObjectMapper();
		List<Setting> setting_list = null;
		List<Link> links_list = null;
		List<BaseLink> baselinks_list = null;
		
		if(baselink != null) {			
			baselinks_list = om.readValue(baselink.toString(), new TypeReference<List<BaseLink>>(){});
		}
		if(settings != null) {
			setting_list = om.readValue(settings.toString(), new TypeReference<List<Setting>>(){});			
		}
		if(links != null) {			
			links_list = om.readValue(links.toString(), new TypeReference<List<Link>>(){});
		}
		
		ConfigurationWrapper global_instance = ConfigurationWrapper.getInstance(true);
		global_instance.update_all_settings(setting_list);
		global_instance.update_all_links(links_list);
		global_instance.update_all_baselinks(baselinks_list);
		
		global_instance.override_and_save_to_db();
	}
}
