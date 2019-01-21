import static spark.Spark.*;
import com.google.gson.Gson;
import com.smartcar.sdk.*;
import com.smartcar.sdk.data.*;
import java.io.*;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.database.*;

public class Main{
  // global variable to save our accessToken
  private static String access;
  private static Gson gson = new Gson();

  public static void main(String[] args) {
    port(8000);

    String clientId = "ac6a5ec5-7bec-4fea-b657-a5ce1bf77769";
    String clientSecret = "cdb9cca2-0b84-41ce-9e21-fec4de347fa3";
    String redirectUri = "http://localhost:8000/exchange";
    String[] scope = { "read_vehicle_info read_location control_security" };
    boolean testMode = false;

    try {
      FileInputStream serviceAccount = new FileInputStream("smartcar-e646d-firebase-adminsdk-xcvea-e3b51b5470.json");

      FirebaseOptions options = new FirebaseOptions.Builder()
          .setCredentials(GoogleCredentials.fromStream(serviceAccount))
          .setDatabaseUrl("https://smartcar-e646d.firebaseio.com/").build();

      FirebaseApp.initializeApp(options);
    } catch (Exception e) {
    }

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference("server/saving-data/fireblog");

    AuthClient client = new AuthClient(clientId, clientSecret, redirectUri, scope, testMode);

    get("/login", (req, res) -> {
      String link = client.getAuthUrl();
      res.redirect(link);
      return null;
    });

    get("/exchange", (req, res) -> {
      String code = req.queryMap("code").value();

      Auth auth = client.exchangeCode(code);

      // in a production app you'll want to store this in some kind of persistent
      // storage
      access = auth.getAccessToken();

      return "";
    });

    get("/vehicle", (req, res) -> {
      SmartcarResponse<VehicleIds> vehicleIdResponse = AuthClient.getVehicleIds(access);
      // the list of vehicle ids
      String[] vehicleIds = vehicleIdResponse.getData().getVehicleIds();

      // instantiate the first vehicle in the vehicle id list
      Vehicle vehicle = new Vehicle(vehicleIds[0], access);

      VehicleInfo info = vehicle.info();
      System.out.println(gson.toJson(info));

      SmartcarResponse response = vehicle.location();
      System.out.println(response.getData());

      vehicle.unlock();
        
      res.type("application/json");

      DatabaseReference usersRef = ref.child("users");
      usersRef.child(clientId).setValueAsync(new User(gson.toJson(info), gson.toJson(response.getData())));

      return gson.toJson(response.getData()) + gson.toJson(info);
    });
  }
}

class User {

  public String info;
  public String position;

  public User(String info, String position) {
    this.info = info;
    this.position = position;
  }

}