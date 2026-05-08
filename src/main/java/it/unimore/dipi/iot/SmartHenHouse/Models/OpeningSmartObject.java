package it.unimore.dipi.iot.SmartHenHouse.Models;
import org.eclipse.paho.client.mqttv3.MqttClient;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        this.isOpen = false;
    }

    /**
     * Handle OPEN and CLOSE commands
     *
     * @param topic   MQTT topic
     * @param payload message content
     * @param type    opening object type
     * @param logger  logger instance
     */
    protected void handleOpeningCommand(String topic, String payload, String type, Logger logger) {

        if(payload.equalsIgnoreCase("OPEN")) {

            if(!isOpen) {
                isOpen = true;
                logger.debug("{} received OPEN command from topic {}", type, topic);
            } else {
                logger.debug("{} already OPEN", type);
            }

        } else if(payload.equalsIgnoreCase("CLOSE")) {

            if(isOpen) {
                isOpen = false;
                logger.debug("{} received CLOSE command from topic {}", type, topic);
            } else {
                logger.debug("{} already CLOSED", type);
            }

        } else {
            logger.error("{} received unknown command {} from topic {}", type, payload, topic);
        }
    }

    /**
     * Return current state of the opening object
     */
    public boolean isOpen() {
        return isOpen;
    }
}

