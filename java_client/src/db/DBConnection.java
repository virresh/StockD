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
package db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sql2o.Connection;
import org.sql2o.Sql2o;

import common.Constants;
import common.Queries;
import main.FxApp;


public class DBConnection {
	private static Sql2o ds = null;
	private static Logger logger = FxApp.logger;
	// Replace data source with sql2o. Thus, if connection is made once, it'll never fail
	// on subsequent tries
	private static void initDataSource() throws SQLException{
		if(ds != null) {
			return;
		}
		String connstring = "jdbc:derby:" + System.getProperty("user.dir") + "/" + Constants.dbname + ";create=true";
		System.out.println(connstring);
		ds = new Sql2o(connstring, Constants.appname, Constants.appname);
		
		// perform sanity checks in case the database was newly created
		try(Connection conn = ds.open()){
			DatabaseMetaData dbm = conn.getJdbcConnection().getMetaData();
			ResultSet rs2 = dbm.getTables(null, null, Constants.settings, null);
			if(! rs2.next()) {
				conn.getJdbcConnection().createStatement().execute(Queries.createSettingsTable());
				logger.log(Level.INFO, "Created a new Settings Table.");
			}
			rs2.close();
			
			ResultSet rs3 = dbm.getTables(null, null, Constants.links, null);
			if(! rs3.next()) {
				conn.getJdbcConnection().createStatement().execute(Queries.createLinksTable());
				logger.log(Level.INFO, "Created a new Link Table.");
			}
			rs3.close();

			ResultSet rs1 = dbm.getTables(null, null, Constants.base, null);
			if(! rs1.next()) {
				conn.getJdbcConnection().createStatement().execute(Queries.createBaseLinkTable());
				logger.log(Level.INFO, "Created a new Base Link Table.");
			}
			rs1.close();
		}
		catch (Exception ex) {
			throw ex;
		}
	}

	public static synchronized Connection getConnection() throws SQLException{
		if(ds == null)
		{
			initDataSource();
		}
		return ds.open();
	}
}
