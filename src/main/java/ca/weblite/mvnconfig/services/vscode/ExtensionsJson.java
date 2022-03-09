package ca.weblite.mvnconfig.services.vscode;

import ca.weblite.mvnconfig.models.ProjectConfig;
import ca.weblite.mvnconfig.util.JSONUtils;
import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.File;
import java.io.IOException;

class ExtensionsJson {
    private final File projectDirectory;
    private final ProjectConfig config;
    private static final String JAVA_PACK_EXTENSION_NAME = "vscjava.vscode-java-pack";
    private static final String KEY_RECOMMENDATIONS = "recommendations";

    public ExtensionsJson(File projectDirectory, ProjectConfig config) {
        this.projectDirectory = projectDirectory;
        this.config = config;
    }

    private File getExtensionsJson() {
        return new File(projectDirectory, ".vscode" + File.separator + "extensions.json");
    }

    public void save() throws IOException {
        JSONObject root = new JSONObject();
        if (getExtensionsJson().exists()) {
            root = new JSONObject(FileUtils.readFileToString(getExtensionsJson(), "UTF-8"));
        }
        if (!root.has(KEY_RECOMMENDATIONS)) {
            root.put(KEY_RECOMMENDATIONS, new JSONArray());
        }
        JSONArray recommendations = root.getJSONArray(KEY_RECOMMENDATIONS);
        if (JSONUtils.indexOf(recommendations, JAVA_PACK_EXTENSION_NAME) == -1) {
            recommendations.put(recommendations.length(), JAVA_PACK_EXTENSION_NAME);
            File vscodeDir = getExtensionsJson().getAbsoluteFile().getParentFile();
            vscodeDir.mkdirs();
            FileUtils.writeStringToFile(getExtensionsJson(), root.toString(2), "UTF-8");
        }
    }


}
