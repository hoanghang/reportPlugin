package vn.vnpt.reportPlugin.rest;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectCategory;
import com.atlassian.jira.project.ProjectManager;
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
    @Path("/search/orginationUnit/listbg/User/{projectCategories}")
    @Produces({"application/json"})
    public Response getOrginationUnitByBgsUser(@PathParam("projectCategories") Integer projectCategories) {
//        Collection<ProjectCategory> categories = projectManager.getAllProjectCategories();
        ProjectCategory projectCategory = projectManager.getProjectCategoryObject(Long.valueOf(projectCategories));

        Collection<Project> projectCollection =  projectManager.getProjectsFromProjectCategory(projectCategory);
        List<MyObjectMapper> objectMapperList = new ArrayList<MyObjectMapper>();
        for(Project project : projectCollection){
            MyObjectMapper myObjectMapper = new MyObjectMapper();
            myObjectMapper.setId(project.getId());
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

    @GET
    @Path("/search/getProjectCategory")
    @Produces({"application/json"})
    public Response getAllProjectCategories() {
        Collection<ProjectCategory> categories = projectManager.getAllProjectCategories();
        try {
            return Response.ok(MAPPER.writeValueAsString(categories)).build();
        } catch (IOException e) {
            e.printStackTrace();

            return Response.ok("a").build();
        }
    }
}
