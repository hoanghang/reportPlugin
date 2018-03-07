package vn.vnpt.reportPlugin.service.report;

import java.io.File;

public interface ITVASWeeklyReportService {
    public File exportITVASWeeklyReport(String fileName, String project, String types,
                                             String projectCategories, String startDate, String endDate) throws Exception;
}
