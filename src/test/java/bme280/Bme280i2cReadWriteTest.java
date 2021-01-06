package bme280;

import jdk.dio.DeviceManager;
import jdk.dio.i2cbus.I2CDeviceConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Bme280i2cReadWriteTest {
    private final int CTRL_NUM = 1;
    private final static int BME280_ADDR = 0x76;

    private Bme280i2cReadWrite bme280IO;

    public Bme280i2cReadWriteTest() throws Exception {
        I2CDeviceConfig bme280DeviceConfig = new I2CDeviceConfig.Builder()
            .setControllerNumber(CTRL_NUM)
            .setAddress(BME280_ADDR, I2CDeviceConfig.ADDR_SIZE_7)
            .setClockFrequency(I2CDeviceConfig.UNASSIGNED) // bme280 has no clock inside
            .build();

        try {
            bme280IO = new Bme280i2cReadWrite(DeviceManager.open(bme280DeviceConfig));
        } catch (Exception e) {
            e.printStackTrace();
            bme280IO = null;
            throw e;
        }
    }

    @Test
    public void ReadIdRegisterTest() {
        assertNotNull(bme280IO);
        assertEquals(0x60, bme280IO.readRegister(0xD0));
    }

    @Test
    public void ReadF0RegisterTest(){
        assertNotNull(bme280IO);
        assertEquals(0xff, bme280IO.readRegister(0xf0));
    }
}
