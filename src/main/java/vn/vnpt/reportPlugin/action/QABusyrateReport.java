package vn.vnpt.reportPlugin.action;

import com.atlassian.jira.config.IssueTypeManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import vn.vnpt.reportPlugin.util.DateUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;

@Named("QABusyrateReport")
public class QABusyrateReport extends JiraWebActionSupport{

    @ComponentImport
    private final ProjectManager projectManager;
    @ComponentImport
    private final IssueTypeManager issueTypeManager;
    @ComponentImport
    private final ApplicationProperties applicationProperties;

    @Inject
    public QABusyrateReport(ProjectManager projectManager, IssueTypeManager issueTypeManager, ApplicationProperties applicationProperties) {
        this.projectManager = projectManager;
        this.issueTypeManager = issueTypeManager;
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

    public Collection<IssueType> getAllIssueType(){
        return issueTypeManager.getIssueTypes();
    }

    public Collection<Project> getProjectsByCategory(){
        return projectManager.getProjectsFromProjectCategory(projectManager.getProjectCategoryObjectByNameIgnoreCase("software"));
    }
}
