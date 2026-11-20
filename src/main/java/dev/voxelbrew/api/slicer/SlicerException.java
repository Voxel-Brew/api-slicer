package dev.voxelbrew.api.slicer;

import java.io.Serial;

/** A failure specific to a slicer operation. Cancellation uses {@link java.util.concurrent.CancellationException}. */
public abstract sealed class SlicerException extends Exception {
    @Serial
    private static final long serialVersionUID = 1L;

    /** The evaluated plate, printer target, or requested capabilities cannot be sliced by this provider. */
    public static final class UnsupportedInput extends SlicerException {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Describes why a provider rejected the requested plate or target.
         *
         * @param message describes what must change in the plate or target
         */
        public UnsupportedInput(String message) {
            super(message, null);
        }
    }

    /** A backend setting, preset, or executable is unsuitable for this operation. */
    public static final class Configuration extends SlicerException {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Describes a backend configuration problem.
         *
         * @param message describes what must change in the backend configuration
         */
        public Configuration(String message) {
            super(message, null);
        }
    }

    /** The backend failed while converting or slicing the plate. */
    public static final class Execution extends SlicerException {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Describes a failed backend operation.
         *
         * @param message describes the execution failure
         */
        public Execution(String message) {
            this(message, null);
        }

        /**
         * Describes a failed backend operation and retains the underlying cause.
         *
         * @param message describes the execution failure
         * @param cause original backend or conversion failure
         */
        public Execution(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /** The backend output is missing, malformed, or inconsistent with the requested job. */
    public static final class InvalidArtifact extends SlicerException {
        @Serial
        private static final long serialVersionUID = 1L;

        /**
         * Describes why a returned artifact is unusable.
         *
         * @param message describes the output validation failure
         */
        public InvalidArtifact(String message) {
            this(message, null);
        }

        /**
         * Describes why a returned artifact is unusable and retains the underlying cause.
         *
         * @param message describes the output validation failure
         * @param cause original parsing or validation failure
         */
        public InvalidArtifact(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private SlicerException(String message, Throwable cause) {
        super(message, cause);
    }
}
