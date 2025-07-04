package hlml.parser;

import java.util.List;
import java.util.Optional;

import hlml.lexer.Token;

/** Hierarchical collection of tokens in the source file. */
public sealed interface Node {
  /** Asserting a fact about the program. */
  sealed interface Declaration extends Node {
    /** Returns the token that can be used to report this declaration. */
    Token representative(List<Token> tokens);
  }

  /** Declaration of the program's first instructions. */
  record Entrypoint(Statement body) implements Declaration {
    @Override
    public int first(List<Token> tokens) { return body.first(tokens) - 1; }

    @Override
    public int last(List<Token> tokens) { return body.last(tokens); }

    @Override
    public Token representative(List<Token> tokens) {
      return tokens.get(first(tokens));
    }
  }

  /** Creation of a new symbol by the user. */
  sealed interface Definition extends Declaration {
    /** Visibility modifier of the defined symbol if there is any. */
    Optional<Token.Public> modifier();

    /** Identifier of the defined symbol. */
    Token.Identifier identifier();

    @Override
    default Token representative(List<Token> tokens) { return identifier(); }
  }

  /** Defining a symbol as building linked to the processor. */
  record Link(
    Optional<Token.Public> modifier,
    Token.Identifier building,
    Optional<Token.Identifier> alias) implements Definition
  {
    @Override
    public int first(List<Token> tokens) {
      if (modifier.isPresent()) { return tokens.indexOf(modifier.get()); }
      return tokens.indexOf(building) - 1;
    }

    @Override
    public int last(List<Token> tokens) {
      return tokens.indexOf(identifier()) + 1;
    }

    @Override
    public Token.Identifier identifier() { return alias.orElse(building); }
  }

  /** Defining a symbol as an alias to another one. */
  record Using(
    Optional<Token.Public> modifier,
    Mention used,
    Optional<Token.Identifier> alias) implements Definition
  {
    @Override
    public int first(List<Token> tokens) {
      if (modifier.isPresent()) { return tokens.indexOf(modifier.get()); }
      return used.first(tokens) - 1;
    }

    @Override
    public int last(List<Token> tokens) {
      return tokens.indexOf(identifier()) + 1;
    }

    @Override
    public Token.Identifier identifier() {
      return alias.orElseGet(used::identifier);
    }
  }

  /** Defining a symbol that is a parametrized set of instructions that resolve
   * to a value. */
  record Proc(
    Optional<Token.Public> modifier,
    Token.Identifier identifier,
    List<Parameter> parameters,
    Statement body) implements Definition
  {
    @Override
    public int first(List<Token> tokens) {
      if (modifier.isPresent()) { return tokens.indexOf(modifier.get()); }
      return tokens.indexOf(identifier) - 1;
    }

    @Override
    public int last(List<Token> tokens) { return body.last(tokens); }
  }

  /** Defining a parameter for a proc. */
  record Parameter(Token.Identifier identifier, boolean in_out)
    implements Node
  {
    @Override
    public int first(List<Token> tokens) { return tokens.indexOf(identifier); }

    @Override
    public int last(List<Token> tokens) {
      return tokens.indexOf(identifier) + (in_out ? 1 : 0);
    }
  }

  /** Defining a symbol that holds an known value. */
  record Const(
    Optional<Token.Public> modifier,
    Token.Identifier identifier,
    Expression value) implements Definition
  {
    @Override
    public int first(List<Token> tokens) {
      if (modifier.isPresent()) { return tokens.indexOf(modifier.get()); }
      return tokens.indexOf(identifier) - 1;
    }

    @Override
    public int last(List<Token> tokens) { return value.last(tokens) + 1; }
  }

  /** Defining a symbol that holds an unknown value. */
  record GlobalVar(
    Optional<Token.Public> modifier,
    Token.Identifier identifier,
    Optional<Expression> initial_value) implements Definition
  {
    @Override
    public int first(List<Token> tokens) {
      if (modifier.isPresent()) { return tokens.indexOf(modifier.get()); }
      return tokens.indexOf(identifier) - 1;
    }

    @Override
    public int last(List<Token> tokens) {
      if (initial_value.isPresent()) {
        return initial_value.get().last(tokens) + 1;
      }
      return tokens.indexOf(identifier) + 1;
    }
  }

  /** Instructions that can be given to the processor. */
  sealed interface Statement extends Node {}

  /** Sequentially executed collection of statements. */
  record Block(int first, List<Statement> inner_statements)
    implements Statement
  {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) {
      if (!inner_statements.isEmpty()) {
        return inner_statements.get(inner_statements.size() - 1).last(tokens)
          + 1;
      }
      return first + 1;
    }
  }

  /** Statements that branch the control flow. */
  record If(
    List<LocalVar> variables,
    Expression condition,
    Statement true_branch,
    Optional<Statement> false_branch) implements Statement
  {
    @Override
    public int first(List<Token> tokens) {
      if (!variables.isEmpty()) { return variables.get(0).first(tokens) - 1; }
      return condition.first(tokens) - 1;
    }

    @Override
    public int last(List<Token> tokens) {
      if (false_branch.isPresent()) { return false_branch.get().last(tokens); }
      return true_branch.last(tokens);
    }
  }

  /** Statements that loop the control flow. */
  record While(
    Optional<Token.Identifier> label,
    List<LocalVar> variables,
    Expression condition,
    Optional<Statement> interleaved,
    Statement loop) implements Statement
  {
    @Override
    public int first(List<Token> tokens) {
      if (label.isPresent()) { return tokens.indexOf(label.get()); }
      if (!variables.isEmpty()) { return variables.get(0).first(tokens) - 1; }
      return condition.first(tokens) - 1;
    }

    @Override
    public int last(List<Token> tokens) {
      return loop.last(tokens);
    }
  }

  /** Statements that exit a loop. */
  record Break(int first, Optional<Token.Identifier> label)
    implements Statement
  {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) {
      if (label.isPresent()) { return tokens.indexOf(label.get()) + 1; }
      return first + 1;
    }
  }

  /** Statements that skip the remaining in a loop. */
  record Continue(int first, Optional<Token.Identifier> label)
    implements Statement
  {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) {
      if (label.isPresent()) { return tokens.indexOf(label.get()) + 1; }
      return first + 1;
    }
  }

  /** Statements that provide a value to the procedures caller. */
  record Return(int first, Optional<Expression> value) implements Statement {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) {
      if (value.isPresent()) { return value.get().last(tokens) + 1; }
      return first + 1;
    }
  }

  /** Defining a symbol that holds an unknown value. */
  record LocalVar(
    Token.Identifier identifier,
    Optional<Expression> initial_value) implements Statement
  {
    @Override
    public int first(List<Token> tokens) {
      return tokens.indexOf(identifier) - 1;
    }

    @Override
    public int last(List<Token> tokens) {
      if (initial_value.isPresent()) {
        return initial_value.get().last(tokens) + 1;
      }
      return tokens.indexOf(identifier) + 1;
    }
  }

  /** Statements that affect the processors context. Useful for parsing as all
   * the initial tokens of these statements are same as expressions. */
  sealed interface Affect extends Statement {}

  /** Statements that mutate a variable. */
  sealed interface Mutate extends Affect {
    /** The mutated variable. */
    SymbolAccess target();

    @Override
    default int first(List<Token> tokens) { return target().first(tokens); }

    @Override
    default int last(List<Token> tokens) { return target().last(tokens) + 1; }
  }

  /** Statements that increment the value hold in a variable. */
  record Increment(SymbolAccess target) implements Mutate {}

  /** Statements that decrement the value hold in a variable. */
  record Decrement(SymbolAccess target) implements Mutate {}

  /** Statements that set the value hold in a variable. */
  sealed interface Assign extends Affect {
    /** The changed variable. */
    SymbolAccess target();

    /** The new value or the right operand. */
    Expression source();

    @Override
    default int first(List<Token> tokens) { return target().first(tokens); }

    @Override
    default int last(List<Token> tokens) { return source().last(tokens) + 1; }
  }

  /** Statements that set the target to be the same as the source. */
  record DirectlyAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the multiplication of the target and
   * the source. */
  record MultiplyAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the division of the target and the
   * source. */
  record DivideAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the integer division of the target
   * and the source. */
  record DivideIntegerAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the modulus of the target and the
   * source. */
  record ModulusAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the addition of the target and the
   * source. */
  record AddAssign(SymbolAccess target, Expression source) implements Assign {}

  /** Statements that set the target to be the subtract of the target and the
   * source. */
  record SubtractAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the left shift of the target and the
   * source. */
  record ShiftLeftAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the right shift of the target and the
   * source. */
  record ShiftRightAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise and of the target and the
   * source. */
  record AndBitwiseAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise xor of the target and the
   * source. */
  record XorBitwiseAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that set the target to be the bitwise or of the target and the
   * source. */
  record OrBitwiseAssign(SymbolAccess target, Expression source)
    implements Assign
  {}

  /** Statements that evaluate an expression and discard its value. Useful for
   * side effects, not the value. */
  record Discard(Expression source) implements Affect {
    @Override
    public int first(List<Token> tokens) { return source.first(tokens); }

    @Override
    public int last(List<Token> tokens) { return source.last(tokens) + 1; }
  }

  /** Calculations that denote a value. */
  sealed interface Expression extends Node {}

  /** Expression made up of one operand and an operator at the left. */
  sealed interface UnaryOperation extends Expression {
    /** Operand of the operator. */
    Expression operand();

    @Override
    default int first(List<Token> tokens) {
      return operand().first(tokens) - 1;
    }

    @Override
    default int last(List<Token> tokens) { return operand().last(tokens); }
  }

  /** Expression made up of two operands and an operator in the middle. */
  sealed interface BinaryOperation extends Expression {
    /** Operand that is at the left of the operator. */
    Expression left_operand();

    /** Operand that is at the right of the operator. */
    Expression right_operand();

    @Override
    default int first(List<Token> tokens) {
      return left_operand().first(tokens);
    }

    @Override
    default int last(List<Token> tokens) {
      return right_operand().last(tokens);
    }
  }

  /** Expressions at precedence level 11. */
  sealed interface Precedence11 extends Expression {}

  /** Expression that yields one if the left operand is not zero. Otherwise,
   * evaluates the right operand and yields one if it is not zero. If that is
   * also not the case, yields zero. */
  record LogicalOr(Precedence11 left_operand, Precedence11 right_operand)
    implements Precedence11, BinaryOperation
  {}

  /** Expressions at precedence level 10. */
  sealed interface Precedence10 extends Precedence11 {}

  /** Expression that yields zero if the left operand is zero. Otherwise,
   * evaluates the right operand and yields zero if it is zero. If that is also
   * not the case, yields one. */
  record LogicalAnd(Precedence10 left_operand, Precedence10 right_operand)
    implements Precedence10, BinaryOperation
  {}

  /** Expressions at precedence level 9. */
  sealed interface Precedence09 extends Precedence10 {}

  /** Expression that yields one when the left operand is equal to the right
   * operand, and zero otherwise. */
  record EqualTo(Precedence09 left_operand, Precedence09 right_operand)
    implements Precedence09, BinaryOperation
  {}

  /** Expression that yields one when the left operand is not equal to the right
   * operand, and zero otherwise. */
  record NotEqualTo(Precedence09 left_operand, Precedence09 right_operand)
    implements Precedence09, BinaryOperation
  {}

  /** Expression that yields one when the left operand is equal to the right
   * operand without any operand undergoing implicit conversions, and zero
   * otherwise. */
  record StrictlyEqualTo(Precedence09 left_operand, Precedence09 right_operand)
    implements Precedence09, BinaryOperation
  {}

  /** Expressions at precedence level 8. */
  sealed interface Precedence08 extends Precedence09 {}

  /** Expression that yields one when the left operand is less than the right
   * operand, and zero otherwise. */
  record LessThan(Precedence08 left_operand, Precedence08 right_operand)
    implements Precedence08, BinaryOperation
  {}

  /** Expression that yields one when the left operand is less than or equal to
   * the right operand, and zero otherwise. */
  record LessThanOrEqualTo(
    Precedence08 left_operand,
    Precedence08 right_operand) implements Precedence08, BinaryOperation
  {}

  /** Expression that yields one when the left operand is greater than the right
   * operand, and zero otherwise. */
  record GreaterThan(Precedence08 left_operand, Precedence08 right_operand)
    implements Precedence08, BinaryOperation
  {}

  /** Expression that yields one when the left operand is greater than or equal
   * to the right operand, and zero otherwise. */
  record GreaterThanOrEqualTo(
    Precedence08 left_operand,
    Precedence08 right_operand) implements Precedence08, BinaryOperation
  {}

  /** Expressions at precedence level 7. */
  sealed interface Precedence07 extends Precedence08 {}

  /** Expression that yields a number that has the bit pattern that is the OR'ed
   * version of its operands matching bits. */
  record BitwiseOr(Precedence07 left_operand, Precedence07 right_operand)
    implements Precedence07, BinaryOperation
  {}

  /** Expressions at precedence level 6. */
  sealed interface Precedence06 extends Precedence07 {}

  /** Expression that yields a number that has the bit pattern that is the
   * XOR'ed version of its operands matching bits. */
  record BitwiseXor(Precedence06 left_operand, Precedence06 right_operand)
    implements Precedence06, BinaryOperation
  {}

  /** Expressions at precedence level 5. */
  sealed interface Precedence05 extends Precedence06 {}

  /** Expression that yields a number that has the bit pattern that is the
   * AND'ed version of its operands matching bits. */
  record BitwiseAnd(Precedence05 left_operand, Precedence05 right_operand)
    implements Precedence05, BinaryOperation
  {}

  /** Expressions at precedence level 4. */
  sealed interface Precedence04 extends Precedence05 {}

  /** Expression that yields the left operand's bits shifted left by right
   * operand when the operands are taken as 53-bit signed two's complement
   * integers. */
  record LeftShift(Precedence04 left_operand, Precedence04 right_operand)
    implements Precedence04, BinaryOperation
  {}

  /** Expression that yields the left operand's bits shifted right by right
   * operand when the operands are taken as 53-bit signed two's complement
   * integers. */
  record RightShift(Precedence04 left_operand, Precedence04 right_operand)
    implements Precedence04, BinaryOperation
  {}

  /** Expressions at precedence level 3. */
  sealed interface Precedence03 extends Precedence04 {}

  /** Expression that yields the addition of its operands. */
  record Addition(Precedence03 left_operand, Precedence03 right_operand)
    implements Precedence03, BinaryOperation
  {}

  /** Expression that yields the subtraction of its operands. */
  record Subtraction(Precedence03 left_operand, Precedence03 right_operand)
    implements Precedence03, BinaryOperation
  {}

  /** Expressions at precedence level 2. */
  sealed interface Precedence02 extends Precedence03 {}

  /** Expression that yields the multiplication of its operands. */
  record Multiplication(Precedence02 left_operand, Precedence02 right_operand)
    implements Precedence02, BinaryOperation
  {}

  /** Expression that yields the division of its operands. */
  record Division(Precedence02 left_operand, Precedence02 right_operand)
    implements Precedence02, BinaryOperation
  {}

  /** Expression that yields the floor of the division of its operands. */
  record IntegerDivision(Precedence02 left_operand, Precedence02 right_operand)
    implements Precedence02, BinaryOperation
  {}

  /** Expression that yields the left operand in mod right operand. */
  record Modulus(Precedence02 left_operand, Precedence02 right_operand)
    implements Precedence02, BinaryOperation
  {}

  /** Expressions at precedence level 1. */
  sealed interface Precedence01 extends Precedence02 {}

  /** Expression that keeps the sign of a number. */
  record Promotion(Precedence01 operand)
    implements Precedence01, UnaryOperation
  {}

  /** Expression that flips the sign of a number. */
  record Negation(Precedence01 operand)
    implements Precedence01, UnaryOperation
  {}

  /** Expression that flips every bit when the value is taken as an 53-bit
   * signed two's complement integer. */
  record BitwiseNot(Precedence01 operand)
    implements Precedence01, UnaryOperation
  {}

  /** Expression that yields one when the operand is zero, and zero
   * otherwise. */
  record LogicalNot(Precedence01 operand)
    implements Precedence01, UnaryOperation
  {}

  /** Expressions at precedence level 0. */
  sealed interface Precedence00 extends Precedence01 {}

  /** Expression that enforces a specific order of evaluation for
   * subexpressions. */
  record Grouping(Expression grouped) implements Precedence00 {
    @Override
    public int first(List<Token> tokens) { return grouped.first(tokens) - 1; }

    @Override
    public int last(List<Token> tokens) { return grouped.last(tokens) + 1; }
  }

  /** Expression that start with a symbol. */
  sealed interface SymbolBased extends Precedence00 {}

  /** Expression that denotes the value held by a symbol. */
  record SymbolAccess(Mention accessed) implements SymbolBased {
    @Override
    public int first(List<Token> tokens) { return accessed.first(tokens); }

    @Override
    public int last(List<Token> tokens) { return accessed.last(tokens); }
  }

  /** Expression that calls a procedure. */
  record Call(Mention called, List<Expression> arguments)
    implements SymbolBased
  {
    @Override
    public int first(List<Token> tokens) { return called.first(tokens); }

    @Override
    public int last(List<Token> tokens) {
      if (!arguments.isEmpty()) {
        return arguments.get(arguments.size() - 1).last(tokens) + 1;
      }
      return called.first(tokens) + 2;
    }
  }

  /** Expression that senses a property of an object. */
  record MemberAccess(Precedence00 object, Token.Identifier member)
    implements Precedence00
  {
    @Override
    public int first(List<Token> tokens) { return object.first(tokens); }

    @Override
    public int last(List<Token> tokens) { return tokens.indexOf(member); }
  }

  /** Expression that calls a procedure by passing the first argument at the
   * beginning as if the procedure was a member of the first argument. */
  record MemberCall(
    Precedence00 first_argument,
    Token.Identifier called,
    List<Expression> remaining_arguments) implements Precedence00
  {
    @Override
    public int first(List<Token> tokens) {
      return first_argument.first(tokens);
    }

    @Override
    public int last(List<Token> tokens) {
      if (!remaining_arguments.isEmpty()) {
        return remaining_arguments
          .get(remaining_arguments.size() - 1)
          .last(tokens)
          + 1;
      }
      return first_argument.last(tokens) + 4;
    }
  }

  /** Expression that directly denotes a compile-time known number value. */
  record NumberConstant(int first, double value) implements Precedence00 {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) { return first; }
  }

  /** Expression that directly denotes a compile-time known color value. */
  record ColorConstant(int first, int value) implements Precedence00 {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) { return first; }
  }

  /** Expression that directly denotes a compile-time known string value. */
  record StringConstant(int first, String value) implements Precedence00 {
    @Override
    public int first(List<Token> tokens) { return first; }

    @Override
    public int last(List<Token> tokens) { return first; }
  }

  /** Mentioning a symbol by its scope and identifier. */
  record Mention(Optional<Token.Identifier> source, Token.Identifier identifier)
    implements Node
  {
    @Override
    public int first(List<Token> tokens) {
      if (source.isPresent()) { return tokens.indexOf(source.get()); }
      return tokens.indexOf(identifier);
    }

    @Override
    public int last(List<Token> tokens) { return tokens.indexOf(identifier); }
  }

  /** Index of the node's first token. Used for reporting diagnostics with a
   * source location. */
  int first(List<Token> tokens);

  /** Index of the node's last token. Used for reporting diagnostics with a
   * source location. */
  int last(List<Token> tokens);

  /** Index of the node's first character's first byte from the beginning of the
   * file. Used for reporting diagnostics with a source location. */
  default int start(List<Token> tokens) {
    return tokens.get(first(tokens)).start();
  }

  /** Index of the first byte of the character after the node's last one from
   * the beginning of the file. Used for reporting diagnostics with a source
   * location. */
  default int end(List<Token> tokens) {
    return tokens.get(last(tokens)).end();
  }

  /** Returns the text of the node as it was in the source. */
  default String text(String contents, List<Token> tokens) {
    return contents.substring(start(tokens), end(tokens));
  }
}
