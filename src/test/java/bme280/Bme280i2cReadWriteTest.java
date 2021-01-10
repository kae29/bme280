package bme280;

import jdk.dio.DeviceManager;
import jdk.dio.i2cbus.I2CDeviceConfig;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class Bme280i2cReadWriteTest {
    private final int CTRL_NUM = 1;
    private final static int BME280_ADDR = 0x76;

    private static Bme280i2cReadWrite bme280IO;

    public Bme280i2cReadWriteTest() throws Exception {
        if (bme280IO != null) {
            return;
        }
        
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
        assertEquals(0x60, bme280IO.readRegister(Bme280Registers.CHIP_ID_REG));
    }

    @Test
    public void ReadF0RegisterTest(){
        assertNotNull(bme280IO);
        assertEquals(0xff, bme280IO.readRegister(0xf0));
    }
    
    @Test
    public void WriteOversamplingTest1() {
        bme280IO.writeRegister(Bme280Registers.CTRL_HUM_REG, 0b010);
        bme280IO.writeRegister(Bme280Registers.CTRL_MES_REG, 0b00100111); 

        assertEquals(0b00100111, bme280IO.readRegister(Bme280Registers.CTRL_MES_REG));
        assertEquals(0b010, bme280IO.readRegister(Bme280Registers.CTRL_HUM_REG));

        // verify that humidity is not equal to 0x8000
        assertNotEquals(0x80, bme280IO.readRegister(Bme280Registers.HUM_MSB));
        assertNotEquals(0x00, bme280IO.readRegister(Bme280Registers.HUM_LSB));

        int temp_xlsb = bme280IO.readRegister(Bme280Registers.TEMP_XLSB);
        int temp_lsb = bme280IO.readRegister(Bme280Registers.TEMP_LSB);
        int temp_msb = bme280IO.readRegister(Bme280Registers.TEMP_MSB);
        
        // verify that temperature is not equal to 0x800000
        assertNotEquals(0x800000, temp_msb*0x10000+temp_lsb*0x100+temp_xlsb);

        // verify that pressure is not equal to 0x800000
        int pres_xlsb = bme280IO.readRegister(Bme280Registers.PRES_XLSB);
        int pres_lsb = bme280IO.readRegister(Bme280Registers.PRES_LSB);
        int pres_msb = bme280IO.readRegister(Bme280Registers.PRES_MSB);

        assertNotEquals(0x800000, pres_msb*0x10000+pres_lsb*0x100+pres_xlsb);
    }

    @Test
    public void WriteOversamplingTest2() {
        bme280IO.writeRegister(Bme280Registers.CTRL_HUM_REG, 0b000); // oversampling = 000
        bme280IO.writeRegister(Bme280Registers.CTRL_MES_REG, 0b00000011); // normal mode ovesamplings = 000

        assertEquals(0b00000011, bme280IO.readRegister(Bme280Registers.CTRL_MES_REG));
        assertEquals(0b000, bme280IO.readRegister(Bme280Registers.CTRL_HUM_REG));

        // verify that humidity is not equal to 0x8000
        assertEquals(0x80, bme280IO.readRegister(Bme280Registers.HUM_MSB));
        assertEquals(0x00, bme280IO.readRegister(Bme280Registers.HUM_LSB));

        int temp_xlsb = bme280IO.readRegister(Bme280Registers.TEMP_XLSB);
        int temp_lsb = bme280IO.readRegister(Bme280Registers.TEMP_LSB);
        int temp_msb = bme280IO.readRegister(Bme280Registers.TEMP_MSB);
        
        // verify that temperature is not equal to 0x800000
        assertEquals(0x800000, temp_msb*0x10000+temp_lsb*0x100+temp_xlsb);

        // verify that pressure is not equal to 0x800000
        int pres_xlsb = bme280IO.readRegister(Bme280Registers.PRES_XLSB);
        int pres_lsb = bme280IO.readRegister(Bme280Registers.PRES_LSB);
        int pres_msb = bme280IO.readRegister(Bme280Registers.PRES_MSB);

        assertEquals(0x800000, pres_msb*0x10000+pres_lsb*0x100+pres_xlsb);
    }
}
