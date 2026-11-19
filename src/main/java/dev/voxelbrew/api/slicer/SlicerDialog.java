package dev.voxelbrew.api.slicer;

import dev.voxelbrew.api.model.vui.Layout;
import java.util.Map;

/**
 * Frontend-neutral description of a slicer interaction.
 *
 * @param <R> accepted settings type
 */
public interface SlicerDialog<R> {
    /** {@return the human-readable dialog title} */
    String title();

    /** {@return the controls and their arrangement, interpreted by the frontend} */
    Layout layout();

    /**
     * Checks the supplied values. Each message should explain how to correct the value.
     *
     * @param values input values, which may be incomplete while the host gathers input
     * @return errors keyed by {@link dev.voxelbrew.api.model.vui.Input#id()}, or an empty map when valid
     */
    Map<String, String> validate(Map<String, String> values);

    /**
     * Builds the value returned by the host after settings are accepted.
     *
     * @param values validated input values keyed by component ID
     * @return accepted settings
     */
    R buildSettings(Map<String, String> values);
}
