package com.ulashchick.podcast.common.persistance;

public class CassandraKeyspace {

  public static final String DATACENTER = "datacenter1";
  public static final String KEYSPACE = "podcast";

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

  public static class SubscriptionsByUser {
    public static final String TABLE_NAME = "subscriptions_by_user";
    public static final String USER_ID = "userId";
    public static final String FEED_ID = "feedId";

    private SubscriptionsByUser() {
    }
  }

  private CassandraKeyspace() {
  }

}
