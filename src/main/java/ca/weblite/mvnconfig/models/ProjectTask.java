package ca.weblite.mvnconfig.models;

import java.util.*;
import java.util.stream.Collectors;

import static ca.weblite.mvnconfig.util.MVNConfigUtils.isMavenCommand;

public class ProjectTask {
    private String label;
    private String name;
    private List<String> command = new ArrayList<>();
    private boolean isBuildTask;
    private boolean isRunTask;
    private boolean isDebugTask;
    private String group;

    private ProjectTask copiedFrom;


    public ProjectTask() {

    }

    public ProjectTask(ProjectTask task) {
        this.label = task.label;
        this.name = task.name;
        this.command.addAll(task.command);
        this.isBuildTask = task.isBuildTask;
        this.isDebugTask = task.isDebugTask;
        this.group = task.group;
        this.copiedFrom = task;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getCommand() {
        return new ArrayList<>(command);
    }

    public List<String> getCommand(boolean includeMVN, boolean includeProfiles) {
        List<String> out = new ArrayList<>();
        List<String> commands = new ArrayList<>(command);
        if (!commands.isEmpty() && !includeMVN && isMavenCommand(commands.get(0))) {
            commands.remove(0);
        }
        if (!includeProfiles) {
            int len = commands.size();
            boolean isProfileCommand = false;
            for (int i = 0; i < len; i++) {
                String cmd = commands.get(i).trim();
                if (cmd.equals("-P")) {
                    isProfileCommand = true;
                } else if (isProfileCommand) {
                    isProfileCommand = false;
                } else if (!cmd.startsWith("-P ")) {
                    out.add(cmd);
                }
            }
        } else {
            out.addAll(commands);
        }
        return out;

    }

    public String getCommandAsString(boolean includeMVN) {
        List<String> args = includeMVN ? getCommand() : getCommandWithoutMVN();
        StringBuilder sb = new StringBuilder();
        for (String arg : args) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append("\"").append(arg).append("\"");
        }
        return sb.toString();
    }

    public void setCommand(Iterable<String> command) {
        this.command.clear();
        for (String cmd : command) {
            this.command.add(cmd);
        }

    }



    public boolean isBuildTask() {
        return isBuildTask;
    }

    public void setBuildTask(boolean buildTask) {
        isBuildTask = buildTask;
    }

    public boolean isRunTask() {
        return isRunTask;
    }

    public void setRunTask(boolean runTask) {
        isRunTask = runTask;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public boolean isDebugTask() {
        return isDebugTask;
    }

    public void setDebugTask(boolean debugTask) {
        isDebugTask = debugTask;
    }

    public List<String> getCommandWithoutMVN() {
        List<String> out = new ArrayList<>(command);
        if (!out.isEmpty() && isMavenCommand(out.get(0))) {
            out.remove(0);
        }
        return out;
    }

    public List<String> getGoals() {
        return getCommandWithoutMVN().stream().filter(arg -> !arg.startsWith("-")).collect(Collectors.toList());
    }

    public Set<String> getDisabledProfiles() {
        List<String> profileCommands = getCommandWithoutMVN().stream().filter(arg -> arg.startsWith("-P ")).collect(Collectors.toList());
        Set<String> out = new LinkedHashSet<>();
        for (String command : profileCommands) {
            command = command.substring(command.indexOf(" ")+1).trim();
            for (String subcommand : command.split(",")) {
                subcommand = subcommand.trim();
                if (subcommand.startsWith("!")) {
                    out.add(subcommand.substring(1));
                }

            }
        }

        List<String> commands = getCommandWithoutMVN();
        int len = commands.size();
        boolean isProfileCommand = false;
        for (int i=0; i<len; i++) {
            String command = commands.get(i);
            if (command.equals("-P")) {
                isProfileCommand = true;
            } else if (isProfileCommand) {
                isProfileCommand = false;
                for (String subcommand : command.split(",")) {
                    subcommand = subcommand.trim();
                    if (subcommand.startsWith("!")) {
                        out.add(subcommand.substring(1));
                    }
                }
            }
        }
        return out;
    }


    public Set<String> getActiveProfiles() {
        List<String> profileCommands = getCommandWithoutMVN().stream().filter(arg -> arg.startsWith("-P ")).collect(Collectors.toList());
        Set<String> out = new LinkedHashSet<>();
        for (String command : profileCommands) {
            command = command.substring(command.indexOf(" ")+1).trim();
            for (String subcommand : command.split(",")) {
                subcommand = subcommand.trim();
                if (subcommand.startsWith("!")) {
                    continue;
                }

                out.add(subcommand);
            }
        }

        List<String> commands = getCommandWithoutMVN();
        int len = commands.size();
        boolean isProfileCommand = false;
        for (int i=0; i<len; i++) {
            String command = commands.get(i);
            if (command.equals("-P")) {
                isProfileCommand = true;
            } else if (isProfileCommand) {
                isProfileCommand = false;
                for (String subcommand : command.split(",")) {
                    subcommand = subcommand.trim();
                    if (subcommand.startsWith("!")) {
                        continue;
                    }

                    out.add(subcommand);
                }
            }
        }
        return out;
    }

    public Properties getProperties() {
        Properties out = new Properties();
        for (String arg : getCommandWithoutMVN()) {
            if (!arg.startsWith("-D")) {
                continue;
            }
            String propertyName = arg.substring(2);
            String propertyValue = "true";
            if (propertyName.contains("=")) {
                propertyValue = propertyName.substring(propertyName.indexOf("=")+1);
                propertyName = propertyName.substring(0, propertyName.indexOf("="));
            }
            out.setProperty(propertyName, propertyValue);
        }
        return out;

    }

    public boolean isEqualOrCopiedFrom(ProjectTask t) {
        return this == t || copiedFrom != null && copiedFrom.isEqualOrCopiedFrom(t);
    }
}
