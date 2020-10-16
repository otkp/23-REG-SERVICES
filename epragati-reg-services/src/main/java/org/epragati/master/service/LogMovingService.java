package org.epragati.master.service;

public interface LogMovingService {

	public void moveStagingToLog(String applicationNo);

	public void movePaymnetsToLog(String applicationNo);

	public void moveRegServiceToLog(String applicationNo);
}
