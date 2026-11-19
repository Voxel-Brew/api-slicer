package dev.voxelbrew.api.slicer;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SlicerDescriptorTest {
    @Test
    void preservesDisplayValuesAndValidatesIdentifiers() {
        var descriptor = new SlicerDescriptor("local.slicer-2", " Local slicer ", " 1 ");
        assertThat(descriptor.name()).isEqualTo(" Local slicer ");
        assertThat(descriptor.version()).isEqualTo(" 1 ");
        for (String invalid : new String[] {null, "", "Uppercase", "../path", "has space", "-leading"}) {
            assertThatThrownBy(() -> new SlicerDescriptor(invalid, "Slicer", "1"))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        assertThatThrownBy(() -> new SlicerDescriptor("slicer", " ", "1")).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SlicerDescriptor("slicer", "Slicer", " "))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new SlicerDescriptor("slicer", "Slicer", null))
                .isInstanceOf(NullPointerException.class);
    }
}
