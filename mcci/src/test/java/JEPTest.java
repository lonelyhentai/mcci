import jep.JepException;
import jep.Interpreter;
import jep.SharedInterpreter;

public class JEPTest {
    public static void main(String[] args) {
        try(Interpreter inter = new SharedInterpreter()) {
            inter.exec("from java.lang import System");
            inter.exec("s = \"Hello World\"");
            inter.exec("System.out.println(s)");
            inter.exec("print(s)");
            inter.exec("print(s[1:-1])");
        } catch (JepException e) {
            e.printStackTrace();
        }
    }
}
