package it.unimore.dipi.iot.SmartHenHouse.Models;
import it.unimore.dipi.iot.SmartHenHouse.Messages.MessageDescriptor;
import org.eclipse.paho.client.mqttv3.MqttClient;

/**
 * Abstract class representing a generic opening smart object
 * such as a door or a window.
 *
 * It manages the open/close state and provides common actuator logic.
 *
 * Subclasses must define their own MQTT subscription topics.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public abstract class OpeningSmartObject extends AbstractSmartObject {

    protected static final String BROKER_URL = "tcp://localhost:1883";
    protected static final int PUBLISH_DELAY = 5000;

    protected boolean isOpen;


    public OpeningSmartObject(String deviceId, MqttClient client) {
        super(deviceId, client);
        this.isOpen = true;
    }

    /**
     * Handle OPEN and CLOSE commands
     */
    @Override
    public void handleCommand(String topic, String payload) {

        try {

            MessageDescriptor message;

            if(payload.equalsIgnoreCase("OPEN")) {

                if(!isOpen) {
                    isOpen = true;
                    message = new MessageDescriptor(System.currentTimeMillis(), "DOOR", "OPENED");
                } else {
                    message = new MessageDescriptor(System.currentTimeMillis(), "DOOR", "ALREADY_OPENED");
                }

            } else if(payload.equalsIgnoreCase("CLOSE")) {

                if(isOpen) {
                    isOpen = false;
                    message = new MessageDescriptor(System.currentTimeMillis(), "DOOR", "CLOSED");
                } else {
                    message = new MessageDescriptor(System.currentTimeMillis(), "DOOR", "ALREADY_CLOSED");
                }

            } else {
                message = new MessageDescriptor(System.currentTimeMillis(), "DOOR", "UNKNOWN_COMMAND");
            }

            publish(buildTopic("sensor/door"), gson.toJson(message));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Return current state of the opening object
     */
    public boolean isOpen() {
        return isOpen;
    }
}

