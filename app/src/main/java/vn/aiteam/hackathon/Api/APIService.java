package vn.aiteam.hackathon.Api;

import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface APIService {
    @POST("/conversations/{username}/respond")
    Call<String> query(@Body RequestBody requestBody, @Path("username") String userName);
}
