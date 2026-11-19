package dev.voxelbrew.api.slicer;

import dev.voxelbrew.api.model.printing.Cancellation;
import dev.voxelbrew.api.model.printing.ManufacturingModel;
import dev.voxelbrew.api.model.printing.PrintArtifact;
import java.util.Optional;
import java.util.function.Consumer;

/** An operation on one detached, evaluated build plate. */
@FunctionalInterface
public interface Slicer {
    /**
     * Collects settings before running and returns a validated artifact with manufacturing provenance.
     * Returns empty when the settings dialog is dismissed.
     *
     * @param model evaluated geometry, process settings, and resolved hardware for one plate
     * @param window host interface for synchronous settings dialogs
     * @param cancellation cancellation signal for conversion and execution
     * @param diagnostics sink for progress and backend diagnostics
     * @return completed artifact, or empty when the settings dialog is dismissed
     * @throws java.util.concurrent.CancellationException when execution is cancelled
     * @throws SlicerException when a slicer-specific input, configuration, execution, or artifact failure occurs
     * @throws Exception when another failure occurs, such as an I/O error
     */
    Optional<PrintArtifact> slice(
            ManufacturingModel model, SlicerWindow window, Cancellation cancellation, Consumer<String> diagnostics)
            throws Exception;
}
