package ci553.happyshop.storageAccess;

import ci553.happyshop.orderManagement.OrderState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OrderFileManagerTest {

    @TempDir
    Path tempDir;

    @Test
    void createOrderFile_createsFileWithContent() throws Exception {
        Path orderedDir = Files.createDirectory(tempDir.resolve("ordered"));
        int orderId = 10;

        String content = """
                OrderId: 10
                State: Ordered
                OrderedDateTime: 2025-01-01 10:00:00
                ProgressingDateTime:
                CollectedDateTime:
                """;

        OrderFileManager.createOrderFile(orderedDir, orderId, content);

        Path file = orderedDir.resolve("10.txt");
        assertTrue(Files.exists(file), "Order file should exist");

        String read = Files.readString(file);
        assertTrue(read.contains("OrderId: 10"));
        assertTrue(read.contains("State: Ordered"));
    }

    @Test
    void updateAndMoveOrderFile_movesFileAndUpdatesStateToProgressing() throws Exception {
        Path orderedDir = Files.createDirectory(tempDir.resolve("ordered"));
        Path progressingDir = Files.createDirectory(tempDir.resolve("progressing"));

        int orderId = 12;
        String content = """
                OrderId: 12
                State: Ordered
                OrderedDateTime: 2025-01-01 10:00:00
                ProgressingDateTime:
                CollectedDateTime:
                """;

        OrderFileManager.createOrderFile(orderedDir, orderId, content);

        boolean ok = OrderFileManager.updateAndMoveOrderFile(
                orderId, OrderState.Progressing, orderedDir, progressingDir
        );

        assertTrue(ok, "updateAndMoveOrderFile should return true");
        assertFalse(Files.exists(orderedDir.resolve("12.txt")), "File should be moved out of ordered folder");
        assertTrue(Files.exists(progressingDir.resolve("12.txt")), "File should exist in progressing folder");

        String updated = Files.readString(progressingDir.resolve("12.txt"));
        assertTrue(updated.contains("State: Progressing"));
        assertTrue(updated.contains("ProgressingDateTime:"), "ProgressingDateTime line should exist");
    }

    @Test
    void readOrderFile_throwsIfMissing() {
        Path orderedDir = tempDir.resolve("ordered");
        Exception ex = assertThrows(Exception.class, () -> OrderFileManager.readOrderFile(orderedDir, 999));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }
}
