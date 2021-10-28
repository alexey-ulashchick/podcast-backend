package com.ulashchick.podcast.common.persistance;

public class CassandraKeyspace {

  public static final String DATACENTER = "datacenter1";
  public static final String KEYSPACE = "dashboard";

  public static class UserByEmailTable {

    public static final String TABLE_NAME = "users_by_email";
    public static final String EMAIL = "email";
    public static final String ID = "id";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String IMAGE_URL = "imageUrl";

    private UserByEmailTable() {
    }
  }

  private CassandraKeyspace() {
  }

}
