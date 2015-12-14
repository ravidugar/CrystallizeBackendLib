package edu.cornell.softwareengineering.crystallize.test;

public class TestConstants {
	public static String baseURL = "http://crystallizebackendlib-dsj2xu3cnz.elasticbeanstalk.com";
	
	public static String getInsertURL() {
		return baseURL + "/Insert";
	}
	
	public static String getQueryURL() {
		return baseURL + "/Query";
	}
	
	public static String getDeleteURL() {
		return baseURL + "/Delete";
	}

	public static String getAddTableURL() {
		return baseURL + "/AddTable";
	}
	
	public static String getDeleteTableURL() {
		return baseURL + "/DeleteTable";
	}
}
