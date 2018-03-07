package vn.vnpt.reportPlugin.service.report;

import java.io.File;

public interface QABusyRateWeeklyService {
    public File exportQABusyRateWeeklyReport(String fileName, String project,
                                             String projectCategories, String startDate, String endDate) throws Exception;
}
