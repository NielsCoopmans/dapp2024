package be.kuleuven.dsgt4;

public class Customer {
    private String email;
    public Customer(){
    }
    public Customer(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
