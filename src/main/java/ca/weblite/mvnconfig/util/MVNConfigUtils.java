package ca.weblite.mvnconfig.util;

public class MVNConfigUtils {
    public static boolean isMavenCommand(String arg) {
        String lcarg = arg.toLowerCase();
        return "mvn".equalsIgnoreCase(arg) ||
                "mvnw".equalsIgnoreCase(arg) ||
                "$mvn$".equalsIgnoreCase(arg) ||
                "$mvn".equalsIgnoreCase(arg) ||
                lcarg.endsWith("/mvn") ||
                lcarg.endsWith("/mvnw") ||
                lcarg.endsWith("\\mvn") ||
                lcarg.endsWith("\\mvnw") ||
                lcarg.endsWith("\\mvn.exe") ||
                lcarg.endsWith("\\mvnw.exe") ||
                lcarg.endsWith("\\mvnw.bat");
    }
}
