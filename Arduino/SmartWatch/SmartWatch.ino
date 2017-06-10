/*
 * control smartwatch display mode
 */
#define TIMEDISPLAY 0
#define HRVDISPLAY 1
#define STEPDISPLAY 2
#define STATES 3
volatile int currstate = 0;

/* record the real world time (h:m:s)*/
volatile int realtime[3];

/* for detect IBI */
volatile int BPM;                   // int that holds raw Analog in 0. updated every 2mS
volatile int IBI = 600;             // int that holds the time interval between beats! Must be seeded!
volatile boolean Pulse = false;     // "True" when User's live heartbeat is detected. "False" when not a "live beat".
volatile boolean QS = false;        // becomes true when Arduoino finds a beat.

/* for handle message from cellphone */
char prefix;
char ch;

void setup() {
  OLED_init();
  PulseSensor_interruptSetup();
  GY80_init();
  // inital real world time and can be update by cellphone
  realtime[0] = 0;
  realtime[1] = 0;
  realtime[2] = 0;
  // set baud rate for buleteeth
  Serial.begin(115200);
}
/* temp area */


void loop() {
  // run display mode by 'currstate'
  switch(currstate) {
    case TIMEDISPLAY:
      {
      // check update info from cellphone
      if (Serial.available()) {
        prefix = Serial.read();
        int value = 0;
        while (true) {
          if (Serial.available()) {
            ch = Serial.read();
            if (ch == ';')
              break;
            
            value *= 10;
            value += ch - '0';  
          }
        }
        //Serial.println(value);
        switch(prefix) {
          case 'h':
            realtime[0] = value;
            Serial.println("hour OK");
            break;
          case 'm':
            realtime[1] = value;
            Serial.println("minute OK");
            break;
          case 's':
            realtime[2] = value;
            Serial.println("second OK");
            break;
        }
      }
      // make string by realtime
      String second = String(realtime[2],DEC);
      if (second.length() == 1)
        second = "0" + second;
      String minute = String(realtime[1],DEC);
      if (minute.length() == 1)
        minute = "0" + minute;
      String hour = String (realtime[0],DEC);
      if (hour.length() == 1)
        hour = "0" + hour;
      String t_str = String(hour+":"+minute);
      // draw time in OLED
      OLED_drawtime(t_str.c_str(),second.c_str());
      }
      break;
    case HRVDISPLAY:
      {
      // update BPM when get new IBI
      if (QS) {
        String bpm_str = String(BPM,DEC);
        String t_str = String("HeartRate : "+bpm_str);
        OLED_drawhrv(t_str.c_str());
      }
      }
      break;
    case STEPDISPLAY:
      {
      //get altitude
      String altitude_str = String(GY80_getaltitude(),2);
      String t_str = String("high : "+altitude_str);
      OLED_drawstep(t_str.c_str());
      }
      break;
  }
  delay(100);
}
