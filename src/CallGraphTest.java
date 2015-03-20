import com.ibm.wala.ipa.cha.ClassHierarchyException;
import com.ibm.wala.shrikeCT.InvalidClassFileException;


public class CallGraphTest {

	public static void main(String[] args) throws ClassHierarchyException {
		CallGraphWALA cg = new CallGraphWALA();
		try {
			cg.init("C:/Users/Fladson Gomes/Desktop/workspace_luna/teste");
//			cg.init("C:/Users/Fladson Gomes/Desktop/workspace_luna/teste");
		} catch (UnsupportedOperationException | InvalidClassFileException e) {
			e.printStackTrace();
		}

	}

}
