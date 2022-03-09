package ca.weblite.mvnconfig.services.netbeans;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.models.ProjectTask;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

class NBActions {
    private final File projectDirectory;
    private final ProjectConfig config;
    private final String profileName;
    private final List<ProjectTask> defaultTasks = new ArrayList<>();

    private static final Set<String> DEFAULT_ACTION_NAMES = new HashSet<>(Arrays.asList("build", "debug", "test", "run", "build-with-dependencies"));

    public NBActions(File projectDirectory, ProjectConfig config, String profileName) {
        this.projectDirectory = projectDirectory;
        this.config = config;
        this.profileName = profileName;
    }

    private File getNBActionsFile() {
        if (profileName == null) {
            return new File(projectDirectory, "nbactions.xml");
        }
        return new File(projectDirectory, "nbactions-"+profileName+".xml");
    }

    void save() throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IOException("Failed to write XML to "+getNBActionsFile(), e);
        }
        Document doc;
        if (getNBActionsFile().exists()) {
            try {
                doc = builder.parse(getNBActionsFile());
            } catch (SAXException e) {
                throw new IOException("Failed to write xml to "+getNBActionsFile(), e);
            }
        } else {
            try {
                doc = builder.parse(new ByteArrayInputStream("<?xml version=\"1.0\" encoding=\"UTF-8\"?><actions/>".getBytes(StandardCharsets.UTF_8)));
            } catch (SAXException e) {
                throw new IOException("Failed to write xml to "+getNBActionsFile(), e);
            }
        }
        Element actions = doc.getDocumentElement();

        ProjectTask runTask = getRunTask();
        if (runTask != null) {
            defaultTasks.add(runTask);
            Element runElement = findRunAction(actions);
            if (runElement == null) {
                actions.appendChild(createActionElementForTask(doc, runTask));
            } else {
                actions.replaceChild(createActionElementForTask(doc, runTask), runElement);
            }
        }

        ProjectTask debugTask = getDebugTask();
        if (debugTask != null) {
            defaultTasks.add(debugTask);
            Element debugElement = findDebugAction(actions);
            if (debugElement == null) {
                actions.appendChild(createActionElementForTask(doc, debugTask));
            } else {
                actions.replaceChild(createActionElementForTask(doc, debugTask), debugElement);
            }
        }

        ProjectTask testTask = getTestTask();
        if (testTask != null) {
            defaultTasks.add(testTask);
            Element testElement = findTestAction(actions);
            if (testElement == null) {
                actions.appendChild(createActionElementForTask(doc, testTask));
            } else {
                actions.replaceChild(createActionElementForTask(doc, testTask), testElement);
            }
        }

        ProjectTask buildTask = getBuildTask();
        if (buildTask != null) {
            defaultTasks.add(buildTask);
            Element buildElement = findBuildAction(actions);
            if (buildElement == null) {
                actions.appendChild(createActionElementForTask(doc, buildTask));
            } else {
                actions.replaceChild(createActionElementForTask(doc, buildTask), buildElement);
            }
        }

        buildTask = getBuildWithDependenciesTask();
        if (buildTask != null) {
            defaultTasks.add(buildTask);
            Element buildElement = findBuildWithDependenciesAction(actions);
            if (buildElement == null) {
                actions.appendChild(createActionElementForTask(doc, buildTask));
            } else {
                actions.replaceChild(createActionElementForTask(doc, buildTask), buildElement);
            }
        }

        for (ProjectTask task : getRemainingTasks()) {
            Element actionElement = findActionByName(actions, getActionNameForTaskName(task.getName()));
            if (actionElement == null) {
                actions.appendChild(createActionElementForTask(doc, task));
            } else {
                actions.replaceChild(createActionElementForTask(doc, task), actionElement);
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        } catch (TransformerConfigurationException e) {
            throw new IOException("Failed to write XML to "+getNBActionsFile(), e);
        }
        DOMSource source = new DOMSource(doc);
        File nbActionsFile = getNBActionsFile();
        if (!nbActionsFile.getParentFile().exists()) {
            nbActionsFile.getParentFile().mkdirs();
        }
        try (FileOutputStream fos = new FileOutputStream(nbActionsFile)) {
            StreamResult result = new StreamResult(fos);
            transformer.transform(source, result);
        } catch (TransformerException e) {
            throw new IOException("Failed to write XML to "+nbActionsFile, e);
        }


    }


    private boolean isInstalledAsDefaultTask(ProjectTask t) {
        for (ProjectTask defaultTask : defaultTasks) {
            if (defaultTask.isEqualOrCopiedFrom(t)) {
                return true;
            }
        }
        return false;
    }

    private List<ProjectTask> getRemainingTasks() {
        List<ProjectTask> out = new ArrayList<>();
        for (ProjectTask task : config) {
            if (!isInstalledAsDefaultTask(task)) {
                out.add(task);
            }
        }
        return out;
    }


    private ProjectTask getRunTask() {
        ProjectTask t = config.getRunTask(profileName);
        if (t == null) {
            t = config.getRunTask();
        }
        if (t != null) {
            // Because we need to change its name
            t = new ProjectTask(t);
            t.setName("run");
        }
        return t;
    }

    private ProjectTask getDebugTask() {
        ProjectTask t = config.getDebugTask(profileName);
        if (t == null) {
            t = config.getDebugTask();
        }
        if (t == null) {
            t = getRunTask();
        }
        if (t != null) {
            t = new ProjectTask(t);
            t.setName("debug");
        }
        return t;
    }

    private ProjectTask getTestTask() {
        ProjectTask t = config.getBuildTask(profileName);
        if (t == null) {
            t = config.getBuildTask();
        }
        if (t != null) {
            // Because we need to change its name
            t = new ProjectTask(t);
            t.setName("test");
            List<String> origCommand = t.getCommand();
            List<String> newCommand = new ArrayList<>();
            for (String arg : origCommand) {
                if (!arg.equals("-DskipTests") || arg.equals("-Dmaven.test.skip=true")) {
                    continue;
                }
                newCommand.add(arg);
            }
            t.setCommand(newCommand);
        }
        return t;
    }

    private ProjectTask getBuildTask() {
        ProjectTask t = config.getBuildTask(profileName);
        if (t == null) {
            t = config.getBuildTask();
        }
        if (t != null) {
            // Because we need to change its name
            t = new ProjectTask(t);
            t.setName("build");

        }
        return t;
    }

    private ProjectTask getBuildWithDependenciesTask() {
        ProjectTask t = config.getBuildTask(profileName);
        if (t == null) {
            t = config.getBuildTask();
        }
        if (t != null) {
            // Because we need to change its name
            t = new ProjectTask(t);
            t.setName("build-with-dependencies");

        }
        return t;
    }

    private Element findRunAction(Element actions) {
        return findActionByName(actions, "run");
    }

    private Element findDebugAction(Element actions) {
        return findActionByName(actions, "debug");
    }

    private Element findBuildAction(Element actions) {
        return findActionByName(actions, "build");
    }

    private Element findTestAction(Element actions) {
        return findActionByName(actions, "test");
    }

    private Element findBuildWithDependenciesAction(Element actions) {
        return findActionByName(actions, "build-with-dependencies");
    }

    private Element findActionByName(Element actions, String name) {
        XPath xpath = XPathFactory.newInstance().newXPath();
        try {
            Node node = (Node)xpath.compile("./actionName[text() = '"+name+"']").evaluate(actions, XPathConstants.NODE);
            if (node != null) {
                return (Element)node.getParentNode();
            }
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Error trying to find action named "+name, e);
        }
        return null;
    }

    /**
     * Checks if the given name is one of the default action names.  E.g. build, test, run, debug, etc...
     * @param taskName
     * @return
     */
    private static boolean isDefaultActionName(String taskName) {
        return DEFAULT_ACTION_NAMES.contains(taskName);
    }

    private static String getActionNameForTaskName(String taskName) {
        if (isDefaultActionName(taskName)) {
            return taskName;
        } else {
            return "CUSTOM-" + taskName;
        }
    }

    private Element createActionElementForTask(Document doc, ProjectTask task) {
        Element action = doc.createElement("action");
        Element actionName = doc.createElement("actionName");
        action.appendChild(actionName);
        actionName.setTextContent(getActionNameForTaskName(task.getName()));

        Element displayName = doc.createElement("displayName");
        action.appendChild(displayName);
        String labelPrefix = task.getGroup() == null ? "" : task.getGroup() + " > ";
        displayName.setTextContent(labelPrefix + task.getLabel());


        Element goals = doc.createElement("goals");
        action.appendChild(goals);
        for (String goal : task.getGoals()) {
            Element goalEl = doc.createElement("goal");
            goalEl.setTextContent(goal);
            goals.appendChild(goalEl);
        }
        Element properties = doc.createElement("properties");
        action.appendChild(properties);
        for (String propertyName : task.getProperties().stringPropertyNames()) {
            Element propEl = doc.createElement(propertyName);
            propEl.setTextContent(task.getProperties().getProperty(propertyName));
            properties.appendChild(propEl);
        }
        if (task.isDebugTask()) {
            Element jdpaListen = doc.createElement("jpda.listen");
            jdpaListen.setTextContent("true");
            properties.appendChild(jdpaListen);
        }

        Element activatedProfiles = doc.createElement("activatedProfiles");
        Set<String> profiles = task.getActiveProfiles();
        if (!profiles.isEmpty()) {
            action.appendChild(activatedProfiles);
            for (String profile : profiles) {
                Element profileEl = doc.createElement("activatedProfile");
                activatedProfiles.appendChild(profileEl);
                profileEl.setTextContent(profile);
            }
        }

        if (task.isBuildTask()) {
            Element packagings = doc.createElement("packagings");
            action.appendChild(packagings);
            Element packaging = doc.createElement("packaging");
            packaging.setTextContent("*");
            packagings.appendChild(packaging);

            if (task.getName().equals("build-with-dependencies")) {
                Element reactor = doc.createElement("reactor");
                action.appendChild(reactor);
                reactor.setTextContent("also-make");
            }
        }

        return action;




    }
}
