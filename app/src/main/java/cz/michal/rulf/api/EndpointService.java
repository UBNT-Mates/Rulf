package cz.michal.rulf.api;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Streaming;

public interface EndpointService {
    @Streaming
    @GET("download?size=200000000")
    Call<ResponseBody> getDownloadSpeed(@Header("x-test-token") String token);
}