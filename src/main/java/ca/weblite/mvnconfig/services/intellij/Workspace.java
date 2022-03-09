package ca.weblite.mvnconfig.services.intellij;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.models.ProjectTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Encapsulates an IntelliJ .idea/workspace.xml file for writing ProjectConfig
 * there.
 */
class Workspace {
    private final File projectDirectory;
    private final ProjectConfig config;

    public Workspace(File projectDirectory, ProjectConfig config) {
        this.projectDirectory = projectDirectory;
        this.config = config;
    }

    private File getWorkspaceXml() {
        return new File(projectDirectory, ".idea" + File.separator + "workspace.xml");
    }

    private Element findRunManagerElement(Document doc, boolean appendIfNotFound) {
        NodeList children = doc.getDocumentElement().getChildNodes();
        int len = children.getLength();
        for (int i=0; i<len; i++) {
            Node n = children.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element)n;
            if (e.getTagName().equals("component") && "RunManager".equals(e.getAttribute("name"))) {
                return e;
            }
        }
        if (appendIfNotFound) {
            Element e = doc.createElement("component");
            e.setAttribute("name", "RunManager");
            doc.getDocumentElement().appendChild(e);
            return e;
        }
        return null;
    }

    public void save() throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("Failed to write XML to "+getWorkspaceXml(), e);
        }
        Document doc;
        if (getWorkspaceXml().exists()) {
            try {
                doc = builder.parse(getWorkspaceXml());
            } catch (SAXException e) {
                throw new IOException("Failed to write xml to "+getWorkspaceXml(), e);
            }
        } else {
            try {
                doc = builder.parse(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project/>".getBytes(StandardCharsets.UTF_8)));
            } catch (SAXException e) {
                throw new IOException("Failed to write xml to "+getWorkspaceXml(), e);
            }
        }

        Element runManagerElement = findRunManagerElement(doc, true);
        for (ProjectTask task : config) {
            Element existingConfig = findRunConfigurationForTask(runManagerElement, task);
            if (existingConfig == null) {
                runManagerElement.appendChild(createRunConfigurationForTask(doc, task));
            } else {
                runManagerElement.replaceChild(createRunConfigurationForTask(doc, task), existingConfig);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (TransformerConfigurationException e) {
            throw new IOException("Failed to write XML to "+getWorkspaceXml(), e);
        }
        DOMSource source = new DOMSource(doc);
        File workspaceFile = getWorkspaceXml();
        if (!workspaceFile.getParentFile().exists()) {
            workspaceFile.getParentFile().mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(workspaceFile)) {
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IOException("Failed to write XML to "+workspaceFile, e);
        }


    }



    private Element findRunConfigurationForTask(Element runManagerElement, ProjectTask task) {
        NodeList children = runManagerElement.getChildNodes();
        int len = children.getLength();
        for (int i=0; i<len; i++) {
            Node n = children.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element)n;
            if (!"configuration".equals(e.getTagName())) {
                continue;
            }
            if (task.getLabel().equals(e.getAttribute("name"))) {
                return e;
            }
        }
        return null;
    }
    private Element createRunConfigurationForTask(Document doc, ProjectTask task) {
        Element configuration = doc.createElement("configuration");
        configuration.setAttribute("name", task.getLabel());
        configuration.setAttribute("type", "MavenRunConfiguration");
        configuration.setAttribute("factoryName", "Maven");
        if (task.getGroup() != null) {
            configuration.setAttribute("folderName", task.getGroup());
        }
        Element mavenSettings = doc.createElement("MavenSettings");
        configuration.appendChild(mavenSettings);
        {
            Element opt = doc.createElement("option");
            opt.setAttribute("name", "myGeneralSettings");
            mavenSettings.appendChild(opt);
        }
        {
            Element opt = doc.createElement("option");
            opt.setAttribute("name", "myRunnerSettings");
            mavenSettings.appendChild(opt);

            Element mavenRunnerSettings = doc.createElement("MavenRunnerSettings");

            opt.appendChild(mavenRunnerSettings);
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "delegateBuildToMaven");
                opt.setAttribute("value", "true");
                mavenRunnerSettings.appendChild(opt);
            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "environmentProperties");
                Element map = doc.createElement("map");
                opt.appendChild(map);
                mavenRunnerSettings.appendChild(opt);
            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "jreName");
                opt.setAttribute("value", "#USE_PROJECT_JDK");
                mavenRunnerSettings.appendChild(opt);
            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "mavenProperties");
                Element map = doc.createElement("map");
                opt.appendChild(map);
                for (String property : task.getCommand()) {
                    if (property.startsWith("-D")) {
                        Element entry = doc.createElement("entry");
                        int equalsPos = property.indexOf("=");
                        if (equalsPos < 0) {
                            entry.setAttribute("key", property.substring(2));
                            entry.setAttribute("value", "true");
                        } else {
                            entry.setAttribute("key", property.substring(2, equalsPos));
                            entry.setAttribute("value", property.substring(equalsPos+1));
                        }
                        map.appendChild(entry);
                    }
                }
            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "passParentEnv");
                opt.setAttribute("value", "true");
                mavenRunnerSettings.appendChild(opt);
            }
            {
                if (task.getCommand().stream().anyMatch(arg -> arg.equals("-DskipTests") || arg.equals("-Dmaven.test.skip=true"))) {
                    opt = doc.createElement("option");
                    opt.setAttribute("name", "skipTests");
                    opt.setAttribute("value", "true");
                    mavenRunnerSettings.appendChild(opt);
                }

            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "vmOptions");
                StringBuilder sb = new StringBuilder();
                for (String arg : task.getCommand()) {
                    if (arg.startsWith("-X")) {
                        if (sb.length() > 0) {
                            sb.append(" ");
                        }
                        sb.append(arg);
                    }
                }
                opt.setAttribute("value", sb.toString());
                mavenRunnerSettings.appendChild(opt);
            }
        }
        {
            Element opt = doc.createElement("option");
            opt.setAttribute("name", "myRunnerParameters");
            mavenSettings.appendChild(opt);

            Element mavenRunnerParameters = doc.createElement("MavenRunnerParameters");
            opt.appendChild(mavenRunnerParameters);
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "profiles");
                mavenRunnerParameters.appendChild(opt);

                Element set = doc.createElement("set");
                opt.appendChild(set);
                Set<String> profiles = task.getActiveProfiles();

                for (String profile : profiles) {
                    opt = doc.createElement("option");
                    opt.setAttribute("value", profile);
                    set.appendChild(opt);
                }

                opt = doc.createElement("option");
                opt.setAttribute("name", "goals");
                mavenRunnerParameters.appendChild(opt);

                Element list = doc.createElement("list");
                opt.appendChild(list);
                boolean firstArg = true;
                for (String goal : task.getGoals()) {

                    opt = doc.createElement("option");
                    opt.setAttribute("value", goal);
                    list.appendChild(opt);
                }


            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "pomFileName");
                mavenRunnerParameters.appendChild(opt);
            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "profilesMap");
                mavenRunnerParameters.appendChild(opt);

                Element map = doc.createElement("map");
                opt.appendChild(map);
            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "resolveToWorkspace");
                opt.setAttribute("value", "false");
                mavenRunnerParameters.appendChild(opt);
            }
            {
                opt = doc.createElement("option");
                opt.setAttribute("name", "workingDirPath");
                opt.setAttribute("value", "$PROJECT_DIR$");
                mavenRunnerParameters.appendChild(opt);
            }

        }

        Element method = doc.createElement("method");
        method.setAttribute("v", "2");
        configuration.appendChild(method);
        return configuration;

    }

}
