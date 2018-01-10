package vn.vnpt.reportPlugin.rest;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import vn.vnpt.reportPlugin.entities.MyObjectMapper;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Path("/report")
public class ReportRestResource {

    @ComponentImport
    private final ProjectManager projectManager;
    private final static ObjectMapper MAPPER = new ObjectMapper();

    @Inject
    public ReportRestResource(ProjectManager projectManager) {
        this.projectManager = projectManager;
    }


    @GET
    @Path("/search/getProjectsByProjectCategories/{projectCategories}")
    @Produces({"application/json"})
    public Response getProjectsByCategories(@PathParam("projectCategories") String projectCategories) {
        Collection<Project> projectListTmp = new ArrayList<Project>();
        String[] projectCategoryList = null;
        if (!projectCategories.equalsIgnoreCase("all")) {
            projectCategoryList = projectCategories.split(",");
            for(int i = 0; i < projectCategoryList.length; i++) {
                ProjectCategory projectCategory = projectManager.getProjectCategory(Long.valueOf(projectCategoryList[i]));
                projectListTmp.addAll(projectManager.getProjectsFromProjectCategory(projectCategory));
            }
        }else{
            projectListTmp = projectManager.getProjects();
        }

        List<MyObjectMapper> objectMapperList = new ArrayList<MyObjectMapper>();
        for(Project project : projectListTmp){
            MyObjectMapper myObjectMapper = new MyObjectMapper();
            myObjectMapper.setKey(project.getKey());
            myObjectMapper.setName(project.getName());
            objectMapperList.add(myObjectMapper);
        }
        MAPPER.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return Response.ok(MAPPER.writeValueAsString(objectMapperList)) .build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.ok("null!").build();
        }
    }

    @GET
    @Path("/search/getProjectsByProjectTypes/{projectTypes}/{projectCategories}")
    @Produces({"application/json"})
    public Response getProjectsByCategoriesAndTypes(@PathParam("projectTypes") String projectTypes,
                                            @PathParam("projectCategories") String projectCategories) {
        String[] projectTypeList = null;
        String[] projectCategoryList = null;
        List<Project> projectList = new ArrayList<Project>();
        int count = 0;

        if (ObjectUtils.isNotEmpty(projectTypes)) {
            projectTypeList = projectTypes.split(",");
        }

        if (ObjectUtils.isNotEmpty(projectCategories)) {
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
                    if (count == 0) {
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
                    if (count == 0) {
                        projectList.add(project);
                    }
                }
            }
        }

        List<MyObjectMapper> objectMapperList = new ArrayList<MyObjectMapper>();
        for(Project project : projectList){
            MyObjectMapper myObjectMapper = new MyObjectMapper();
            myObjectMapper.setKey(project.getKey());
            myObjectMapper.setName(project.getName());
            objectMapperList.add(myObjectMapper);
        }
        MAPPER.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        try {
            return Response.ok(MAPPER.writeValueAsString(objectMapperList)).build();
        } catch (IOException e) {
            e.printStackTrace();
            return Response.ok("null!").build();
        }
    }
}
