

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

void setup() {
  Serial.begin(9600);     // opens serial port, sets data rate to 9600 bps
  pinMode(2, OUTPUT);
  pinMode(3, OUTPUT);
  pinMode(4, OUTPUT);
}


void loop() {  
  // print the string when a newline arrives:
  if (stringComplete) {
    inputString.trim();
    Serial.print(inputString);
    
    if(inputString=="12345"){
      digitalWrite(2, HIGH);  
      Serial.print("rojo-");  
    }   
    else if(inputString=="54321"){
      digitalWrite(2, LOW); 
      Serial.print("verde");    
    } 
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
  digitalWrite(3, HIGH);
}


void serialEvent() {  
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // add it to the inputString:
    inputString += inChar;
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar == '\n') {
      stringComplete = true;
      break;
    }
  }
}