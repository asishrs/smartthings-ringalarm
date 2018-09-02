/**
 *  Ring Alarm - SmartThings integration
 *
 *  Licence Details.
 *	https://opensource.org/licenses/MIT
 *
 *  Copyright 2019 Asish Soudhamma
 *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software 
 *  and associated documentation files (the "Software"), to deal in the Software without restriction, 
 *  including without limitation the rights to use, copy, modify, merge, publish, distribute, 
 *  sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is 
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all copies or substantial 
 *  portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. 
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE 
 * OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

preferences {
	input(name: "username", type: "text", title: "Username", required: "true", description: "Ring Alarm Username")
	input(name: "password", type: "password", title: "Password", required: "true", description: "Ring Alarm Password")
	input(name: "apiurl", type: "text", title: "API Url", required: "true", description: "Ring Alarm API URL")
	input(name: "apikey", type: "text", title: "API Key", required: "false", description: "Ring Alarm API Api Key")
	input(name: "locationId", type: "text", title: "Location Id", required: "false", description: "Ring Alarm Location Id")
	input(name: "zid", type: "text", title: "ZID", required: "false", description: "Ring Alarm ZID")
}

metadata {	
	definition (name: "RingAlarm", namespace: "asishrs", author: "Asish Soudhamma") {
		capability "Alarm"
		capability "Polling"
        capability "Contact Sensor"
		command "off"
		command "home"
		command "away"
		command "update_state"
		attribute "events", "string"
		attribute "messages", "string"
		attribute "status", "string"
	}

tiles(scale: 2) {
    multiAttributeTile(name:"status", type: "generic", width: 6, height: 4){
        tileAttribute ("device.status", key: "PRIMARY_CONTROL") {
            attributeState "off", label:'${name}', icon: "st.security.alarm.off", backgroundColor: "#1998d5"
            attributeState "home", label:'${name}', icon: "st.Home.home4", backgroundColor: "#e58435"
            attributeState "away", label:'${name}', icon: "st.security.alarm.on", backgroundColor: "#e53935"
			attributeState "pending off", label:'${name}', icon: "st.security.alarm.off", backgroundColor: "#ffffff"
			attributeState "pending away", label:'${name}', icon: "st.Home.home4", backgroundColor: "#ffffff"
			attributeState "pending home", label:'${name}', icon: "st.security.alarm.on", backgroundColor: "#ffffff"
			attributeState "away_count", label:'countdown', icon: "st.security.alarm.on", backgroundColor: "#ffffff"
			attributeState "failed set", label:'error', icon: "st.secondary.refresh", backgroundColor: "#d44556"
			attributeState "alert", label:'${name}', icon: "st.alarm.beep.beep", backgroundColor: "#ffa81e"
			attributeState "alarm", label:'${name}', icon: "st.security.alarm.alarm", backgroundColor: "#d44556"
        }
    }	
	
    standardTile("off", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#008CC1", nextState: "pending")
        state ("away", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#505050", nextState: "pending")
        state ("home", label:"off", action:"off", icon: "st.security.alarm.off", backgroundColor: "#505050", nextState: "pending")
        state ("pending", label:"pending", icon: "st.security.alarm.off", backgroundColor: "#ffffff")
	}
	
    standardTile("away", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#505050", nextState: "pending") 
		state ("away", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#008CC1", nextState: "pending")
        state ("home", label:"away", action:"away", icon: "st.security.alarm.on", backgroundColor: "#505050", nextState: "pending")
		state ("pending", label:"pending", icon: "st.security.alarm.on", backgroundColor: "#ffffff")
		state ("away_count", label:"pending", icon: "st.security.alarm.on", backgroundColor: "#ffffff")
	}
	
    standardTile("home", "device.alarm", width: 2, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false) {
        state ("off", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#505050", nextState: "pending")
        state ("away", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#505050", nextState: "pending")
		state ("home", label:"home", action:"home", icon: "st.Home.home4", backgroundColor: "#008CC1", nextState: "pending")
		state ("pending", label:"pending", icon: "st.Home.home4", backgroundColor: "#ffffff")
	}
	valueTile("events", "device.events", width: 6, height: 2, canChangeIcon: false, inactiveLabel: true, canChangeBackground: false, decoration: "flat", wordWrap: true) {
		state ("default", label:'${currentValue}')
	}

	main(["status"])
	details(["status","off", "home", "away"])
	}
}

def installed() {
  init()
}

def updated() {
  unschedule()
  init()
}
  
def init() {
	log.info "Setting up Schedule (every 5 minutes)..."
	runEvery5Minutes(poll)
}

def off() {
	log.info "Setting Ring Alarm mode to 'Off'"
	ringApiCall ('off')
}

def home() { 
	log.info "Setting Ring Alarm mode to 'Home'"
	ringApiCall ('home')
}

def away() {
	log.info "Setting Ring Alarm mode to 'Away'"
	ringApiCall ('away')
}

def update_state() {
	log.info "Refreshing Ring Alarm state..."
	poll()
}

def ringApiCall(state){
	def timeout = false;
	def params = [
		uri: "${settings.apiurl}/${state}",
		headers: [
			'x-api-key':settings.apikey
		],
		body: [
			user: settings.username,
			password: settings.password,
			locationId: settings.locationId,
			zid: settings.zid
		]
	]

	try {
		httpPostJson(params) { resp ->
			log.debug "Ring Alarm ${state.toUpperCase()} response data: ${resp.data}"
		}

		def alarm_state = device.currentValue("alarm")
		sendEvent(name: 'alarm', value: state)
		sendEvent(name: "status", value: state)
		sendEvent(name: 'presence', value: state)
	} catch (e) {
		timeout = true
		log.debug "Ring Alarm SET to ${state.toUpperCase()} Error: $e"
	}

	if (!timeout) {
    	runIn(2, poll)
    } else {
    	runIn(10, poll)
    }
}

def poll() {
    log.info "Checking Ring Alarm Status."
	def params = [
		uri: "${settings.apiurl}/status",
		headers: [
			'x-api-key':settings.apikey
		],
		body: [
			user: settings.username,
			password: settings.password,
			locationId: settings.locationId,
			zid: settings.zid
		]
	]

	try {
		httpPostJson(params) { resp ->
			log.debug "Ring Alarm Status Response data: ${resp.data.message}"
			def alarm_state = device.currentValue("alarm")
			sendEvent(name: "alarm", value: resp.data.message)
			sendEvent(name: "status", value: resp.data.message)
			sendEvent(name: 'presence', value: resp.data.message)
		}
	} catch (e) {
		log.debug "Ring Alarm Status check Error: $e"
	}
}