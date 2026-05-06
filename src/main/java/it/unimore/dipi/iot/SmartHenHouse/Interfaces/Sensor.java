package it.unimore.dipi.iot.SmartHenHouse.Interfaces;

/**
 * Sensor interface representing a generic IoT sensor capable of
 * generating and publishing data to an MQTT broker.
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public interface Sensor {

    /**
     * Generate and publish sensor data to the MQTT broker
     */
    void publishData() throws Exception;
}


