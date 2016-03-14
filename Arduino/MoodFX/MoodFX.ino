#include <ESP8266WiFi.h>
#include <Adafruit_MQTT.h>
#include <Adafruit_MQTT_Client.h>
#include <Adafruit_NeoPixel.h>

/************************* ESP8266 Huzzah Feather Settings *******************
Board           "Generic ESP8266 Module"
Flash Mode      "QIO"
Flash Frequency "40MHz"
Upload Using    "Serial"
CPU Frequency   "80 MHz"
Flash Size      "4M"
Reset Method    "nodemcu"
******************************************************************************/


/************************* NeoPixel ******************************************/
#define PIN         15
#define NUM_PIXELS  32
Adafruit_NeoPixel pixel = Adafruit_NeoPixel(NUM_PIXELS, PIN, NEO_GRB + NEO_KHZ800);


/************************* MoodLight *****************************************/
#define DELAY_TIME 250
#define MAX_BRIGHT 190
volatile bool isMood  = false;
volatile int  red     = 0;
volatile int  green   = 126;
volatile int  blue    = 126;
volatile int  idx     = 0;


/************************* WiFi related **************************************/
#define WLAN_SSID     "WIFI SSID"
#define WLAN_PASSWORD "WIFI PASSWORD"


/************************* MQTT related **************************************/
volatile bool isConnecting          = false;
const char MQTT_SERVER[] PROGMEM    = "iot.eclipse.org";  // MQTT broker url
const int  MQTT_PORT                = 1883;               // MQTT broker port
const char MQTT_CLIENTID[] PROGMEM  = "MoodFX";           // MQTT client id
const char MQTT_USERNAME[] PROGMEM  = "";                 // MQTT username
const char MQTT_PASSWORD[] PROGMEM  = "";                 // MQTT password
const char MQTT_NEO_INPUT[] PROGMEM = "huzzah/1";         // Topic for incoming messages
const char MQTT_NEO_MSG[] PROGMEM   = "huzzah/1/msg";     // Topic for outgoing messages

// Create ESP8266 WiFi client to connect to MQTT server
WiFiClient wifiClient;
Adafruit_MQTT_Client mqttClient(&wifiClient, MQTT_SERVER, MQTT_PORT, MQTT_CLIENTID, MQTT_USERNAME, MQTT_PASSWORD);

// Setup topics
Adafruit_MQTT_Subscribe huzzahIncoming = Adafruit_MQTT_Subscribe(&mqttClient, MQTT_NEO_INPUT);
Adafruit_MQTT_Publish   huzzahOutgoing = Adafruit_MQTT_Publish(&mqttClient, MQTT_NEO_MSG); 

// Bug in Arduino 1.6.6, it seems to need  a function declaration
void mqttConnect();
void moodTransition();
void hsb2rgb(uint16_t index, uint8_t sat, uint8_t bright);


// Interrupt handler which calls the mood transition method
void inline handler (void) {
  if (isMood) moodTransition();
  timer0_write(ESP.getCycleCount() + 40000000); //80000000 ca. 1 sec  
}


void setup() {    
  Serial.begin(115200);
  delay(10);

  // Connect to WiFi
  Serial.println();
  Serial.print("Connecting to ");
  Serial.println(WLAN_SSID);

  WiFi.begin(WLAN_SSID, WLAN_PASSWORD);
  while (WiFi.status() != WL_CONNECTED) {       
    delay(500);
    Serial.print(".");
  }
  Serial.println();

  Serial.println("WiFi connected");
  Serial.print("IP address: "); Serial.println(WiFi.localIP());

  // Initialize NeoPixels
  pinMode(PIN, OUTPUT);
  pixel.begin();
  
  // Subscribe to Neo Input Topic
  mqttClient.subscribe(&huzzahIncoming);

  // Attach interrupt that calls the executes the handler every 500ms
  noInterrupts();
  timer0_isr_init();
  timer0_attachInterrupt(handler);
  timer0_write(ESP.getCycleCount() + 40000000);
  interrupts();
}


void loop() {  
  // (Re)Connect if needed
  mqttConnect();

  if (isMood) {
    String outputMsg = String(red) + "," + String(green) + "," + String(blue);
    char charBuf[outputMsg.length() + 1]; 
    outputMsg.toCharArray(charBuf, outputMsg.length() + 1);        
    huzzahOutgoing.publish(charBuf);      
  }
  
  Adafruit_MQTT_Subscribe *subscription;
  while((subscription = mqttClient.readSubscription(5000))) {
    if (subscription == &huzzahIncoming) {
      // Extract rgb color values from message
      long hex = (long) strtol((char *)huzzahIncoming.lastread, NULL, 16);
      int  r   = hex >> 16;
      int  g   = hex >> 8 & 0xFF;
      int  b   = hex & 0xFF;
      for (int i = 0 ; i < NUM_PIXELS ; i++) {
        pixel.setPixelColor(i, pixel.Color(r, g, b));
        pixel.show();
      }
      // Check for "mood" message
      String msg = String((char *)huzzahIncoming.lastread);
      isMood = msg == "mood";      
    }    
  }  
}


/**
 * Connect to MQTT broker
 */
void mqttConnect() {    
  // Return if already connected.
  if (mqttClient.connected() || isConnecting) { return; }

  int8_t ret;
  Serial.print("Connecting to MQTT... ");

  isConnecting = true;
  uint8_t retries = 3;
  while ((ret = mqttClient.connect()) != 0) { // connect will return 0 for connected      
       Serial.println(mqttClient.connectErrorString(ret));
       Serial.println("Retrying MQTT connection in 5 seconds...");
       mqttClient.disconnect();
       delay(5000);  // wait 5 seconds
       retries--;
       if (retries == 0) {         
         while (1); // basically die and wait for WDT to reset me
       }
  }
  isConnecting = false;
  Serial.println("MQTT Connected!");
}

/**
 * This method will cycle through the colors by simply changing
 * the hue of the color
 */
void moodTransition() {
  idx++; // Increase hue index
  if (idx > 768) idx = 0;

  // Adjust hue value of color
  hsb2rgb(idx, 255, 190);

  // Set neo pixels 
  for (int i = 0 ; i < NUM_PIXELS ; i++) { 
    pixel.setPixelColor(i, pixel.Color(red, green, blue));
    pixel.show();     
  }  
}

/******************************************************************************
 * accepts hue, saturation and brightness values and outputs three 8-bit color
 * values in an array (color[])
 *
 * saturation (sat) and brightness (bright) are 8-bit values.
 *
 * hue is a value between 0 and 767. hue values out of range are
 * rendered as 0.
 *
 *****************************************************************************/
void hsb2rgb(uint16_t hue, uint8_t sat, uint8_t bright) {
  uint16_t r_temp, g_temp, b_temp;
  uint8_t hue_mod;
  uint8_t inverse_sat = (sat ^ 255);

  hue     = hue % 768;
  hue_mod = hue % 256;

  if (hue < 256) {
    r_temp = hue_mod ^ 255;
    g_temp = hue_mod;
    b_temp = 0;
  } else if (hue < 512) {
    r_temp = 0;
    g_temp = hue_mod ^ 255;
    b_temp = hue_mod;
  } else if ( hue < 768) {
    r_temp = hue_mod;
    g_temp = 0;
    b_temp = hue_mod ^ 255;
  } else {
    r_temp = 0;
    g_temp = 0;
    b_temp = 0;
  }

  r_temp = ((r_temp * sat) / 255) + inverse_sat;
  g_temp = ((g_temp * sat) / 255) + inverse_sat;
  b_temp = ((b_temp * sat) / 255) + inverse_sat;

  r_temp = (r_temp * bright) / 255;
  g_temp = (g_temp * bright) / 255;
  b_temp = (b_temp * bright) / 255;

  red   = (uint8_t)r_temp;
  green = (uint8_t)g_temp;
  blue  = (uint8_t)b_temp;
}
