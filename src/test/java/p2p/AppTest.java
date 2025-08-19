package p2p;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class AppTest {

    @Test
    @DisplayName("Should answer with true")
    public void shouldAnswerWithTrue() {
        assertTrue(true);
    }

    @Test
    @DisplayName("App main method should exist and be callable")
    public void testAppMainMethodExists() {
        assertDoesNotThrow(() -> {
            try {
                Class<?> appClass = Class.forName("p2p.App");
                assertNotNull(appClass, "App class should exist");
                
                java.lang.reflect.Method mainMethod = appClass.getMethod("main", String[].class);
                assertNotNull(mainMethod, "Main method should exist");
                assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                          "Main method should be static");
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                fail("Failed to find App class or main method: " + e.getMessage());
            }
        });
    }

    @Test
    @DisplayName("App should have proper package structure")
    public void testPackageStructure() {
        assertDoesNotThrow(() -> {
            try {
                Class.forName("p2p.App");
                Class.forName("p2p.controller.FileController");
                Class.forName("p2p.service.FileSharer");
                Class.forName("p2p.utils.UploadUtils");
            } catch (ClassNotFoundException e) {
                fail("Failed to find core classes: " + e.getMessage());
            }
        }, "All core classes should be present in correct packages");
    }
}
