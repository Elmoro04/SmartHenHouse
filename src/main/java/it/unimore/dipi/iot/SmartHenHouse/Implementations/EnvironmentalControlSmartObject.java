package it.unimore.dipi.iot.SmartHenHouse.Implementations;

import it.unimore.dipi.iot.SmartHenHouse.Messages.MessageDescriptor;
import it.unimore.dipi.iot.SmartHenHouse.Models.*;
import org.eclipse.paho.client.mqttv3.*;
import java.util.Random;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Concrete Smart Object representing an Environmental Control system.
 * It simulates temperature sensing and reacts to heating/cooling commands.
 *
 * Includes a main method to run as standalone MQTT smart object.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public class EnvironmentalControlSmartObject extends AbstractSmartObject {

    private final static Logger logger = LoggerFactory.getLogger(EnvironmentalControlSmartObject.class);


    private static final String BROKER_URL = "tcp://127.0.0.1:1883";
    private static final int PUBLISH_DELAY = 15000;

    private Random random;

    int temperature = 21;
    private boolean heatingOn;
    private boolean coolingOn;

    public EnvironmentalControlSmartObject(String deviceId, MqttClient client) {
        super(deviceId, client);
        this.random = new Random();
        this.heatingOn = false;
        this.coolingOn = false;
    }

    /**
     * Subscribe to heating and cooling actuator topics
     */
    @Override
    public void subscribeToCommands() throws Exception {

        client.subscribe(buildTopic("actuator/heating"), (topic, msg) -> {
            enqueueCommand(topic, new String(msg.getPayload()));
        });

        client.subscribe(buildTopic("actuator/cooling"), (topic, msg) -> {
            enqueueCommand(topic, new String(msg.getPayload()));
        });
        logger.debug("{} subscribed to heating and cooling commands", deviceId);
    }

    /**
     * Simulate and publish temperature value and heating and cooling state
     */
    @Override
    public void publishData() throws Exception {

        temperature = temperature - 1 + random.nextInt(2);
        if(heatingOn) {
            temperature += 1;
        }
        if(coolingOn) {
            temperature -= 1;
        }

        MessageDescriptor tempMsg = new MessageDescriptor(
                System.currentTimeMillis(),
                "TEMPERATURE",
                String.valueOf(temperature)
        );

        MessageDescriptor heatMsg = new MessageDescriptor(
                System.currentTimeMillis(),
                "HEATING_STATE",
                heatingOn ? "ON" : "OFF"
        );

        MessageDescriptor coolMsg = new MessageDescriptor(
                System.currentTimeMillis(),
                "COOLING_STATE",
                coolingOn ? "ON" : "OFF"
        );

        publish(buildTopic("sensor/temperature"), gson.toJson(tempMsg));
        publish(buildTopic("sensor/heating"), gson.toJson(heatMsg));
        publish(buildTopic("sensor/cooling"), gson.toJson(coolMsg));

        logger.debug("{} published env state: {} {} {}", deviceId, gson.toJson(tempMsg), gson.toJson(heatMsg), gson.toJson(coolMsg));
    }

    /**
     * Handle heating and cooling commands
     */
    @Override
    public void handleCommand(String topic, String payload) {

        try {

            if(topic.contains("heating")) {

                heatingOn = payload.equalsIgnoreCase("ON");
                logger.debug("{} set heating to {}", deviceId, heatingOn ? "ON" : "OFF");

            } else if(topic.contains("cooling")) {

                coolingOn = payload.equalsIgnoreCase("ON");
                logger.debug("{} set cooling to {}", deviceId, coolingOn ? "ON" : "OFF");

            } else {

                logger.error("{} received unknown command on topic {}: {}", deviceId, topic, payload);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            logger.info("EnvironmentalControlSmartObject connected to MQTT broker with clientId: {}", clientId);

            String deviceId = "env-" + uniqueId.substring(0, 5);

            EnvironmentalControlSmartObject device =
                    new EnvironmentalControlSmartObject(deviceId, client);

            device.publishDeviceInfo("EnvironmentalControl", logger);
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

