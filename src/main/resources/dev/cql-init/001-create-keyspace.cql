create keyspace if not exists dashboard
    with replication = {'class' : 'SimpleStrategy', 'replication_factor' : 1 }
     and durable_writes = true;
