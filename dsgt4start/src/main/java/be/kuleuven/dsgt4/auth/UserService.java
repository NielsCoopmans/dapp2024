package be.kuleuven.dsgt4.auth;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {


    @Autowired
    FirebaseApp firebaseApp;

    @Autowired
    Boolean isProduction;

    @PostConstruct
    public void initializeUsers() {
        if(isProduction) {
            try {
                createUserIfNotExists("nielscoopmans@gmail.com", "manager");
                createUserIfNotExists("anothermanager@example.com", "manager");
                // Add more users as needed
            } catch (FirebaseAuthException e) {
                e.printStackTrace();
            }
        }
    }

    public void createUserIfNotExists(String email, String role) throws FirebaseAuthException {
        UserRecord userRecord;
        try {
            userRecord = FirebaseAuth.getInstance().getUserByEmail(email);
        } catch (FirebaseAuthException e) {
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword("admin123");

            userRecord = FirebaseAuth.getInstance().createUser(request);
        }

        // Set custom claims
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        FirebaseAuth.getInstance().setCustomUserClaims(userRecord.getUid(), claims);
    }
}