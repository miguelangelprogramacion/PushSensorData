package world.we.deserve.DTO;

import java.util.Date;

/**
 * Created by miguelangelprogramacion on 10/18/15.
 */
public class SensorAccelerometer {

    private int idSensorAccelerometer;
    private Double value;
    private Long datetime;

    public SensorAccelerometer() {
    }

    public SensorAccelerometer(int idSensorAccelerometer, Double value, Long datetime) {
        this.idSensorAccelerometer = idSensorAccelerometer;
        this.value = value;
        this.datetime = datetime;
    }

    public int getIdSensorAccelerometer() {
        return idSensorAccelerometer;
    }

    public void setIdSensorAccelerometer(int idSensorAccelerometer) {
        this.idSensorAccelerometer = idSensorAccelerometer;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Long getDatetime() {
        return datetime;
    }

    public void setDatetime(Long datetime) {
        this.datetime = datetime;
    }
}
