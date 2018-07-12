package jobs;

import models.APIMethod;
import models.APIParam;
import play.Play;
import play.jobs.Job;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by bakhyt on 10/16/17.
 */
public class UpdateAPIDocJob extends Job {
    String apiClass = "API.java";

    public UpdateAPIDocJob() {
    }




    public UpdateAPIDocJob(String apiClass) {
        this.apiClass = apiClass;
    }

    @Override
    public void doJob() {
        System.out.println("starting job");
        String projectsFolder = Play.applicationPath.getPath()+"/";


        String controllersFolder = "app/controllers/";

        List<APIMethod> methods = new ArrayList<>();

        //extract api methods
        try {
            File f = new File(projectsFolder + controllersFolder + apiClass);

            methods.addAll(extractMethods(f));

            for (APIMethod m : methods) {
//                File classFile = new File(projectsFolder + controllersFolder + m.getClassName() + ".java");
                File classFile = new File(projectsFolder + controllersFolder + m.getClassName() + ".java");
                FileReader in = new FileReader(classFile);
                BufferedReader br = new BufferedReader(in);
                List<String> lines = new ArrayList<>();
                String currentLine = "";

                boolean methodLine = false;
                while ((currentLine = br.readLine()) != null) {
                    if (currentLine.trim().startsWith("public static ")) {
                        if (methodLine) {
                            break;
                        } else {
                            if (currentLine.contains("public static Result " + m.getMethodName() + "(Context context)")) {
                                methodLine = true;
                            }
                        }
                    }

                    if (methodLine) {
                        lines.add(currentLine);
                    }
                }

                br.close();
                in.close();

                List<APIParam> params = new ArrayList<>();

                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i).trim();
                    if (!line.trim().startsWith("//")) {
                        String getPrefix = "context.get";

                        if (line.contains(getPrefix)) {
                            try {
                                if(!line.contains("Message")){
                                    String word = "";
                                    String startStr = line.substring(line.indexOf(getPrefix));
                                    word = startStr.substring(0, startStr.indexOf(")") + 1);
                                    System.out.println("word: " + word);

                                    APIParam param = new APIParam();
                                    System.out.println("paramName: " + word);
                                    String wordWithoutPrefix = word.replaceFirst(getPrefix, "");
                                    System.out.println("wordWithoutPrefix: " + wordWithoutPrefix);
                                    param.setRequired(!wordWithoutPrefix.startsWith("Optional"));

                                    String wordWithoutOptional = wordWithoutPrefix.replaceFirst("Optional", "");
                                    System.out.println("wordWithoutOptional: " + wordWithoutOptional);

                                    param.setType(wordWithoutOptional
                                            .substring(0, wordWithoutOptional.indexOf("("))
                                            .toLowerCase());
                                    System.out.println("param: " + param.getType());

                                    Pattern p = Pattern.compile("\"([^\"]*)\"");
                                    Matcher matcher = p.matcher(wordWithoutOptional);
                                    if (matcher.find()) {
                                        param.setName(matcher.group(1));
                                    }

                                    params.add(param);

                                    System.out.println(param);
                                }

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        if (line.contains("getRequiredUser")) {
                            m.setNeedAuth(true);
                        }
                    }

                    //System.out.println("\t\t" + line);
                }

                m.setParams(params);
            }
            saveMethods(methods);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void saveMethods(List<APIMethod> methods) {

        List<APIMethod> all = APIMethod.find("deleted=0").fetch();
        for (APIMethod a : all) {
            a.setDeleted(1);
            a._save();
        }

        for (APIMethod method : methods) {
            saveMethod(method);
            System.out.println();
            System.out.println("\n" + generateTest(method) + "\n");
        }
    }

    public void saveMethod(APIMethod method) {

        APIMethod m = APIMethod.find("name=:name")
                .setParameter("name", method.getName())
                .first();
        if (m == null) {
            m = new APIMethod();
            m.setName(method.getName());
        }

        m.setClassName(method.getClassName());
        m.setMethodName(method.getMethodName());
        m.setNeedAuth(method.isNeedAuth());
        m.setDeleted(0);
        m.setApiName("BlaBlaChatApi");
        m.save();

        List<APIParam> params = APIParam.find("apiMethod=:method").setParameter("method",m).fetch();

        for (APIParam param:params){
            param.setDeleted(1);
            param._save();
        }

        for (APIParam param:method.getParams()){
            APIParam p = APIParam.find("apiMethod=:method and name=:name")
                    .setParameter("method",m)
                    .setParameter("name",param.getName())
                    .first();
            if (p==null){
                p = new APIParam();
                p.setName(param.getName());
                p.setApiMethod(m);
            }
            if(param.getType().toLowerCase().contains("ornull")){
                param.setType(param.getType().replace("ornull",""));
            }
            p.setType(param.getType());
            p.setRequired(param.isRequired());
            p.setDeleted(0);
            p._save();
        }
    }

    public String generateTest(APIMethod method) {
        StringBuilder sb = new StringBuilder();
        sb.append("@Test\npublic void t2_#command#(){"
                .replaceFirst("#time#", System.currentTimeMillis() + "")
                .replace("#command#", method.getName())).append("\n");

        sb.append("JSONObject command = getCommand(\"#command#\");".replace("#command#", method.getName())).append("\n");

        sb.append("JSONObject params = command.getJSONObject(\"params\");").append("\n");

        for (APIParam param : method.getParams()) {
            if (param.getName() != null) {

                String str = "params.put(\"#paramName#\", #value#);".replace("#paramName#", param.getName());
                if (param.getType().equalsIgnoreCase("string")) {
                    str = str.replace("#value#", "\"\"");
                } else if (param.getType().equalsIgnoreCase("integer")
                        || param.getType().equalsIgnoreCase("long")
                        ) {
                    str = str.replace("#value#", "0");
                } else if (param.getType().equalsIgnoreCase("integer")
                        || param.getType().equalsIgnoreCase("long")) {
                    str = str.replace("#value#", "0");
                } else if (param.getType().equalsIgnoreCase("code")) {
                    str = str.replace("#value#", "\"\"");
                } else if (param.getType().equalsIgnoreCase("double")) {
                    str = str.replace("#value#", "0.0");
                } else if (param.getType().equalsIgnoreCase("boolean")) {
                    str = str.replace("#value#", "false");
                } else if (param.getType().equalsIgnoreCase("list")
                        || param.getType().equalsIgnoreCase("codelist")
                        || param.getType().equalsIgnoreCase("integerlist")
                        || param.getType().equalsIgnoreCase("longlist")) {
                    str = str.replace("#value#", "new JSONArray(\"[]\")");
                } else if (param.getType().equalsIgnoreCase("jsonarray")) {
                    str = str.replace("#value#", "new JSONArray(\"[]\")");
                }
                sb.append(str);

                if (param.isRequired()){
                    sb.append("//required");
                }
                sb.append("\n");

            }
        }

        sb.append("command.set(\"params\", params);").append("\n");

        sb.append("Http.Response r = runPost(command, userid, token);").append("\n");

        sb.append("assertStatus(200, r);").append("\n");

        sb.append("}").append("\n");

        return sb.toString();
    }

    public static List<APIMethod> extractMethods(File file) throws Exception {

        List<APIMethod> methods = new ArrayList<>();
        FileReader in = new FileReader(file);
        BufferedReader br = new BufferedReader(in);
        List<String> lines = new ArrayList<>();
        String currentLine = "";

        while ((currentLine = br.readLine()) != null) {
            lines.add(currentLine);
        }

        br.close();
        in.close();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.startsWith("case \"")) {
                APIMethod apiMethod = new APIMethod();
                apiMethod.setName(line.replace("case \"", "").replace("\"", "").replace(":", ""));
                String nextLine = lines.get(i + 1).trim();
                String methodInfo = nextLine.replaceFirst("result = ", "");
                System.out.println("methodInfo: " + methodInfo);
                apiMethod.setClassName(methodInfo.split("\\.")[0]);//Original
//                apiMethod.setClassName("BlaBlaChatApi");//BlaBlaChatApi hardcoded
                apiMethod.setApiName("BlaBlaChatApi");
                apiMethod.setMethodName(methodInfo.split("\\.")[1].split("\\(")[0]);
                System.out.println(apiMethod+" here");
                methods.add(apiMethod);
            }
        }
        return methods;
    }
}
