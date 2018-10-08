r#include <SoftwareSerial.h>
#include <Stepper.h>
#include <Wire.h>
int In_Light = 3;            //In1 繼電器跟Arduino接的腳位為3
int In_Pad = 4;
int In_Water = 5;
// 定義連接藍牙模組的序列埠
SoftwareSerial BT(8, 9); // 接收腳, 傳送腳
char val;  // 儲存接收資料的變數
const byte SCREEN_PIN = 13;  // 定義LED腳位
int T_low_bound = 24;
int T_up_bound = 31;
int Wet_low_bound = 40;
int Wet_up_bound = 70;

const int x = 22;
const int y = 55;
const int SLAVE_ADDRESS = 1;
char incomingByte = 0;

const int stepsPerRevolution = 2048;  
// change this to fit the number of steps per revolution
// for your motor

// initialize the stepper library on pins 8 through 11:
Stepper myStepper(stepsPerRevolution, 10, 12, 11, 13);
int count = 0;
int flag =0;
void setup() {
  // set the speed at 60 rpm:
  myStepper.setSpeed(14);
  // initialize the serial port:
  Wire.begin(SLAVE_ADDRESS);    // join I2C bus as a slave with address 1
  Wire.onReceive(receiveEvent); // register event
  Serial.begin(9600);
  BT.begin(115200);
  pinMode(SCREEN_PIN, OUTPUT);
  pinMode(In_Light,OUTPUT); // pin 3
  pinMode(In_Pad,OUTPUT);  //pin 4
  pinMode(In_Water,OUTPUT);  //pin 5
  digitalWrite(In_Water, LOW);
  digitalWrite(In_Light, LOW);
  digitalWrite(In_Pad, LOW);
}

void loop() {
  // step one revolution  in one direction:
  if(count == 30000) flag = flag + 1;
  if(flag == 4) {
     if(T_low_bound > x) digitalWrite(In_Water,HIGH);  //通電
     if(T_up_bound < x) digitalWrite(In_Water,LOW);  //通電
     if(Wet_low_bound > y) digitalWrite(In_Water,HIGH);  //通電  
     if(Wet_up_bound < y)  digitalWrite(In_Water,LOW);  //通電
     Serial.print("reset");
     flag = 0;
  }
   
   
   if (BT.available()) {
      val = BT.read();
      Serial.print(val);
      if (val == '9') {
        digitalWrite(SCREEN_PIN, LOW);
        // 回應命令發送端，告知「已關布幕」
        BT.println("SCREEN_CLOSE\n");
        Serial.print("SCREEN_CLOSE\n");
        for(int i=0; i<3.5; i++){
            myStepper.step(-stepsPerRevolution);
        }
        flag = 0;
      }
      
      else if (val == '8') {
        digitalWrite(SCREEN_PIN, HIGH);
        // 回應命令發送端，告知「已開布幕」
        BT.println("SCREEN_OPEN\n");
        Serial.print("SCREEN_OPEN\n");
        for(int i=0; i<3.5; i++){
            myStepper.step(stepsPerRevolution);
        }
        flag = 0;
      }
      else if (val == '2') { 
        // 回應命令發送端，告知「已開加溫燈」 
        BT.println("LIGHT_OPEN\n");
        Serial.print("LIGHT_OPEN\n");     
        digitalWrite(In_Light,HIGH);  //通電
        flag = 0;      
      }
      
      else if (val == '3'){ 
        // 回應命令發送端，告知「已關加溫燈」 
        BT.println("LIGHT_CLOSE\n");
        Serial.print("LIGHT_CLOSE\n");
        digitalWrite(In_Light,LOW);   //不通電
        flag = 0;
      }
      
      else if (val == '4') {
        // 回應命令發送端，告知「已開灑水器」 
        BT.println("WATER_OPEN\n");
        Serial.print("WATER_OPEN\n");        
        digitalWrite(In_Water,HIGH);  //通電 
        flag = 0;       
      }
      
      else if (val == '5') {
        // 回應命令發送端，告知「已關灑水器」 
        BT.println("WATER_CLOSE\n");
        Serial.print("WATER_CLOSE\n"); 
        digitalWrite(In_Water,LOW);   //不通電
        flag = 0;
      }
      else if (val == '6'){ 
        // 回應命令發送端，告知「已經開加溫墊」 
        BT.println("Pad_Open\n");
        Serial.print("Pad_Open\n");
        digitalWrite(In_Pad,HIGH);   //通電
        flag = 0;
      }
      
      else if (val == '7') {
        // 回應命令發送端，告知「已經關加溫墊」 
        BT.println("Pad_Close\n");
        Serial.print("Pad_Close\n");        
        digitalWrite(In_Pad,LOW);  //不通電
        flag = 0;        
      }
       else {
          Serial.print(val);
       }
   }
   count = count + 1;
}

void receiveEvent(int howMany)
{
  while (Wire.available()) 
  {
    // receive one byte from Master
    incomingByte = Wire.read();
    Serial.print(incomingByte);
  }
}
