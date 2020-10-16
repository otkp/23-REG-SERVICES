package org.epragati.reports.excel;

import java.util.Optional;

public interface ReportNameAndFieldOrdeService {

	public void addReport(ReportNameAndFieldOrderVO vo) throws Exception;

	public Optional<ReportNameAndFieldOrderVO> getReport(String reportName);
}
