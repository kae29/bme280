package bme280;

import java.nio.ByteBuffer;

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
        ByteBuffer dst = ByteBuffer.allocateDirect(1);
        try {
            int bytesread = i2CDevice.read(register, 1, dst);
        }
        catch (Exception e) {
            return 0;
        }

        dst.rewind();    
        int result = dst.get();
        return result < 0? result+256:result;
    }

    /**
     * Update register with one byte data.
     * @param register BME 280 register
     * @param data one byte data
     */
    public void writeRegister(int register, int data) {
        if (register == 0xf5 || register == 0xf4 || register == 0xf2 || register == 0xe0) {
            if (data < 256) {
                ByteBuffer dst = ByteBuffer.allocateDirect(1);
                try {
                    dst.put(data<128?(byte)data:(byte)(data-256));
                    int bytewritten = i2CDevice.write(register, 1, dst);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
