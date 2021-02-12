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
        
        setChipMode(0b00); // Set Sleeping Mode
        setChipMode(0b11); // Set Normal Mode
        setOversampling(0b001); // set Oversampling x 1
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
    public int getChipMode() {
        return bme280IO.readRegister(Bme280Registers.CTRL_MES_REG) & 0b00000011;
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
     * Set the same oversampling for all sensors
     * @param oversampling new oversampling value
     */
    public void setOversampling(int oversampling) {
        assert oversampling >=0b000 && oversampling <= 0b101;
        // first write humidity since the value CTRL_HUM 
        //  will be updated only after CTRL_MES s owerwritten (see Chapter 5.4.5)
        bme280IO.writeRegister(Bme280Registers.CTRL_HUM_REG, oversampling);

        int chipMode = getChipMode();
        int regData = (oversampling << 5) | (oversampling << 2) | chipMode;
        
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
     * see Chapter 4.2.3 Compensation formulas
     * @return current Temperature
     */
    public long getTemperature() {
        final long msb = bme280IO.readRegister(Bme280Registers.TEMP_MSB);
        final long lsb = bme280IO.readRegister(Bme280Registers.TEMP_LSB);
        final long xlsb = bme280IO.readRegister(Bme280Registers.TEMP_XLSB) >> 4; // xlsb located in bits [7:4] see Table 18
        
        final long rawData = (msb << 12) | (lsb<<4) | xlsb;

        long var1 = (((rawData >> 3)  - (bme280Calibration.DigT1() << 1))*bme280Calibration.DigT2() >> 11);
        long var2 = ((rawData >> 4) - bme280Calibration.DigT1());
        var2  = ((((var2*var2) >> 12)*bme280Calibration.DigT3()) >> 14);
        bme280Calibration.t_fine = var1+var2;
        
        return (bme280Calibration.t_fine*5+128)>>8;
    }

    /**
     * Returns pressure in Pa as unsigned 32 bit integer.
     * Output value of "102354" equals 102354 Pa = 1023.54 hPa.
     * see Chapter 4.2.3 Compensation formulas
     * @return current Pressure
     */
    public long getPressure() {
        final long msb = bme280IO.readRegister(Bme280Registers.PRES_MSB);
        final long lsb = bme280IO.readRegister(Bme280Registers.PRES_LSB);
        final long xlsb = bme280IO.readRegister(Bme280Registers.PRES_LSB) >> 4; // xlsb located in bits [7:4] see Table 18
        
        long rawData = (msb << 12) | (lsb<<4) | xlsb;
        
        long var1 = (bme280Calibration.t_fine >> 1) - 64000;
        long var2 = (((var1 >> 2) * (var1 >> 2)) >> 11) * bme280Calibration.DigP6();
        var2 = var2 + ((var1 * bme280Calibration.DigP5()) << 1);
        var2 = (var2 >> 2) + (bme280Calibration.DigP4() << 16);
        var1 = (((bme280Calibration.DigP3() * (((var1 >> 2)*(var1 >> 2)) >> 13)) >> 3) +
                ((bme280Calibration.DigP2() * var1) >> 1)) >> 18;
        var1 = ((32768 + var1) * bme280Calibration.DigP1()) >> 15;
        if (var1 == 0) {
            return 0; // to avoid exception caused by division by zero
        }
        long pressure = ((1048576 - rawData) - (var2 >> 12)) * 3125;
        pressure = pressure < 0x80000000 ? (pressure << 1) / var1 : (pressure / var1) * 2;
        var1  = (bme280Calibration.DigP9()*(((pressure >> 3)*(pressure >> 3)) >> 13)) >> 12;
        var2 = ((pressure >> 2) * bme280Calibration.DigP8()) >> 13;
        pressure = pressure + ((var1 + var2 + bme280Calibration.DigP7()) >> 4);

        return pressure;
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
