package dev.voxelbrew.api.slicer;

import static org.assertj.core.api.Assertions.assertThat;

import dev.voxelbrew.api.model.printing.Cancellation;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class SlicerProviderTest {
    @Test
    void providersCanOmitExecutableDiscovery() throws Exception {
        SlicerProvider provider = new SlicerProvider() {
            @Override
            public SlicerDescriptor descriptor() {
                return new SlicerDescriptor("test", "Test slicer", "1");
            }

            @Override
            public Slicer create() {
                return (model, window, cancellation, diagnostics) -> Optional.empty();
            }
        };
        assertThat(provider.discoverExecutables(new Cancellation())).isEmpty();
    }
}
