package vn.vnpt.reportPlugin.action.report;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import vn.vnpt.reportPlugin.service.report.ITVASTroubleshootingTimeReportService;
import vn.vnpt.reportPlugin.service.report.QABusyRateWeeklyService;

import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ITVASTroubleshootingTimeReportExportAction extends JiraWebActionSupport {

    private String projects;
    private String projectCategories;
    private String toDate;
    private String fromDate;

    private final ITVASTroubleshootingTimeReportService itvasTroubleshootingTimeReportService;

    public ITVASTroubleshootingTimeReportExportAction(ITVASTroubleshootingTimeReportService itvasTroubleshootingTimeReportService) {
        this.itvasTroubleshootingTimeReportService = itvasTroubleshootingTimeReportService;
    }

    @Override
    public String execute() throws Exception {
        try {
            DateFormat df = new SimpleDateFormat("ddMMMyy");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);

            String fileName = "ITVAS_Troubleshooting_Report_";
            ServletOutputStream outputStream;
            fileName = fileName + reportDate + ".xlsx";
            File fileExport = itvasTroubleshootingTimeReportService.exportITVASTroubleshootingTimeReport(fileName,
                    projects, projectCategories, fromDate, toDate);

            System.out.println("date: " + fromDate + "to: " + toDate);
            getHttpResponse().setContentType("application/xlsx");
            getHttpResponse().setContentLength(new Long(fileExport.length()).intValue());
            getHttpResponse().setBufferSize(4096);
            // getHttpResponse().setBufferSize(new Long(fileExport.length()).intValue());
            getHttpResponse().setHeader("Content-Disposition", "attachment; filename=" + fileExport.getName());
            outputStream = getHttpResponse().getOutputStream();

            FileInputStream input = new FileInputStream(fileExport);
            org.apache.commons.io.IOUtils.copy(input, outputStream);

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

    public String getProjectCategories() {
        return projectCategories;
    }

    public void setProjectCategories(String projectCategories) {
        this.projectCategories = projectCategories;
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
