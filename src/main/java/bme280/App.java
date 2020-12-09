package bme280;

/**
 * BME 280 driver demonstration file.
 *
 */
public class App 
{
    /**
     * BME 280 address.
     * Address taken from `sudo i2cdetect -y` output command.
     * To see dump of BME 280: `sudo i2cdump -y 1 0x76`
     */
    private final static int BME280_ADDR = 0x76;
    
    public static void main( String[] args )
    {
        long Temp;
        long Pressure;
        long Humid;

        System.out.println( "Amina's BME 280 i2c driver demonstration." );
        Bme280Sensor bme280Sensor = new Bme280Sensor(BME280_ADDR);
        
        System.console().writer().println("Chip ID = " + bme280Sensor.getChipId());
        
        for (int i=0; i<100; i++) {
            Temp = bme280Sensor.getTemperature();
            Pressure = bme280Sensor.getPressure();
            Humid = bme280Sensor.getHumidity();

            System.console().writer().println("Iteration = " + i);
            System.console().writer().println("Temperature = " + Temp);
            System.console().writer().println("Pressure = " + Pressure);
            System.console().writer().println("Humidity = " + Humid);

            // delay for one second:
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }
}
