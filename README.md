# Smart Hen House - IoT System

## Overview

This project implements a simplified IoT system for monitoring and controlling a smart hen house using the MQTT protocol.

The system is composed of distributed devices (sensors and smart objects) and a central **Data Collector Manager (DCM)** that processes data and sends commands.

---

## System Architecture

The architecture follows a modular IoT design:

- **Sensors (Devices)** → publish environmental data  
- **Smart Objects** → act as both sensors and actuators  
- **Data Collector Manager** → processes data and controls actuators  

---

## MQTT Topic Structure

All communication is based on hierarchical MQTT topics:
