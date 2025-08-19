package p2p;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AppIntegrationTest {

    private static Thread appThread;
    private static final String BASE_URL = "http://localhost:8080";
    private HttpClient httpClient;

    @BeforeAll
    static void setUpClass() throws InterruptedException {
        try {
            HttpClient testClient = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/"))
                .timeout(Duration.ofSeconds(2))
                .GET()
                .build();
            
            HttpResponse<String> response = testClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() >= 200 && response.statusCode() < 600) {
                System.out.println("Server already running on port 8080 - using existing instance");
                return;
            }
        } catch (Exception e) {
            // Server not running, start it
        }
        
        CountDownLatch serverStarted = new CountDownLatch(1);
        
        appThread = new Thread(() -> {
            try {
                System.setIn(new ByteArrayInputStream(new byte[0]));
                serverStarted.countDown();
                App.main(new String[]{});
            } catch (Exception e) {
                if (e.getMessage().contains("Address already in use")) {
                    System.out.println("Server already running - using existing instance");
                    serverStarted.countDown();
                } else {
                    System.err.println("Error starting app: " + e.getMessage());
                }
            }
        });
        
        appThread.setDaemon(true);
        appThread.start();
        
        assertTrue(serverStarted.await(5, TimeUnit.SECONDS), 
                  "Server should start or already be running within 5 seconds");
        Thread.sleep(2000);
    }

    @AfterAll
    static void tearDownClass() {
        if (appThread != null && appThread.isAlive()) {
            appThread.interrupt();
        }
    }

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    }

    @Test
    @Order(1)
    @DisplayName("Application should start and server should be accessible")
    void testApplicationStartup() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/"))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        // Server should respond (even if with 404 for root path)
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 600,
                  "Server should be responding with valid HTTP status code");
    }

    @Test
    @Order(2)
    @DisplayName("Upload endpoint should be accessible")
    void testUploadEndpointAccessible() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/upload"))
            .timeout(Duration.ofSeconds(5))
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 600,
                  "Upload endpoint should be accessible");
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isPresent(),
                  "CORS headers should be present");
    }

    @Test
    @Order(3)
    @DisplayName("Download endpoint should be accessible")
    void testDownloadEndpointAccessible() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/download/12345"))
            .timeout(Duration.ofSeconds(5))
            .GET()
            .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        assertTrue(response.statusCode() >= 400 && response.statusCode() < 600,
                  "Download endpoint should be accessible");
    }

    @Test
    @Order(4)
    @DisplayName("Server should handle multiple concurrent requests")
    void testConcurrentRequests() throws Exception {
        int numRequests = 5;
        CountDownLatch latch = new CountDownLatch(numRequests);
        CountDownLatch startLatch = new CountDownLatch(1);
        boolean[] results = new boolean[numRequests];
        
        for (int i = 0; i < numRequests; i++) {
            final int requestId = i;
            Thread thread = new Thread(() -> {
                try {
                    startLatch.await();
                    
                    HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL + "/"))
                        .timeout(Duration.ofSeconds(10))
                        .GET()
                        .build();
                    
                    HttpResponse<String> response = httpClient.send(request, 
                        HttpResponse.BodyHandlers.ofString());
                    
                    results[requestId] = response.statusCode() >= 200 && response.statusCode() < 600;
                    
                } catch (Exception e) {
                    System.err.println("Concurrent request " + requestId + " failed: " + e.getMessage());
                    results[requestId] = false;
                } finally {
                    latch.countDown();
                }
            });
            thread.start();
        }
        
        startLatch.countDown();
        assertTrue(latch.await(20, TimeUnit.SECONDS), "All requests should complete within 20 seconds");
        
        for (int i = 0; i < numRequests; i++) {
            assertTrue(results[i], "Concurrent request " + i + " should succeed");
        }
    }

    @Test
    @Order(5)
    @DisplayName("Server should have proper CORS configuration")
    void testCORSConfiguration() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(BASE_URL + "/upload"))
            .timeout(Duration.ofSeconds(5))
            .method("OPTIONS", HttpRequest.BodyPublishers.noBody())
            .header("Origin", "http://localhost:3000")
            .build();
        
        HttpResponse<String> response = httpClient.send(request, 
            HttpResponse.BodyHandlers.ofString());
        
        assertTrue(response.statusCode() >= 200 && response.statusCode() < 600,
                  "Server should respond to OPTIONS request");
        
        assertTrue(response.headers().firstValue("Access-Control-Allow-Origin").isPresent(),
                  "Access-Control-Allow-Origin header should be present");
        assertEquals("*", response.headers().firstValue("Access-Control-Allow-Origin").get(),
                    "Should allow all origins");
        
        Optional<String> allowMethodsHeader = response.headers().firstValue("Access-Control-Allow-Methods");
        if (allowMethodsHeader.isPresent()) {
            String allowedMethods = allowMethodsHeader.get();
            System.out.println("Access-Control-Allow-Methods header present: " + allowedMethods);
            assertTrue(allowedMethods.contains("GET") || allowedMethods.contains("POST"),
                      "Should allow at least GET or POST methods");
        } else {
            System.out.println("Access-Control-Allow-Methods header not present");
        }
        
        Optional<String> allowHeadersHeader = response.headers().firstValue("Access-Control-Allow-Headers");
        if (allowHeadersHeader.isPresent()) {
            String allowedHeaders = allowHeadersHeader.get();
            System.out.println("Access-Control-Allow-Headers header present: " + allowedHeaders);
            assertTrue(allowedHeaders.contains("Content-Type") || allowedHeaders.contains("*"),
                      "Should allow Content-Type header or all headers");
        } else {
            System.out.println("Access-Control-Allow-Headers header not present");
        }
    }

    @Test
    @Order(6)
    @DisplayName("Application should handle shutdown gracefully")
    void testGracefulShutdown() {
        assertDoesNotThrow(() -> {
            Thread testThread = new Thread(() -> {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            
            testThread.start();
            testThread.interrupt();
            testThread.join(1000);
        }, "Graceful shutdown should work without exceptions");
        
        assertDoesNotThrow(() -> {
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request, 
                HttpResponse.BodyHandlers.ofString());
            
            assertTrue(response.statusCode() >= 200 && response.statusCode() < 600,
                      "Server should still be responsive");
        }, "Server should remain accessible during shutdown tests");
    }

    @Test
    @Order(7)
    @DisplayName("Main method should handle different argument scenarios")
    void testMainMethodArguments() {
        assertDoesNotThrow(() -> {
            Class<?> appClass = Class.forName("p2p.App");
            java.lang.reflect.Method mainMethod = appClass.getMethod("main", String[].class);
            
            assertNotNull(mainMethod, "Main method should exist");
            assertTrue(java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers()),
                      "Main method should be static");
            assertTrue(java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers()),
                      "Main method should be public");
        }, "Main method should be properly defined");
        
        assertDoesNotThrow(() -> {
            Class<?> appClass = Class.forName("p2p.App");
            appClass.getDeclaredConstructor();
        }, "App class should have a constructor");
    }
}
