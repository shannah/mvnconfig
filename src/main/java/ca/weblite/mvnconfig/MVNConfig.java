package ca.weblite.mvnconfig;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.services.ConfLoader;
import ca.weblite.mvnconfig.services.ConfWriter;
import ca.weblite.mvnconfig.services.intellij.IntellijConfWriter;
import ca.weblite.mvnconfig.services.netbeans.NetBeansConfWriter;
import ca.weblite.mvnconfig.services.vscode.VSCodeConfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class MVNConfig {
    private ProjectConfig projectConfig;

    public void load(InputStream configFileInputStream) throws IOException {
        projectConfig = new ProjectConfig();
        new ConfLoader().load(projectConfig, configFileInputStream);
    }

    public void load(File configFile) throws IOException {
        try (FileInputStream fis = new FileInputStream(configFile)) {
            load(fis);
        }
    }

    public void writeConfigToProject(File projectDirectory, ConfWriter writer) throws IOException {
        if (projectConfig == null) {
            throw new IllegalStateException("Must load project config before writing to project");
        }
        writer.write(projectDirectory, projectConfig);
    }

    public void writeConfigToIntelliJProject(File projectDirectory) throws IOException {
        writeConfigToProject(projectDirectory, new IntellijConfWriter());
    }

    public void writeConfigToNetBeansProject(File projectDirectory) throws IOException {
        writeConfigToProject(projectDirectory, new NetBeansConfWriter());
    }

    public void writeConfigToVSCodeProject(File projectDirectory) throws IOException {
        writeConfigToProject(projectDirectory, new VSCodeConfWriter());
    }



}
