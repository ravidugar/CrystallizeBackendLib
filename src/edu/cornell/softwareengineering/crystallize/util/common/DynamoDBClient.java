package edu.cornell.softwareengineering.crystallize.util.common;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;

public class DynamoDBClient {
	private static AmazonDynamoDBClient dynamoClient;
	private static DynamoDB dynamoDB;
	
	public static AmazonDynamoDBClient getDynamoClient() {
        if(dynamoClient == null) {
        	AWSCredentialsProvider credentialsProvider = new ClasspathPropertiesFileCredentialsProvider();
        	dynamoClient = new AmazonDynamoDBClient(credentialsProvider);
        	dynamoDB = new DynamoDB(dynamoClient);
        }
		return dynamoClient;
	}
	
	public static void closeDynamoClient() {
		if(dynamoClient != null) {
			dynamoClient.shutdown();
			dynamoClient = null;
		}
	}
	
	public static Table getTable(String tableName) {
		if(dynamoDB == null) {
			 getDynamoClient();
			 dynamoDB = new DynamoDB(dynamoClient);
		}
		return dynamoDB.getTable(tableName);
	}
}
