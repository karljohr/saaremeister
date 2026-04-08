package ee.kool.saaremeister.server.TMXLoaders;

import ee.kool.saaremeister.server.ServerLauncher;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for resolving asset paths.
 */
public final class ServerAssetPaths {
    public static final String ASSETS_DIR_PROPERTY = "saaremeister.assets.dir";
    public static final String ASSETS_DIR_ENV = "SAAREMEISTER_ASSETS_DIR";

    // If you move TMXAssets into the repo's top-level assets folder, leave this as "assets".
    // If you keep TMXAssets under shared/src, change this to "shared/src".
    private static final String LOCAL_ASSETS_DIR = "assets";

    /**
     * Private constructor to prevent instantiation.
     */
    private ServerAssetPaths() {
    }

    /**
     * Resolves a path relative to the assets' directory.
     */
    public static Path resolve(String relativePath) {
        for (Path baseDir : getCandidateAssetDirs()) {
            Path candidate = baseDir.resolve(relativePath).normalize();
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        throw new IllegalStateException(
                "Asset not found: " + relativePath + ". Checked: " + getCandidateAssetDirs()
        );
    }

    /**
     * Returns a list of candidate asset directories.
     */
    private static List<Path> getCandidateAssetDirs() {
        Set<Path> candidates = new LinkedHashSet<>();

        String configuredPath = System.getProperty(ASSETS_DIR_PROPERTY);
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv(ASSETS_DIR_ENV);
        }

        if (configuredPath != null && !configuredPath.isBlank()) {
            candidates.add(Paths.get(configuredPath).toAbsolutePath().normalize());
        }

        Path codeSourceBase = getCodeSourceBaseDir();
        if (codeSourceBase != null) {
            candidates.add(codeSourceBase.resolve("assets").toAbsolutePath().normalize());

            Path repoRoot = findRepoRoot(codeSourceBase);
            if (repoRoot != null) {
                candidates.add(repoRoot.resolve(LOCAL_ASSETS_DIR).toAbsolutePath().normalize());
            }
        }

        Path workingDir = Paths.get("").toAbsolutePath().normalize();
        candidates.add(workingDir.resolve("assets").normalize());

        Path workingDirRepoRoot = findRepoRoot(workingDir);
        if (workingDirRepoRoot != null) {
            candidates.add(workingDirRepoRoot.resolve(LOCAL_ASSETS_DIR).toAbsolutePath().normalize());
        }

        return new ArrayList<>(candidates);
    }

    /**
     * Returns the directory containing the server jar.
     */
    private static Path getCodeSourceBaseDir() {
        try {
            Path codeSource = Paths.get(
                    ServerLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            ).toAbsolutePath().normalize();

            if (Files.isRegularFile(codeSource)) {
                Path parent = codeSource.getParent();
                if (parent != null && "lib".equals(String.valueOf(parent.getFileName()))) {
                    Path appHome = parent.getParent();
                    if (appHome != null) {
                        return appHome;
                    }
                }
                return parent;
            }

            return codeSource;
        } catch (URISyntaxException | RuntimeException e) {
            return null;
        }
    }

    /**
     * Finds the root of the repo so that we can look for the local assets' folder.
     */
    private static Path findRepoRoot(Path start) {
        Path current = start;
        while (current != null) {
            if (Files.exists(current.resolve("settings.gradle"))) {
                return current;
            }
            current = current.getParent();
        }
        return null;
    }
}