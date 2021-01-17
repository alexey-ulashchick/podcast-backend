package com.ulashchick.dashboard.auth;


import protos.com.dashboard.ulashchick.auth.RegisterUserRequest;

public class Application {

  public static void main(String[] args) {
    final RegisterUserRequest request = RegisterUserRequest.newBuilder()
        .setEmail("test@test.com")
        .setPassword("123")
        .build();

    System.out.println(request.toString());
  }

}
