package vn.vnpt.reportPlugin.service.report.impl;

import com.aspose.cells.*;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.service.report.ITVASTroubleshootingTimeReportService;
import vn.vnpt.reportPlugin.util.DateUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.util.*;

@Named("ITVASTroubleshootingTimeReportServiceImpl")
public class ITVASTroubleshootingTimeReportServiceImpl implements ITVASTroubleshootingTimeReportService {

    @ComponentImport
    private final IssueManager issueManager ;
    @ComponentImport
    private final ProjectManager projectManager;
    @ComponentImport
    private final CustomFieldManager customFieldManager;
    @ComponentImport
    private final ChangeHistoryManager changeHistoryManager;

    private final String INCIDENT_SEVERITY = "customfield_10505";
    private final String BIEU_HIEN_SU_CO = "customfield_11302";

    @Inject
    public ITVASTroubleshootingTimeReportServiceImpl(IssueManager issueManager, ProjectManager projectManager,
                                                     CustomFieldManager customFieldManager,
                                                     ChangeHistoryManager changeHistoryManager) {
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.customFieldManager = customFieldManager;
        this.changeHistoryManager = changeHistoryManager;
    }

    /**
    * Function get file export
    * Get Incident ticket of project which belong to project Category or list selected project, all ticket have created
    * date between start date and end date.
     * Tính tổng thời gian xử lý sự cố (tính theo phút)
     * với các ticket đã đóng: thời gian xl = thời gian đóng - thời gian tạo
     * các ticket đã resolved: thời gian xl = resolved date - created date
     * các ticket ở trạng thái còn lại (khác cancelled): thời gian xl = thời điểm xuất báo cáo - created date
     * các sự cố đc tính thời gian là các sự cố có mức độ nghiêm trọng M0, M1 và có biểu hiện sự cố khác với 9. lỗi phần mềm
    * */

    public File exportITVASTroubleshootingTimeReport(String fileName, String project, String projectCategories, String startDate, String endDate) throws Exception {
        Date from = DateUtil.convertStringtoDate("dd/MMM/yy", startDate);
        Date to = DateUtil.convertStringtoDate("dd/MMM/yy", endDate);
        /*get list project*/
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

        InputStream is = getClass().getResourceAsStream("/templates/reportTemplates/Template_ITVAS_Troubleshooting_Time_Report.xlsx");

        Workbook workbook = new Workbook(is);
        Worksheet sheet = workbook.getWorksheets().get("Sheet1");
        setCellData(sheet.getCells().get("B" + 4), from);
        setCellData(sheet.getCells().get("B" + 5), to);
        int index = 10;
        if(projectList != null) {
            for (Project project1 : projectList) {
                setCellData(sheet.getCells().get("A" + index), project1.getName());
                long countClosedM0 = 0;
                long countResolvedM0 = 0;
                long countM0 = 0;
                long countClosedM1 = 0;
                long countResolvedM1 = 0;
                long countM1 = 0;
                /*get list issue in project*/
                Collection<Long> listIssueId = getIssueIdsForProject(project1.getId());
                if(listIssueId != null) {
                    for (Long issueId : listIssueId) {
                        Issue issue = issueManager.getIssueObject(issueId);
                        /*check created date between start and end date*/
                        if(!from.after(issue.getCreated()) && !to.before(issue.getCreated())) {
                            if (checkIncident(issue) && checkIncidentSeverity(issue)) {
                                /*get incident severity and check it*/
                                CustomField incidentSeverity = customFieldManager.getCustomFieldObject(INCIDENT_SEVERITY);
                                if(issue.getCustomFieldValue(incidentSeverity) != null) {
                                    if (checkIncidentSeverityM0(issue.getCustomFieldValue(incidentSeverity).toString())) {
                                        if (issue.getStatus().getName().equalsIgnoreCase("Closed")
                                                && issue.getResolution().getName().equalsIgnoreCase("Done")) {
                                            List<ChangeItemBean> itemBeans = changeHistoryManager.getChangeItemsForField(issue, "status");
                                            for (int i = itemBeans.size() - 1; i >= 0; i--) {
                                                if (itemBeans.get(i).getTo().equalsIgnoreCase("Closed")) {
                                                    countClosedM0 += itemBeans.get(i).getCreated().getTime() - issue.getCreated().getTime();
                                                    i = -1;
                                                }
                                            }
                                        } else {
                                            if (issue.getStatus().getName().equalsIgnoreCase("Resolved")
                                                    && issue.getResolution().getName().equalsIgnoreCase("Done")) {
                                                countResolvedM0 += issue.getResolutionDate().getTime() - issue.getCreated().getTime();
                                            } else {
                                                if (issue.getStatus().getName() != "Closed" && issue.getStatus().getName() != "Resolved"
                                                        && issue.getStatus().getName() != "Cancelled") {
                                                    countM0 += new Date().getTime() - issue.getCreated().getTime();
                                                }
                                            }
                                        }
                                    } else {
                                        if (checkIncidentSeverityM1(issue.getCustomFieldValue(incidentSeverity).toString())) {
                                            if (issue.getStatus().getName().equalsIgnoreCase("Closed")
                                                    && issue.getResolution().getName().equalsIgnoreCase("Done")) {
                                                List<ChangeItemBean> itemBeans = changeHistoryManager.getChangeItemsForField(issue, "status");
                                                for (int i = itemBeans.size() - 1; i >= 0; i--) {
                                                    if (itemBeans.get(i).getTo().equalsIgnoreCase("Closed")) {
                                                        countClosedM1 += itemBeans.get(i).getCreated().getTime() - issue.getCreated().getTime();
                                                        i = -1;
                                                    }
                                                }
                                            } else {
                                                if (issue.getStatus().getName().equalsIgnoreCase("Resolved")
                                                        && issue.getResolution().getName().equalsIgnoreCase("Done")) {
                                                    countResolvedM1 += issue.getResolutionDate().getTime() - issue.getCreated().getTime();
                                                } else {
                                                    if (issue.getStatus().getName() != "Closed" && issue.getStatus().getName() != "Resolved"
                                                            && issue.getStatus().getName() != "Cancelled") {
                                                        countM1 += new Date().getTime() - issue.getCreated().getTime();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    setCellData(sheet.getCells().get("B" + index), countM0 /(60*1000));
                    setCellData(sheet.getCells().get("C" + index), countResolvedM0 /(60*1000));
                    setCellData(sheet.getCells().get("D" + index), countClosedM0/(60*1000));
                    setCellData(sheet.getCells().get("E" + index), (countM0 + countResolvedM0 + countClosedM0)/(60*1000));
                    setCellData(sheet.getCells().get("F" + index), countM1 /(60*1000));
                    setCellData(sheet.getCells().get("G" + index), countResolvedM1/(60*1000));
                    setCellData(sheet.getCells().get("H" + index), countClosedM1/(60*1000));
                    setCellData(sheet.getCells().get("I" + index), (countM1 + countResolvedM1 + countClosedM1)/(60*1000));
                    index++;
                }
            }
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

    private boolean checkIncident(Issue issue){
        if (issue.getIssueType().getName().equalsIgnoreCase("Incident")||
                issue.getIssueType().getName().equalsIgnoreCase("Incident_NoNotifyTD")){
//        if (issue.getIssueType().getId().equalsIgnoreCase("10320")){
//        if (issue.getIssueType().getId().equalsIgnoreCase("10200")){
            return true;
        }
        return false;
    }

    private boolean checkIncidentSeverityM0(String incidentSeverity){
        if (incidentSeverity.contains("M0 -")) {
            return true;
        }
        return false;
    }
    private boolean checkIncidentSeverityM1(String incidentSeverity){
        if (incidentSeverity.contains("M1 -")) {
            return true;
        }
        return false;
    }

    private boolean checkIncidentSeverity(Issue issue){
//        CustomField incidentSeverity = customFieldManager.getCustomFieldObject(INCIDENT_SEVERITY);
        CustomField bieuHienSuCo = customFieldManager.getCustomFieldObject(BIEU_HIEN_SU_CO);
//        if((issue.getCustomFieldValue(incidentSeverity).equals("M0 - Sự cố đặc biệt nghiêm trọng")
//                || issue.getCustomFieldValue(incidentSeverity).equals("M1 - Sự cố nghiêm trọng"))
//                && (!issue.getCustomFieldValue(bieuHienSuCo).equals("9. Lỗi tính năng phần mềm")
//                && !issue.getCustomFieldValue(bieuHienSuCo).equals("10. Khác"))){
//            return true;
//        }
        System.out.println("bieuHienSuCo" + bieuHienSuCo.getFieldName());
        System.out.println("bieuHienSuCo1" + issue.getCustomFieldValue(bieuHienSuCo));
        if(issue.getCustomFieldValue(bieuHienSuCo) != null) {
            if (!issue.getCustomFieldValue(bieuHienSuCo).equals("9. Lỗi tính năng phần mềm")
                    && !issue.getCustomFieldValue(bieuHienSuCo).equals("10. Khác")) {
                return true;
            }
        }
        return false;
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
