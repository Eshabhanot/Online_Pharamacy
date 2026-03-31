package in.cg.main.controller;

import in.cg.main.dto.InventoryAddRequest;
import in.cg.main.dto.InventoryResponse;
import in.cg.main.service.InventoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryControllerTest {

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private InventoryController inventoryController;

    @Test
    void addBatch_returnsOk() {
        InventoryAddRequest request = new InventoryAddRequest();
        InventoryResponse responseDto = new InventoryResponse();
        responseDto.setBatchNumber("B-1");
        when(inventoryService.addBatch(request)).thenReturn(responseDto);

        ResponseEntity<InventoryResponse> response = inventoryController.addBatch(request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("B-1", response.getBody().getBatchNumber());
        verify(inventoryService).addBatch(request);
    }

    @Test
    void reduceStock_returnsSuccessMessage() {
        ResponseEntity<String> response = inventoryController.reduceStock(1L, 5);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("Stock reduced successfully", response.getBody());
        verify(inventoryService).reduceStock(1L, 5);
    }

    @Test
    void getExpiringBatches_returnsList() {
        InventoryResponse dto = new InventoryResponse();
        dto.setBatchNumber("EXP-1");
        when(inventoryService.getExpiringBatches(10)).thenReturn(List.of(dto));

        ResponseEntity<List<InventoryResponse>> response = inventoryController.getExpiringBatches(10);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        verify(inventoryService).getExpiringBatches(10);
    }

    @Test
    void getLowStockBatches_returnsList() {
        InventoryResponse dto = new InventoryResponse();
        dto.setBatchNumber("LOW-1");
        when(inventoryService.getLowStockBatches()).thenReturn(List.of(dto));

        ResponseEntity<List<InventoryResponse>> response = inventoryController.getLowStockBatches();

        assertEquals(200, response.getStatusCode().value());
        assertEquals("LOW-1", response.getBody().get(0).getBatchNumber());
        verify(inventoryService).getLowStockBatches();
    }
}
