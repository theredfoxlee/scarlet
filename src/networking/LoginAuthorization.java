package networking;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

public class LoginAuthorization {
//    private String login;
//    private String password;
    private HashMap<String,String> users= new HashMap<>();

    LoginAuthorization(){
        getUsername();
    }

    private void getUsername(){

        try{
            FileReader users_file = new FileReader("users.txt");
            BufferedReader bufread = new BufferedReader(users_file);

            String single_line;
            while((single_line=bufread.readLine())!=null){
                String [] users_from_file = single_line.split(":");
                //appending map
                users.put(users_from_file[0],users_from_file[1]);
            }

        }
        catch(Exception e){
            System.out.println("Exception:"+e);
        }
    }

    public boolean autorize(String login, String password){
        boolean user_exist;

        if(users.containsKey(login)) {
            if (users.get(login).equals(password)) {
                user_exist = true;
            } else {
                user_exist = false;
            }
        }
        else{
            user_exist = false;
        }
        return user_exist;

        }

}

