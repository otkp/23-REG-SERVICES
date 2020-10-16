package org.epragati.reports.excel;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class ReportNameAndFieldOrdeServiceImpl implements ReportNameAndFieldOrdeService {
	@Autowired
	private ReportNameAndFieldOrderDAO dao;
	@Autowired
	private ReportNameAndFieldOrderMapper mapper;

	@Override
	public void addReport(ReportNameAndFieldOrderVO vo) throws Exception {
		dao.save(mapper.convertVO(vo));
	}

	@Override
	public Optional<ReportNameAndFieldOrderVO> getReport(String reportName) {
		return mapper.convertEntity(dao.findByReportName(reportName));
	}


}
