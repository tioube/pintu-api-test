package commons;

import java.sql.DriverManager;
import java.sql.Connection;

public class connectDB {

    public static void main (String[] args) {

        Connection connection=null;

        try {
            Class.forName("org.postgresql.Driver");
            connection= DriverManager.getConnection("jdbc:postgresql://10.22.103.39:5432/core_loan_db","postgres","postgres123");
            if(connection!=null){
                System.out.println("Connection OK ");
            } else {
                System.out.println("Connection Failed");
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
