package ca.weblite.mvnconfig.services.netbeans;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.services.AbstractConfigurationWriter;

import java.io.File;
import java.io.IOException;

public class NetBeansConfWriter extends AbstractConfigurationWriter {
    @Override
    public void write(File projectDirectory, ProjectConfig config) throws IOException {
        NBActions nbActions = new NBActions(projectDirectory, config, null);
        nbActions.save();



    }
}
