package it.unimore.dipi.iot.SmartHenHouse.Implementations;

import it.unimore.dipi.iot.SmartHenHouse.Messages.MessageDescriptor;
import it.unimore.dipi.iot.SmartHenHouse.Models.*;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Concrete Device representing a Presence Monitoring Sensor.
 * It simulates and publishes the number of chickens detected.
 *
 * Includes a main method to run as standalone MQTT producer.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */

public class PresenceMonitoringDevice extends AbstractDevice {

    private final static Logger logger = LoggerFactory.getLogger(PresenceMonitoringDevice.class);

    private Random random;
    int chickens = 10;

    private static final String BROKER_URL = "tcp://127.0.0.1:1883";
    private static final int PUBLISH_DELAY = 5000;

    public PresenceMonitoringDevice(String deviceId, MqttClient client) {
        super(deviceId, client);
        this.random = new Random();
    }

    /**
     * Generate and publish the number of chickens detected
     */
    @Override
    public void publishData() throws Exception {

        chickens += random.nextInt(10) - 5;

        MessageDescriptor message = new MessageDescriptor(
                System.currentTimeMillis(),
                "PRESENCE",
                String.valueOf(chickens)
        );

        String payload = gson.toJson(message);

        publish(buildTopic("sensor/presence"), payload);
        logger.debug("{} finded {} chickens", deviceId, payload);
    }

    public static void main(String[] args) {

        try {

            // UUID unico
            String uniqueId = UUID.randomUUID().toString();

            // Client ID
            String clientId = uniqueId;

            MqttClient client = new MqttClient(BROKER_URL, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.connect(options);
            logger.info("PresenceMonitoringDevice connected to MQTT broker with clientId: {}", clientId);

            // Device ID derivato dallo stesso UUID
            String deviceId = "presence-" + uniqueId.substring(0, 5);

            PresenceMonitoringDevice device =
                    new PresenceMonitoringDevice(deviceId, client);

            device.publishDeviceInfo("PresenceMonitoringDevice", logger);

            while (true) {

                device.publishData();

                Thread.sleep(PUBLISH_DELAY);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

