package ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Manages file paths for the BudgeIt application.
 * Ensures compatibility across different execution environments:
 * - From VS Code (relative paths)
 * - As a packaged JAR or EXE (home directory)
 */
public class FilePathManager {
    private static final String APP_FOLDER = ".budgeit";
    private static final String SAVE_FILE = "UserAccount.json";

    /**
     * Gets the path where user data (UserAccount.json) should be saved.
     * - If running from VS Code: uses ./data/
     * - If packaged (JAR/EXE): uses ~/.budgeit/
     *
     * @return the save location path as a String
     */
    public static String getSaveLocation() {
        // Check if we're running from VS Code (project root has "src" folder)
        if (isRunningFromProject()) {
            // Ensure data folder exists in development mode
            try {
                Files.createDirectories(Paths.get("./data"));
            } catch (IOException e) {
                System.err.println("Could not create data folder: " + e.getMessage());
            }
            return "./data/" + SAVE_FILE;
        } else {
            // Running as packaged app - use home directory
            return getAppDataFolder() + File.separator + SAVE_FILE;
        }
    }

    /**
     * Gets the application data folder, creating it if necessary.
     *
     * @return the path to the app data folder
     */
    public static String getAppDataFolder() {
        String userHome = System.getProperty("user.home");
        String appFolder = userHome + File.separator + APP_FOLDER;

        // Create folder if it doesn't exist
        try {
            Files.createDirectories(Paths.get(appFolder));
        } catch (IOException e) {
            System.err.println("Could not create app data folder: " + e.getMessage());
        }

        return appFolder;
    }

    /**
     * Checks if the application is running from the project directory
     * (during development in VS Code) by checking the class resource protocol.
     *
     * @return true if running from class files (file protocol), false if running from JAR (jar protocol)
     */
    private static boolean isRunningFromProject() {
        java.net.URL url = FilePathManager.class.getResource("FilePathManager.class");
        return url != null && "file".equals(url.getProtocol());
    }

    /**
     * Gets a resource from the classpath (e.g., images).
     * This works for both JAR and development environments.
     *
     * @param resourcePath the path within resources (e.g., "images/gefraks.jpg")
     * @return the URL to the resource, or null if not found
     */
    public static String getResourcePath(String resourcePath) {
        try {
            ClassLoader classLoader = FilePathManager.class.getClassLoader();
            if (classLoader.getResource(resourcePath) != null) {
                return classLoader.getResource(resourcePath).toString();
            }
        } catch (Exception e) {
            System.err.println("Could not load resource: " + resourcePath);
        }
        return null;
    }

    /**
     * Gets an InputStream for a resource on the classpath.
     * This is useful for loading binary resources like images.
     *
     * @param resourcePath the path within resources (e.g., "images/gefraks.jpg")
     * @return an InputStream for the resource, or null if not found
     */
    public static java.io.InputStream getResourceAsStream(String resourcePath) {
        ClassLoader classLoader = FilePathManager.class.getClassLoader();
        return classLoader.getResourceAsStream(resourcePath);
    }
}
