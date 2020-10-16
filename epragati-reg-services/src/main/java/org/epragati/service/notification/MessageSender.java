package org.epragati.service.notification;

import java.util.Arrays;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.epragati.notification.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MessageSender {
	
	@Value("${notification.sms.queue}")
	private String smsQueue;
	
	@Value("${notification.email.queue}")
	private String emailQueue;
	
	@Value("${activemq.broker-url}")
	private String brokerUrl;
	
	
	private static final Logger logger = LoggerFactory.getLogger(MessageSender.class);

	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageProducer producer = null;
	private final static String SMS_QUEUE = "QUEUE.IN.sms";
	private final static String EMAIL_QUEUE = "QUEUE.IN.email";
	
	@Autowired
	private ActiveMQConnectionFactory activeMQConnectionFactory;

	public void sendSMSMessage(DestinationInfo destinationInfo) {

		try {
			
			connection = activeMQConnectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(SMS_QUEUE);
			producer = session.createProducer(destination);
			ObjectMessage objectMessage = session.createObjectMessage();
			objectMessage.setObject(destinationInfo);
			producer.setPriority(9);
			producer.send(objectMessage);

			if(connection!=null){
				connection.close();
			}

		} catch (JMSException e) {
			logger.error("Exception message is [{}]",e.getMessage());
		}
	}
	
	
	  public ActiveMQConnectionFactory activeMQConnectionFactory() {
	    ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
	    activeMQConnectionFactory.setTrustedPackages(Arrays.asList("org.epragati.notification","org.epragati.notification.response"));
	    activeMQConnectionFactory.setTrustAllPackages(true);
	    activeMQConnectionFactory.setBrokerURL(brokerUrl);

	    return activeMQConnectionFactory;
	  }
	
	
	public void sendEmailMessage(DestinationInfo destinationInfo) {

		try {/*
			
			activeMQConnectionFactory = this.activeMQConnectionFactory();
			connection = activeMQConnectionFactory.createConnection();
			connection.start();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue(EMAIL_QUEUE);
			producer = session.createProducer(destination);
			ObjectMessage objectMessage = session.createObjectMessage();
			objectMessage.setObject(destinationInfo);
			producer.send(objectMessage);
			
			if(connection!=null){
				connection.close();
			}

		*/} catch (Exception e) {
			logger.error("Exception message is [{}]",e.getMessage());
		}
	}
}

