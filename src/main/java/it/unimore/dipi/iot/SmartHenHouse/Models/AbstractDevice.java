package it.unimore.dipi.iot.SmartHenHouse.Models;

import com.google.gson.Gson;
import it.unimore.dipi.iot.SmartHenHouse.Interfaces.Sensor;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract base class representing a generic IoT device.
 * It provides common functionalities such as MQTT communication
 * and topic management.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public abstract class AbstractDevice implements Sensor {

    protected String deviceId;
    protected MqttClient client;

    protected static Gson gson = new Gson();

    public AbstractDevice(String deviceId, MqttClient client) {
        this.deviceId = deviceId;
        this.client = client;
    }

    /**
     * Build a standard MQTT topic for the device
     *
     * @param subTopic specific sub-topic (e.g., sensor/temperature)
     * @return full topic string
     */
    protected String buildTopic(String subTopic) {
        return "device/" + deviceId + "/" + subTopic;
    }

    /**
     * Publish a message to the MQTT broker
     *
     * @param topic   MQTT topic
     * @param payload message content
     */
    protected void publish(String topic, String payload) throws Exception {
        MqttMessage msg = new MqttMessage(payload.getBytes());
        msg.setQos(0);
        client.publish(topic, msg);
    }
    /**
     * Publish the information of the device to the MQTT broker
     *
     * @param type type of the device
     */
    public void publishDeviceInfo(String type, Logger logger) throws Exception {

        String topic = buildTopic("info");

        String payload = type;

        MqttMessage msg = new MqttMessage(payload.getBytes());
        msg.setRetained(true);

        client.publish(topic, msg);
        logger.debug("{} published device info: {}", deviceId, payload);
    }
}
