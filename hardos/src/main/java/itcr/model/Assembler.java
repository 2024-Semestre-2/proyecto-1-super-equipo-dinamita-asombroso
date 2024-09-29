package itcr.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembler class provides methods to validate assembly code instructions.
 */
public class Assembler {

  private static final String MOV_REGEX = "^MOV\\s+([A-D]X)\\s*,\\s*(([A-D]X)|\\d+)(\\s*//.*)?$";
  private static final String LOAD_REGEX = "^LOAD\\s+([A-D]X)(\\s*//.*)?$";
  private static final String STORE_REGEX = "^STORE\\s+([A-D]X)(\\s*//.*)?$";
  private static final String ADD_REGEX = "^ADD\\s+([A-D]X)(\\s*//.*)?$";
  private static final String SUB_REGEX = "^SUB\\s+([A-D]X)(\\s*//.*)?$";
  private static final String INC_REGEX = "^INC\\s+([A-D]X)(\\s*//.*)?$|^INC(\\s*//.*)?$";
  private static final String DEC_REGEX = "^DEC\\s+([A-D]X)(\\s*//.*)?$|^DEC(\\s*//.*)?$";
  private static final String SWAP_REGEX = "^SWAP\\s+([A-D]X)\\s*,\\s*([A-D]X)(\\s*//.*)?$";
  private static final String INT_REGEX = "^INT\\s+\\_[0-9][0-9]H(\\s*//.*)?$";
  private static final String JMP_REGEX = "^JMP\\s*[+\\-]?\\d+(\\s*//.*)?$";
  private static final String CMP_REGEX = "^CMP\\s+([A-D]X)\\s*,\\s*([A-D]X)(\\s*//.*)?$";
  private static final String JE_REGEX = "^JE\\s*[+\\-]?\\d+(\\s*//.*)?$";
  private static final String JNE_REGEX = "^JNE\\s*[+\\-]?\\d+(\\s*//.*)?$";
  private static final String PARAM_REGEX = "^PARAM\\s+(-?\\d+\\s*(,\\s-?\\d+\\s*){0,2})(\\s*//.*)?$";
  private static final String PUSH_REGEX = "^PUSH\\s+([A-D]X)(\\s*//.*)?$";
  private static final String POP_REGEX = "^POP\\s+([A-D]X)(\\s*//.*)?$";
  private static final String COMMENT_REGEX = "^(//.*)$";

  /**
   * Validates a single instruction against known patterns.
   *
   * @param instruction the instruction to validate
   * @param lineNumber the line number of the instruction
   * @return null if the instruction is valid, otherwise an error message
   */
  private static String validateInstruction(String instruction, int lineNumber) {
    if (instruction.matches(MOV_REGEX)) return null;
    if (instruction.matches(LOAD_REGEX)) return null;
    if (instruction.matches(STORE_REGEX)) return null;
    if (instruction.matches(ADD_REGEX)) return null;
    if (instruction.matches(SUB_REGEX)) return null;
    if (instruction.matches(INC_REGEX)) return null;
    if (instruction.matches(DEC_REGEX)) return null;
    if (instruction.matches(SWAP_REGEX)) return null;
    if (instruction.matches(INT_REGEX)) return null;
    if (instruction.matches(JMP_REGEX)) return null;
    if (instruction.matches(CMP_REGEX)) return null;
    if (instruction.matches(JE_REGEX)) return null;
    if (instruction.matches(JNE_REGEX)) return null;
    if (instruction.matches(PARAM_REGEX)) return null;
    if (instruction.matches(PUSH_REGEX)) return null;
    if (instruction.matches(POP_REGEX)) return null;
    if (instruction.matches(COMMENT_REGEX)) return null;

    // If we reach this point, the instruction is not recognized
    return String.format("Error en línea %d: Instrucción no reconocida '%s'", lineNumber, instruction);
  }

  /**
   * Validates the format of the given assembly code.
   *
   * @param code the assembly code to validate
   * @return null if the code is valid, otherwise an error message
   */
  public static String validateFormat(String code) {
    if (code == null || code.isEmpty()) {
      return "Error: El código está vacío o es nulo";
    }

    String[] lines = code.split("\\r?\\n");
    List<String> errors = new ArrayList<>();

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i].trim();
      if (!line.isEmpty()) {
        String error = validateInstruction(line, i + 1);
        if (error != null) {
          errors.add(error);
        }
      }
    }

    if (errors.isEmpty()) {
      return null;  // There are no errors
    } else {
      return String.join("\n", errors);
    }
  }
}