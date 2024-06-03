package be.kuleuven.dsgt4;

import com.google.cloud.firestore.Firestore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/broker/exhausts")
public class ExhaustController {

    private final SupplierServiceExhaust supplierServiceExhaust;

    public ExhaustController(SupplierServiceExhaust supplierServiceExhaust) {
        this.supplierServiceExhaust = supplierServiceExhaust;
    }

    @GetMapping
    public Exhaust[] getAllExhausts(){
        return supplierServiceExhaust.getAllExhausts();
    }

}
