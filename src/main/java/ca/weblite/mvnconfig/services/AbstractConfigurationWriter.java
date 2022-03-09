package ca.weblite.mvnconfig.services;

import ca.weblite.mvnconfig.models.ProjectConfig;

import java.io.File;
import java.io.IOException;

public abstract class AbstractConfigurationWriter implements ConfWriter {
    @Override
    public abstract void write(File projectDirectory, ProjectConfig config) throws IOException;
}
