package ca.weblite.mvnconfig.services.intellij;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.services.AbstractConfigurationWriter;


import java.io.File;
import java.io.IOException;

public class IntellijConfWriter extends AbstractConfigurationWriter {
    @Override
    public void write(File projectDirectory, ProjectConfig config) throws IOException {
        new Workspace(projectDirectory, config).save();
    }


}
