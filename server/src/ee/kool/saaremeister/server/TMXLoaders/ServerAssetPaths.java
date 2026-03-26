package ee.kool.saaremeister.server.TMXLoaders;

import ee.kool.saaremeister.server.ServerLauncher;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ServerAssetPaths {
    public static final String ASSETS_DIR_PROPERTY = "saaremeister.assets.dir";
    public static final String ASSETS_DIR_ENV = "SAAREMEISTER_ASSETS_DIR";
    private static final String DISTRIBUTION_ASSETS_DIR = "assets";

    private ServerAssetPaths() {
    }

    public static Path resolve(String relativePath) {
        return getAssetsDir().resolve(relativePath).normalize();
    }

    private static Path getAssetsDir() {
        String configuredPath = System.getProperty(ASSETS_DIR_PROPERTY);
        if (configuredPath == null || configuredPath.isBlank()) {
            configuredPath = System.getenv(ASSETS_DIR_ENV);
        }

        if (configuredPath != null && !configuredPath.isBlank()) {
            return Paths.get(configuredPath).toAbsolutePath().normalize();
        }

        return getDefaultAssetsDir();
    }

    private static Path getDefaultAssetsDir() {
        try {
            Path codeSource = Paths.get(ServerLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            Path appHome = codeSource.getParent();

            if (appHome != null && "lib".equals(appHome.getFileName().toString())) {
                appHome = appHome.getParent();
            }

            if (appHome != null) {
                return appHome.resolve(DISTRIBUTION_ASSETS_DIR).toAbsolutePath().normalize();
            }
        } catch (URISyntaxException | RuntimeException ignored) {
            // Fall back to a local assets directory if the install root cannot be inferred.
        }

        return Paths.get(DISTRIBUTION_ASSETS_DIR).toAbsolutePath().normalize();
    }
}
