package org.epragati.service.notification;

import javax.jms.Destination;

import org.epragati.notification.response.NotificationResponse;

public interface NotificationService {
	
	/**
	 * Sends Notification
	 * @param notificationResponse
	 */
	public void sendMessage(Destination destinationInfo);
	
	/**
	 *  Saves Notification Response
	 * @param notificationResponse
	 */
	public void receiveMessage(NotificationResponse notificationResponse);
}
