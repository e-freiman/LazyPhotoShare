package service.database;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

import java.util.List;
import java.util.stream.Collectors;

public class DBProxy {
    private static final String TABLE_NAME = "ChatsTable";
    private static final String CHAT_ID = "chat_id";
    private static final String ENDPOINT_TEMPLATE = "dynamodb.%s.amazonaws.com";

    private Table chatRecordsTableCache = null;
    private AmazonDynamoDB dynamoDBClientCache = null;

    private AmazonDynamoDB getDynamoDBClient() {
        if (dynamoDBClientCache == null) {
            dynamoDBClientCache = AmazonDynamoDBClientBuilder.standard()
                    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
                            String.format(ENDPOINT_TEMPLATE, System.getenv("AWS_REGION")), System.getenv("AWS_REGION")))
                    .build();
        }
        return dynamoDBClientCache;
    }

    private Table getChatRecordsTable() {
        if (chatRecordsTableCache == null) {
            DynamoDB dynamoDB = new DynamoDB(getDynamoDBClient());
            chatRecordsTableCache = dynamoDB.getTable(TABLE_NAME);
        }
        return chatRecordsTableCache;
    }

    public void put(final Chat record) {
        getChatRecordsTable().putItem(new Item().withPrimaryKey(CHAT_ID, record.getChatId()));
    }

    public List<Chat> scan() {
        ScanRequest req = new ScanRequest();
        req.setTableName(TABLE_NAME);
        ScanResult scanResult = getDynamoDBClient().scan(req);

        return scanResult.getItems().stream()
                .flatMap(entry -> entry.entrySet().stream())
                .map(entry -> new Chat(Long.parseLong(entry.getValue().getN())))
                .collect(Collectors.toList());
    }
}
