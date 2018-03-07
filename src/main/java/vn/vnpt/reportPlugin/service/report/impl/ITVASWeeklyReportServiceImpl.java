package vn.vnpt.reportPlugin.service.report.impl;

import com.aspose.cells.*;
import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.service.report.ITVASWeeklyReportService;
import vn.vnpt.reportPlugin.util.DateUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.util.*;

@Named("ITVASWeeklyReportServiceImpl")
public class ITVASWeeklyReportServiceImpl implements ITVASWeeklyReportService{

    @ComponentImport
    private final IssueManager issueManager ;
    @ComponentImport
    private final ProjectManager projectManager;
    @ComponentImport
    private final IssueTypeManager issueTypeManager;

    @Inject
    public ITVASWeeklyReportServiceImpl(IssueManager issueManager, ProjectManager projectManager, IssueTypeManager issueTypeManager) {
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.issueTypeManager = issueTypeManager;
    }


    public File exportITVASWeeklyReport(String fileName, String project, String types,
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

//        List<String> issueTypes = new ArrayList<String>();
//        if(!types.equalsIgnoreCase("all")){
//            String[] type = types.split(",");
//            issueTypes = Arrays.asList(type);
//        }else{
//            Collection<IssueType> typeList = issueTypeManager.getIssueTypes();
//            for (IssueType type : typeList){
//                issueTypes.add(type.getId());
//            }
//        }

        InputStream is = getClass().getResourceAsStream("/templates/reportTemplates/Template_ITVAS_Weekly_Report.xlsx");
        Workbook workbook = new Workbook(is);
        Worksheet sheet = workbook.getWorksheets().get("Sheet1");
        int index = 11;

        setCellData(sheet.getCells().get("B" + 4), from);
        setCellData(sheet.getCells().get("B" + 5), to);

        for(Project project1 : projectList){
            setCellData(sheet.getCells().get("A" + index), project1.getName());

//            Map<String, List<Issue>> issueMapByType = new HashMap<String, List<Issue>>();
//            issueMapByType.put("Service request", new ArrayList<Issue>());
//            issueMapByType.put("Change SD", new ArrayList<Issue>());
//            issueMapByType.put("Incident", new ArrayList<Issue>());

            int countSROpen = 0;
            int countSRResolved = 0;
            int countSRClosed = 0;
            int countChangeOpen = 0;
            int countChangeResolved = 0;
            int countChangeClosed = 0;
            int countIncidentOpen = 0;
            int countIncidentResolved = 0;
            int countIncidentClosed = 0;

            Collection<Long> listIssueId = getIssueIdsForProject(project1.getId());
            if(listIssueId != null) {
                for (Long issueId : listIssueId) {
                    Issue issue = issueManager.getIssueObject(issueId);
                    if(!from.after(issue.getCreated()) && !to.before(issue.getCreated())) {
                        if (issue.getIssueType().getName().contains("Incident")) {
                            if (issue.getStatus().getName().equalsIgnoreCase("Resolved")) {
                                countIncidentResolved++;
                            } else {
                                if (issue.getStatus().getName().equalsIgnoreCase("Closed") ||
                                        issue.getStatus().getName().equalsIgnoreCase("Cancelled")) {
                                    countIncidentClosed++;
                                } else {
                                    countIncidentOpen++;
                                }
                            }
                        } else {
                            if (issue.getIssueType().getName().contains("Change SD")) {
                                if (issue.getStatus().getName().equalsIgnoreCase("Resolved")) {
                                    countChangeResolved++;
                                } else {
                                    if (issue.getStatus().getName().equalsIgnoreCase("Closed") ||
                                            issue.getStatus().getName().equalsIgnoreCase("Cancelled")) {
                                        countChangeClosed++;
                                    } else {
                                        countChangeOpen++;
                                    }
                                }
                            } else {
                                if (issue.getIssueType().getName().contains("Service Request")) {
                                    if (issue.getStatus().getName().equalsIgnoreCase("Resolved")) {
                                        countSRResolved++;
                                    } else {
                                        if (issue.getStatus().getName().equalsIgnoreCase("Closed") ||
                                                issue.getStatus().getName().equalsIgnoreCase("Cancelled")) {
                                            countSRClosed++;
                                        } else {
                                            countSROpen++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            int sum = countSROpen + countSRResolved + countSRClosed + countChangeOpen + countChangeResolved +
                    countChangeClosed + countIncidentOpen + countIncidentResolved + countIncidentClosed;
            setCellData(sheet.getCells().get("B" + index), countSROpen);
            setCellData(sheet.getCells().get("C" + index), countSRResolved);
            setCellData(sheet.getCells().get("D" + index), countSRClosed);
            setCellData(sheet.getCells().get("E" + index), countChangeOpen);
            setCellData(sheet.getCells().get("F" + index), countChangeResolved);
            setCellData(sheet.getCells().get("G" + index), countChangeClosed);
            setCellData(sheet.getCells().get("H" + index), countIncidentOpen);
            setCellData(sheet.getCells().get("I" + index), countIncidentResolved);
            setCellData(sheet.getCells().get("J" + index), countIncidentClosed);
            setCellData(sheet.getCells().get("K" + index), sum);
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
