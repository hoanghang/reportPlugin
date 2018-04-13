package vn.vnpt.reportPlugin.action;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.util.DateUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Named("ITVASOverdueReport")
public class ITVASOverdueReport extends JiraWebActionSupport {

    @ComponentImport
    private final ProjectManager projectManager;
    @ComponentImport
    private final ApplicationProperties applicationProperties;

    @Inject
    public ITVASOverdueReport(ProjectManager projectManager, ApplicationProperties applicationProperties) {
        this.projectManager = projectManager;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public String execute() throws Exception {
        return "success";
    }

    public String getDateFormat() {
        return DateUtil.getJsDateFormat(applicationProperties);
    }

    public List<Project> getAllProject (){
        List<Project> projectList = projectManager.getProjects();
        List<Project> labs =  new ArrayList<Project>();
        for (Project project : projectList){
            try {
                if (project.getProjectCategory().getId() == 10000 || project.getProjectCategory().getId() == 10101 ||
                        project.getProjectCategory().getId() == 10104 || project.getProjectCategory().getId() == 10105) {
                    labs.add(project);
                    System.out.println("project" + project.getName());
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
        return labs;
    }

    public Collection<Project> getProjectsByCategory(){
        return projectManager.getProjectsFromProjectCategory(projectManager.getProjectCategoryObjectByNameIgnoreCase("software"));
    }

    public Collection<ProjectCategory> getAllProjectCategories(){
        return projectManager.getAllProjectCategories();
    }
}
