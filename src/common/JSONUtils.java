package common;

import java.io.File;
import java.io.IOException;
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

import models.BaseLink;
import models.ConfigurationWrapper;
import models.Link;
import models.Setting;

public class JSONUtils {
	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	private static JsonNode read_file(String location) {
		JsonNode json = null;
		ObjectMapper om = new ObjectMapper();
		File f = new File(location);
		if(f.canRead() == false) {
			logger.log(Level.FINER, "Do not have read permissions on file.");
			return null;
		}

		try {
			json = om.readTree(f);
		} catch (JsonProcessingException e) {
			logger.log(Level.FINER, "Jackson failed to parse JSON.");
			logger.log(Level.FINER, e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.log(Level.FINER, "IO Exception");
			logger.log(Level.FINER, e.getMessage());
			e.printStackTrace();
		}
		return json;
	}
	
	public static void read_settings_from_URL(String url) {
		// Read settings from URL and override in database
	}
	
	public static void read_settings_from_file(String location) {
		// Read settings from URL and override in database
	}
	
	public static void reset_configuration_from_URL(String url) {
		// Read all configurations from URL and override in database
	}
	
	public static void reset_configuration_from_file(String location) throws JsonParseException, JsonMappingException, IOException, SQLException {
		// Read all configurations from file and override in database
		JsonNode jnode = read_file(location);
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
		System.out.println(baselinks_list);
		ConfigurationWrapper global_instance = ConfigurationWrapper.getInstance();
		global_instance.update_all_settings(setting_list);
		global_instance.update_all_links(links_list);
		global_instance.update_all_baselinks(baselinks_list);
		
		global_instance.override_and_save_to_db();
	}
}
