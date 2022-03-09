package ca.weblite.mvnconfig.models;

import java.util.*;
import java.util.stream.Collectors;

public class ProjectConfig implements Iterable<ProjectTask> {
    private List<ProjectTask> tasks = new ArrayList<>();

    public void addTask(ProjectTask task) {
        tasks.add(task);
    }

    public void clearTasks() {
        tasks.clear();
    }


    @Override
    public Iterator<ProjectTask> iterator() {
        return tasks.iterator();
    }

    public List<ProjectTask> getRunTasks() {
        return tasks.stream().filter(t -> t.isRunTask()).collect(Collectors.toList());
    }

    public ProjectTask getRunTask() {
        List<ProjectTask> tasks = getRunTasks();
        if (tasks.size() == 1) return tasks.get(0);

        ProjectTask out = tasks.stream().filter(t -> t.getGroup() == null).findFirst().orElse(null);
        if (out != null) return out;

        out = tasks.stream().findFirst().orElse(null);
        return out;
    }

    public ProjectTask getRunTask(final String group) {
        if (group == null) {
            return getRunTasks().stream().filter(t -> null == t.getGroup()).findFirst().orElse(null);
        }
        return getRunTasks().stream().filter(t -> group.equals(t.getGroup())).findFirst().orElse(null);
    }

    public List<ProjectTask> getDebugTasks() {
        return tasks.stream().filter(t -> t.isDebugTask()).collect(Collectors.toList());
    }

    public ProjectTask getDebugTask() {
        List<ProjectTask> tasks = getDebugTasks();
        if (tasks.size() == 1) return tasks.get(0);

        ProjectTask out = tasks.stream().filter(t -> t.getGroup() == null).findFirst().orElse(null);
        if (out != null) return out;

        out = tasks.stream().findFirst().orElse(null);
        return out;
    }

    public ProjectTask getDebugTask(final String group) {
        if (group == null) {
            return getDebugTasks().stream().filter(t -> null == t.getGroup()).findFirst().orElse(null);
        }
        return getDebugTasks().stream().filter(t -> group.equals(t.getGroup())).findFirst().orElse(null);
    }

    public List<ProjectTask> getBuildTasks() {
        return tasks.stream().filter(t -> t.isBuildTask()).collect(Collectors.toList());
    }

    public ProjectTask getBuildTask() {
        List<ProjectTask> tasks = getBuildTasks();
        if (tasks.size() == 1) return tasks.get(0);

        ProjectTask out = tasks.stream().filter(t -> t.getGroup() == null).findFirst().orElse(null);
        if (out != null) return out;

        out = tasks.stream().findFirst().orElse(null);
        return out;
    }

    public ProjectTask getBuildTask(final String group) {
        if (group == null) {
            return getBuildTasks().stream().filter(t -> null == t.getGroup()).findFirst().orElse(null);
        }
        return getBuildTasks().stream().filter(t -> group.equals(t.getGroup())).findFirst().orElse(null);
    }

    public Set<String> getTaskGroups() {
        LinkedHashSet<String> out = new LinkedHashSet<>();
        for (ProjectTask task : this) {
            if (task.getGroup() != null) {
                out.add(task.getGroup());
            }
        }
        return out;
    }
}

