package models;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sql2o.Sql2oException;

import common.Queries;
import db.DBConnection;

public class ConfigurationWrapper {
	private List<Setting> all_settings;
	private List<Link> all_links;
	private List<BaseLink> base_links;
	private static ConfigurationWrapper instance;
	private static Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
	
	public List<Setting> get_all_settings(){
		return all_settings;
	}
	
	public List<Link> get_all_links(){
		return all_links;
	}
	
	public List<BaseLink> get_base_links(){
		return base_links;
	}
	
	private ConfigurationWrapper() {
		all_settings = new ArrayList<Setting>();
		all_links = new ArrayList<Link>();
		base_links = new ArrayList<BaseLink>();
	}
	
	public void add_setting(Setting s) {
		all_settings.add(s);
	}

	public void add_link(Link s) {
		all_links.add(s);
	}

	public void add_baselink(BaseLink s) {
		base_links.add(s);
	}

	public void update_all_settings(List<Setting> s) {
		all_settings = s;
	}

	public void update_all_links(List<Link> s) {
		all_links = s;
	}

	public void update_all_baselinks(List<BaseLink> s) {
		base_links = s;
	}
	
	
	public void override_and_save_to_db() throws SQLException {
		if(all_settings != null) {
			for(Setting s: all_settings) {
				try {					
					DBConnection.getConnection().createQuery(Queries.insertSetting()).bind(s).executeUpdate();
				}
				catch(Sql2oException ex) {
					if(ex.getMessage().contains("duplicate key")) {
						DBConnection.getConnection().createQuery(Queries.updateSetting()).bind(s).executeUpdate();
					}
				}
			}
		}
		if(base_links != null) {
			for(BaseLink s: base_links) {
				try {					
					DBConnection.getConnection().createQuery(Queries.insertBaseLink()).bind(s).executeUpdate();
				}
				catch(Sql2oException ex) {
					if(ex.getMessage().contains("duplicate key")) {
						DBConnection.getConnection().createQuery(Queries.updateBaseLink()).bind(s).executeUpdate();
					}
				}
			}
		}
		if(all_links != null) {
			for(Link s: all_links) {
				try {					
					DBConnection.getConnection().createQuery(Queries.insertLink()).bind(s).executeUpdate();
				}
				catch(Sql2oException ex) {
					if(ex.getMessage().contains("duplicate key")) {
						DBConnection.getConnection().createQuery(Queries.updateLink()).bind(s).executeUpdate();
					}
				}
			}
		}
		logger.log(Level.INFO, "All settings updated\n");
	}
	
	public void load_from_from_db() {
		try {
			update_all_baselinks(
					DBConnection.getConnection()
					.createQuery(Queries.readBaseLinks())
					.executeAndFetch(BaseLink.class)
			);
			
			update_all_links(
					DBConnection.getConnection()
					.createQuery(Queries.readNormalLinks())
					.executeAndFetch(Link.class)
			);

			update_all_settings(
					DBConnection.getConnection()
					.createQuery(Queries.readSettings())
					.executeAndFetch(Setting.class)
			);
		}
		catch (SQLException e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage());
		}
	}
	
	public static ConfigurationWrapper getInstance() {
		if(instance != null) {
			return instance;
		}
		else {
			instance = new ConfigurationWrapper();
			instance.load_from_from_db();
			return instance;
		}
	}
	
}
