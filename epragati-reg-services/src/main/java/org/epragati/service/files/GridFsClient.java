package org.epragati.service.files;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.fa.dto.FinancialAssistanceDTO;
import org.epragati.fa.vo.FinancialAssistanceVO;
import org.epragati.images.vo.ImageInput;
import org.epragati.rta.service.impl.RTAServiceImpl;
import org.epragati.util.document.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSFile;

@Component
public class GridFsClient {

	private static final Logger logger = LoggerFactory.getLogger(GridFsClient.class);

	@Autowired
	private GridFsOperations operations;
	
	@Autowired
	private RTAServiceImpl  rtaServiceImpl;

	public List<KeyValue<String, List<ImageEnclosureDTO>>> convertImages(List<ImageInput> inputImages,
			String applicantNo, MultipartFile[] uploadfiles, String imageStatus) throws IOException {

		Map<String, List<ImageEnclosureDTO>> typeTodto = new HashMap<>();

		for (ImageInput imageInput : inputImages) {

			MultipartFile file = uploadfiles[imageInput.getFileOrder()];

			ImageEnclosureDTO imageDto = storeFileToGridFs(file, applicantNo, imageInput, imageStatus);

			List<ImageEnclosureDTO> dtos;
			if (typeTodto.containsKey(imageInput.getType())) {
				dtos = typeTodto.get(imageInput.getType());
			} else {
				dtos = new ArrayList<>();
				typeTodto.put(imageInput.getType(), dtos);
			}

			IOUtils.closeQuietly(file.getInputStream());

			dtos.add(imageDto);
		}

		return typeTodto.keySet().stream().map(k -> new KeyValue<String, List<ImageEnclosureDTO>>(k, typeTodto.get(k)))
				.collect(Collectors.toList());
	}

	public ImageEnclosureDTO storeFileToGridFs(MultipartFile file, String applicantNo, ImageInput imageInput,
			String imageStatus) throws IOException {

		ImageEnclosureDTO imageDto = convertFromInput(applicantNo, imageInput, file, imageStatus);
		try {
			String extension = FilenameUtils.getExtension(file.getOriginalFilename());
			DBObject metaData = convertToMetaData(applicantNo, imageInput, imageStatus, extension);

			GridFSFile fsFile = operations.store(file.getInputStream(), imageDto.getImageDetails(), metaData);
			logger.info("File Id - [{}]", fsFile.getId());

			imageDto.setImageId(fsFile.getId().toString());

			return imageDto;

		} finally {
			closeInputStream(file.getInputStream(), imageDto.getImageId());
		}
	}
	
	public List<ImageEnclosureDTO> saveFileForCollectionCorrection(MultipartFile[] files) throws IOException {

		List<ImageEnclosureDTO> list = new ArrayList<>();
		for (MultipartFile file : files) {
			ImageEnclosureDTO imageDto = new ImageEnclosureDTO();
			String fileName = new Date().getTime() + file.getOriginalFilename();
			GridFSFile fsFile = operations.store(file.getInputStream(), fileName, file.getContentType());
			imageDto.setImageId(fsFile.getId().toString());
			imageDto.setActive(true);
			imageDto.setEnclosureName(file.getOriginalFilename());
			list.add(imageDto);
		}
		return list;
	}

	private DBObject convertToMetaData(String applicantNo, ImageInput imageInput, String imageStatus,
			String imageType) {
		DBObject metaData = new BasicDBObject();
		metaData.put("applicantNo", applicantNo);
		metaData.put("type", imageInput.getType());
		metaData.put("fileOrder", imageInput.getFileOrder());
		metaData.put("pageNo", imageInput.getPageNo());
		metaData.put("enclosureName", imageInput.getEnclosureName());
		metaData.put("imageStaus", imageStatus);
		metaData.put("imageType", imageType);
		return metaData;
	}

	public void removeImages(List<ImageEnclosureDTO> pages) {

		pages.stream().forEach(page -> {

			Optional<GridFSDBFile> fileOptional = findFilesInGridFsById(page.getImageId());
			try {
				if (fileOptional.isPresent()) {

					operations.delete(new Query(GridFsCriteria.where("_id").is(page.getImageId())));
				}
			} finally {
				if(fileOptional.isPresent()) {
				closeInputStream(fileOptional.get().getInputStream(), page.getImageId());
				}
			}
		});
	}

	public Optional<String> updateFileToGridFs(MultipartFile file, String imageId, String imageStatus)
			throws IOException {

		Optional<GridFSDBFile> fileOptional = findFilesInGridFsById(imageId);

		if (fileOptional.isPresent()) {
			GridFSDBFile fsFile = fileOptional.get();
			try {

				DBObject metaData = fsFile.getMetaData();
				metaData.put("imageStaus", imageStatus);

				String fileName = fsFile.getFilename();
				operations.delete(new Query(GridFsCriteria.where("_id").is(imageId)));

				GridFSFile fsNewFile = operations.store(file.getInputStream(), fileName, metaData);
				logger.info("New File Id - [{}]", fsNewFile.getId());
				String fileId = fsNewFile.getId().toString();
				GridFsResource resource = operations.getResource(fsNewFile.getFilename());
				closeInputStream(resource.getInputStream(), fileId);
				return Optional.of(fsNewFile.getId().toString());
			} finally {
				closeInputStream(fsFile.getInputStream(), imageId);
			}

		}

		return Optional.empty();
	}

	public Optional<GridFSDBFile> findFilesInGridFsById(String id) {

		GridFSDBFile file = operations.findOne(new Query(GridFsCriteria.where("_id").is(id)));

		return Optional.ofNullable(file);
	}

	public ImageEnclosureDTO convertFromInput(String applicationNo, ImageInput imageInput, MultipartFile file,
			String imageStatus) {

		ImageEnclosureDTO imageDto = new ImageEnclosureDTO();

		String fileName = applicationNo + "-" + imageInput.getFileOrder() + "-" + imageInput.getPageNo();

		// Rename or add new filed of fileName
		imageDto.setImageDetails(fileName);

		imageDto.setReferenceId(applicationNo);
		imageDto.setImageType(imageInput.getType());
		imageDto.setPageNo(imageInput.getPageNo());
		imageDto.setEnclosureName(imageInput.getEnclosureName());
		imageDto.setImageStaus(imageStatus);
		imageDto.setBasedOnRole(imageInput.getBasedOnRole());
		if(imageInput.getQuarterNumber()!=null&&imageInput.getQuarterNumber()>0) {
			imageDto.setQuarterNumber(imageInput.getQuarterNumber());
		}
		if(StringUtils.isNoneBlank(imageInput.getLongitude())) {
			imageDto.setLongitude(imageInput.getLongitude());
		}
		if(StringUtils.isNoneBlank(imageInput.getLatitude())) {
			imageDto.setLatitude(imageInput.getLatitude());
		}
		if(imageInput.getTimestamp() !=null) {
			imageDto.setTimestamp(imageInput.getTimestamp());
		}
		if(StringUtils.isNoneBlank(imageInput.getStatus())) {
			imageDto.setImageStaus(imageInput.getStatus());
		}
		if(StringUtils.isNoneBlank(imageInput.getRemarks())) {
			imageDto.setImageComment(imageInput.getRemarks());
		}
		if(StringUtils.isNoneBlank(imageInput.getRemarks())) {
			imageDto.setRemarks(imageInput.getRemarks());
		}
		return imageDto;
	}

	public boolean deleteFileToGridFs(String imageId) {

		Optional<GridFSDBFile> fileOptional = findFilesInGridFsById(imageId);

		try {
			if (fileOptional.isPresent()) {
				operations.delete(new Query(GridFsCriteria.where("_id").is(imageId)));
				return true;
			}
		} finally {
			if (fileOptional.isPresent()) {
			closeInputStream(fileOptional.get().getInputStream(), imageId);
			}
		}

		return false;
	}
	public List<KeyValue<String, List<ImageEnclosureDTO>>> convertImagesForEibt(List<ImageInput> inputImages,
			String applicantNo, MultipartFile[] uploadfiles, String imageStatus) throws IOException {

		Map<String, List<ImageEnclosureDTO>> typeTodto = new HashMap<>();

		for (ImageInput imageInput : inputImages) {
			MultipartFile file = uploadfiles[imageInput.getFileOrder()];
			String extension = FilenameUtils.getExtension(file.getOriginalFilename());
			if(imageInput.getFileOrder().intValue() == 2 && imageInput.getType().equalsIgnoreCase("excel")) {
				if(!(extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls"))) {
					logger.error("Please upload excel");
					throw new BadRequestException("Please upload excel");
				}
				continue;
			}
			
			
			if((extension.equalsIgnoreCase("xlsx") || extension.equalsIgnoreCase("xls"))) {
				logger.error("Please upload only images");
				throw new BadRequestException("Please upload only images");
			}
			ImageEnclosureDTO imageDto = storeFileToGridFs(file, applicantNo, imageInput, imageStatus);

			List<ImageEnclosureDTO> dtos;
			if (typeTodto.containsKey(imageInput.getType())) {
				dtos = typeTodto.get(imageInput.getType());
			} else {
				dtos = new ArrayList<>();
				typeTodto.put(imageInput.getType(), dtos);
			}
			dtos.add(imageDto);
		}

		return typeTodto.keySet().stream().map(k -> new KeyValue<String, List<ImageEnclosureDTO>>(k, typeTodto.get(k)))
				.collect(Collectors.toList());
	}
	
	private void closeInputStream(InputStream inputStream, String imageId) {
		try {
			IOUtils.closeQuietly(inputStream);
			logger.debug("IO Input Stream Closed for Image [{}]: ",imageId);
		}catch (Exception e) {
			logger.info("Exception while  IOUtils.closeQuietly, for Image:{} ",imageId);
		}

	}
	public List<KeyValue<String, List<ImageEnclosureDTO>>> convertImagesForFa(List<ImageInput> inputImages,
			String applicantNo, MultipartFile[] uploadfiles, String imageStatus,FinancialAssistanceDTO financialAssistanceDTO,
			FinancialAssistanceVO financialAssistanceVO) throws IOException {

		Map<String, List<ImageEnclosureDTO>> typeTodto = new HashMap<>();

		for (ImageInput imageInput : inputImages) {

			MultipartFile file = uploadfiles[imageInput.getFileOrder()];
			
			 FinancialAssistanceVO fileOrderSize = rtaServiceImpl.getFileOrderSize(financialAssistanceDTO,financialAssistanceVO);
			 
			 List<ImageInput> images = fileOrderSize.getImages();

			ImageEnclosureDTO imageDto = storeFileToGridFs(file, applicantNo, images.get(0), imageStatus);

			List<ImageEnclosureDTO> dtos;
			if (typeTodto.containsKey(imageInput.getType())) {
				dtos = typeTodto.get(imageInput.getType());
			} else {
				dtos = new ArrayList<>();
				typeTodto.put(imageInput.getType(), dtos);
			}

			IOUtils.closeQuietly(file.getInputStream());

			dtos.add(imageDto);
		}

		return typeTodto.keySet().stream().map(k -> new KeyValue<String, List<ImageEnclosureDTO>>(k, typeTodto.get(k)))
				.collect(Collectors.toList());
	}
}
