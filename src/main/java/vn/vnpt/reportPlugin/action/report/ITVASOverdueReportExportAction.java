package vn.vnpt.reportPlugin.action.report;

import com.atlassian.jira.web.action.JiraWebActionSupport;
import vn.vnpt.reportPlugin.service.report.ITVASOverdueReportService;
import vn.vnpt.reportPlugin.service.report.ITVASWeeklyReportService;
import vn.vnpt.reportPlugin.service.report.QABusyRateWeeklyService;

import javax.inject.Inject;
import javax.servlet.ServletOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class ITVASOverdueReportExportAction extends JiraWebActionSupport {

    private String projects;
    private String projectCategories;
    private String toDate;
    private String fromDate;

    private final ITVASOverdueReportService itvasOverdueReportService;

    @Inject
    public ITVASOverdueReportExportAction(ITVASOverdueReportService itvasOverdueReportService) {
        this.itvasOverdueReportService = itvasOverdueReportService;
    }

    @Override
    public String execute() throws Exception {
        try {
            DateFormat df = new SimpleDateFormat("ddMMMyy");
            Date today = Calendar.getInstance().getTime();
            String reportDate = df.format(today);

            String fileName = "Overdue_report_ITVAS_";
            ServletOutputStream outputStream;
            fileName = fileName + reportDate + ".xlsx";
            File fileExport = itvasOverdueReportService.exportITVASOverdueReport(fileName,
                    projects, projectCategories, fromDate, toDate);

            System.out.println("date: " + fromDate + "to: " + toDate);
            getHttpResponse().setContentType("application/xlsx");
            getHttpResponse().setContentLength(new Long(fileExport.length()).intValue());
            getHttpResponse().setBufferSize(4096);
            // getHttpResponse().setBufferSize(new Long(fileExport.length()).intValue());
            getHttpResponse().setHeader("Content-Disposition", "attachment; filename=" + fileExport.getName());
            outputStream = getHttpResponse().getOutputStream();

//            com.aspose.cells.License excelLicense = new com.aspose.cells.License();

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
