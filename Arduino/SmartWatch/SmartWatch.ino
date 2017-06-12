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
volatile int IBIdata_length = 0;
// array to hold last 256 IBI values
#define IBIlength 256
volatile int back = 0;
volatile int IBIdata[IBIlength];

/* for record HRV LF/HF */
float HRV = 0.0;
float LF_HF = 0.0;

/* for record step */
volatile unsigned int steps = 0;
int SignalX = 0;
volatile boolean SignalXisnew = false;

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
    
    switch(prefix) {
      case 'h':
        realtime[0] = value;
        Serial.println("OK");
        break;
      case 'm':
        realtime[1] = value;
        Serial.println("OK");
        break;
      case 's':
        realtime[2] = value;
        Serial.println("OK");
        break;
      case 'g':
        if (IBIdata_length < IBIlength)
          Serial.println("NO");
        else {
          if (value >= IBIlength)
            Serial.println("OK");
          else {
            int i = back + value;
            i %= IBIlength;
            Serial.println(IBIdata[i]);
          }
        }
        break;
      case 'r':
        HRV = ((float)value / 100.0);
        Serial.println("OK");
        break;
      case 'l':
        LF_HF = ((float)value / 100.0);
        Serial.println("OK");
        break;
      case 'c':
        steps = 0;
        Serial.println("OK");
        break;
      default:
        Serial.println("NO");
        break;
    }
  }
  // run display mode by 'currstate'
  switch(currstate) {
    case TIMEDISPLAY:
      {
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
      String hrv_str = String(HRV,2);
      String lfhf_str = String(LF_HF,2);
      String bpm_str = String(BPM,DEC);
      String t1_str = String("HRV : "+hrv_str);
      String t2_str = String("LF/HF : "+lfhf_str);
      String t3_str = String("HeartRate : "+bpm_str);
      OLED_drawhrv(t1_str.c_str(),t2_str.c_str(),t3_str.c_str());
      }
      break;
    case STEPDISPLAY:
      {
      //get altitude
      String altitude_str = String(GY80_getaltitude(),2);
      String steps_str = String(steps,DEC);
      String t1_str = String("steps : "+steps_str);
      String t2_str = String("altitude : "+altitude_str);
      OLED_drawstep(t1_str.c_str(),t2_str.c_str());
      }
      break;
  }
  //update accelx
  SignalX = GY80_getaccelx();
  SignalXisnew = true;
  delay(100);
}
