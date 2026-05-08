package it.unimore.dipi.iot.SmartHenHouse.Implementations;

import it.unimore.dipi.iot.SmartHenHouse.Messages.MessageDescriptor;
import it.unimore.dipi.iot.SmartHenHouse.Models.*;
import org.eclipse.paho.client.mqttv3.*;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Smart Object representing a Window.
 *
 * Includes a main method to run as standalone MQTT smart object.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public class WindowSmartObject extends OpeningSmartObject {

    private final static Logger logger =  LoggerFactory.getLogger(WindowSmartObject.class);


    public WindowSmartObject(String deviceId, MqttClient client) {
        super(deviceId, client);
    }


    @Override
    public void handleCommand(String topic, String payload) {
        handleOpeningCommand(topic, payload, "WINDOW", logger);
    }

    @Override
    public void subscribeToCommands() throws Exception {

        client.subscribe(buildTopic("actuator/window"), (topic, msg) -> {
            String payload = new String(msg.getPayload());
            enqueueCommand(topic, payload);
        });

        logger.debug("{} subscribed to {}", deviceId, buildTopic("actuator/window"));
    }

    /**
     * Publish window state (OPEN / CLOSED)
     */
    @Override
    public void publishData() throws Exception {

        String state = isOpen ? "OPEN" : "CLOSED";

        MessageDescriptor message = new MessageDescriptor(
                System.currentTimeMillis(),
                "WINDOW",
                state
        );

        String payload = gson.toJson(message);

        publish(buildTopic("sensor/window"), payload);
        logger.debug("{} published window state: {}", deviceId, payload);
    }

    public static void main(String[] args) {

        try {

            String uniqueId = UUID.randomUUID().toString();
            String clientId = uniqueId;

            MqttClient client = new MqttClient(BROKER_URL, clientId);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            client.connect(options);
            logger.info("WindowSmartObject connected to MQTT broker with clientId: {}", clientId);

            String deviceId = "window-" + uniqueId.substring(0, 5);

            WindowSmartObject device =
                    new WindowSmartObject(deviceId, client);

            device.publishDeviceInfo("Window", logger);
            device.subscribeToCommands();

            while (true) {

                device.processCommands();

                device.publishData();

                Thread.sleep(PUBLISH_DELAY);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

