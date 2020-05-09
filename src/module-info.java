/**
 * @author viresh
 *
 */
module StockD {
	exports common;
	exports main;
	exports models;

	requires org.apache.derby.engine;
	requires org.apache.derby.commons;
	requires javafx.base;
	requires javafx.controls;
	requires javafx.fxml;
	requires javafx.graphics;
	requires com.jfoenix;
	requires java.sql;
	requires java.logging;
	requires sql2o;
	requires jackson.databind;
	requires jackson.core;
}