package vn.vnpt.reportPlugin.service.report;

import java.io.File;

public interface ITVASOverdueReportService {

    public File exportITVASOverdueReport(String fileName, String project,
                                        String projectCategories, String startDate, String endDate) throws Exception;
}
