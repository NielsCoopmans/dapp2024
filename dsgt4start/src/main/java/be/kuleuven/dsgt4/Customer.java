package be.kuleuven.dsgt4;

public class Customer {
    private String email;
    private boolean admin;
    public Customer(){
    }
    public Customer(String email) {
        this.email = email;
        this.admin = false;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin(){
        return this.admin;
    }
}
