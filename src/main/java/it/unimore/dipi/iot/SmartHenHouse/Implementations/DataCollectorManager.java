package it.unimore.dipi.iot.SmartHenHouse.Implementations;

import org.eclipse.paho.client.mqttv3.*;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import it.unimore.dipi.iot.SmartHenHouse.Messages.MessageDescriptor;

/**
 * Data Collector Manager responsible for:
 * - Subscribing to all sensor data
 * - Managing temperature thresholds
 * - Managing chicken presence
 * - Sending commands to actuators
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */

public class DataCollectorManager {

    private final static Logger logger = LoggerFactory.getLogger(DataCollectorManager.class);


    private static final String BROKER_URL = "tcp://localhost:1883";

    private MqttClient client;
    private Queue<String[]> messageQueue = new LinkedList<>();

    private static Gson gson = new Gson();

    // Temperature thresholds
    private int minTemp = 18;
    private int maxTemp = 25;
    private Map<String, Boolean> heatingState = new HashMap<>();
    private Map<String, Boolean> coolingState = new HashMap<>();

    // Chicken presence and time thresholds
    private List<String> doorIds = new ArrayList<>();
    private Map<String, Integer> presenceMap = new HashMap<>();
    private int expectedChickens = 10;
    private LocalTime closingTime = LocalTime.of(18, 0);
    private boolean closedToday = false;
    private LocalTime openingTime = LocalTime.of(6, 0);
    private boolean openedToday = false;

    public DataCollectorManager() throws Exception {

        String clientId = "manager-" + UUID.randomUUID();
        client = new MqttClient(BROKER_URL, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);

        client.connect(options);
    }

    /**
     * Start listening to all sensor data
     */
    public void start() throws Exception {

        client.subscribe("device/+/info", (topic, msg) -> {
            String payload = new String(msg.getPayload());
            messageQueue.add(new String[]{topic, payload, "INFO"});
        });

        client.subscribe("device/+/sensor/#", (topic, msg) -> {
            String payload = new String(msg.getPayload());
            messageQueue.add(new String[]{topic, payload, "SENSOR"});
        });

        logger.info("DataCollectorManager started and subscribed to topics");
    }


    /**
     * Handle incoming messages
     */
    private void onMessage(String topic, String payload) {

        try {

            MessageDescriptor message = gson.fromJson(payload, MessageDescriptor.class);

            if(message.getType().equalsIgnoreCase("TEMPERATURE")) {
                handleTemperature(topic, message.getValue());
            }
            else if(message.getType().equalsIgnoreCase("PRESENCE")) {
                handlePresence(topic, message.getValue());
            }
            else if(message.getType().equalsIgnoreCase("HEATING_STATE")) {
                String deviceId = topic.split("/")[1];
                heatingState.put(deviceId, message.getValue().equalsIgnoreCase("ON"));
            }
            else if(message.getType().equalsIgnoreCase("COOLING_STATE")) {
                String deviceId = topic.split("/")[1];
                coolingState.put(deviceId, message.getValue().equalsIgnoreCase("ON"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handle temperature logic
     */
    private void handleTemperature(String topic, String value) throws Exception {

        int temp = Integer.parseInt(value);
        String deviceId = topic.split("/")[1];

        boolean heating = heatingState.getOrDefault(deviceId, false);
        boolean cooling = coolingState.getOrDefault(deviceId, false);

        // Cooling logic
        if(temp > maxTemp && !cooling) {
            sendCommandTemperature(deviceId, "actuator/cooling", "ON", temp);
        } else if(temp <= maxTemp && cooling) {
            sendCommandTemperature(deviceId, "actuator/cooling", "OFF", temp);
        }

        // Heating logic
        if(temp < minTemp && !heating) {
            sendCommandTemperature(deviceId, "actuator/heating", "ON", temp);
        } else if(temp >= minTemp && heating) {
            sendCommandTemperature(deviceId, "actuator/heating", "OFF", temp);
        }
    }

    /**
     * Handle chicken presence logic (multi-device)
     */
    private void handlePresence(String topic, String value) throws Exception {

        int chickens = Integer.parseInt(value);

        String deviceId = topic.split("/")[1];

        presenceMap.put(deviceId, chickens);
    }

    /**
     * Handle automatic door closing in the evening
     */
    private void handleClosingTime() throws Exception {
        int totalChickens = presenceMap.values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();

        if((LocalTime.now().isAfter(openingTime) && !openedToday) && totalChickens == expectedChickens){

            for(String doorId : doorIds) {
                sendCommand(doorId, "actuator/door", "CLOSE");
            }

            closedToday = true;
            logger.info("All doors closed for the day");
        }

        // reset a mezzanotte
        if(LocalTime.now().isBefore(closingTime)) {
            closedToday = false;
        }
    }
    /**
     * Handle automatic door opening in the morning
     */
    private void handleOpeningTime() throws Exception {

        if(LocalTime.now().isAfter(closingTime) && !closedToday) {

            for(String doorId : doorIds) {
                sendCommand(doorId, "actuator/door", "OPEN");
            }

            openedToday = true;
            logger.info("All doors opened for the day");
        }

        // reset a mezzanotte
        if(LocalTime.now().isBefore(openingTime)) {
            openedToday = false;
        }
    }

    /**
     * Send command to a device via MQTT
     */
    private void sendCommand(String deviceId, String action, String value) throws Exception {

        String topic = "device/" + deviceId + "/" + action;

        MqttMessage msg = new MqttMessage(value.getBytes());
        msg.setQos(0);

        client.publish(topic, msg);
        if(!action.contains("heating") && !action.contains("cooling")) {
            logger.info("Sent command to topic: {} with value: {}", topic, value);
        }
    }

    /**
     * Send command to an environmental SO with temperature context for better logging
     */
    private void sendCommandTemperature(String deviceId, String action, String value, int temperature) throws Exception {
        sendCommand(deviceId, action, value);
        String topic = "device/" + deviceId + "/" + action;
        logger.info("Sent command to topic: {} with value: {} cause temperature is {}", topic, value, temperature);
    }

    /**
     * Handle device info messages to identify door devices
     */
    private void handleDeviceInfo(String topic, String payload) {

        String deviceId = topic.split("/")[1];
        logger.info("Discovered device: {} of type {}", deviceId, payload);

        if(payload.equalsIgnoreCase("DOOR")) {

            if(!doorIds.contains(deviceId)) {
                doorIds.add(deviceId);
            }

        }
    }

    /**
     * Handle queued MQTT messages
     */
    private void handleMessage(String[] data) {

        try {

            String topic = data[0];
            String payload = data[1];
            String type = data[2];

            if(type.equals("INFO")) {
                handleDeviceInfo(topic, payload);
            } else {
                onMessage(topic, payload);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

        try {

            DataCollectorManager manager = new DataCollectorManager();

            manager.start();

            while (true) {

                while (!manager.messageQueue.isEmpty()) {

                    String[] data = manager.messageQueue.poll();

                    manager.handleMessage(data);
                }
                manager.handleClosingTime();
                manager.handleOpeningTime();
                Thread.sleep(100);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}

