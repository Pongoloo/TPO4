/**
 *
 *  @author Karwowski Jakub S27780
 *
 */

package zad1;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClientTask implements Runnable{
    private Client client;
    private List<String> requirements;
    private boolean showRes;
    private String clientLog;
    public ClientTask(Client client, List<String> requirements, boolean showRes) {
        this.client = client;
        this.requirements = requirements;
        this.showRes = showRes;
    }

    public static ClientTask create(Client c, List<String> reqList, boolean showRes) {
        return new ClientTask(c,reqList,showRes);
    }

    @Override
    public void run() {
        if(showRes){
            handleClientShowingServerOutputs();
        } else{
            handleClient();
        }
    }
    public String get() throws InterruptedException, ExecutionException {
        if(clientLog==null){
            throw new InterruptedException("XD");
        }
        if(clientLog.equals("")){
            throw new ExecutionException(
                    new Throwable("VILLAIN MUSIC PLAYING"));
        }
        return clientLog;
    }
    private void handleClient() {
        client.connect();
        client.send("login "+client.getId());
        for (String requirement : requirements) {
            client.send(requirement);
            System.out.println(requirement);
        }
        client.send("bye and log transfer");
    }

    private void handleClientShowingServerOutputs() {
        client.connect();

        System.out.println(client.send("login " + client.getId()));

        for (String requirement : requirements) {
            System.out.println(client.send(requirement));
            System.out.println(requirement);
        }
        System.out.println(client.send("bye and log transfer"));
    }


    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public List<String> getRequirements() {
        return requirements;
    }

    public void setRequirements(List<String> requirements) {
        this.requirements = requirements;
    }

    public boolean isShowRes() {
        return showRes;
    }

    public void setShowRes(boolean showRes) {
        this.showRes = showRes;
    }



}
