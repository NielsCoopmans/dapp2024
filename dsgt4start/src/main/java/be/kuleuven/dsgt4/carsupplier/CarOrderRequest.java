package be.kuleuven.dsgt4.carsupplier;

import java.util.UUID;

public class CarOrderRequest {
    private UUID carId;
    private String customerName;
    private String address;

    public CarOrderRequest() {
    }

    public CarOrderRequest(UUID carId, String customerName, String address) {
        this.carId = carId;
        this.customerName = customerName;
        this.address = address;
    }

    public UUID getCarId() {
        return carId;
    }

    public void setCarId(UUID carId) {
        this.carId = carId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
