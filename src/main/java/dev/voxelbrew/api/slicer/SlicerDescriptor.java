package dev.voxelbrew.api.slicer;

import lombok.NonNull;

/**
 * Immutable discovery metadata for slicer providers.
 *
 * <p>The ID is used for programmatic selection and must remain stable across releases. Choose an ID
 * distinct from built-ins and other plugins; duplicate IDs do not create separate menu entries.
 * Settings and operation capabilities belong to the implementation.
 *
 * @param id nonempty identifier matching {@code [a-z0-9][a-z0-9.-]*}, for example {@code cloud-model}
 * @param name nonblank display label used by the host, for example {@code Cloud model upload}
 * @param version nonblank version of the slicer implementation shown by the host
 */
public record SlicerDescriptor(
        String id, @NonNull String name, @NonNull String version) {
    /**
     * Creates and validates discovery metadata without normalizing either value.
     *
     * @param id stable slicer identifier
     * @param name human-readable display label
     * @param version slicer implementation version
     * @throws IllegalArgumentException if the ID is null or invalid, or the name or version is blank
     * @throws NullPointerException if the name or version is null
     */
    public SlicerDescriptor {
        if (id == null || !id.matches("[a-z0-9][a-z0-9.-]*")) throw new IllegalArgumentException("Invalid slicer ID");
        if (name.isBlank()) throw new IllegalArgumentException("Missing slicer name");
        if (version.isBlank()) throw new IllegalArgumentException("Missing slicer version");
    }
}
