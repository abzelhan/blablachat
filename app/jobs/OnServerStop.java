package jobs;

import utils.helpers.ESClientHelper;
import play.jobs.Job;
import play.jobs.OnApplicationStop;

import java.io.IOException;

/**
 * Created by abzal  on 3/19/18.
 */
@OnApplicationStop
public class OnServerStop extends Job {
    public void doJob() {
        try {
            ESClientHelper.getInstance().close();
            System.out.println("Elastic Search Client closed!");
        } catch (IOException e) {
            System.out.println("Some shit happens!");

        }
    }
}
