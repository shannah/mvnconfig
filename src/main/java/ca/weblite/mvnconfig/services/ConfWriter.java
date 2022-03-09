package ca.weblite.mvnconfig.services;

import ca.weblite.mvnconfig.models.ProjectConfig;

import java.io.File;
import java.io.IOException;

public interface ConfWriter {
    public void write(File projectDirectory, ProjectConfig config) throws IOException;
}
