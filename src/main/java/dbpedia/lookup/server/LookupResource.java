package dbpedia.lookup.server;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Path( "message" )
public class LookupResource
{
  @GET
  @Produces( MediaType.TEXT_PLAIN )
  public String message()
  {
    return "TESTESTEST! ";
  }
}