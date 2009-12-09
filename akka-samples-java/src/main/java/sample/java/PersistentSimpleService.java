/**
 * Copyright (C) 2009 Scalable Solutions.
 */

package sample.java;

import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;

import se.scalablesolutions.akka.annotation.transactionrequired;
import se.scalablesolutions.akka.annotation.prerestart;
import se.scalablesolutions.akka.annotation.postrestart;
import se.scalablesolutions.akka.state.PersistentMap;
import se.scalablesolutions.akka.state.CassandraStorage;

import java.nio.ByteBuffer;

/**
 * Try service out by invoking (multiple times):
 * <pre>
 * curl http://localhost:9998/persistentjavacount
 * </pre>
 * Or browse to the URL from a web browser.
 */
@Path("/persistentjavacount")
@transactionrequired
public class PersistentSimpleService {
  private String KEY = "COUNTER";

  private boolean hasStartedTicking = false;
  private PersistentMap<byte[], byte[]> storage = CassandraStorage.newMap();

  @GET
  @Produces({"application/html"})
  public String count() {
    if (!hasStartedTicking) {
      storage.put(KEY.getBytes(), ByteBuffer.allocate(2).putInt(0).array());
      hasStartedTicking = true;
      return "Tick: 0\n";
    } else {
      byte[] bytes = (byte[])storage.get(KEY.getBytes()).get();
      int counter = ByteBuffer.wrap(bytes).getInt();
      storage.put(KEY.getBytes(), ByteBuffer.allocate(4).putInt(counter + 1).array());
      return "Tick: " + counter + "\n";
    }
  }

  @prerestart
  public void preRestart() {
    System.out.println("Prepare for restart by supervisor");
  }

  @postrestart
  public void postRestart() {
    System.out.println("Reinitialize after restart by supervisor");
  }
}