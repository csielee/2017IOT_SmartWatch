// Arduino Wire library is required if I2Cdev I2CDEV_ARDUINO_WIRE implementation
// is used in I2Cdev.h
#include "Wire.h"

// I2Cdev and BMP085 must be installed as libraries, or else the .cpp/.h files
// for both classes must be in the include path of your project
#include "I2Cdev.h"
#include "ADXL345.h"
#include "L3G4200D.h"
#include "BMP085.h"

// class default I2C address is 0x53
// specific I2C addresses may be passed as a parameter here
// ALT low = 0x53 (default for SparkFun 6DOF board)
// ALT high = 0x1D
ADXL345 accel;

// default address is 105
// specific I2C address may be passed here
L3G4200D gyro;

// class default I2C address is 0x77
// specific I2C addresses may be passed as a parameter here
// (though the BMP085 supports only one address)
BMP085 bmp085;

void GY80_init() {
    // join I2C bus (I2Cdev library doesn't do this automatically)
    Wire.begin();

    // initialize device
    accel.initialize();
    gyro.initialize();
    bmp085.initialize();

    // verify connection
    
}
float GY80_getaltitude() {
  float temperature;
  float pressure;
  float altitude;
  int32_t lastMicros;

  // request pressure (3x oversampling mode, high detail, 23.5ms delay)
  bmp085.setControl(BMP085_MODE_PRESSURE_3);
  lastMicros = micros();
  while (micros() - lastMicros < bmp085.getMeasureDelayMicroseconds());

  // read calibrated pressure value in Pascals (Pa)
  pressure = bmp085.getPressure();

  // calculate absolute altitude in meters based on known pressure
  // (may pass a second "sea level pressure" parameter here,
  // otherwise uses the standard value of 101325 Pa)
  altitude = bmp085.getAltitude(pressure);
  return altitude;
}
