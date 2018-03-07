package vn.vnpt.reportPlugin.service.report;

import java.io.File;

public interface MilestoneReportService {
    public File exportMilestoneReport(String fileName, String projectCat, String projects, String projectType,
                                      String milestoneStatus, String toDate, String fromDate)  throws Exception;
}
