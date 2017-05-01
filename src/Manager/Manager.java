package Manager;

import Consumers.RemoteConsumer;
import Consumers.RemoteConsumerHelper;
import Producers.RemoteProducer;
import Producers.RemoteProducerHelper;
import Producers.RessourceType;
import org.apache.commons.cli.*;
import org.omg.CORBA.*;

import javax.swing.text.html.Option;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class Manager
{
    private static boolean gameFinished(RemoteConsumer[] consumers, boolean allFinished)
    {
        if(allFinished)
        {
            for (RemoteConsumer remoteConsumer : consumers)
            {
                if(!remoteConsumer.finished())
                    return false;
            }

            return true;
        }
        else
        {
            boolean result = false;

            for (RemoteConsumer remoteConsumer : consumers)
            {
                if(remoteConsumer.finished())
                {
                    result = true;
                    break;
                }
            }

            for (RemoteConsumer remoteConsumer : consumers)
            {
                remoteConsumer.setGameOver(true);
            }

            return result;
        }
    }

    private synchronized static LinkedList<String> loadProducers(RessourceType ressourceType)
    {
        LinkedList<String> producers = new LinkedList<String>();

        try
        {
            FileReader fileReader = new FileReader(ressourceType.toString().toLowerCase() + "_producers.drg");
            BufferedReader bufferedReader = new BufferedReader(fileReader);

            String current;

            while((current = bufferedReader.readLine()) != null)
            {
                producers.add(current);
            }

            fileReader.close();
        }
        catch (IOException ioException)
        {
            System.out.println("Unable to discover " + ressourceType.toString().toLowerCase() + " producers!");
            System.exit(12);
        }

        return producers;
    }

    public static void main(String[] args)
    {
        BufferedReader userInputReader = new BufferedReader(new InputStreamReader(System.in));
        String userResponse;

        Options options = new Options();

        options.addOption("t", "turn-by-turn", false, "every game agent plays on its own turn");
        options.addOption("a", "all-finished", false, "the game is finished once all consumers targets are achieved");
        options.addOption("f", "first-finished", false, "the game is finished once the first consumer achieves its target");
        options.addOption("s", "score-save", false, "keep scores in a file and print out a score table at the end of game");
        options.addOption("h", "help", false, "shows help");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter helpFormatter = new HelpFormatter();
        helpFormatter.printHelp("Manager -h", options, false);
        CommandLine commandLine = null;

        try
        {
            commandLine = parser.parse(options, args);
        }
        catch (ParseException e)
        {
            System.err.println("Failed to parse given options! Enter 'java Manager -h' for help.");
            System.exit(-1);
        }

        boolean manual = false;
        boolean waitForAll = true;
        boolean generateScore = false;

        if(commandLine.hasOption("t"))
        {
            manual = true;
        }

        if(commandLine.hasOption("a"))
        {
            System.out.println("wait for all");
            waitForAll = true;
        }

        if(commandLine.hasOption("f"))
        {
            waitForAll = false;
        }

        if(commandLine.hasOption("s"))
        {
            generateScore = true;
        }

        try
        {
            ORB corba = ORB.init(args, null);

            LinkedList<String> remoteConsumersIOR = new LinkedList<String>();
            LinkedList<String> remoteWoodProducersIOR = loadProducers(RessourceType.WOOD);
            LinkedList<String> remoteMarbleProducersIOR = loadProducers(RessourceType.MARBLE);

            try
            {
                FileReader fileReader = new FileReader("consumers.drg");
                BufferedReader bufferedReader = new BufferedReader(fileReader);

                String current;

                while((current = bufferedReader.readLine()) != null)
                {
                    remoteConsumersIOR.add(current);
                }

                fileReader.close();
            }
            catch (IOException inputOutputException)
            {
                System.out.println("Unable to read consumers list file!");
                System.exit(12);
            }

            RemoteConsumer[] remoteConsumers = new RemoteConsumer[remoteConsumersIOR.size()];
            RemoteProducer[] remoteWoodProducers = new RemoteProducer[remoteWoodProducersIOR.size()];
            RemoteProducer[] remoteMarbleProducers = new RemoteProducer[remoteMarbleProducersIOR.size()];

            System.out.println("Discovering producers...");

            for (int i = 0; i < remoteWoodProducersIOR.size(); i++)
            {
                remoteWoodProducers[i] = RemoteProducerHelper.narrow(corba.string_to_object(remoteWoodProducersIOR.get(i)));
            }

            for (int i = 0; i < remoteMarbleProducersIOR.size(); i++)
            {
                remoteMarbleProducers[i] = RemoteProducerHelper.narrow(corba.string_to_object(remoteMarbleProducersIOR.get(i)));
            }

            System.out.println("Successfully discovered " + remoteWoodProducers.length + " wood producers, " + remoteMarbleProducers.length + " marble producers.");
            System.out.println("Discovering consumers...");

            for (int i = 0; i < remoteConsumersIOR.size(); i++)
            {
                remoteConsumers[i] = RemoteConsumerHelper.narrow(corba.string_to_object(remoteConsumersIOR.get(i)));
            }

            System.out.println("Successfully discovered " + remoteConsumers.length + " consumers.");
            System.out.println("DRG is ready to start the game. Would you like to make producers and consumers ready to go? [YyNn]");
            userResponse = userInputReader.readLine();

            if(!userResponse.matches("[Yy]"))
            {
                System.out.println("Negative or unknown response was recognized. System exiting...");
                System.exit(0);
            }

            System.out.println("Getting producers ready...");

            for (RemoteProducer remoteWoodProducer : remoteWoodProducers)
            {
                remoteWoodProducer.setReadyToGo(true);
                System.out.println(remoteWoodProducer._toString());
            }

            for (RemoteProducer remoteMarbleProducer : remoteMarbleProducers)
            {
                remoteMarbleProducer.setReadyToGo(true);
                System.out.println(remoteMarbleProducer._toString());
            }

            System.out.println("All discovered producers joined the game.");
            System.out.println("Getting consumers ready...");

            for (RemoteConsumer remoteConsumer : remoteConsumers)
            {
                if(manual)
                {
                    remoteConsumer.setManualMode(true);
                    remoteConsumer.setMyTurn(false);
                }

                remoteConsumer.setReadyToGo(true);

                System.out.println(remoteConsumer._toString());
            }

            System.out.println("All discovered consumers joined the game.");
            System.out.println("Game is in progress...");

            long timeZero = System.nanoTime();

            while(!gameFinished(remoteConsumers, waitForAll))
            {
                for (RemoteConsumer remoteConsumer : remoteConsumers)
                {
                    System.out.println(remoteConsumer._toString());

                    if(manual)
                    {
                        remoteConsumer.setMyTurn(true);

                        while (remoteConsumer.myTurn()) {
                            //Waiting for the current agent to finish
                        }
                    }
                }

                if(!manual)
                {
                    try
                    {
                        Thread.sleep(2000);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }

            long totalTime = System.nanoTime() - timeZero;

            long totalSeconds = totalTime / 1000000000;

            System.out.println("Game ended after " + totalSeconds + " seconds.");

            if(generateScore)
            {
                /*
                if(waitForAll)
                {
                    for (RemoteConsumer remoteConsumer : remoteConsumers)
                    {

                    }
                }
                else
                {
                    for (RemoteConsumer remoteConsumer : remoteConsumers)
                    {

                    }
                }*/
            }
        }
        catch (SystemException systemException)
        {
            System.out.println("CORBA system is down!");
            System.exit(-1);
        }
        catch (IOException inputOutputException)
        {
            System.out.println("I/O exception thrown by user input!");
            System.exit(-1);
        }
    }
}
