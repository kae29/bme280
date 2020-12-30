package bme280;

import jdk.dio.DeviceManager;
import jdk.dio.i2cbus.I2CDeviceConfig;

/**
 * BME 280 Sensor class.
 */
public class Bme280Sensor {
    /**
     * Basic read-write i2c operations
     */
    private Bme280i2cReadWrite bme280IO;
    /**
     * BME 280 Calibration parameters read from sensor.
     */
    private Bme280CalibrationParams bme280Calibration;

    /**
     * Create BME 280 Sensor
     * @param i2cSensorAddr - I2C BME 280 addr. Get addr by command: sudo i2cdetect -y 1
     */
    public Bme280Sensor(int i2cSensorAddr) {
        /** 
         * controller number is 1 since: sudo i2cdetect -y 1
         */
        final int CTRL_NUM = 1;

        I2CDeviceConfig bme280DeviceConfig = new I2CDeviceConfig.Builder()
            .setControllerNumber(CTRL_NUM)
            .setAddress(i2cSensorAddr, I2CDeviceConfig.ADDR_SIZE_7)
            .setClockFrequency(I2CDeviceConfig.UNASSIGNED) // bme280 has no clock inside
            .build();

        try {
            bme280IO = new Bme280i2cReadWrite(DeviceManager.open(bme280DeviceConfig));
        } catch (Exception e) {
            e.printStackTrace();
            bme280IO = null;
            java.lang.System.exit(1);
        }
        // initialize calibration parameters
        bme280Calibration = new Bme280CalibrationParams(this);
        
        // TODO initialize oversampling. By default it could be 0 and sensor is in sleep mode.
    }

    /**
     * Read Calibration Data.
     * @param calibReg - calibration register address
     * @return calibration data
     */
    public int readCalibrationData(int calibReg) {
        return (int) bme280IO.readRegister(calibReg);
    }

    /**
     * Return chip Id.
     * @return chip id
     */
    public long getChipId() {
        return bme280IO.readRegister(Bme280Registers.CHIP_ID_REG);
    }

    /**
     * Return chip mode.
     * 0b00 - Sleep Mode
     * 0b10 and 0b01 - Force Mode
     * 0b11 - Normal Mode
     * @return chip mode
     */
    public long getChipMode() {
        return bme280IO.readRegister(Bme280Registers.CTRL_MES_REG) & 0x03;
    }

    /**
     * Get oversampling.
     *   000 - Skipped (output set to 0x8000)
     *   001 - oversampling x 1
     *   010 - oversampling x 2
     *   011 - oversampling x 4
     *   100 - oversampling x 8
     *   101, other  - oversampling x 16
     * @param sensorType - temperature(0), pressure(1) or humidity(2)
     * @return oversampling
     */
    public long getOversampling(int sensorType) {
        if (sensorType == 0) {
            final long rawData = bme280IO.readRegister(Bme280Registers.CTRL_MES_REG);
            return (rawData & 0b11100000) >> 5;
        }
        if (sensorType == 1) {
            final long rawData = bme280IO.readRegister(Bme280Registers.CTRL_MES_REG);
            return (rawData & 0b00011100) >> 2;
        }
        if (sensorType == 2) {
            return bme280IO.readRegister(Bme280Registers.CTRL_HUM_REG) & 0b00000111;
        }
        return 0;
    }
    
    /**
     * Set Temperature/Pressure/Humidity oversampling
     * @param sensorType - temperature(0), pressure(1) or humidity(2)
     * @param oversampling new oversampling value
     */
    public void setOversampling(int sensorType, int oversampling) {
        assert sensorType >= 0 && sensorType <= 2;
        assert oversampling >=0b000 && oversampling <= 0b111;
        int regData = 0;
        if (sensorType == 0) {
            // read existing register data to overwrite only related oversampling
            regData =  bme280IO.readRegister(Bme280Registers.CTRL_MES_REG);
            regData = (regData & 0b00011111) | (oversampling << 5);
            bme280IO.writeRegister(Bme280Registers.CTRL_MES_REG, regData);
        }
        if (sensorType == 1) {
            regData =  bme280IO.readRegister(Bme280Registers.CTRL_MES_REG);
            regData = (regData & 0b11100011) | (oversampling << 2);
            bme280IO.writeRegister(Bme280Registers.CTRL_MES_REG, oversampling);
        }
        if (sensorType == 2) {
            bme280IO.writeRegister(Bme280Registers.CTRL_HUM_REG, oversampling);
        }
    }

    /**
     * Set Chip Mode.
     *   0b00 - Sleep Mode
     *   0x10 or 0x01 - Forced Mode
     *   0x11 - Normal Mode
     */
    public void setChipMode(int newMode) {
        int regData = bme280IO.readRegister(Bme280Registers.CTRL_MES_REG);
        // clear first 2 bits
        regData = regData & 0xFC;
        // set new Mode
        regData = regData | (newMode & 0x03);
        bme280IO.writeRegister(Bme280Registers.CTRL_MES_REG, regData);
    }

    /**
     * Return sensor status.
     * bit 0 - im_update
     * bit 2 - measuring
     * @return status
     */
    public long getStatus() {
        return bme280IO.readRegister(Bme280Registers.STATUS_REG);
    }

    /**
     * Returns temperature in DegC, resolution is 0.01 DegC. 
     * Output value of “5123” equals 51.23 DegC.
     * @return current Temperature
     */
    public long getTemperature() {
      return 0;
    }

    /**
     * Returns pressure in Pa as unsigned 32 bit integer.
     * Output value of "102354" equals 102354 Pa = 1023.54 hPa.
     * @return current Pressure
     */
    public long getPressure() {
      return 0;
    }

    /**
     * Returns humidity in %RH as unsigned 32 bit integer.
     * Output value of “47445” represents 47445 = 47.445 %RH.
     * @return current humidity
     */
    public long getHumidity() {
      return 0;
    }
}
