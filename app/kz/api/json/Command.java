package kz.api.json;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by baha on 6/10/15.
 */
public class Command {
    protected String command;

    public boolean is(String commandName){
        return command.equalsIgnoreCase(commandName);
    }

    /*
    List<CommandParameter> params;

    public String getParam(String name){
        String cpp = "";
        if (params!=null){
            for (CommandParameter cp:params){
                if (cp.getName().equalsIgnoreCase(name)){
                    cpp = cp.getValue();
                    break;
                }
            }
        }
        return cpp;
    }

    public List<String> getAllParams(String name){
        List<String> cps = new ArrayList<String>();
        if (params!=null){
            for (CommandParameter cp:params){
                if (cp.getName().equalsIgnoreCase(name)){
                    cps.add(cp.getValue());
                }
            }
        }
        return cps;
    }

    public List<CommandParameter> getParams() {
        return params;
    }

    public void setParams(List<CommandParameter> params) {
        this.params = params;
    }*/

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public String toString() {
        return "Command{" +
                "command='" + command + '\'' +
                '}';
    }
}
