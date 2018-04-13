package vn.vnpt.reportPlugin.service.report;

import java.io.File;

public interface ITVASTroubleshootingTimeReportService {
    public File exportITVASTroubleshootingTimeReport(String fileName, String project,
                                             String projectCategories, String startDate, String endDate) throws Exception;
}
