package vn.vnpt.reportPlugin.entities;

public class MyObjectMapper {
    private long id;
    private String name;

    public MyObjectMapper(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public MyObjectMapper() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
