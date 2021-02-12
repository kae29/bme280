package bme280;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Bme280CalibrationParamsTest {
    private final static int BME280_ADDR = 0x76;

    private static Bme280Sensor bme280Sensor;

    public Bme280CalibrationParamsTest() throws Exception {
        if (bme280Sensor != null) {
            return;
        }

        bme280Sensor = new Bme280Sensor(BME280_ADDR);
    }

    @Test
    public void ReadCalibrationParamTest() {
        Bme280CalibrationParams calibParams = new Bme280CalibrationParams(bme280Sensor);

        assertNotNull(calibParams);
        assertEquals(28479, calibParams.DigT1());
        assertEquals(26381, calibParams.DigT2());
        assertEquals(50, calibParams.DigT3());

        assertEquals(37155, calibParams.DigP1());
        assertEquals(-10787, calibParams.DigP2());
        assertEquals(3024, calibParams.DigP3());
        assertEquals(6304, calibParams.DigP4());
        assertEquals(22, calibParams.DigP5());
        assertEquals(-7, calibParams.DigP6());
        assertEquals(9900, calibParams.DigP7());
        assertEquals(-10230, calibParams.DigP8());
        assertEquals(4285, calibParams.DigP9());

        assertEquals(75, calibParams.DigH1());
        assertEquals(360, calibParams.DigH2());
        assertEquals(0, calibParams.DigH3());
        assertEquals(325, calibParams.DigH4());
        assertEquals(0, calibParams.DigH5());
        assertEquals(30, calibParams.DigH6());
    }
}
