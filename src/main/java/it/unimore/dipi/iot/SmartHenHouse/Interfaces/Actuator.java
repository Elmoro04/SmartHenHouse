package it.unimore.dipi.iot.SmartHenHouse.Interfaces;

/**
 * Actuator interface representing a generic IoT actuator capable of
 * receiving commands and executing actions accordingly.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public interface Actuator {

    /**
     * @param topic   the MQTT topic
     * @param payload the message content
     */
    void handleCommand(String topic, String payload);
}

