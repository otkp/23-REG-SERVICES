
/**
 * 
 */
package org.epragati.reports.service.impl;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.epragati.reports.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

@Service
public class ReportQrCodeImpl implements ReportService {
	


	private static final Logger logger = LoggerFactory.getLogger(ReportServiceImpl.class);

	@Override
	public String sendPDF(String qrData) throws WriterException, IOException {
		String charset = "UTF-8";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BufferedImage bi = createQRCode(qrData, charset, 200, 200);
		ImageIO.write(bi, "png", baos);
		baos.flush();
		byte[] imageInByte = baos.toByteArray();
		baos.close();
		return Base64.encodeBase64String(imageInByte);

	}

	public String sendPDFURL(String  qrData) throws  WriterException, IOException {
		
		byte[] imageInByte =qrData.getBytes();
		return Base64.encodeBase64String(imageInByte);

	}

	public static BufferedImage createQRCode(String qrCodeData, String charset, int qrCodeheight, int qrCodewidth)
			throws WriterException, IOException {
		int BLACK = 0xFF000000;
		int WHITE = 0xFFFFFFFF;

		// Generate BitMatrix
		BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.getBytes(charset), charset),
				BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();

		// Converting BitMatrix to Buffered Image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
			}
		}
		return image;
	}
	public static BufferedImage createQRCodes(URL qrCodeData, String charset, int qrCodeheight, int qrCodewidth)
			throws WriterException, IOException {
		int BLACK = 0xFF000000;
		int WHITE = 0xFFFFFFFF;

		// Generate BitMatrix
		BitMatrix matrix = new MultiFormatWriter().encode(new String(qrCodeData.toString().getBytes(charset), charset),
				BarcodeFormat.QR_CODE, qrCodewidth, qrCodeheight);
		int width = matrix.getWidth();
		int height = matrix.getHeight();

		// Converting BitMatrix to Buffered Image
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
			}
		}
		return image;
	}

	@Override
	public String readQRCode(String filePath, Map hintMap)
			throws FileNotFoundException, IOException, NotFoundException {

		BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
				new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(convertPdfToImage(filePath))))));

		Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);

		return qrCodeResult.getText();
	}

	public String convertPdfToImage(String filePath) throws IOException {
		File pdfFile = new File(filePath + ".pdf");
		File imgFile = new File(filePath + ".png");
		PDDocument document = PDDocument.load(pdfFile);
		PDFRenderer renderer = new PDFRenderer(document);
		BufferedImage image = renderer.renderImageWithDPI(0, 600, ImageType.RGB);
		ImageIO.write(image, "png", new File(imgFile.getAbsolutePath()));
		document.close();

		return imgFile.getAbsolutePath();
	}
}
