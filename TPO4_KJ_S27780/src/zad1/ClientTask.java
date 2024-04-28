package zad1;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class ClientTask extends FutureTask<String> {
    private Client client;
    private List<String> requirements;
    private boolean showRes;

    public ClientTask(Client client, List<String> requirements, boolean showRes) {
        super(() -> {
            if(showRes){
               return handleClientShowingServerOutputs(client,requirements);
            } else{
                return handleClient(client,requirements);
            }
        });
        this.client=client;
        this.requirements=requirements;
        this.showRes=showRes;

        }


    public static ClientTask create(Client c, List<String> reqList, boolean showRes) {
        return new ClientTask(c,reqList,showRes);
    }


    // Metoda statyczna do obsługi klienta bez pokazywania wyników
    private static String handleClient(Client client, List<String> requirements) {
        client.connect();
        client.send("login " + client.getId());
        for (String requirement : requirements) {
            client.send(requirement);
        }
        return client.send("bye and log transfer");
    }

    private static String handleClientShowingServerOutputs(Client client, List<String> requirements) {
        StringBuilder result = new StringBuilder();
        client.connect();
        result.append(client.send("login " + client.getId())).append("\n");
        for (String requirement : requirements) {
            result.append(client.send(requirement)).append("\n");
            result.append(requirement).append("\n");
        }
        result.append(client.send("bye and log transfer")).append("\n");
        return result.toString();
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
