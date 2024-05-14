package be.kuleuven.dsgt4.carsupplier;

public class OrderResponse {
    private String message;
    private String carDescription;
    private String customerName;
    private String address;

    public OrderResponse() {
    }

    public OrderResponse(String message, String carDescription, String customerName, String address) {
        this.message = message;
        this.carDescription = carDescription;
        this.customerName = customerName;
        this.address = address;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCarDescription() {
        return carDescription;
    }

    public void setCarDescription(String carDescription) {
        this.carDescription = carDescription;
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
