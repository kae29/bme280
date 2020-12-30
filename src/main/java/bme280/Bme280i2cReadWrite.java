package bme280;

import jdk.dio.i2cbus.I2CDevice;
/**
 * This class contain basic read-write i2c methods for BME 280.
 */
public class Bme280i2cReadWrite {
    /**
     * i2c Device driver
     */
    private I2CDevice i2CDevice;

    /**
     * Default constructor.
     * @param i2CDevice - i2c device driver
     */
    public Bme280i2cReadWrite(I2CDevice i2CDevice) {
        this.i2CDevice = i2CDevice;
    }

    /**
     * Read one byte from Register
     * @param register BME 280 register
     * @return register data
     */
    public int readRegister(int register) {
        return 0;
    }

    /**
     * Update register with one byte data.
     * @param register BME 280 register
     * @param data one byte data
     */
    public void writeRegister(int register, int data) {

    }
}
