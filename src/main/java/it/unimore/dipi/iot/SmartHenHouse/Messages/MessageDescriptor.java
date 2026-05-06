package it.unimore.dipi.iot.SmartHenHouse.Messages;

/**
 * MessageDescriptor represents a generic MQTT message payload
 * exchanged between devices and the Data Collector Manager.
 *
 * It follows a structured JSON format including:
 * - timestamp: message generation time
 * - type: type of data or command
 * - value: associated value
 *
 * @author Francesco Morelli
 * @project Smart Hen House
 * @created 16/04/2026
 */
public class MessageDescriptor {

    private long timestamp;
    private String type;
    private String value;

    public MessageDescriptor() {}

    public MessageDescriptor(long timestamp, String type, String value) {
        this.timestamp = timestamp;
        this.type = type;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
