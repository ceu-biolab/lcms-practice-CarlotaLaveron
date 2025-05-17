package lipid;
import java.util.List;

public class LipidPriority {
    private static final List<LipidType> PRIORITY = List.of(
            LipidType.PG,
            LipidType.PE,
            LipidType.PI,
            LipidType.PA,
            LipidType.PS,
            LipidType.PC
    );


    public static boolean isHigherPriority(LipidType l1, LipidType l2) {
        //ES MAYOR L1 SI SU INDEX ES MENOR
        return PRIORITY.indexOf(l1) < PRIORITY.indexOf(l2);
    }

}
