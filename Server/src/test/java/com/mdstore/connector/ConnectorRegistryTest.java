package com.mdstore.connector;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectorRegistryTest {

    @Mock
    private SupplierConnector vietShareConnector;

    @Mock
    private SupplierConnector dummyConnector;

    @Test
    void init_loadsAllConnectorsAndRetrievesThemByCode() {
        // Arrange
        when(vietShareConnector.getSupplierCode()).thenReturn("VIETSHARE");
        when(dummyConnector.getSupplierCode()).thenReturn("DUMMY");
        
        List<SupplierConnector> list = List.of(vietShareConnector, dummyConnector);

        // Act
        ConnectorRegistry registry = new ConnectorRegistry(list);

        // Assert
        assertThat(registry.getConnector("VIETSHARE")).isSameAs(vietShareConnector);
        assertThat(registry.getConnector("DUMMY")).isSameAs(dummyConnector);
    }

    @Test
    void getConnector_whenCodeNotFound_throwsIllegalArgumentException() {
        // Arrange
        when(vietShareConnector.getSupplierCode()).thenReturn("VIETSHARE");
        List<SupplierConnector> list = List.of(vietShareConnector);
        ConnectorRegistry registry = new ConnectorRegistry(list);

        // Act & Assert
        assertThatThrownBy(() -> registry.getConnector("INVALID_CODE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported supplier code: INVALID_CODE");
    }

    @Test
    void getAllConnectors_returnsAllRegisteredConnectors() {
        // Arrange
        when(vietShareConnector.getSupplierCode()).thenReturn("VIETSHARE");
        when(dummyConnector.getSupplierCode()).thenReturn("DUMMY");
        List<SupplierConnector> list = List.of(vietShareConnector, dummyConnector);
        ConnectorRegistry registry = new ConnectorRegistry(list);

        // Act & Assert
        assertThat(registry.getAllConnectors()).containsExactlyInAnyOrder(vietShareConnector, dummyConnector);
    }
}
