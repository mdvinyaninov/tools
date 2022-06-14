package iteco.devops.config;

import javax.xml.bind.annotation.XmlElement;
import java.util.List;

public class PanelConfig {
    private long id;
    private long width = 1000;
    private long height = 500;
    private String name;
    private List<String> vars;

    public long getId() { return id; }
    public String getName() { return name; }
    public long getWidth() { return width; }
    public long getHeight() { return height; }
    public List<String> getVars() { return vars; }

    @XmlElement(name = "Id")
    public void setId(long id) { this.id = id; }
    @XmlElement(name = "Name")
    public void setName(String name) { this.name = name; }
    @XmlElement(name = "Width")
    public void setWidth(long width) { this.width = width; }
    @XmlElement(name = "Height")
    public void setHeight(long height) { this.height = height; }
    @XmlElement(name = "Var")
    public void setVars(List<String> vars) { this.vars = vars; }
}
