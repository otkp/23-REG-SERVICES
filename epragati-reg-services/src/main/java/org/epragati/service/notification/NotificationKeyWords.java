package org.epragati.service.notification;

public enum NotificationKeyWords {

	SMS("SMS"), EMAIL("EMAIL"), MESSAGE("message"), SUBJECT("subject"), TO("to"),transactionId("transactionId") ;

	private String value;

	NotificationKeyWords(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
