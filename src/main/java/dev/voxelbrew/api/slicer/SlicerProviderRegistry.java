package dev.voxelbrew.api.slicer;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Discovers classpath and installed slicer providers for a host workflow. */
public interface SlicerProviderRegistry extends AutoCloseable {
    /**
     * Configures plugin discovery on the service-loaded instance.
     *
     * @param directory trusted plugin directory
     * @return configured registry
     * @throws IOException if the directory cannot be read
     */
    SlicerProviderRegistry withDirectory(Path directory) throws IOException;

    /** {@return descriptors sorted by provider ID} */
    List<SlicerDescriptor> descriptors();

    /**
     * Looks up an installed provider by its stable ID.
     *
     * @param id provider identifier
     * @return installed provider
     * @throws IllegalArgumentException if the ID is unknown
     */
    SlicerProvider require(String id);

    /**
     * Releases plugin resources after all operations using the registry finish.
     *
     * @throws IOException if plugin resources cannot be closed
     */
    @Override
    void close() throws IOException;
}
