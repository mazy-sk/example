package org.example;

import javax.annotation.Resource;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class MessageReceiverTest {

	@Resource(mappedName="jms/ConnectionFactory")
	private ConnectionFactory cf;
	
	@Resource(mappedName="jms/ExampleQueue")
	private Destination destination;
		
	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive jar = ShrinkWrap
				.create(JavaArchive.class, "example-ejb.jar")
				.addClasses(MessageReceiver.class, MessageRepository.class,
						MessageRepositoryImpl.class)
				.addAsManifestResource(EmptyAsset.INSTANCE,"beans.xml");
		System.out.println(jar.toString(true));
		return jar;
	}

	@Test
	public void testSendAllowedMessage() throws InterruptedException {
		Message request = sendMessage("Hello world",TextMessage.class);
		String response = receiveMessage(request);
		Assert.assertEquals("world", response);
	}
	
	@Test
	public void testSendWrongMessage() {
		Message request = sendMessage("Something",TextMessage.class);
		String response = receiveMessage(request);
		Assert.assertEquals("ERROR: Valid payload is only \"Hello world\"", response);
	}
	
	@Test
	public void testSendWrongTypeMessage() {
		Message request = sendMessage("MapMessage",MapMessage.class);
		String response = receiveMessage(request);
		Assert.assertTrue(response.contains("Allowed are only text messages!"));
	}
	

	private Message sendMessage(String msg, Class<? extends Message> messageType) {
		Message message = null;
		Connection connection = null;
		try {
			connection = cf.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			MessageProducer messageProducer = session.createProducer(destination);
			if (messageType != null && messageType.isAssignableFrom(TextMessage.class))
				message = session.createTextMessage(msg);
			else {
				MapMessage mapMessage = session.createMapMessage();
				mapMessage.setStringProperty("text", msg);
				message = mapMessage;
			}
			message.setJMSReplyTo(destination);
			messageProducer.send(message);
			System.out.println("Sent message id: " + message.getJMSMessageID());
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                }
            }
        }
		return message;
	}
	
	private String receiveMessage(Message request) {
		String msg = null;
		Connection connection = null;
		try {
			connection = cf.createConnection();
			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = request.getJMSReplyTo();
			if (destination == null)
				destination = this.destination;
			String messageSelector = "(JMSCorrelationID = '"+request.getJMSMessageID()+"')";
			System.out.println("Message selector: " + messageSelector);
			MessageConsumer messageConsumer = session.createConsumer(destination, messageSelector);
			connection.start();
			Message response = messageConsumer.receive(4000);
			if (response == null)
				msg = "Response is not found";
			else if (response instanceof TextMessage) {
				msg = ((TextMessage) response).getText();
			} else {
				msg = "Response is not type TextMessage";
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                }
            }
        }
		return msg;
	}

}
