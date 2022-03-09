package ca.weblite.mvnconfig;

import ca.weblite.mvnconfig.services.ConfWriter;
import ca.weblite.mvnconfig.services.intellij.IntellijConfWriter;
import ca.weblite.mvnconfig.services.netbeans.NetBeansConfWriter;
import ca.weblite.mvnconfig.services.vscode.VSCodeConfWriter;
import picocli.CommandLine;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "mvnconfig", version = "mvnconfig 1.0.0", mixinStandardHelpOptions = true)
public class MVNConfigMain implements Callable<Integer> {

    enum IDE {
        IntelliJ,
        NetBeans,
        Eclipse,
        VSCode
    }


    @CommandLine.Option(names = {"-i", "--ide"},  description = "Output options: ${COMPLETION-CANDIDATES}", required = true)
    private IDE ide;

    @CommandLine.Option(names = {"-f", "--mvnconfig"}, description = "Path to mvnconfig.toml file")
    private File mvnConfigFile;

    @CommandLine.Parameters(index = "0", description = "Target Project directory")
    File projectDirectory;



    private ConfWriter getConfigurationWriter() {
        switch (ide) {
            case IntelliJ:
                return new IntellijConfWriter();
            case NetBeans:
                return new NetBeansConfWriter();
            case VSCode:
                return new VSCodeConfWriter();
            default:
                throw new UnsupportedOperationException("The configuration writer for ide "+ide+" is not implemented yet.");
        }
    }

    @Override
    public Integer call() throws Exception {
        File projectDirLocal = projectDirectory;
        if (projectDirLocal == null) {
            projectDirLocal = new File(".").getAbsoluteFile().getParentFile();
        }
        if (!projectDirLocal.exists()) {
            throw new FileNotFoundException("Project directory "+projectDirLocal+" does not exist.");
        }

        File configFileLocal = mvnConfigFile;
        if (configFileLocal == null) {
            configFileLocal = new File(projectDirLocal, "mvnconfig.toml");
        }
        if (!configFileLocal.exists()) {
            throw new FileNotFoundException("The config file "+mvnConfigFile+" could not be found");
        }
        MVNConfig configurator = new MVNConfig();
        configurator.load(configFileLocal);
        configurator.writeConfigToProject(projectDirLocal, getConfigurationWriter());
        System.out.println("Updated "+ide+ "configuration in project at "+projectDirLocal);
        return 0;
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new MVNConfigMain()).execute(args);
        System.exit(exitCode);
    }



}
