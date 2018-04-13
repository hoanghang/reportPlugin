package vn.vnpt.reportPlugin.service.report.impl;

import com.aspose.cells.*;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.service.report.ITVASOverdueReportService;
import vn.vnpt.reportPlugin.util.DateUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.*;

@Named("ITVASOverdueReportServiceImpl")
public class ITVASOverdueReportServiceImpl implements ITVASOverdueReportService{

    @ComponentImport
    private final IssueManager issueManager ;
    @ComponentImport
    private final ProjectManager projectManager;

    @Inject
    public ITVASOverdueReportServiceImpl(IssueManager issueManager, ProjectManager projectManager) {
        this.issueManager = issueManager;
        this.projectManager = projectManager;
    }

    public File exportITVASOverdueReport(String fileName, String project, String projectCategories, String startDate, String endDate) throws Exception {
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

        InputStream is = getClass().getResourceAsStream("/templates/reportTemplates/Template_ITVAS_Overdue_Report.xlsx");
        Workbook workbook = new Workbook(is);
        Worksheet sheetGeneral = workbook.getWorksheets().get("General");
        Worksheet sheetDetail = workbook.getWorksheets().get("Detail");
        int index = 8;
        int indexDetail = 2;

        setCellData(sheetGeneral.getCells().get("B" + 3), from);
        setCellData(sheetGeneral.getCells().get("B" + 4), to);

        Calendar c = new GregorianCalendar();
        c.set(Calendar.HOUR_OF_DAY, 0); //anything 0 - 23
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        Date d1 = c.getTime();
        System.out.println("date ++" + d1.getTime());

        for(Project project1 : projectList){
            setCellData(sheetGeneral.getCells().get("A" + index), project1.getName());

            Collection<Long> listIssueId = getIssueIdsForProject(project1.getId());
            int countSR = 0;
            int countChange = 0;
            int countIncident = 0;
            if(listIssueId != null) {
                for (Long issueId : listIssueId) {
                    Issue issue = issueManager.getIssueObject(issueId);
                    if(!issue.getCreated().after(to)){
                        if(!issue.getStatus().getName().equalsIgnoreCase("Closed")
                                && !issue.getStatus().getName() .equalsIgnoreCase("Cancelled")) {
                            if (issue.getDueDate() != null) {
//                                if (new Date(issue.getDueDate().getDate()).before(date)) {
                                if (issue.getDueDate().before(d1)) {
                                    System.out.println("duedate ++" + issue.getDueDate() +"---"+ issue.getKey());
                                    String typeName = issue.getIssueType().getName();
                                    if (typeName.contains("Change SD")) {
                                        countChange++;
                                    } else {
                                        if (typeName.contains("Incident")) {
                                            countIncident++;
                                        } else {
                                            if (typeName.contains("Service Request")) {
                                                countSR++;
                                            }
                                        }
                                    }
                                    if (typeName.contains("Change SD") || typeName.contains("Incident") || typeName.contains("Service Request")) {
                                        setCellData(sheetDetail.getCells().get("A" + indexDetail), issue.getIssueType().getName());
                                        setCellData(sheetDetail.getCells().get("B" + indexDetail), project1.getName());
                                        setCellData(sheetDetail.getCells().get("C" + indexDetail), issue.getKey());
                                        setCellData(sheetDetail.getCells().get("D" + indexDetail), issue.getSummary());
                                        setCellData(sheetDetail.getCells().get("E" + indexDetail), (issue.getAssignee() == null) ? "" : issue.getAssignee().getDisplayName());
                                        setCellData(sheetDetail.getCells().get("F" + indexDetail), issue.getReporter().getDisplayName());
                                        setCellData(sheetDetail.getCells().get("G" + indexDetail), issue.getPriority().getName());
                                        setCellData(sheetDetail.getCells().get("H" + indexDetail), issue.getStatus().getName());
                                        setCellData(sheetDetail.getCells().get("I" + indexDetail), issue.getCreated());
                                        setCellData(sheetDetail.getCells().get("J" + indexDetail), (issue.getDueDate() == null) ? "" : issue.getDueDate());
                                        setCellData(sheetDetail.getCells().get("K" + indexDetail), (issue.getResolutionDate() == null) ? "" : issue.getResolutionDate());
                                        indexDetail++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            setCellData(sheetGeneral.getCells().get("B" + index), countChange);
            setCellData(sheetGeneral.getCells().get("C" + index), countSR);
            setCellData(sheetGeneral.getCells().get("D" + index), countIncident);
            setCellData(sheetGeneral.getCells().get("E" + index), countIncident + countChange + countSR);
            index ++;
        }


        workbook.save(fileName);
        return new File(fileName);
    }

    private List<Project> getProjectList(List<String> projectKeyList){
        List<Project> projectList = new ArrayList<Project>();
        for(String s : projectKeyList) {
            projectList.add(projectManager.getProjectByCurrentKey(s));
        }
        return projectList;
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
