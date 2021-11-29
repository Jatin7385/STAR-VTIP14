#include <SoftwareSerial.h>
#include <TinyGPS.h>
#include <SD.h>
#include <SPI.h>
#include <SFE_BMP180.h>
#include <Wire.h>
SFE_BMP180 pressure;
double baseline;
TinyGPS gps;  //Creates a new instance of the TinyGPS object
 
#define S_RX    4                // Define software serial RX pin
#define S_TX    3                // Define software serial TX pin

int statusFlag = 0;

SoftwareSerial SoftSerial(S_RX, S_TX);    // Configure SoftSerial library

float x,y,z,latitude,longitude,speedval;
int altflag = 0,locflag = 0,speedflag = 0,parachuteflag = 0;
double altitudeval = 0,prevaltitude = 0;

int ledPin = 10;
int buzzer = 7;
const int chipSelect = 5;

void setup() {
  Serial.begin(9600);
  SoftSerial.begin(9600);

  pinMode(ledPin,OUTPUT);
  pinMode(buzzer,OUTPUT);

  Serial.println("Project by TEAM E");

  if (pressure.begin())
  Serial.println("BMP180 init success");
  else
  {
  Serial.println("BMP180 init fail (disconnected?)\n\n");
  while (1);
  }
  baseline = readPressure();
  Serial.print("Initializing SD card...");

  // see if the card is present and can be initialized:
  if (!SD.begin(chipSelect)) {
    Serial.println("Card failed, or not present");
    // don't do anything more:
    while (1);
  }
  Serial.println("Card initialized.");
}

void loop() {  
  bool newData = false;
  unsigned long chars;
  unsigned short sentences, failed;

  // For one second we parse GPS data and report some key values
  for (unsigned long start = millis(); millis() - start < 1000;)
  {
    while (Serial.available())
    {
      char c = Serial.read();
      //Serial.print(c);
      if (gps.encode(c)) 
        newData = true;  
    }
  }

  if (newData)      //If newData is true
  {
    float flat, flon;
    unsigned long age;
    gps.f_get_position(&flat, &flon, &age);   
    Serial.print("Latitude = ");
    Serial.print(flat == TinyGPS::GPS_INVALID_F_ANGLE ? 0.0 : flat, 6);
    Serial.print(" ,Longitude = ");
    Serial.print(flon == TinyGPS::GPS_INVALID_F_ANGLE ? 0.0 : flon, 6);

  }
 
  Serial.println(failed);

  Serial.print(",Relative altitude: ");
  Serial.print(altitudeval, 1);
  Serial.print("meters, ");
  Serial.print("Status : ");
  if(statusFlag == 0){
    Serial.println("Awaiting Launch,");
  }
  else if(statusFlag == 1){Serial.println("IGNITION!!,");}
  else if(statusFlag == 2){Serial.println("Apogee Reached!!,");}
  else if(statusFlag == 3){Serial.println("Parachute Deployed!!,");}
  else if(statusFlag == 4){Serial.println("LANDED!!,");}
  if(altitudeval!=0 && parachuteflag == 0)
  {
    statusFlag = 1;
  }
  else if(altitudeval!=0 && parachuteflag == 1){statusFlag = 2;}
  
  
  if(altitudeval<prevaltitude && parachuteflag <4){parachuteflag++;}

  if(parachuteflag == 4){//DEPLOY PARACHUTE
    statusFlag = 3;
    }
  if(parachuteflag == 4 && altitudeval == 0)
  {
    statusFlag = 4;
    digitalWrite(ledPin,HIGH);
    tone(buzzer,1000);
    delay(500);
    digitalWrite(ledPin,LOW);
    noTone(buzzer);
    delay(500);
  }
  prevaltitude = altitudeval;
  delay(1000);
}

double readPressure()
{
char status;
double T, P, p0, a;
status = pressure.startTemperature();
if (status != 0)
{
delay(status);
status = pressure.getTemperature(T);
if (status != 0)
{
status = pressure.startPressure(3);
if (status != 0)
{
delay(status);
status = pressure.getPressure(P, T);
if (status != 0)
{
return (P);
}
}
}
}
} 
