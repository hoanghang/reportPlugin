package vn.vnpt.reportPlugin.action.report;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import vn.vnpt.reportPlugin.service.report.MilestoneReportService;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;

public class MilestoneExportAction extends JiraWebActionSupport {
    private String projects;
    private String projectCat;
    private String milestoneStatus;
    private String projectType;
    private String toDate;
    private String fromDate;

//    @ComponentImport
    private final MilestoneReportService milestoneReportService;

    @Inject
    public MilestoneExportAction( MilestoneReportService milestoneReportService) {
        this.milestoneReportService = milestoneReportService;
    }

    @Override
    public String execute() throws Exception {
        try {
            String fileName = "Milestone_report.xlsx";
            ServletOutputStream outputStream;
            File fileExport =
                    milestoneReportService.exportMilestoneReport(fileName, projectCat, projects, projectType,
                    milestoneStatus, toDate, fromDate);

            System.out.println("date: " + fromDate + "to: " + toDate);
            getHttpResponse().setContentType("application/xlsx");
            getHttpResponse().setContentLength(new Long(fileExport.length()).intValue());
            getHttpResponse().setBufferSize(4096);
            // getHttpResponse().setBufferSize(new Long(fileExport.length()).intValue());
            getHttpResponse().setHeader("Content-Disposition", "attachment; filename=" + fileExport.getName());
            outputStream = getHttpResponse().getOutputStream();

            FileInputStream input = new FileInputStream(fileExport);
            org.apache.commons.io.IOUtils.copy(input, outputStream);
//            byte[] buffer = new byte[4096];
//            int length;
//            while ((length = input.read(buffer)) > 0) {
//                outputStream.write(buffer, 0, length);
//            }

            outputStream.flush();
            outputStream.close();
            input.close();
            fileExport.delete();

            return "success";
        } catch (Exception ex) {
            ex.printStackTrace();
            return "error";
        }
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getProjectCat() {
        return projectCat;
    }

    public void setProjectCat(String projectCat) {
        this.projectCat = projectCat;
    }

    public String getMilestoneStatus() {
        return milestoneStatus;
    }

    public void setMilestoneStatus(String milestoneStatus) {
        this.milestoneStatus = milestoneStatus;
    }

    public String getProjectType() {
        return projectType;
    }

    public void setProjectType(String projectType) {
        this.projectType = projectType;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }
}
