# Developing a slicer provider

Compile with Java 21 against `dev.voxelbrew.api:slicer` and its model API. Use compile-only dependencies and ship your implementation and any private libraries in the plugin JAR. Register the public, no-argument provider class by putting its fully qualified name in `META-INF/services/dev.voxelbrew.api.slicer.SlicerProvider`.

## Example provider

The standalone example returns a small fixture toolpath to demonstrate the host workflow. A production provider converts and slices the evaluated geometry and derives the actual backend version from the executable or service response.

Keep the dialog controls as fields so the layout, validation, and settings builder share their IDs and defaults. Return all field errors from `validate()` and call it again in `buildSettings()` to protect callers that build settings directly. Both methods work with incomplete input and perform no I/O.

```java
package example;

import dev.voxelbrew.api.model.printing.*;
import dev.voxelbrew.api.model.vui.*;
import dev.voxelbrew.api.slicer.*;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public final class ExampleProvider implements SlicerProvider {
    private static final SlicerDescriptor DESCRIPTOR =
            new SlicerDescriptor("example", "Example slicer", "1.0");

    @Override
    public SlicerDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public Optional<SlicerDialog<?>> settingsDialog() {
        return Optional.of(new SettingsDialog());
    }

    @Override
    public String checkConfiguration(SlicerWindow window, Cancellation cancellation) {
        cancellation.check();
        window.show(new SettingsDialog()).orElseThrow(java.util.concurrent.CancellationException::new);
        return "example-fixture 1";
    }

    @Override
    public Slicer create() {
        return new Operation();
    }

    private static final class Operation implements Slicer {
        @Override
        public Optional<PrintArtifact> slice(ManufacturingModel model, SlicerWindow window,
                Cancellation cancellation, Consumer<String> diagnostics) throws SlicerException.UnsupportedInput {
            cancellation.check();
            var settings = window.show(new SettingsDialog());
            cancellation.check();
            if (settings.isEmpty()) return Optional.empty();

            var printer = model.profile().printer();
            if (!printer.outputFormat().equals("gcode") || model.instances().isEmpty())
                throw new SlicerException.UnsupportedInput("Expected a nonempty plate targeting text G-code");

            // Fixture output. A production implementation slices model.placedMesh(instance)
            // with model.setup().supports(), model.profile(), and its accepted settings.
            var accepted = settings.get();
            var cooling = accepted.partCooling() ? "M106 S128\n" : "";
            var bytes = (cooling + ";TYPE:Perimeter\nG90\nM82\nG1 Z0.2\nG1 X10 E1 F"
                    + accepted.feedRateMmPerMinute() + "\n").getBytes(StandardCharsets.UTF_8);
            diagnostics.accept("Fixture G-code generated");
            cancellation.check();

            var target = model.setup().workspaceSettings().getOrDefault(PrintTarget.PRINTER_ID, "");
            return Optional.of(PrintArtifact.of("gcode", bytes, Map.of(
                    "document", model.documentId(),
                    "revision", model.revision(),
                    "backend", DESCRIPTOR.id(),
                    "backend_version", "example-fixture 1",
                    "printer", printer.id(),
                    "nozzle_mm", Double.toString(printer.nozzleMm()),
                    "target_kind", target.isBlank() ? "profile" : "printer",
                    "target_id", target.isBlank() ? model.setup().printerId() : target,
                    "target_profile", model.setup().printerId())));
        }
    }

    private record Settings(int feedRateMmPerMinute, boolean partCooling) {}

    private static final class SettingsDialog implements SlicerDialog<Settings> {
        private final TextField feedRate = new TextField("feed", "Feed rate (mm/min)", "600");
        private final CheckBox partCooling = new CheckBox("partCooling", "Enable part cooling", false);

        @Override
        public String title() {
            return "Example slicer settings";
        }

        @Override
        public Layout layout() {
            return new VerticalLayout(feedRate, partCooling);
        }

        @Override
        public Map<String, String> validate(Map<String, String> values) {
            var errors = new LinkedHashMap<String, String>();
            if (!isPositiveInteger(feedRateValue(values)))
                errors.put(feedRate.id(), "Enter a positive whole number");
            var cooling = partCoolingValue(values);
            if (!"true".equals(cooling) && !"false".equals(cooling))
                errors.put(partCooling.id(), "Choose true or false");
            return Map.copyOf(errors);
        }

        @Override
        public Settings buildSettings(Map<String, String> values) {
            var errors = validate(values);
            if (!errors.isEmpty()) throw new IllegalArgumentException("Invalid slicer settings: " + errors);
            return new Settings(Integer.parseInt(feedRateValue(values)), Boolean.parseBoolean(partCoolingValue(values)));
        }

        private String feedRateValue(Map<String, String> values) {
            return values.getOrDefault(feedRate.id(), feedRate.value()).strip();
        }

        private String partCoolingValue(Map<String, String> values) {
            return values.getOrDefault(partCooling.id(), Boolean.toString(partCooling.selected()));
        }

        private static boolean isPositiveInteger(String value) {
            try {
                return Integer.parseInt(value) > 0;
            } catch (NumberFormatException invalid) {
                return false;
            }
        }
    }
}
```

Register `example.ExampleProvider` and place the JAR in the host's trusted plugin directory. The provider's descriptor version identifies the plugin implementation. `backend_version` identifies the actual slicer that generated the bytes.

## Operation and host lifecycle

The host selects the printer target, resolves hardware and process settings, evaluates a detached model for each plate, and calls a fresh operation for each plate. The desktop renders `settingsDialog()` for each named installation. Implement `checkConfiguration()` to validate the proposed settings and verify the executable or service without a plate. Return the version reported by the backend. The fixture above returns its own version because it runs entirely in-process. The desktop offers installations that passed the check in its Process tab. During slicing, the desktop resolves the window request from saved values and dialog defaults. Use the same field IDs and defaults in both dialogs. The CLI resolves the same fields from invocation options, saved values, and dialog defaults in descending priority. Resolve operation settings before creating temporary inputs or contacting an external slicer.

Check cancellation while converting and immediately before returning. Use `Cancellation.onCancel` to interrupt a blocking backend and remove the listener on completion. Propagate running cancellation as `CancellationException`. Report unsupported plates and targets with `SlicerException.UnsupportedInput`, unsuitable backend settings with `SlicerException.Configuration`, backend execution failures with `SlicerException.Execution`, and invalid output with `SlicerException.InvalidArtifact`. Preserve the original cause when wrapping a failure. Actual I/O failures may propagate as `IOException`. Scope temporary inputs, executable output, and cleanup to one invocation. Compare the actual output format to the resolved hardware and include provenance before returning. For Orca packages, preserve the printer validation metadata, including `model` and `tool`.

A host discovers the registry implementation directly. Keep the registry open until every operation using its plugins completes.

```java
try (var registry = ServiceLoader.load(SlicerProviderRegistry.class).findFirst().orElseThrow()) {
    registry.withDirectory(pluginDirectory);
    var provider = registry.require(model.setup().backendId());
    var artifact = provider.create().slice(model, window, cancellation, diagnostics);
    // If present, independently inspect the bytes and verify provenance.
    // Recheck cancellation and the live document revision under the publication guard.
    // Attach the validated artifact. Save or submit only on an explicit host action.
}
```

Discovery reads descriptors and must be safe without installed executables or reachable services. Providers only receive detached geometry, manufacturing settings, and target identity. Printer credentials remain in the host.
