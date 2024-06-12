package be.kuleuven.dsgt4;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.config.EnableHypermediaSupport;
import org.springframework.hateoas.config.HypermediaWebClientConfigurer;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EnableHypermediaSupport(type = EnableHypermediaSupport.HypermediaType.HAL)
@SpringBootApplication
public class Dsgt4Application {

	@SuppressWarnings("unchecked")
	public static void main(String[] args)  {
		System.setProperty("server.port", System.getenv().getOrDefault("PORT", "8080"));
		SpringApplication.run(Dsgt4Application.class, args);
}

	@Bean
	public boolean isProduction() {
		return Objects.equals(System.getenv("GAE_ENV"), "standard");
	}

	@Bean
	public String projectId() {
		if (this.isProduction()) {
			return "dapp4-demo";
		} else {
			return "demo-distributed-systems-kul";
		}
	}

	@Bean
	public FirebaseApp firebaseAppConfig() throws IOException {
		// Initialize Firebase options with credentials
		if(isProduction()) {

			String projectId = this.projectId();
			String secretId = "dapp4-demo-firebase-adminsdk-30rxl-290a4732fa";
			String versionId = "1";

			try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
				SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);
				AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);

				String jsonConfig = response.getPayload().getData().toStringUtf8();

				InputStream serviceAccount = new ByteArrayInputStream(jsonConfig.getBytes(StandardCharsets.UTF_8));

				FirebaseOptions options = new FirebaseOptions.Builder()
						.setCredentials(GoogleCredentials.fromStream(serviceAccount))
						.build();

				return FirebaseApp.initializeApp(options);
			}


		}
		else {
            return null;
		}
	}

	@Bean
	public Firestore db() throws IOException {
		if (isProduction()) {
			return FirestoreOptions.getDefaultInstance()
					.toBuilder()
					.setProjectId(this.projectId())
					.build()
					.getService();
		} else {
			return FirestoreOptions.getDefaultInstance()
					.toBuilder()
					.setProjectId(this.projectId())
					.setCredentials(new FirestoreOptions.EmulatorCredentials())
					.setEmulatorHost("localhost:8084")
					.build()
					.getService();
		}
	}
	/*
	 * You can use this builder to create a Spring WebClient instance which can be used to make REST-calls.
	 */
	@Bean
	WebClient.Builder webClientBuilder(HypermediaWebClientConfigurer configurer) {
		return configurer.registerHypermediaTypes(WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(HttpClient.create()))
				.codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(100 * 1024 * 1024)));
	}

	@Bean
	HttpFirewall httpFirewall() {
		DefaultHttpFirewall firewall = new DefaultHttpFirewall();
		firewall.setAllowUrlEncodedSlash(true);
		return firewall;
	}

	@Bean
	public ScheduledExecutorService scheduledExecutorService() {
		return Executors.newScheduledThreadPool(10);  // Customize the pool size as needed
	}





}
