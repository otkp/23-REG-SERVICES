package org.epragati.service.notification.impl;

import java.time.LocalDateTime;

import javax.jms.Destination;

import org.epragati.common.dao.NotificationLogDAO;
import org.epragati.common.dto.NotificationLogDTO;
import org.epragati.notification.response.NotificationResponse;
import org.epragati.service.notification.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

	private static final Logger logger = LoggerFactory.getLogger(NotificationServiceImpl.class);

	@Autowired
	private NotificationLogDAO notificationLogDAO;

	/**
	 * Sends Notification
	 * @param notificationResponse
	 */

	@Override
	public void sendMessage(Destination destinationInfo) {

	}

	/**
	 *  Saves Notification Response
	 * @param notificationResponse
	 */
	@Override
	public void receiveMessage(NotificationResponse notificationResponse) {

		NotificationLogDTO notificationDTO = new NotificationLogDTO();

		notificationDTO.setDateTime(LocalDateTime.now());
		notificationDTO.setMessage(notificationResponse.getMessage());
		notificationDTO.setNotificationType(notificationResponse.getProtocol());
		notificationDTO.setReceiver(notificationResponse.getTo());
		notificationDTO.setServiceId(notificationResponse.getServiceId());
		notificationDTO.setStatus(notificationResponse.getStatus());
		notificationDTO.setTransactionNumber(notificationResponse.getTransactionId());
		notificationDTO.setOperator("");
		
		logger.debug("Notification Responce Saved");
		notificationLogDAO.save(notificationDTO);

	}
}
