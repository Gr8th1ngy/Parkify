import static spark.Spark.*;
import com.google.gson.Gson;
import com.smartcar.sdk.*;
import com.smartcar.sdk.data.*;
import java.io.*;

public class Main {
  // global variable to save our accessToken
  private static String access;
  private static Gson gson = new Gson();

  public static void main(String[] args) {

    port(8000);

    // TODO: Authorization Step 1a: Launch Smartcar authentication dialog
    String clientId = "ac6a5ec5-7bec-4fea-b657-a5ce1bf77769";
    String clientSecret = "cdb9cca2-0b84-41ce-9e21-fec4de347fa3";
    String redirectUri = "http://localhost:8000/exchange";
    String[] scope = { "read_vehicle_info read_location control_security" };
    boolean testMode = false;
    
    FileInputStream serviceAccount = new FileInputStream("path/to/serviceAccountKey.json");

    AuthClient client = new AuthClient(clientId, clientSecret, redirectUri, scope, testMode);

    get("/login", (req, res) -> {
      // TODO: Authorization Step 1b: Launch Smartcar authentication dialog
      String link = client.getAuthUrl();
      res.redirect(link);
      return null;
    });

    get("/exchange", (req, res) -> {
      String code = req.queryMap("code").value();

      // TODO: Request Step 1: Obtain an access token
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

      // TODO: Request Step 4: Make a request to Smartcar API
      VehicleInfo info = vehicle.info();
      System.out.println(gson.toJson(info));

      SmartcarResponse response = vehicle.location();
      System.out.println(gson.toJson(response.getData()));

      vehicle.unlock();
      res.type("application/json");

      return gson.toJson(info) + gson.toJson(response.getData());
    });
  }
}
