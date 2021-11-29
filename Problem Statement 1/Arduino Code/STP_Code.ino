#include <SD.h>
#include <SPI.h>
#include "HX711.h"

#define DOUT  3
#define CLK  2

HX711 scale;

float calibration_factor = 352950.0; //97050 worked for my 440lb max scale setup

const int chipSelect = 4;

const int relay = 7;

const int abortbtn = 6;
int abortFlag = 0;

const int ignitionBtn = 5;
int ignitionFlag = 0;

int ledPin = 13;
int go = 0;

const int buzzer = 9;


const int lm35_pin = A0;  /* LM35 O/P pin */
int temp_adc_val;
float temp_val;

 
void setup() {
  Serial.begin(9600);

  pinMode(relay,OUTPUT);
  pinMode(ignitionBtn,INPUT);
  pinMode(abortbtn,INPUT);
  
  Serial.println("HX711 calibration sketch");
  Serial.println("Remove all weight from scale");
  Serial.println("After readings begin, place known weight on scale");
  Serial.println("Press + or a to increase calibration factor");
  Serial.println("Press - or z to decrease calibration factor");

  scale.begin(DOUT, CLK);
  scale.set_scale();
  scale.tare(); //Reset the scale to 0

  long zero_factor = scale.read_average(); //Get a baseline reading
  Serial.print("Zero factor: "); //This can be used to remove the need to tare the scale. Useful in permanent scale projects.
  Serial.println(zero_factor);
  pinMode(13,OUTPUT);
 

  Serial.print("Initializing SD card...");

  // see if the card is present and can be initialized:
  if (!SD.begin(chipSelect)) {
    Serial.println("Card failed, or not present");
    // don't do anything more:
    while (1);
  }
  Serial.println("card initialized.");
  
  scale.begin(LOADCELL_DOUT_PIN, LOADCELL_SCK_PIN);
  pinMode(lm35_pin,INPUT);
  pinMode(buzzer,OUTPUT);

  String dataString;
  dataString = "Thrust,AVTemperature"; 
  File dataFile = SD.open("log.csv",FILE_WRITE);
    if(dataFile)
    {
      dataFile.println(dataString);
      dataFile.close();
      Serial.println(dataString);
    }
    else
    {
      Serial.println("Couldn't access file");
    }
}

int countDownSequence()
{
  Serial.println("CountDown Sequence Beginning");
  for(int i=10;i>=0;i--)
  {
    Serial.println("T-" + i);
    if(digitalRead(abortbtn) == HIGH)
    {
      abortFlag = 1; 
      break;
    }
    digitalWrite(ledPin,HIGH);
    tone(buzzer,1000);
    delay(100);
    digitalWrite(ledPin,LOW);
    noTone(buzzer)
    delay(900);
  }
  if(abortFlag == 0)
  {
    Serial.println("IGNITION!!!");
    return 1;
  }
  else
  {
    Serial.println("ABORTED!!!!");
    return 0;
  }
}

void loop() {
  if(digitalRead(ignitionBtn) == HIGH)
  {
    ignitionFlag = 1;

    go = countDownSequence();
  }

  if(ignitionFlag == 1 && go == 1){
    
  
  temp_adc_val = analogRead(lm35_pin);  /* Read Temperature temp_adc_val=analog to digital converted value  */
  temp_val = (temp_adc_val * 4.88)/10;
  Serial.print("Temperature = ");
  Serial.print(temp_val);
  Serial.print(" Degree Celsius");
  Serial.println();

  scale.set_scale(calibration_factor); //Adjust to this calibration factor

  float p=scale.get_units()*9.8;
  scale.set_scale(calibration_factor); //Adjust to this calibration factor
  Serial.print("Thrust: ");
  Serial.print(p, 2);
  Serial.print(" Newton");
  Serial.println();

 
  String dataString;
  dataString = String(p,2) + "," + String(temp_val);
  File dataFile = SD.open("log.csv",FILE_WRITE);
    if(dataFile)
    {
      dataFile.println(dataString);
      dataFile.close();
      Serial.println(dataString);
    }
    else
    {
      Serial.println("Couldn't access file");
    }
  delay(1000);
  
}
