package dev.voxelbrew.api.slicer;

import dev.voxelbrew.api.model.printing.Cancellation;
import java.nio.file.Path;
import java.util.List;
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
     * Finds local executable candidates on explicit request during configuration. Hosts may fill the
     * settings dialog's {@code executable} input with a selected path. Discovery must not launch the
     * candidates. Hosts can use {@link #checkConfiguration} to check the selected installation.
     *
     * @param cancellation cancels an in-progress search
     * @return absolute paths to candidate executables, or an empty list if none are found or supported
     * @throws Exception if discovery fails
     */
    default List<Path> discoverExecutables(Cancellation cancellation) throws Exception {
        return List.of();
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
