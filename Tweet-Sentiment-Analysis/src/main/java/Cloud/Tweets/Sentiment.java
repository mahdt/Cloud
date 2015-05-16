package Cloud.Tweets;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import twitter4j.Status;

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
import com.likethecolor.alchemy.api.Client;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
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


public class Sentiment {

	public static final Logger LOGGER = Logger.getLogger(App.class.getCanonicalName());
	public static void main(String[] args)
	{

        Client client = new Client();

        Properties alchemyProperties = new Properties();
        InputStream input = App.class.getClassLoader().getResourceAsStream("alchemy.properties");
        try {
            alchemyProperties.load(input);
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, "IOException Occurred when getting the alchemy.properties", ex);
            System.exit(-1);
        }

        alchemyProperties.setProperty("alchemy.apiKey", "396fbc1656335747c200d681989fba99e6ecd82b");
        System.out.println(alchemyProperties.getProperty("alchemy.apiKey"));
        client.setAPIKey(alchemyProperties.getProperty("alchemy.apiKey"));
        
        final AbstractCall<SentimentAlchemyEntity> abc= new SentimentCall(new CallTypeText("I am so sad today"));
        
        
        try {
			Response<SentimentAlchemyEntity> x =  (client.call(abc));//toString(ToStringStyle.MULTI_LINE_STYLE));
			Iterator<SentimentAlchemyEntity> y =  (x.iterator());
			while(y.hasNext())
			{
				SentimentAlchemyEntity z = y.next();
				System.out.println(z.getType());
			}
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
