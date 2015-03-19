import com.ibm.wala.shrikeCT.InvalidClassFileException;


public class CallGraphTest {

	public static void main(String[] args) {
		CallGraphWALA cg = new CallGraphWALA();
		try {
			cg.init(false,"C:/Users/Fladson Gomes/Desktop/workspace_luna/teste",null);
		} catch (UnsupportedOperationException | InvalidClassFileException e) {
			e.printStackTrace();
		}

	}

}
