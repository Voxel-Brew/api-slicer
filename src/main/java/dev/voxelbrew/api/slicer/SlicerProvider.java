package dev.voxelbrew.api.slicer;

import dev.voxelbrew.api.model.printing.Cancellation;
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

    /**
     * Tests a specific installation's settings and backend without a plate.
     * Hosts call this on explicit request during configuration.
     *
     * @param window resolves the proposed settings
     * @param cancellation cancels the check
     * @return actual backend version reported by the operational installation
     * @throws Exception if the installation fails the check
     */
    default String checkConfiguration(SlicerWindow window, Cancellation cancellation) throws Exception {
        throw new SlicerException.Configuration("This provider does not support installation checks");
    }

    /** {@return a fresh operation for one invocation} */
    Slicer create();
}
