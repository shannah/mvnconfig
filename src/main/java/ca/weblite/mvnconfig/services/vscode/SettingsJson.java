package ca.weblite.mvnconfig.services.vscode;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.models.ProjectTask;
import ca.weblite.mvnconfig.util.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

class SettingsJson {
    private final File projectDirectory;
    private final ProjectConfig config;
    private static final String KEY_FAVOURITES = "maven.terminal.favorites";

    public SettingsJson(File projectDirectory, ProjectConfig config) {
        this.config = config;
        this.projectDirectory = projectDirectory;
    }

    private File getSettingsJson() {
        return new File(projectDirectory, ".vscode" + File.separator + "settings.json");
    }

    public void save() throws IOException {
        JSONObject root = getSettingsJson().exists() ?
                new JSONObject(FileUtils.readFileToString(getSettingsJson(), "UTF-8")) :
                new JSONObject();

        if (!root.has(KEY_FAVOURITES)) {
            root.put(KEY_FAVOURITES, new JSONArray());
        }

        JSONArray favorites = root.getJSONArray(KEY_FAVOURITES);
        //List<JSONObject> favouritesList = JSONUtils.toList(favorites, new ArrayList<JSONObject>());
        for (ProjectTask task : config) {

            String labelPrefix = task.getGroup() == null ? "" : task.getGroup() + " > ";
            String label = labelPrefix + task.getLabel();

            int pos = JSONUtils.indexOf(favorites, JSONObject.class, o -> o.has("alias") && o.getString("alias").equals(label));
            if (pos == -1) {
                favorites.put(favorites.length(), createFavouriteTask(task));
            } else {
                favorites.put(pos, createFavouriteTask(task));
            }
        }

        getSettingsJson().getAbsoluteFile().getParentFile().mkdirs();
        FileUtils.writeStringToFile(getSettingsJson(), root.toString(2), "UTF-8");
    }

    private JSONObject createFavouriteTask(ProjectTask task) {
        JSONObject out = new JSONObject();
        String labelPrefix = task.getGroup() == null ? "" : task.getGroup() + " > ";
        String label = labelPrefix + task.getLabel();
        out.put("alias", label);
        out.put("command", task.getCommandAsString(false));
        return out;

    }
}
