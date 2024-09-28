package itcr.model;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Assembler {

    private static final String MOV_REGEX = "^MOV\\s+([A-D]X)\\s*,\\s*(([A-D]X)|\\d+)$";
    private static final String LOAD_REGEX = "^LOAD\\s+([A-D]X)\\s*$";
    private static final String STORE_REGEX = "^STORE\\s+([A-D]X)\\s*$";
    private static final String ADD_REGEX = "^ADD\\s+([A-D]X)\\s*$";
    private static final String SUB_REGEX = "^SUB\\s+([A-D]X)\\s*$";
    private static final String INC_REGEX = "^INC\\s+([A-D]X)\\s*$|INC";
    private static final String DEC_REGEX = "^DEC\\s+([A-D]X)\\s*$|DEC";
    private static final String SWAP_REGEX = "^SWAP\\s+([A-D]X)\\s*,\\s*([A-D]X)\\s*$";
    private static final String INT_REGEX = "^INT\\s+\\_[0-9][0-9]H\\s*$";
    private static final String JMP_REGEX = "^JMP\\s*[+\\-]?\\d+\\s*$";
    private static final String CMP_REGEX = "^CMP\\s+([A-D]X)\\s*,\\s*([A-D]X)\\s*$";
    private static final String JE_REGEX = "^JE\\s*[+\\-]?\\d+\\s*$";
    private static final String JNE_REGEX = "^JNE\\s*[+\\-]?\\d+\\s*$";
    private static final String PARAM_REGEX = "^PARAM\\s+(-?\\d+\\s*(,\\s-?\\d+\\s*){0,2})$";


    private static final String PUSH_REGEX = "^PUSH\\s+([A-D]X)\\s*$";
    private static final String POP_REGEX = "^POP\\s+([A-D]X)\\s*$";

    public static boolean validateInstruction(String instruction) {
        return instruction.matches(MOV_REGEX) ||
               instruction.matches(LOAD_REGEX) ||
               instruction.matches(STORE_REGEX) ||
               instruction.matches(ADD_REGEX) ||
               instruction.matches(SUB_REGEX) ||
               instruction.matches(INC_REGEX) ||
               instruction.matches(DEC_REGEX) ||
               instruction.matches(SWAP_REGEX) ||
               instruction.matches(INT_REGEX) ||
               instruction.matches(JMP_REGEX) ||
               instruction.matches(CMP_REGEX) ||
               instruction.matches(JE_REGEX) ||
               instruction.matches(JNE_REGEX) ||
               instruction.matches(PARAM_REGEX) ||
               instruction.matches(PUSH_REGEX) ||
               instruction.matches(POP_REGEX);
    }

    public static boolean validateFormat(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        String[] lines = code.split("\\r?\\n");
        for (String line : lines) {
            line = line.trim();
            System.out.println(line);
            if (!line.isEmpty() && !validateInstruction(line)) {
                return false;
            }
        }
        return true;
    }

    /* 
    public static void main(String[] args) {
        Assembler assembler = new Assembler();
        String code = "MOV AX, 14\nADD AX\nSUB AX\nJMP -5\nADD AX\nINC\nINC AX\nINT _20H\nINT _20H\nDEC\nDEC BX\nJMP -5\nJE -5\nJNE -4";

        if (assembler.validateFormat(code)) {
            System.out.println("El código ensamblador es válido.");
        } else {
            System.out.println("El código ensamblador no es válido.");
        }
    }*/
}
