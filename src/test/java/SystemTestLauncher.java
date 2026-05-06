

import java.io.IOException;

/**
 * Test launcher to start all components:
 * - PresenceMonitoringDevice
 * - EnvironmentalControlSmartObject
 * - DoorSmartObject
 * - WindowSmartObject
 * - DataCollectorManager
 *
 * Each component runs as a separate process.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public class SystemTestLauncher {

    public static void main(String[] args) {

        try {

            // Start devices first
            startProcess("it.unimore.dipi.iot.SmartHenHouse.Implementations.PresenceMonitoringDevice");
            Thread.sleep(3000);
            startProcess("it.unimore.dipi.iot.SmartHenHouse.Implementations.EnvironmentalControlSmartObject");
            Thread.sleep(3000);
            startProcess("it.unimore.dipi.iot.SmartHenHouse.Implementations.DoorSmartObject");
            Thread.sleep(3000);
            startProcess("it.unimore.dipi.iot.SmartHenHouse.Implementations.WindowSmartObject");

            // Wait a bit before starting manager
            Thread.sleep(10000);

            // Start Data Collector Manager
            startProcess("it.unimore.dipi.iot.SmartHenHouse.Implementations.DataCollectorManager");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void startProcess(String className) throws IOException {

        new ProcessBuilder(
                "java",
                "-cp",
                System.getProperty("java.class.path"),
                className
        ).inheritIO().start();
    }
}

