package vn.vnpt.reportPlugin.service.report.impl;

import com.aspose.cells.*;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.service.report.QABusyRateWeeklyService;
import vn.vnpt.reportPlugin.util.DateUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.util.*;

@Named("QABusyRateWeeklyServiceIplm")
public class QABusyRateWeeklyServiceIplm implements QABusyRateWeeklyService {

    @ComponentImport
    private final IssueManager issueManager ;
    @ComponentImport
    private final ProjectManager projectManager;
    @ComponentImport
    private final CustomFieldManager customFieldManager;

    final String STATUS_OPEN = "Open";
    final String STATUS_IN_PROGRESS = "In progress";
    final String STATUS_RESOLVED = "Resolved";
    final String STATUS_CLOSED = "Closed";
    final String STATUS_CANCELLED = "Cancelled";
    final String CUSTOM_FIELD_INCIDENT_SEVERITY = "customfield_10505"; // LAB
//    final String CUSTOM_FIELD_INCIDENT_SEVERITY = "customfield_11000"; // test
//    final String CUSTOM_FIELD_INCIDENT_SEVERITY = "customfield_10100";  // local



    @Inject
    public QABusyRateWeeklyServiceIplm(IssueManager issueManager, ProjectManager projectManager, CustomFieldManager customFieldManager) {
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.customFieldManager = customFieldManager;
    }

    public File exportQABusyRateWeeklyReport(String fileName, String project,
                                             String projectCategories, String startDate, String endDate) throws Exception {
        Date from = DateUtil.convertStringtoDate("dd/MMM/yy", startDate);
        Date to = DateUtil.convertStringtoDate("dd/MMM/yy", endDate);

        List<Project> projectList = new ArrayList<Project>();
        if(!project.equalsIgnoreCase("all")){
            List<String> projectKeyList = new ArrayList<String>();
            String[] projects = project.split(",");
            projectKeyList = Arrays.asList(projects);
            projectList = getProjectList(projectKeyList);
        }else {
            if(!projectCategories.equalsIgnoreCase("all")){
                String[] projectCat = projectCategories.split(",");
                for(int i = 0; i < projectCat.length; i++) {
                    ProjectCategory projectCategory = projectManager.getProjectCategory(Long.valueOf(projectCat[i]));
                    projectList.addAll(projectManager.getProjectsFromProjectCategory(projectCategory));
                }
            }else{
                projectList = projectManager.getProjects();
            }
        }

        InputStream is = getClass().getResourceAsStream("/templates/reportTemplates/Template_incident_report_ITVAS.xlsx");

        Workbook workbook = new Workbook(is);
        Worksheet sheet = workbook.getWorksheets().get("General");
        Worksheet sheetDetail = workbook.getWorksheets().get("Detail");
        int index = 12;
        int indexDetail = 4;

        setCellData(sheet.getCells().get("B" + 4), from);
        setCellData(sheet.getCells().get("B" + 5), to);
        if(projectList != null) {
            for (Project project1 : projectList) {
                Collection<Long> listIssueId = getIssueIdsForProject(project1.getId());
                int countInProgress0 = 0;
                int countResolved0 = 0;
                int countClosed0 = 0;
                int countInProgress1 = 0;
                int countResolved1 = 0;
                int countClosed1 = 0;
                int countInProgress4 = 0;
                int countResolved4 = 0;
                int countClosed4 = 0;

                if(listIssueId != null) {
                    for (Long issueId : listIssueId) {
                        Issue issue = issueManager.getIssueObject(issueId);
                        if(!from.after(issue.getCreated()) && !to.before(issue.getCreated())) {
                            if (checkIncident(issue)) {
                                int checkSeverity = checkSeverityOfIncident(issue);
                                if (checkSeverity == 0) {
                                    if (issue.getStatus().getName().equalsIgnoreCase(STATUS_CLOSED) || issue.getStatus().getName().equalsIgnoreCase(STATUS_CANCELLED)) {
                                        countClosed0++;
                                    } else {
                                        if (issue.getStatus().getName().equalsIgnoreCase(STATUS_RESOLVED)) {
                                            countResolved0++;
                                        } else {
                                            countInProgress0++;
                                        }
                                    }
                                }
                                if (checkSeverity == 1) {
                                    if (issue.getStatus().getName().equalsIgnoreCase(STATUS_CLOSED) || issue.getStatus().getName().equalsIgnoreCase(STATUS_CANCELLED)) {
                                        countClosed1++;
                                    } else {
                                        if (issue.getStatus().getName().equalsIgnoreCase(STATUS_RESOLVED)) {
                                            countResolved1++;
                                        } else {
                                            countInProgress1++;
                                        }
                                    }
                                }
                                if (checkSeverity == 4) {
                                    if (issue.getStatus().getName().equalsIgnoreCase(STATUS_CLOSED) || issue.getStatus().getName().equalsIgnoreCase(STATUS_CANCELLED)) {
                                        countClosed4++;
                                    } else {
                                        if (issue.getStatus().getName().equalsIgnoreCase(STATUS_RESOLVED)) {
                                            countResolved4++;
                                        } else {
                                            countInProgress4++;
                                        }
                                    }
                                }

                                if(checkSeverity == 1 || checkSeverity == 0 || checkSeverity == 4){
                                    setCellData(sheetDetail.getCells().get("A" + indexDetail), project1.getName());
                                    setCellData(sheetDetail.getCells().get("B" + indexDetail), issue.getKey());
                                    setCellData(sheetDetail.getCells().get("C" + indexDetail), issue.getSummary());
                                    setCellData(sheetDetail.getCells().get("D" + indexDetail), issue.getStatus().getName());
                                    setCellData(sheetDetail.getCells().get("E" + indexDetail), issue.getReporter().getDisplayName());
                                    setCellData(sheetDetail.getCells().get("F" + indexDetail), (issue.getAssignee() == null) ? "" : issue.getAssignee().getDisplayName());
                                    setCellData(sheetDetail.getCells().get("G" + indexDetail), issue.getCreated());
                                    setCellData(sheetDetail.getCells().get("H" + indexDetail), (issue.getDueDate()== null) ? "" : issue.getDueDate());
                                    setCellData(sheetDetail.getCells().get("I" + indexDetail), issue.getCustomFieldValue(
                                            customFieldManager.	getCustomFieldObject(CUSTOM_FIELD_INCIDENT_SEVERITY)).toString());
                                    indexDetail++;
                                }
                            }
                        }
                    }
                    int sum = countInProgress0 + countResolved0 + countClosed0 +
                            countInProgress1 + countResolved1 + countClosed1 +
                            countInProgress4 + countResolved4 + countClosed4 ;
                    setCellData(sheet.getCells().get("A" + index), project1.getName());
                    setCellData(sheet.getCells().get("B" + index), countInProgress0);
                    setCellData(sheet.getCells().get("C" + index), countResolved0);
                    setCellData(sheet.getCells().get("D" + index), countClosed0);
                    setCellData(sheet.getCells().get("E" + index), countInProgress1);
                    setCellData(sheet.getCells().get("F" + index), countResolved1);
                    setCellData(sheet.getCells().get("G" + index), countClosed1);
                    setCellData(sheet.getCells().get("H" + index), countInProgress4);
                    setCellData(sheet.getCells().get("I" + index), countResolved4);
                    setCellData(sheet.getCells().get("J" + index), countClosed4);
                    setCellData(sheet.getCells().get("K" + index), sum);
                    index++;
                }
            }
        }
        workbook.save(fileName);
        return new File(fileName);

    }


    private int checkSeverityOfIncident(Issue issue){
		try {
            CustomField field = customFieldManager.getCustomFieldObject(CUSTOM_FIELD_INCIDENT_SEVERITY);
            String value = issue.getCustomFieldValue(field).toString();
            if (value.contains("M0")) {
                return 0;
            }
            if (value.contains("M1")) {
                return 1;
            }
            if (value.contains("M4")) {
                return 4;
            }
        }catch (Exception ex){
            ex.printStackTrace();
            return -1;
        }
        return -1;
    }

    private List<Project> getProjectList(List<String> projectKeyList){
        List<Project> projectList = new ArrayList<Project>();
        for(String s : projectKeyList) {
            projectList.add(projectManager.getProjectByCurrentKey(s));
        }
        return projectList;
    }

    private List<String> getProjectIdList(List<Project> projects){
        List<String> projectIdList = new ArrayList<String>();
        for(Project project : projects){
            projectIdList.add(project.getId().toString());
        }
        return projectIdList;
    }

    private Collection<Long> getIssueIdsForProject (long projectId) {
        Collection<Long> listIssue = new ArrayList<Long>();
        try {
            listIssue.addAll(issueManager.getIssueIdsForProject(projectId));
        } catch (Exception e) {
            return new ArrayList<Long>(1234);
        }
        return listIssue;
    }

    private boolean checkIncident(Issue issue){
        if (issue.getIssueType().getName().equalsIgnoreCase("Incident")||
                issue.getIssueType().getName().equalsIgnoreCase("Incident_NoNotifyTD")){
//        if (issue.getIssueType().getId().equalsIgnoreCase("10320")){
//        if (issue.getIssueType().getId().equalsIgnoreCase("10200")){
            return true;
        }
        return false;
    }

    private Collection<Long> getIssueId (List<String> projectList) {
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
