package vn.aiteam.hackathon.Api;

public class ApiUtils {
    public static final String BASE_URL = "http://35.202.67.118/";//"http://122.102.112.71:8280/";

    public static APIService getAPIService() {

        return RetrofitClient.getClient(BASE_URL).create(APIService.class);
    }
}
