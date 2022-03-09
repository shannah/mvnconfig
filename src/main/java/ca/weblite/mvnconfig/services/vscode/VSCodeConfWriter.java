package ca.weblite.mvnconfig.services.vscode;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.services.AbstractConfigurationWriter;

import java.io.File;
import java.io.IOException;

public class VSCodeConfWriter extends AbstractConfigurationWriter {
    @Override
    public void write(File projectDirectory, ProjectConfig config) throws IOException {
        SettingsJson settingsJson = new SettingsJson(projectDirectory, config);
        settingsJson.save();

        ExtensionsJson extensionsJson = new ExtensionsJson(projectDirectory, config);
        extensionsJson.save();

    }
}
