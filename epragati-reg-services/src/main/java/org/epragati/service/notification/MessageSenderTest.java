package org.epragati.service.notification;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.Connection;
import org.epragati.notification.DestinationInfo;

public class MessageSenderTest {

	private ActiveMQConnectionFactory factory = null;
	private Connection connection = null;
	private Session session = null;
	private Destination destination = null;
	private MessageProducer producer = null;

	public MessageSenderTest() {

	}

	public DestinationInfo getEmail() throws IOException {

		DestinationInfo destInfo = new DestinationInfo();
		destInfo.setHost("smtp.gmail.com");
		destInfo.setPort("465");
		destInfo.setProtocol("smtps");
		
		/* * <to uri="smtps://smtp.gmail.com?username=YOURGMAILACCOUNT@gmail.com&amp;password=YourPassowrd&amp;to={header.To}&amp;subject=Mail From Stardust&amp;mail.smtp.port=465&amp;mail.smtp.ssl.socketFactory.port=465&amp;
		 * mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory"/>
		*/ 

		//destInfo.setSendAttachment(true);
		//String filePath = "C:\\Users\\pbattula\\Desktop\\Passport\\capture.png";
		//destInfo.setAttachment(Files.readAllBytes(new File(filePath).toPath()));

		
		destInfo.setHost("webmail.otsi-usa.com");
		destInfo.setPort("25");
		destInfo.setProtocol("smtp");
		 
		destInfo.setFileName("AirtelBill.pdf");
		Map<String, String> parameters = new HashMap<String, String>();
		destInfo.setParameters(parameters);
		destInfo.setEmailBody("hi this is test message");
	//	parameters.put("mail.smtp.ssl.socketFactory.port", "465");
		parameters.put("username", "ranjithnis01");
		parameters.put("from", "ranjithnis01@gmail.com");
		parameters.put("to", "pbattula@otsi-usa.com");
		//parameters.put("password", "Rr9052984280");
		parameters.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        parameters.put("mail.smtp.starttls.enable", "true");
       // parameters.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		parameters.put("subject", "LLR Service hello heyyyyyyyyyyyyyyyy  yo");
		//parameters.put("cc", "pbattula@otsi-usa.com");

		return destInfo;
	}

	public DestinationInfo getSMS() {

		DestinationInfo destInfo = new DestinationInfo();
		destInfo.setHost("msdgweb.mgov.gov.in/esms/sendsmsrequest");
		destInfo.setMessageType("sms");
		destInfo.setTransactionId("1");
		destInfo.setServiceId("LLR01");

		Map<String, String> parameters = new HashMap<String, String>();
		destInfo.setProtocol("http");
		parameters.put("mobileno", "9010893970");
		parameters.put("content", " APRTD Message Working @ " + new Date());
		parameters.put("smsservicetype", "singlemsg");
		parameters.put("password", "APCFST@123");
		parameters.put("username", "APTD-RMND");
		parameters.put("senderid", "APTDMG");
		destInfo.setParameters(parameters);
		System.out.println(destInfo);
		return destInfo;
	}

	public void sendMessage() {

		try {

			factory = new ActiveMQConnectionFactory("tcp://192.168.1.249:61616");

			factory.setTrustAllPackages(true);
			connection = factory.createConnection();
			connection.start();
			DestinationInfo destinationInfo = this.getSMS();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			destination = session.createQueue("QUEUE.IN.sms");
			producer = session.createProducer(destination);
			ObjectMessage objectMessage = session.createObjectMessage();
			objectMessage.setObject(destinationInfo);
			producer.send(objectMessage);

		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			connection.close();	
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		MessageSenderTest sender = new MessageSenderTest();
		sender.sendMessage();
	}
}
