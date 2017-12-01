package vn.vnpt.reportPlugin.service.impl;

import com.aspose.cells.*;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.service.QABusyRateWeeklyService;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Named("QABusyRateWeeklyServiceIplm")
public class QABusyRateWeeklyServiceIplm implements QABusyRateWeeklyService {

    @ComponentImport
    private final IssueManager issueManager ;

    @Inject
    public QABusyRateWeeklyServiceIplm(IssueManager issueManager) {
        this.issueManager = issueManager;
    }

    public File exportQABusyRateWeeklyReport(String fileName, String project,
                                             String issueType, String startDate, String endDate) throws Exception {
        List<String> projectList = new ArrayList<String>();
        if(project != null){
            String[] projects = project.split(",");
            projectList = Arrays.asList(projects);
        }else {
            projectList = new ArrayList<String>();
        }

        InputStream is = getClass().getResourceAsStream("/templates/reportTemplates/reportTemplates/Book1.xlsx");
        Workbook workbook = new Workbook(is);
        Worksheet sheet = workbook.getWorksheets().get("Sheet1");
        int index = 3;
        int count = 1;
        Collection<Long> listIssue = getIssueId(projectList);
        if(listIssue.size() > 0) {
            for (Long id : listIssue) {
                Issue issue = issueManager.getIssueObject(id);
                setCellData(sheet.getCells().get("A" + index), count);
                setCellData(sheet.getCells().get("B" + index), issue.getKey());
                setCellData(sheet.getCells().get("C" + index), issue.getSummary());
                setCellData(sheet.getCells().get("D" + index), issue.getDescription());
                setCellData(sheet.getCells().get("E" + index), issue.getCreated());
                setCellData(sheet.getCells().get("F" + index), issue.getCreator().getName());
                setCellData(sheet.getCells().get("G" + index), issue.getStatus().getName());
                setCellData(sheet.getCells().get("H" + index), issueType);
                setCellData(sheet.getCells().get("I" + index), startDate);
                count++;
                index++;
            }
        }
        workbook.save(fileName);
        return new File(fileName);

    }

    public Collection<Long> getIssueId (List<String> projectList) {
        Collection<Long> listIssue = new ArrayList<Long>();
        if(projectList.size()>0) {
            for (String projectId : projectList) {
                try {
                    listIssue.addAll(issueManager.getIssueIdsForProject(Long.parseLong(projectId)));
                } catch (Exception e) {
                    return new ArrayList<Long>(1234);
                }
            }
        }else{
            return listIssue;
        }
        return listIssue;
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
