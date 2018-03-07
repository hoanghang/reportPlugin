package vn.vnpt.reportPlugin.service.common;

import com.atlassian.jira.project.Project;

import java.util.List;

public interface ProjectService {
    public List<Project> getProjectByCategoriesAndTypes(String cat, String type);
}
