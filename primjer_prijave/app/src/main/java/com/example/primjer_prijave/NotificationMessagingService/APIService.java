package com.example.primjer_prijave.NotificationMessagingService;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {

  @Headers({
          "Content-Type: application/json",
          "Authorization: key=AAAA-nuxLR4:APA91bHSCq3cmpM8qaEUl8x47rQsP22_GaIgab1kGZqRHUt-Ur4QGjhfGgvG71BXeFjz1JZCybNDJGvWTi53IVwxE74l6fOU4skVu_S0KXgajiVz8oz0AAQ5k55nTU0cgkoPMUpZTpbM"
  })

    @POST("fcm/send")
    Call<MyResponse> SendNotification(@Body NotificationSender body);

}
