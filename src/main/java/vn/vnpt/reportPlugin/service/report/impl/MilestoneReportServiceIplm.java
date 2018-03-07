package vn.vnpt.reportPlugin.service.report.impl;

import com.aspose.cells.*;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.type.ProjectTypeManager;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.service.common.ProjectService;
import vn.vnpt.reportPlugin.service.report.MilestoneReportService;
import vn.vnpt.reportPlugin.util.DateUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Named("MilestoneReportServiceIplm")
public class MilestoneReportServiceIplm implements MilestoneReportService {

    @ComponentImport
    private final ProjectManager projectManager;
    @ComponentImport
    private final VersionManager versionManager;
    @ComponentImport
    private final ProjectTypeManager projectTypeManager;
    private final ProjectService projectService;

    @Inject
    public MilestoneReportServiceIplm(ProjectManager projectManager, VersionManager versionManager,
                                      ProjectTypeManager projectTypeManager, ProjectService projectService) {
        this.projectManager = projectManager;
        this.versionManager = versionManager;
        this.projectTypeManager = projectTypeManager;
        this.projectService = projectService;
    }

    public File exportMilestoneReport(String fileName, String projectCat, String projects, String projectType,
                                      String milestoneStatus, String toDate, String fromDate)  throws Exception{
        Date from = DateUtil.convertStringtoDate("dd/MMM/yy", fromDate);
        Date to = DateUtil.convertStringtoDate("dd/MMM/yy", toDate);
        List<String> versionStatuses = new ArrayList<String>();
        if(!milestoneStatus.equalsIgnoreCase("all")){
            String[] verStatus = milestoneStatus.split(",");
            versionStatuses = Arrays.asList(verStatus);
        }

        List<Project> projectList = new ArrayList<Project>();

        if(!projects.equalsIgnoreCase("all")){
            List<String> projectKeyList = new ArrayList<String>();
            String[] projectIds = projects.split(",");
            projectKeyList = Arrays.asList(projectIds);
            projectList = getProjectsByKey(projectKeyList);
        }else{
            projectList = projectService.getProjectByCategoriesAndTypes(projectCat,projectType);
        }

        InputStream is = getClass().getResourceAsStream("/templates/reportTemplates/MilestoneReportTemplate.xlsx");

        Workbook workbook = new Workbook(is);
        Worksheet sheet = workbook.getWorksheets().get("Sheet1");
        int index = 13;
        setCellData(sheet.getCells().get("B" + 3), projectType);
        setCellData(sheet.getCells().get("B" + 4), projectCat);
        setCellData(sheet.getCells().get("B" + 5), projects);
        setCellData(sheet.getCells().get("B" + 6), milestoneStatus);
        setCellData(sheet.getCells().get("B" + 7), from);
        setCellData(sheet.getCells().get("B" + 8), to);
        if(projectList.size()>0) {
            for (Project project : projectList) {
                System.out.println("project: ++++" + project.getName());
                List<Version> versions = versionManager.getVersions(project);
                if (versions.size() > 0) {
                    for (Version version : versions) {
                        try {
                            if (version.getStartDate() != null && version.getReleaseDate() != null && version.getStartDate().after(from) && version.getReleaseDate().before(to)) {
                                if (versionStatuses.size() > 0) {
                                    for (String s : versionStatuses) {
                                        if ((version.isReleased() && s.equalsIgnoreCase("RELEASED")) ||
                                                (!version.isReleased() && s.equalsIgnoreCase("UNRELEASED"))) {
                                            setCellData(sheet.getCells().get("A" + index), project.getProjectTypeKey().getKey());
                                            setCellData(sheet.getCells().get("B" + index), project.getProjectCategory().getName());
                                            setCellData(sheet.getCells().get("C" + index), project.getName());
                                            setCellData(sheet.getCells().get("D" + index), version.getName());
                                            setCellData(sheet.getCells().get("E" + index), (version.isReleased()) ? "Released" : "Un Released");
                                            setCellData(sheet.getCells().get("F" + index), version.getStartDate());
                                            setCellData(sheet.getCells().get("G" + index), version.getReleaseDate());
                                            setCellData(sheet.getCells().get("H" + index), version.getStartDate());
                                            setCellData(sheet.getCells().get("I" + index), version.getReleaseDate());
                                        }
                                    }
                                } else {
                                    setCellData(sheet.getCells().get("A" + index), project.getProjectTypeKey().getKey());
                                    setCellData(sheet.getCells().get("B" + index), project.getProjectCategory().getName());
                                    setCellData(sheet.getCells().get("C" + index), project.getName());
                                    setCellData(sheet.getCells().get("D" + index), version.getName());
                                    setCellData(sheet.getCells().get("E" + index), (version.isReleased()) ? "Released" : "Un Released");
                                    setCellData(sheet.getCells().get("F" + index), version.getStartDate());
                                    setCellData(sheet.getCells().get("G" + index), version.getReleaseDate());
                                    setCellData(sheet.getCells().get("H" + index), version.getStartDate());
                                    setCellData(sheet.getCells().get("I" + index), version.getReleaseDate());

                                }
                            }
                            index++;

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        workbook.save(fileName);
        return new File(fileName);
    }

    private List<Project> getProjectsByKey(List<String> keys){
        List<Project> projectList = new ArrayList<Project>();
        for (String s : keys){
            projectList.add(projectManager.getProjectByCurrentKey(s));
        }
        return projectList;
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
