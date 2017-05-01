package Consumers;


/**
* Consumers/RemoteConsumerOperations.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from RemoteConsumer.idl
* Pondelok, 2017, m�ja 1 16:28:02 CEST
*/

public interface RemoteConsumerOperations 
{
  String idConsumer ();
  boolean readyToGo ();
  boolean myTurn ();
  boolean inObservation ();
  boolean manualMode ();
  Consumers.VectorRessource resCurrent ();
  Consumers.VectorRessource resTarget ();
  char personality ();
  boolean gameOver ();
  long timeFinished ();
  void setIdConsumer (String id);
  void setReadyToGo (boolean status);
  void setMyTurn (boolean go);
  void setInObservation (boolean observation);
  void setManualMode (boolean mode);
  void setResCurrent (Consumers.VectorRessource current);
  void setResTarget (Consumers.VectorRessource target);
  void setPersonality (char personality);
  void setGameOver (boolean over);
  void updateRessources (Consumers.VectorRessource acquired);
  boolean finished ();
  String _toString ();
} // interface RemoteConsumerOperations
