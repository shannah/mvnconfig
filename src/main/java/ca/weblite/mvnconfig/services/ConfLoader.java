package ca.weblite.mvnconfig.services;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.models.ProjectTask;
import com.moandjiezana.toml.Toml;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class ConfLoader {
    public ProjectConfig load(ProjectConfig config, InputStream inputStream) throws IOException {
        Toml toml = new Toml().read(inputStream);
        if (config == null) {
            config = new ProjectConfig();
        }
        config.clearTasks();
        for (String key : toml.toMap().keySet()) {
            if (!toml.containsTable(key)) {
                continue;
            }
            Toml action = toml.getTable(key);
            ProjectTask task = new ProjectTask();
            task.setName(key);
            task.setLabel(action.getString("label", key));
            task.setBuildTask(action.getBoolean("build", false));
            task.setRunTask(action.getBoolean("run", false));
            task.setDebugTask(action.getBoolean("debug", false));
            task.setGroup(action.getString("group", null));

            task.setCommand(action.getList("command", new ArrayList<String>()));
            config.addTask(task);

        }
        return config;
    }
}
