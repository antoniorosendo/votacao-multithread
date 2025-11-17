package common;

// A simple Data Transfer Object with user information
// extends NetProtocol in order to be recognized by client and server
public class Payload extends NetProtocol
   {
   private static final long serialVersionUID = 1L;
   private String            message;
   private int               value;

   public Payload(String message, int value)
      {
      this.message = message;
      this.value   = value;
      }

   public String getMessage()
      {
      return message;
      }

   public int getValue()
      {
      return value;
      }

   @Override
   public String toString()
      {
      return "Payload{ " + "message='" + message + '\'' + ", value=" + value + " }";
      }
   }
