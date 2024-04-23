package zad1;

import java.util.*;
import java.util.concurrent.*;

public class Main {

    public static void main(String[] args) throws Exception {
        String fileName = System.getProperty("user.home") + "/PassTimeServerOptions.yaml";
        Options opts = Tools.createOptionsFromYaml(fileName);
        String host = opts.getHost();
        int port = opts.getPort();
        boolean concur =  opts.isConcurMode();
        boolean showRes = opts.isShowSendRes();
        Map<String, List<String>> clRequests = opts.getClientsMap();
        ExecutorService es = Executors.newCachedThreadPool();
        List<ClientTask> ctasks = new ArrayList<>();
        List<String> clogs = new ArrayList<>();



        // start clients
        clRequests.forEach( (id, reqList) -> {
            Client c = new Client(host, port, id);
            if (concur) {
                ClientTask ctask = ClientTask.create(c, reqList, showRes);
                ctasks.add(ctask);
                es.execute(ctask);
            } else {
                c.connect();
                c.send("login " + id);
                for(String req : reqList) {
                    String res = c.send(req);
                    if (showRes) System.out.println(res);
                }
                String clog = c.send("bye and log transfer");
                System.out.println(clog);
            }
        });

        if (concur) {
            ctasks.forEach( task -> {
                String log = task.get();
                clogs.add(log);
            });
            clogs.forEach( System.out::println);
            es.shutdown();
        }

    }

}