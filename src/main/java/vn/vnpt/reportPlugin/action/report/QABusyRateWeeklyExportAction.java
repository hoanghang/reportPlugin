package vn.vnpt.reportPlugin.action.report;

import com.aspose.cells.*;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
//import org.springframework.util.CollectionUtils;
import sun.misc.IOUtils;
import vn.vnpt.reportPlugin.service.QABusyRateWeeklyService;
//import org.springframework.util.FileCopyUtils;



import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;

@Named("QABusyRateWeeklyExportAction")
public class QABusyRateWeeklyExportAction extends JiraWebActionSupport {
    private String projects;
    private String issueType;
    private String toDate;
    private String fromDate;

    //    @ComponentImport
    private final QABusyRateWeeklyService qaBusyRateWeeklyService;

    @Inject
    public QABusyRateWeeklyExportAction(QABusyRateWeeklyService qaBusyRateWeeklyService) {
        this.qaBusyRateWeeklyService = qaBusyRateWeeklyService;
    }

    @Override
    public String execute() throws Exception {
        try {
            String fileName = "Template.xlsx";
            ServletOutputStream outputStream;
            File fileExport = qaBusyRateWeeklyService.exportQABusyRateWeeklyReport(fileName,
                    projects, issueType, toDate, fromDate);

            ArrayList list = new ArrayList();
//            CollectionUtils.isEmpty(list);

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
            return "error";
        }
    }

    public String getProjects() {
        return projects;
    }

    public void setProjects(String projects) {
        this.projects = projects;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
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

    private void setCellData(Cell cell, Object value) {
        Style style = cell.getStyle();
        style.setBackgroundColor(Color.getWhite());
        style.setForegroundColor(Color.getWhite());
        Font font = style.getFont();
        font.setBold(false);
        font.setColor(Color.getBlack());
        cell.setStyle(style);
        cell.setValue(value);
    }
}
