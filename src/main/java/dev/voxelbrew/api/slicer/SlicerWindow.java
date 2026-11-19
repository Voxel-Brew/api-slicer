package dev.voxelbrew.api.slicer;

import java.util.Optional;

/**
 * Host abstraction for slicer interactions, shared by desktop and headless workflows.
 *
 * <p>The desktop can retain its owner window internally. A CLI can resolve settings from arguments
 * or prompts. Hosts document any thread restrictions.
 */
public interface SlicerWindow {
    /**
     * Resolves a slicer-defined request synchronously, returning after confirmation or dismissal.
     *
     * <p>Dismissal returns an empty optional. Unsupported requests fail explicitly. Validation
     * failures must be corrected or reported to the caller.
     *
     * @param <R> accepted settings type
     * @param dialog request to resolve; must not be {@code null}
     * @return accepted settings, or empty on cancellation
     * @throws UnsupportedOperationException if this host cannot handle the dialog type
     */
    <R> Optional<R> show(SlicerDialog<R> dialog);
}
