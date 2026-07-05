package com.toolsmith.dockerbuilder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.URI;

@SpringBootApplication
public class DockerCommandBuilderApplication {

    public static void main(String[] args) {
        SpringApplication.run(DockerCommandBuilderApplication.class, args);
    }

    /**
     * When run as a packaged desktop app (jpackage), open the UI in the
     * default browser automatically once the embedded server is ready.
     * Harmless no-op on headless/server environments (e.g. Render).
     */
    @EventListener(ApplicationReadyEvent.class)
    public void openBrowserOnStartup() {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI("http://localhost:8080"));
            }
        } catch (Exception e) {
            // Headless environment or browse not supported — ignore, user can open manually.
        }
    }

}
