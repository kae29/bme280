package bme280;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class Bme280OversamplingTest {
    private final static int BME280_ADDR = 0x76;

    private static Bme280Sensor bme280Sensor;

    public Bme280OversamplingTest() throws Exception {
        if (bme280Sensor != null) {
            return;
        }

        bme280Sensor = new Bme280Sensor(BME280_ADDR);
    }

    @Test
    public void InitialValueTest() {
        assertNotNull(bme280Sensor);

        assertEquals(0b11, bme280Sensor.getChipMode());

        assertEquals(0b001, bme280Sensor.getOversampling(0));
        assertEquals(0b001, bme280Sensor.getOversampling(1));
        assertEquals(0b001, bme280Sensor.getOversampling(2));
    }

    @Test
    public void WriteOversamplingTest1() {
        assertNotNull(bme280Sensor);
        assertEquals(0b11, bme280Sensor.getChipMode());

        bme280Sensor.setOversampling(0b000);

        assertEquals(0b000, bme280Sensor.getOversampling(0));
        assertEquals(0b000, bme280Sensor.getOversampling(1));
        assertEquals(0b000, bme280Sensor.getOversampling(2));
    }

    @Test
    public void WriteOversamplingTest2() {
        assertNotNull(bme280Sensor);
        assertEquals(0b11, bme280Sensor.getChipMode());

        bme280Sensor.setOversampling(0b011);

        assertEquals(0b011, bme280Sensor.getOversampling(0));
        assertEquals(0b011, bme280Sensor.getOversampling(1));
        assertEquals(0b011, bme280Sensor.getOversampling(2));
    }

    @Test
    public void WriteOversamplingTest3() {
        assertNotNull(bme280Sensor);
        assertEquals(0b11, bme280Sensor.getChipMode());

        bme280Sensor.setOversampling(0b101);

        assertEquals(0b101, bme280Sensor.getOversampling(0));
        assertEquals(0b101, bme280Sensor.getOversampling(1));
        assertEquals(0b101, bme280Sensor.getOversampling(2));
    }
}
