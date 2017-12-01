package vn.vnpt.reportPlugin.service;

import java.io.File;

public interface QABusyRateWeeklyService {
    public File exportQABusyRateWeeklyReport(String fileName, String project,
                                             String issueType, String startDate, String endDate) throws Exception;
}
