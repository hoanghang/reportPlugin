package vn.vnpt.reportPlugin.action.report;

import com.aspose.cells.*;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import sun.misc.IOUtils;
import vn.vnpt.reportPlugin.service.QABusyRateWeeklyService;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

@Named("IncidentsReportExportAction")
public class IncidentsReportExportAction extends JiraWebActionSupport {
    private String projects;
    private String projectCategories;
    private String toDate;
    private String fromDate;

    //    @ComponentImport
    private final QABusyRateWeeklyService qaBusyRateWeeklyService;

    @Inject
    public IncidentsReportExportAction(QABusyRateWeeklyService qaBusyRateWeeklyService) {
        this.qaBusyRateWeeklyService = qaBusyRateWeeklyService;
    }

    @Override
    public String execute() throws Exception {
        try {
            String fileName = "Incident_report_ITVAS.xlsx";
            ServletOutputStream outputStream;
            File fileExport = qaBusyRateWeeklyService.exportQABusyRateWeeklyReport(fileName,
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
