package it.unimore.dipi.iot.SmartHenHouse.Models;

import com.google.gson.Gson;
import it.unimore.dipi.iot.SmartHenHouse.Interfaces.Actuator;
import org.eclipse.paho.client.mqttv3.MqttClient;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Abstract class representing a Smart Object capable of both
 * sensing data and reacting to actuator commands.
 *
 * Extends AbstractDevice and implements Actuator behavior.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public abstract class AbstractSmartObject extends AbstractDevice implements Actuator {

    protected Queue<String[]> commandQueue = new LinkedList<>();

    protected static Gson gson = new Gson();

    public AbstractSmartObject(String deviceId, MqttClient client) {
        super(deviceId, client);
    }

    /**
     * TODO implemented by subclasses
     */
    public abstract void subscribeToCommands() throws Exception;

    /**
     * TODO implemented by subclasses
     *
     * @param topic   MQTT topic
     * @param payload message content
     */
    @Override
    public abstract void handleCommand(String topic, String payload);

    /**
     * Enqueue incoming MQTT command
     *
     * @param topic   MQTT topic
     * @param payload message content
     */
    protected void enqueueCommand(String topic, String payload) {
        commandQueue.add(new String[]{topic, payload});
    }

    /**
     * Process queued MQTT commands
     */
    public void processCommands() {

        while (!commandQueue.isEmpty()) {

            String[] data = commandQueue.poll();

            String topic = data[0];
            String payload = data[1];

            handleCommand(topic, payload);
        }
    }

}
