package org.epragati.reports.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import com.google.zxing.NotFoundException;
import com.google.zxing.WriterException;

public interface ReportService {

	public String sendPDF(String qrData)throws FileNotFoundException, WriterException, IOException ;
	public String sendPDFURL(String qrData)throws FileNotFoundException, WriterException, IOException ;
	
	public String readQRCode(String filePath,  Map hintMap) throws FileNotFoundException, IOException, NotFoundException;

      
}