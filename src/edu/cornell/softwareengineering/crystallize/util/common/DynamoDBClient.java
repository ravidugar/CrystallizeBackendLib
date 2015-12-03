package edu.cornell.softwareengineering.crystallize.util.common;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest;
import com.amazonaws.services.dynamodbv2.model.DescribeTableRequest;
import com.amazonaws.services.dynamodbv2.model.TableDescription;
import com.amazonaws.services.dynamodbv2.util.Tables;

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
	
    public static void addTable(String tableName) throws Exception {
    	if(dynamoClient == null) getDynamoClient();
    	try {
            // Create table if it does not exist yet
            if (Tables.doesTableExist(dynamoClient, tableName)) {
                System.out.println("Table " + tableName + " is already ACTIVE");
            } else {
                // Create a table with a primary hash key named 'name', which holds a string
                CreateTableRequest createTableRequest = new CreateTableRequest().withTableName(tableName);
                    TableDescription createdTableDescription = dynamoClient.createTable(createTableRequest).getTableDescription();
                System.out.println("Created Table: " + createdTableDescription);

                // Wait for it to become active
                System.out.println("Waiting for " + tableName + " to become ACTIVE...");
                Tables.awaitTableToBecomeActive(dynamoClient, tableName);
                
                // Describe our new table
                DescribeTableRequest describeTableRequest = new DescribeTableRequest().withTableName(tableName);
                TableDescription tableDescription = dynamoClient.describeTable(describeTableRequest).getTable();
                System.out.println("Table Description: " + tableDescription);
            }
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
    }	
}
