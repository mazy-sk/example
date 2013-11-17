package org.example;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.EJB;
import javax.ejb.MessageDriven;
import javax.ejb.MessageDrivenContext;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

@MessageDriven(mappedName = "jms/ExampleQueue", activationConfig = {
		@ActivationConfigProperty(propertyName = "messageSelector", propertyValue = "JMSCorrelationID IS NULL"),
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue") })
public class MessageReceiver implements MessageListener {

	@Resource(mappedName = "jms/ConnectionFactory")
	private ConnectionFactory cf;
	
	@Resource(mappedName="jms/ExampleQueue")
	private Destination destination;

	@Resource
	private MessageDrivenContext mdc;

	@EJB
	private MessageRepository messageRepo;

	@Override
	public void onMessage(Message message) {
		TextMessage msg = null;
		if (message instanceof TextMessage) {
			msg = (TextMessage) message;
			parseMessage(msg);
		} else {
			String resp = "Received message of wrong type: "
					+ message.getClass()+". Allowed are only text messages!";
			reply(message, resp);
			System.out.println(resp);
		}
	}

	private void parseMessage(TextMessage message) {
		try {
			String text = message.getText();
			messageRepo.addMessage(text);
			System.out.println("Received message: " + text);
			if (text != null && text.equalsIgnoreCase("Hello world"))
				reply(message, "world");
			else
				reply(message, "ERROR: Valid payload is only \"Hello world\"");
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			mdc.setRollbackOnly();
			e.printStackTrace();
		}
	}

	private void reply(Message message, String payload) {
		Connection connection = null;
		try {
			connection = cf.createConnection();
			Session session = connection.createSession(false,
					Session.AUTO_ACKNOWLEDGE);
			Destination destination = message.getJMSReplyTo();
			if (destination == null)
				destination = this.destination;
			MessageProducer messageProducer = session
					.createProducer(destination);
			Message response = session.createTextMessage(payload);
			response.setJMSCorrelationID(message.getJMSMessageID());
			messageProducer.send(response);
			System.out.println("Request message id: " + message.getJMSMessageID());
			System.out.println("Response message id: " + response.getJMSMessageID());
			System.out.println("Response message correlation id: " + response.getJMSCorrelationID());
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
	}

}
