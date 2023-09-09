package hlml.builder;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/** Ordered collection of instructions are executed sequentially for a
 * meaningful usage of the processor. */
final class Program {
  /** Returns an empty program. */
  static Program create() {
    return new Program(new ArrayList<>(), new HashMap<>());
  }

  /** Instructions that are added to the program. */
  private final List<Instruction> instructions;

  /** Instruction indices that can be used to jump to an instruction. */
  private final Map<Waypoint, OptionalInt> waypoints;

  /** Constructs. */
  private Program(
    List<Instruction> instructions,
    Map<Waypoint, OptionalInt> waypoints)
  {
    this.instructions = instructions;
    this.waypoints = waypoints;
  }

  /** Add an instruction the the end of the program. */
  void instruct(Instruction instruction) {
    instructions.add(instruction);
  }

  /** Returns a new waypoint at an unknown position. */
  Waypoint waypoint() {
    Waypoint waypoint = new Waypoint(waypoints.size());
    waypoints.put(waypoint, OptionalInt.empty());
    return waypoint;
  }

  /** Makes the given waypoint point to the next instruction that will be
   * given. */
  void define(Waypoint waypoint) {
    waypoints.put(waypoint, OptionalInt.of(instructions.size()));
  }

  /** Returns the index of the instruction that is pointed to by a waypoint. */
  int resolve(Waypoint waypoint) {
    return waypoints.get(waypoint).getAsInt();
  }

  /** Appends the program to an appendable. */
  void append_to(Appendable appendable) throws IOException {
    for (Instruction instruction : instructions) {
      append_instruction(appendable, instruction);
      appendable.append(System.lineSeparator());
    }
  }

  /** Appends an instruction. */
  private void append_instruction(
    Appendable appendable,
    Instruction instruction)
    throws IOException
  {
    switch (instruction) {
      case Instruction.JumpAlways i -> {
        appendable.append("jump ");
        appendable.append(Integer.toString(resolve(i.goal())));
        appendable.append(" always");
      }
      case Instruction.JumpOnTrue i -> {
        appendable.append("jump ");
        appendable.append(Integer.toString(resolve(i.goal())));
        appendable.append(" equal true ");
        append_register(appendable, i.condition());
      }
      case Instruction.JumpOnFalse i -> {
        appendable.append("jump ");
        appendable.append(Integer.toString(resolve(i.goal())));
        appendable.append(" equal false ");
        append_register(appendable, i.condition());
      }
      case Instruction.End i -> appendable.append("end");
      case Instruction.Set i -> {
        appendable.append("set ");
        append_register(appendable, i.target());
        appendable.append(' ');
        append_register(appendable, i.source());
      }
      case Instruction.UnaryOperation i -> {
        appendable.append("op ");
        appendable.append(i.operation_code());
        appendable.append(' ');
        append_register(appendable, i.target());
        appendable.append(' ');
        append_register(appendable, i.operand());
      }
      case Instruction.BinaryOperation i -> {
        appendable.append("op ");
        appendable.append(i.operation_code());
        appendable.append(' ');
        append_register(appendable, i.target());
        appendable.append(' ');
        append_register(appendable, i.left_operand());
        appendable.append(' ');
        append_register(appendable, i.right_operand());
      }
    }
  }

  /** Appends a register. */
  private void append_register(Appendable appendable, Register register)
    throws IOException
  {
    switch (register) {
      case Register.Global r -> {
        appendable.append(r.name().source());
        appendable.append('$');
        appendable.append(r.name().identifier());
      }
      case Register.Local r -> {
        appendable.append(r.symbol().source());
        appendable.append('$');
        appendable.append(r.symbol().identifier());
        appendable.append('$');
        appendable.append(r.identifier());
      }
      case Register.Temporary r -> {
        appendable.append('_');
        appendable.append(Integer.toString(r.index()));
      }
      case Register.Literal r -> {
        DecimalFormat decimal_formatter = new DecimalFormat("0.#");
        decimal_formatter.setMaximumFractionDigits(Integer.MAX_VALUE);
        appendable.append(decimal_formatter.format(r.value()));
      }
    }
  }
}
