/**
 *  Rheem Econet Water Heater
 *
 *  Copyright 2017 Justin Huff
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 *  Last Updated : 2017-01-04
 *
 *  Based on https://github.com/copy-ninja/SmartThings_RheemEcoNet
 */
metadata {
  definition (name: "Rheem Econet Water Heater", namespace: "jjhuff", author: "Justin Huff", ocfDeviceType: "oic.d.waterheater") {
    capability "Actuator"
    capability "Refresh"
    capability "Sensor"
    capability "Switch"
    capability "Thermostat Heating Setpoint"

    command "heatLevelUp"
    command "heatLevelDown"
    command "updateDeviceData", ["string"]
  }

  simulator { }

  tiles(scale: 2) {
    multiAttributeTile(name: "heatingSetpoint", type: "thermostat", width: 6, height: 4) {
      tileAttribute("device.heatingSetpoint", key: "PRIMARY_CONTROL") {
        attributeState("heatingSetpoint", label: '${currentValue}', unit:"dF", backgroundColors: getTempColors(), defaultState: true)
      }
      /*
       tileAttribute("device.switch", key: "SECONDARY_CONTROL") {
       attributeState("off", label: "Off", action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn")
       attributeState("on", label: "On", action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff")
       attributeState("turningOn", label: "Turning on...", icon: "st.switches.switch.on", backgroundColor: "#00a0dc", nextState: "turningOff")
       attributeState("turningOff", label: "Turning off...", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "turningOn")
       }
       */
      tileAttribute("device.switch", key: "OPERATING_STATE") {
        attributeState("off", label: "Off", backgroundColor: "#cccccc")
        attributeState("on", label: "On", backgroundColor: "#bc2323", defaultState: true)
      }
      tileAttribute("device.heatingSetpoint", key: "VALUE_CONTROL") {
        attributeState("VALUE_UP", action: "heatLevelUp")
        attributeState("VALUE_DOWN", action: "heatLevelDown")
      }
    }

    valueTile("heatingSetpointValue", "device.heatingSetpoint", inactiveLabel: false, width: 4, height: 4) {
      state("heatingSetpoint", label: '${currentValue}\u00b0', backgroundColors: getTempColors())
    }

    standardTile("heatLevelUp", "device.switch", canChangeIcon: false, decoration: "flat", width: 2, height: 2) {
      state("heatLevelUp",   action: "heatLevelUp", icon: "st.thermostat.thermostat-up", backgroundColor: "#F7C4BA")
    }

    standardTile("heatLevelDown", "device.switch", canChangeIcon: false, decoration: "flat", width: 2, height: 2) {
      state("heatLevelDown", action: "heatLevelDown", icon: "st.thermostat.thermostat-down", backgroundColor: "#F7C4BA")
    }

    standardTile("switch", "device.switch", canChangeIcon: false, decoration: "flat", width: 2, height: 2) {
      state("off", label: "Off", action: "switch.on", icon: "st.Weather.weather12", backgroundColor: "#cccccc", nextState: "turningOn")
      state("turningOn", label: "On...", icon: "st.Weather.weather12", backgroundColor: "#e86d13", nextState: "on")
      state("on", label: "On", action: "switch.off", icon: "st.Weather.weather12", backgroundColor: "#e86d13", nextState: "turningOff")
      state("turningOff", label: "Off...", icon: "st.Weather.weather12", backgroundColor: "#cccccc", nextState: "off")
    }

    standardTile("refresh", "device.switch", decoration: "flat", width: 2, height: 2) {
      state("default", action: "refresh.refresh", icon: "st.secondary.refresh")
    }

    main("heatingSetpoint")
    details([
      "heatingSetpoint",
      "switch",
      "refresh"
    ])
  }
}

def parse(String description) { }

def refresh() {
  log.debug "refresh"
  parent.refresh()
}

def on() {
  log.debug "on"
  sendEvent(name: "switch", value: "on")
  parent.setDeviceEnabled(this.device, true)
}

def off() {
  log.debug "off"
  sendEvent(name: "switch", value: "off")
  parent.setDeviceEnabled(this.device, false)
}

def setHeatingSetpoint(Number setPoint) {
  log.debug setPoint + "\u00b0"
  /*
    heatingSetPoint = (heatingSetPoint < deviceData.minTemp)? deviceData.minTemp : heatingSetPoint
    heatingSetPoint = (heatingSetPoint > deviceData.maxTemp)? deviceData.maxTemp : heatingSetPoint
  */
  sendEvent(name: "heatingSetpoint", value: setPoint, unit: "F")
  parent.setDeviceSetPoint(this.device, setPoint)
}

def heatLevelUp() {
  def setPoint = device.currentValue("heatingSetpoint")
  setPoint = setPoint + 1
  setHeatingSetpoint(setPoint)
}

def heatLevelDown() {
  def setPoint = device.currentValue("heatingSetpoint")
  setPoint = setPoint - 1
  setHeatingSetpoint(setPoint)
}

def updateDeviceData(data) {
  sendEvent(name: "heatingSetpoint", value: data.setPoint, unit: "F")
  sendEvent(name: "switch", value: data.isEnabled ? "on" : "off")
}

def getTempColors() {
  def colorMap = [
    [value: 90,  color: "#f49b88"],
    [value: 100, color: "#f28770"],
    [value: 110, color: "#f07358"],
    [value: 120, color: "#ee5f40"],
    [value: 130, color: "#ec4b28"],
    [value: 140, color: "#ea3811"]
  ]
  return colorMap
}
