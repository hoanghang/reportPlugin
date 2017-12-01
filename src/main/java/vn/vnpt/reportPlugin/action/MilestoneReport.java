package vn.vnpt.reportPlugin.action;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.type.ProjectType;
import com.atlassian.jira.project.type.ProjectTypeManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.util.DateUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named("MilestoneReport")
public class MilestoneReport  extends JiraWebActionSupport {

    @ComponentImport
    private final ProjectManager projectManager;
    @ComponentImport
    private final ProjectTypeManager projectTypeManager;
    @ComponentImport
    private final ApplicationProperties applicationProperties;

    @Inject
    public MilestoneReport(ProjectManager projectManager, ProjectTypeManager projectTypeManager,
                           ApplicationProperties applicationProperties) {
        this.projectManager = projectManager;
        this.projectTypeManager = projectTypeManager;
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
        return projectManager.getProjects();
    }

    public List<ProjectType> getAllProjectTypes(){
        return projectTypeManager.getAllProjectTypes();
    }

    public Collection<ProjectCategory> getAllProjectCategories(){
        return projectManager.getAllProjectCategories();
    }


}
