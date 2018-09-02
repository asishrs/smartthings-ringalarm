/**
 *  Ring Alarm State

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
 */
definition(
	name: "Ring Alarm State",
	namespace: "asishrs",
	author: "Asish Soudhamma",
	description: "Automatically sets the Ring Alarm alarm state based on the Smartthings mode",
	category: "My Apps",
	iconUrl: "https://cdn.shopify.com/s/files/1/2922/1686/t/2/assets/ring_logo.png?8137716793231487980",
	iconX2Url: "https://cdn.shopify.com/s/files/1/2922/1686/t/2/assets/ring_logo.png?8137716793231487980"
)

preferences {
	page(name: "selectProgram", title: "Ring Alarm State", install: false, uninstall: true,
    			nextPage: "Notifications") {
		section("Use this Alarm...") {
			input "alarmsystem", "capability.alarm", multiple: false, required: true
		}
        section("Set alarm to 'Off' when mode matches") {
			input "modealarmoff", "mode", title: "Select modes for 'Disarmed'", multiple: true, required: false
        }
		section("Set alarm to 'Away' when mode matches") {
			input "modealarmaway", "mode", title: "Select modes for 'Armed Away'", multiple: true, required: false  
        }
		section("Set alarm to 'Home' when mode matches") {
			input "modealarmhome", "mode", title: "Select modes for 'Armed Home'", multiple: true, required: false
        }
	}
    page(name: "Notifications", title: "Notifications Options", install: true, uninstall: true) {
		section("Notifications") {
			input "sendPushMessage", "enum", title: "Send a push notification?", options: ["Yes", "No"], 
            		required: false
			input "phone", "phone", title: "Send a Text Message?", required: false
		}
        section([mobileOnly:true]) {
			label title: "Assign a name", required: false
		}
	}
}

def installed() {
	init()
}

def updated() {
    unsubscribe()
    unschedule()
    init()
}
  
def init() {
	subscribe(app, onAppTouch)
    subscribe(location, "mode", modeaction)
    subscribe(alarmsystem, "alarm", alarmstate)
}

def onAppTouch(event) {
	log.debug("Running App Manually")
    state.locationmode = location.mode
	setalarmmode()
}

def modeaction(evt) {
	state.locationmode = evt.value
	setalarmmode()
}

def setalarmmode() {
    log.debug("Setting Ring Alarm mode ${alarmsystem}")
	state.alarmstate = alarmsystem.currentState("alarm").value.toLowerCase()
    log.debug("Current alarm state is: ${state.alarmstate}")
	if(state.locationmode in modealarmoff && state.alarmstate !="off") {
    	log.debug("Location mode: $state.locationmode")
    	setalarmoff()
    } else if(state.locationmode in modealarmaway && state.alarmstate !="away") {
		log.debug("Location mode: $state.locationmode")
    	setalarmaway()
  	} else if(state.locationmode in modealarmhome && state.alarmstate !="home") {
		log.debug("Location mode: $state.locationmode")
        setalarmhome()
	} else {
		log.debug("No actions set for location mode ${state.locationmode} or ${alarmsystem.displayName} already set to ${state.alarmstate} - aborting")
	}
}

def setalarmoff() {
    def message = "Ring Alarm is DISARMED"
    log.info(message)
    send(message)
    alarmsystem.off()
}
  
def setalarmaway() {
    def message = "Ring Alarm is Armed AWAY"
    log.info(message)
    send(message)
    alarmsystem.away()
}
  
def setalarmhome() {
    def message = "Ring Alarm is Armed HOME"
    log.info(message)
    send(message)
    alarmsystem.home()
}

  
private send(msg) {
	if (sendPushMessage != "No") {
		log.debug("sending push message")
		sendPush(msg)
	}
	if (phone) {
		log.debug("sending text message")
		sendSms(phone, msg)
	}
    
	log.debug msg
}