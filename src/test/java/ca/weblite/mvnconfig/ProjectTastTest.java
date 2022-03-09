package ca.weblite.mvnconfig;

import ca.weblite.mvnconfig.models.ProjectTask;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

public class ProjectTastTest {
    @Test
    public void testActiveProfiles() {
        ProjectTask task = new ProjectTask();
        task.setCommand(Arrays.asList(new String[]{
            "mvn",
            "-P",
            "foo,bar,?baz",
            "verify",
            "-U",
            "-e"
        }));

        Set<String> profiles = task.getActiveProfiles();
        Assertions.assertEquals(3, profiles.size());
        Assertions.assertArrayEquals(new String[]{"foo", "bar", "?baz"}, profiles.toArray(new String[3]));
    }
    @Test
    public void testActiveProfiles2() {
        ProjectTask task = new ProjectTask();
        task.setCommand(Arrays.asList(new String[]{
                "mvn",
                "-P",
                "foo",
                "verify",
                "-U",
                "-e"
        }));

        Set<String> profiles = task.getActiveProfiles();
        Assertions.assertEquals(1, profiles.size());
        Assertions.assertArrayEquals(new String[]{"foo"}, profiles.toArray(new String[1]));
    }

    @Test
    public void testEmptyActiveProfiles() {
        ProjectTask task = new ProjectTask();
        task.setCommand(Arrays.asList(new String[]{
                "mvn",
                "verify",
                "-U",
                "-e"
        }));

        Set<String> profiles = task.getActiveProfiles();
        Assertions.assertEquals(0, profiles.size());

    }

    @Test
    public void testDisabledProfilesProfiles() {
        ProjectTask task = new ProjectTask();
        task.setCommand(Arrays.asList(new String[]{
                "mvn",
                "verify",
                "-P",
                "foo,!bar,?baz",
                "-U",
                "-e"
        }));

        Set<String> profiles = task.getActiveProfiles();
        Set<String> disabledProfiles = task.getDisabledProfiles();
        Assertions.assertEquals(2, profiles.size());
        Assertions.assertEquals(1, disabledProfiles.size());
        Assertions.assertArrayEquals(new String[]{"foo", "?baz"}, profiles.toArray(new String[2]));
        Assertions.assertArrayEquals(new String[]{"bar"}, disabledProfiles.toArray(new String[1]));

    }


}
