package dev.voxelbrew.api.slicer;

import java.util.Optional;

/** Lightweight service provider for a slicer operation. */
public interface SlicerProvider {
    /** {@return stable discovery metadata obtained without probing hardware, services or executables} */
    SlicerDescriptor descriptor();

    /**
     * Exposes the provider's settings independently of an evaluated plate. Construct the dialog
     * without contacting the backend. Use matching input IDs and defaults when the operation
     * requests these settings through its window.
     *
     * @return settings dialog for host configuration, or empty when the provider has no persistent settings
     */
    default Optional<SlicerDialog<?>> settingsDialog() {
        return Optional.empty();
    }

    /** {@return a fresh operation for one invocation} */
    Slicer create();
}
