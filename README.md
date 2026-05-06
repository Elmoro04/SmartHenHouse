# Smart Hen House - IoT System

## Overview

This project implements a simplified IoT system for monitoring and controlling a smart hen house using the MQTT protocol.

The system is composed of distributed devices (sensors and smart objects) and a central Data Collector Manager (DCM) responsible for data processing and actuator control.

The design follows a modular and extensible architecture typical of IoT systems.

---

## System Architecture

The system is structured into three main components:

- Sensors (Devices): publish environmental and presence data
- Smart Objects: act both as sensors and actuators
- Data Collector Manager: processes incoming data and sends commands

Each component communicates through MQTT topics.

---

## MQTT Topic Structure

All communications follow a hierarchical topic structure:
device/{deviceId}/info
device/{deviceId}/sensor/*
device/{deviceId}/actuator/*

Examples:
device/env-123/sensor/temperature
device/door-456/actuator/door
device/presence-789/sensor/presence


---

## Message Format

Sensor data is exchanged using JSON messages with the following structure:
{
"timestamp": 1710000000000,
"type": "TEMPERATURE",
"value": "23"
}


Supported message types include:

- TEMPERATURE
- PRESENCE
- HEATING_STATE
- COOLING_STATE
- DOOR
- WINDOW

Actuator commands are sent as simple string payloads:
ON
OFF
OPEN
CLOSE


---

## Components

### PresenceMonitoringDevice

Simulates the number of chickens detected and publishes presence data.

---

### EnvironmentalControlSmartObject

- Monitors temperature
- Controls heating and cooling systems
- Publishes temperature and system states

---

### DoorSmartObject and WindowSmartObject

- Subscribe to actuator commands
- Maintain internal state (OPEN/CLOSED)
- Publish current state

---

### DataCollectorManager

Responsible for:

- Subscribing to sensor topics
- Processing incoming data
- Managing temperature thresholds
- Aggregating presence from multiple sensors
- Sending commands to actuators only when necessary
- Automatically opening and closing doors based on time and presence

---

## System Logic

### Temperature Control

The Data Collector Manager:

- Activates cooling if temperature exceeds the maximum threshold
- Activates heating if temperature falls below the minimum threshold
- Avoids redundant commands by tracking current system states

---

### Presence Monitoring

- Aggregates presence values from multiple devices
- Closes all doors when:
  - the current time is after the configured closing time
  - all chickens are detected inside

---

### Automatic Door Opening

- At a configured time (default 06:00)
- All registered doors are opened once per day

---

## Execution

Each component includes a main method and can be executed independently.

Execution steps:

1. Start an MQTT broker (e.g., Mosquitto)
2. Change the URL to the broker for the devices and the Data Collector Manager
3. Run one or more devices:
   - PresenceMonitoringDevice
   - EnvironmentalControlSmartObject
   - DoorSmartObject
   - WindowSmartObject
4. Run the DataCollectorManager

---

## Author

Francesco Morelli  
Computer Science and Engineering Student

Project developed for an IOT course.
