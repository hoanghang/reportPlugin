package vn.vnpt.reportPlugin.service.common.impl;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.service.common.ProjectService;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Named("ProjectServiceImpl")
public class ProjectServiceImpl implements ProjectService{
    @ComponentImport
    private final ProjectManager projectManager;

    @Inject
    public ProjectServiceImpl(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }

    public List<Project> getProjectByCategoriesAndTypes(String projectCategories, String projectTypes) {
        String[] projectTypeList = null;
        String[] projectCategoryList = null;
        List<Project> projectList = new ArrayList<Project>();
        int count = 0;

        if (!projectTypes.equalsIgnoreCase("all")) {
            projectTypeList = projectTypes.split(",");
        }

        if (!projectCategories.equalsIgnoreCase("all")) {
            projectCategoryList = projectCategories.split(",");
        }
        if(projectCategoryList == null){
            if(projectTypeList == null) {
                projectList = projectManager.getProjects();
            }if(projectTypeList != null){
                List<Project> projectListTmp = projectManager.getProjects();
                for (Project project : projectListTmp){
                    String projectType = project.getProjectTypeKey().getKey();
                    for (int i = 0; i < projectTypeList.length; i++) {
                        count = 0;
                        if(projectType.equalsIgnoreCase(projectTypeList[i])){
                            count ++;
                        }
                    }
                    if (count != 0) {
                        projectList.add(project);
                    }
                }

            }
        }else{
            Collection<Project> projectListTmp = new ArrayList<Project>();
            for(int i = 0; i < projectCategoryList.length; i++) {
                ProjectCategory projectCategory = projectManager.getProjectCategory(Long.valueOf(projectCategoryList[i]));
                projectListTmp.addAll(projectManager.getProjectsFromProjectCategory(projectCategory));
            }

            if(projectTypeList == null) {
                projectList = new ArrayList<Project>(projectListTmp);
            }if(projectTypeList != null){
                for(Project project : projectListTmp){
                    String projectType = project.getProjectTypeKey().getKey();
                    for (int i = 0; i < projectTypeList.length; i++) {
                        count = 0;
                        if(projectType.equalsIgnoreCase(projectTypeList[i])){
                            count ++;
                        }
                    }
                    if (count != 0) {
                        projectList.add(project);
                    }
                }
            }
        }
        return projectList;
    }
}
