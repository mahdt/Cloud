package Cloud.Tweets;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;


import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.GetQueueUrlResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.likethecolor.alchemy.api.Client;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Executors;

import com.likethecolor.alchemy.api.Client;
import com.likethecolor.alchemy.api.call.*;
import com.likethecolor.alchemy.api.call.type.CallTypeText;
import com.likethecolor.alchemy.api.call.type.CallTypeUrl;
import com.likethecolor.alchemy.api.entity.HeaderAlchemyEntity;
import com.likethecolor.alchemy.api.entity.LanguageAlchemyEntity;
import com.likethecolor.alchemy.api.entity.Response;
import com.likethecolor.alchemy.api.entity.SentimentAlchemyEntity;

import org.apache.commons.lang.builder.ToStringStyle;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class Workers {

	private final Connection connection;
	public Workers() throws SQLException{
		try {
			Class.forName("com.mysql.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			System.out.println("Where is your MySQL JDBC Driver?");
			e.printStackTrace();
		}
		System.out.println("MySQL JDBC Driver Registered!");

		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/project1","root", "Garima@123");

		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Failed to make connection!");
		}
	}

	public Connection getConnection(){
		return connection;
	}

	public void closeConnection(){
		if(connection != null){
			try {
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}


	public void deleteMessage(ReceiveMessageResult res) {

	}

	public AmazonSQS createQ() {
		AWSCredentials credentials;
		try {
			credentials = new PropertiesCredentials(
					SimpleQueueServiceSample.class
					.getResourceAsStream("AwsCredentials.Properties"));

			// credentials = new
			// ProfileCredentialsProvider("~/.aws/AwsCredentials.Properties").getCredentials();
		} catch (Exception e) {
			throw new AmazonClientException(
					"Cannot load the credentials from the credential profiles file. "
							+ "Please make sure that your credentials file is at the correct "
							+ "location (/Users/daniel/.aws/credentials), and is in valid format.",
							e);
		}
		AmazonSQS rahul = new AmazonSQSClient(credentials);
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		rahul.setRegion(usWest2);
		return rahul;
	}

	public static void main(String[] args) throws InterruptedException, ExecutionException {
		Workers worker = null;
		try {
			worker = new Workers();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		Statement statement = null;
		ExecutorService service = Executors.newFixedThreadPool(10);
		AmazonSQS amz = worker.createQ();
		ReceiveMessageResult res = null;
		GetQueueUrlResult rahulQ = amz.getQueueUrl("MyQueue");
		String myQueueUrl = rahulQ.getQueueUrl();
		ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(
				myQueueUrl);
		receiveMessageRequest.setMaxNumberOfMessages(1);
		while(true) {
			List<Message> messages = amz.receiveMessage(receiveMessageRequest)
					.getMessages();
			for (int j = 0; j < messages.size(); j++) {
				JsonElement jsonelement = new JsonParser().parse(messages.get(j).getBody());
				JsonObject jobject = jsonelement.getAsJsonObject();
				String tweetId = jobject.get("id").toString();
				String tweetText = jobject.get("text").toString();
				Future future = service.submit(new AssignTask(tweetText));


				try{
					String sentiment = future.get().toString();
					System.out.println(sentiment);
					String insertSqlStatement = "UPDATE Tweets SET Sentiment = '" + sentiment + "' where TweetID = '"+ tweetId+"'";
					statement = worker.getConnection().createStatement();
					int x = statement.executeUpdate(insertSqlStatement);
					System.out.println("Record is updated into Tweets table!" + tweetId + " "+sentiment);
				}catch(SQLException e)
				{
					System.out.println(e.getMessage());
				}

				try {
					amz.deleteMessage(new DeleteMessageRequest(myQueueUrl,
							messages.get(j).getReceiptHandle()));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
	}

}

class AssignTask implements Callable<String> {

	private String res;
	public static final Logger LOGGER = Logger.getLogger(App.class.getCanonicalName());

	public AssignTask(String res) {
		this.res = res;

	}

	public String call() {
		System.out.println(res);
		String result = getSentiment(res);
		return result;
	}


	public String getSentiment(String res)
	{
		Client client = new Client();

		Properties alchemyProperties = new Properties();
		InputStream input = App.class.getClassLoader().getResourceAsStream("alchemy.properties");
		try {
			alchemyProperties.load(input);
		} catch (IOException ex) {
			System.out.println("byee");
			LOGGER.log(Level.SEVERE, "IOException Occurred when getting the alchemy.properties", ex);

			System.exit(-1);
		}

		client.setAPIKey(alchemyProperties.getProperty("alchemy.apiKey"));

		final AbstractCall<SentimentAlchemyEntity> abc= new SentimentCall(new CallTypeText(res));


		try {
			Response<SentimentAlchemyEntity> x =  (client.call(abc));//toString(ToStringStyle.MULTI_LINE_STYLE));
			Iterator<SentimentAlchemyEntity> y =  (x.iterator());
			while(y.hasNext())
			{
				SentimentAlchemyEntity z = y.next();
				System.out.println(z.getType().toString());
				return z.getType().toString();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "unknown";

	}

}