/**
 * @author Petrykowski Maciej S19267
 */

package S_PASSTIME_SERVER1;


import java.util.List;
import java.util.concurrent.ExecutionException;

public class ClientTask extends Thread {

    Client client;
    List<String> reqs;
    boolean showSendRes;
    String log;
    boolean finish=false;

    private ClientTask(Client client, List<String> reqs, boolean showSendRes) {
        this.client = client;
        this.reqs = reqs;
        this.showSendRes = showSendRes;
    }

    public static ClientTask create(Client c, List<String> reqs, boolean showSendRes) {
        return new ClientTask(c, reqs, showSendRes);
    }

    @Override
    public void run() {
        client.connect();
        client.send("login "+client.getId());
        for (String request : reqs) {
            String response = client.send(request);
            if (showSendRes)
                System.out.println(response);
        }
        log = client.send("bye and log transfer");
        finish=true;
    }

    public String get()throws InterruptedException,ExecutionException
    {
        while (!finish){Thread.sleep(10);}
        return log;
    }
}
