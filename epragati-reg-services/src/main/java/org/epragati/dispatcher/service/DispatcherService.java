package org.epragati.dispatcher.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import org.dom4j.DocumentException;
import org.epragati.dispatcher.vo.InputVO;
import org.epragati.dispatcher.vo.UIFormatVO;

import com.google.zxing.WriterException; 
/**
 * 
 * @author roja.nadendla
 *
 */
public interface DispatcherService {

	/**
	 * Fetching
	 * 
	 * @param inputVo
	 * @return
	 */
	UIFormatVO fetchRecords(InputVO inputVo, String token, String officeCode);

	/**
	 * 
	 * @param qrData
	 * @return
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 * @throws WriterException
	 * @throws IOException
	 */
	public String sendPDF(String qrData) throws FileNotFoundException, DocumentException, WriterException, IOException;

	/**
	 * 
	 * @param qrData
	 * @return
	 * @throws FileNotFoundException
	 * @throws DocumentException
	 * @throws WriterException
	 * @throws IOException
	 */
	public String sendPDFURL(String qrData)
			throws FileNotFoundException, DocumentException, WriterException, IOException;

	/**
	 * 
	 * @param file
	 * @return
	 */
	String getFile(String file);

	/**
	 * 
	 * @param prNo
	 * @return
	 */
	Map<String, Object> getRegistrationDetails(String prNo);

}
