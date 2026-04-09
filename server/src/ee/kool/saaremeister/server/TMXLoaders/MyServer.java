package ee.kool.saaremeister.server.TMXLoaders;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;

/**
 * Server-side file resolution for map assets.
 */
public class MyServer {


    /**
     * Modified filehandle that is built on libGDX's filehandler.
     */
    public static class MyFileHandleResolver implements FileHandleResolver {
        @Override
        public FileHandle resolve(String fileName) {
            FileHandle fileHandle = new FileHandle(ServerAssetPaths.resolve(fileName).toFile());
            System.out.println("Trying to resolve map: " + fileHandle.path());
            return fileHandle;
        }
    }
}
