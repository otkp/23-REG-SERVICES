package org.epragati.dispatcher.service;

import javax.servlet.http.HttpServletResponse;

public interface GenerateExcelDispatcherService {
	/***
	 * @param response
	 * @param officeCode
	 * @return
	 */
	

	void retriveAllRecords(HttpServletResponse response, String officeCode, String fromDates, String toDates);

}
