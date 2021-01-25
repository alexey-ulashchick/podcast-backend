package com.ulashchick.dashboard.auth.persistance;

public class CassandraKeyspace {

  public static final String DATACENTER = "datacenter1";
  public static final String KEYSPACE = "dashboard";

  public static class UserByEmailTable {

    public static final String TABLE_NAME = "users_by_email";
    public static final String EMAIL = "email";
    public static final String PASSWORD = "password";
    public static final String IS_ACTIVE = "is_active";

    private UserByEmailTable() {
    }
  }

  private CassandraKeyspace() {
  }

}
