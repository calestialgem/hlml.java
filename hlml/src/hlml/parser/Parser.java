package hlml.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import hlml.lexer.LexedSource;
import hlml.lexer.Token;

/** Transforms tokens to a list of declarations. */
public final class Parser {
  /** Parses a source file. Returns the declarations in the file. */
  public static ParsedSource parse(LexedSource source) {
    Parser parser = new Parser(source);
    return parser.parse();
  }

  /** Source file that is parsed. */
  private LexedSource source;

  /** Index of the currently parsed token. */
  private int current;

  /** Constructor. */
  private Parser(LexedSource source) {
    this.source = source;
  }

  /** Parses the source file. */
  private ParsedSource parse() {
    current = 0;
    List<Node.Declaration> declarations = new ArrayList<Node.Declaration>();
    while (current != source.tokens.size()) {
      Node.Declaration declaration =
        expect(this::parse_declaration, "top level declaration");
      declarations.add(declaration);
    }
    return new ParsedSource(source, declarations);
  }

  /** Parses a declaration. */
  private Optional<Node.Declaration> parse_declaration() {
    return first_of(this::parse_entrypoint, this::parse_definition);
  }

  /** Parses a entrypoint. */
  private Optional<Node.Entrypoint> parse_entrypoint() {
    if (parse_token(Token.Entrypoint.class).isEmpty()) {
      return Optional.empty();
    }
    Node.Statement body =
      expect(this::parse_block, "body of the entrypoint declaration");
    Node.Entrypoint entrypoint = new Node.Entrypoint(body);
    return Optional.of(entrypoint);
  }

  /** Parses a definition. */
  private Optional<Node.Definition> parse_definition() {
    Optional<Token.Public> modifier = parse_token(Token.Public.class);
    if (modifier.isPresent()) {
      return Optional
        .of(
          expect(
            () -> first_of(
              () -> parse_link(modifier),
              () -> parse_using(modifier),
              () -> parse_proc(modifier),
              () -> parse_const(modifier),
              () -> parse_global_var(modifier)),
            "a definition"));
    }
    return first_of(
      () -> parse_link(modifier),
      () -> parse_using(modifier),
      () -> parse_proc(modifier),
      () -> parse_const(modifier),
      () -> parse_global_var(modifier));
  }

  /** Parses a link. */
  private Optional<Node.Link> parse_link(Optional<Token.Public> modifier) {
    if (parse_token(Token.Link.class).isEmpty()) { return Optional.empty(); }
    Token.Identifier building =
      expect_token(
        Token.Identifier.class,
        "building name of the link definition");
    Optional<Token.Identifier> alias = Optional.empty();
    if (parse_token(Token.As.class).isPresent()) {
      alias =
        Optional
          .of(
            expect_token(
              Token.Identifier.class,
              "alias identifier of the link definition"));
    }
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the alias definition");
    Node.Link link = new Node.Link(modifier, building, alias);
    return Optional.of(link);
  }

  /** Parses a using. */
  private Optional<Node.Using> parse_using(Optional<Token.Public> modifier) {
    if (parse_token(Token.Using.class).isEmpty()) { return Optional.empty(); }
    Node.Mention used =
      expect(this::parse_mention, "mention to used of the alias definition");
    Optional<Token.Identifier> alias = Optional.empty();
    if (parse_token(Token.As.class).isPresent()) {
      alias =
        Optional
          .of(
            expect_token(
              Token.Identifier.class,
              "alias identifier of the alias definition"));
    }
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the alias definition");
    Node.Using using = new Node.Using(modifier, used, alias);
    return Optional.of(using);
  }

  /** Parses a proc. */
  private Optional<Node.Proc> parse_proc(Optional<Token.Public> modifier) {
    if (parse_token(Token.Proc.class).isEmpty()) { return Optional.empty(); }
    Token.Identifier identifier =
      expect_token(
        Token.Identifier.class,
        "identifier of the procedure declaration");
    expect_token(
      Token.OpeningParenthesis.class,
      "parameter list opener `(` of the procedure declaration");
    List<Node.Parameter> parameters = separated_of(this::parse_parameter);
    expect_token(
      Token.ClosingParenthesis.class,
      "parameter list closer `)` of the procedure declaration");
    Node.Statement body =
      expect(this::parse_block, "body of the procedure declaration");
    Node.Proc proc = new Node.Proc(modifier, identifier, parameters, body);
    return Optional.of(proc);
  }

  /** Parses a parameter. */
  private Optional<Node.Parameter> parse_parameter() {
    Optional<Token.Identifier> identifier = parse_token(Token.Identifier.class);
    if (identifier.isEmpty()) { return Optional.empty(); }
    boolean in_out = parse_token(Token.Ampersand.class).isPresent();
    Node.Parameter parameter = new Node.Parameter(identifier.get(), in_out);
    return Optional.of(parameter);
  }

  /** Parses a const. */
  private Optional<Node.Const> parse_const(Optional<Token.Public> modifier) {
    if (parse_token(Token.Const.class).isEmpty()) { return Optional.empty(); }
    Token.Identifier identifier =
      expect_token(
        Token.Identifier.class,
        "identifier of the constant declaration");
    expect_token(
      Token.Equal.class,
      "value separator `=` of the constant declaration");
    Node.Expression initial_value =
      expect(this::parse_expression, "value of the constant declaration");
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the constant declaration");
    Node.Const var = new Node.Const(modifier, identifier, initial_value);
    return Optional.of(var);
  }

  /** Parses a global variable. */
  private Optional<Node.GlobalVar> parse_global_var(
    Optional<Token.Public> modifier)
  {
    if (parse_token(Token.Var.class).isEmpty()) { return Optional.empty(); }
    Token.Identifier identifier =
      expect_token(
        Token.Identifier.class,
        "identifier of the global variable declaration");
    Optional<Node.Expression> initial_value = Optional.empty();
    if (parse_token(Token.Equal.class).isPresent()) {
      Node.Expression given_initial_value =
        expect(
          this::parse_expression,
          "initial value of the global variable declaration");
      initial_value = Optional.of(given_initial_value);
    }
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the global variable declaration");
    Node.GlobalVar var =
      new Node.GlobalVar(modifier, identifier, initial_value);
    return Optional.of(var);
  }

  /** Parses a statement. */
  private Optional<Node.Statement> parse_statement() {
    return first_of(
      this::parse_block,
      this::parse_if,
      this::parse_while,
      this::parse_break,
      this::parse_continue,
      this::parse_return,
      this::parse_local_var,
      this::parse_affect);
  }

  /** Parses a block. */
  private Optional<Node.Block> parse_block() {
    int first = current;
    if (parse_token(Token.OpeningBrace.class).isEmpty()) {
      return Optional.empty();
    }
    List<Node.Statement> body = repeats_of(this::parse_statement);
    expect_token(
      Token.ClosingBrace.class,
      "inner statement list closer `}` of the block statement");
    Node.Block block = new Node.Block(first, body);
    return Optional.of(block);
  }

  /** Parses an if statement. */
  private Optional<Node.If> parse_if() {
    if (parse_token(Token.If.class).isEmpty()) { return Optional.empty(); }
    List<Node.LocalVar> variables = repeats_of(this::parse_local_var);
    Node.Expression condition =
      expect(this::parse_expression, "condition of the if statement");
    Node.Statement true_branch =
      expect(this::parse_block, "true branch of the if statement");
    Optional<Node.Statement> false_branch = Optional.empty();
    if (parse_token(Token.Else.class).isPresent()) {
      false_branch =
        Optional
          .of(
            expect(
              () -> first_of(this::parse_block, this::parse_if),
              "false branch of the if statement"));
    }
    Node.If if_statement =
      new Node.If(variables, condition, true_branch, false_branch);
    return Optional.of(if_statement);
  }

  /** Parses a while statement. */
  private Optional<Node.While> parse_while() {
    int first = current;
    Optional<Token.Identifier> label = parse_token(Token.Identifier.class);
    if (label.isPresent() && parse_token(Token.Colon.class).isEmpty()
      || parse_token(Token.While.class).isEmpty())
    {
      current = first;
      return Optional.empty();
    }
    List<Node.LocalVar> variables = repeats_of(this::parse_local_var);
    Node.Expression condition =
      expect(this::parse_expression, "condition of the while statement");
    Optional<Node.Statement> interleaved = Optional.empty();
    if (parse_token(Token.Semicolon.class).isPresent()) {
      interleaved =
        Optional
          .of(
            expect(
              this::parse_unterminated_affect,
              "interleaved of the while statement"));
    }
    Node.Statement loop =
      expect(this::parse_statement, "loop of the while statement");
    Node.While while_statement =
      new Node.While(
        label,
        variables,
        condition,
        interleaved,
        loop);
    return Optional.of(while_statement);
  }

  /** Parses a break statement. */
  private Optional<Node.Break> parse_break() {
    int first = current;
    if (parse_token(Token.Break.class).isEmpty()) { return Optional.empty(); }
    Optional<Token.Identifier> label = parse_token(Token.Identifier.class);
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the break statement");
    Node.Break break_statement = new Node.Break(first, label);
    return Optional.of(break_statement);
  }

  /** Parses a continue statement. */
  private Optional<Node.Continue> parse_continue() {
    int first = current;
    if (parse_token(Token.Continue.class).isEmpty()) {
      return Optional.empty();
    }
    Optional<Token.Identifier> label = parse_token(Token.Identifier.class);
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the continue statement");
    Node.Continue continue_statement = new Node.Continue(first, label);
    return Optional.of(continue_statement);
  }

  /** Parses a return statement. */
  private Optional<Node.Return> parse_return() {
    int first = current;
    if (parse_token(Token.Return.class).isEmpty()) { return Optional.empty(); }
    Optional<Node.Expression> value = parse_expression();
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the return statement");
    Node.Return return_statement = new Node.Return(first, value);
    return Optional.of(return_statement);
  }

  /** Parses a local variable. */
  private Optional<Node.LocalVar> parse_local_var() {
    if (parse_token(Token.Var.class).isEmpty()) { return Optional.empty(); }
    Token.Identifier identifier =
      expect_token(
        Token.Identifier.class,
        "identifier of the local variable declaration");
    Optional<Node.Expression> initial_value = Optional.empty();
    if (parse_token(Token.Equal.class).isPresent()) {
      Node.Expression given_initial_value =
        expect(
          this::parse_expression,
          "initial value of the local variable declaration");
      initial_value = Optional.of(given_initial_value);
    }
    expect_token(
      Token.Semicolon.class,
      "terminator `;` of the local variable declaration");
    Node.LocalVar var = new Node.LocalVar(identifier, initial_value);
    return Optional.of(var);
  }

  /** Parses an affect statement. */
  private Optional<Node.Affect> parse_affect() {
    Optional<Node.Affect> affect = parse_unterminated_affect();
    if (affect.isPresent()) {
      expect_token(
        Token.Semicolon.class,
        "terminator `;` of the %s statement".formatted(switch (affect.get())
        {
          case Node.Increment i -> "increment";
          case Node.Decrement d -> "decrement";
          case Node.DirectlyAssign a -> "assign";
          case Node.MultiplyAssign a -> "multiply assign";
          case Node.DivideAssign a -> "divide assign";
          case Node.DivideIntegerAssign a -> "divide integer assign";
          case Node.ModulusAssign a -> "modulus assign";
          case Node.AddAssign a -> "add assign";
          case Node.SubtractAssign a -> "subtract assign";
          case Node.ShiftLeftAssign a -> "shift left assign";
          case Node.ShiftRightAssign a -> "shift right assign";
          case Node.AndBitwiseAssign a -> "and bitwise assign";
          case Node.XorBitwiseAssign a -> "xor bitwise assign";
          case Node.OrBitwiseAssign a -> "or bitwise assign";
          case Node.Discard d -> "discard";
        }));
    }
    return affect;
  }

  /** Parses an affect statement without a terminator. */
  private Optional<Node.Affect> parse_unterminated_affect() {
    Optional<Node.Expression> expression = parse_expression();
    if (expression.isEmpty()) { return Optional.empty(); }
    Node.Affect affect = new Node.Discard(expression.get());
    if (expression.get() instanceof Node.SymbolAccess target) {
      Optional<AssignmentParser> assignment_parser =
        parse_assignment_operator();
      if (assignment_parser.isPresent()) {
        Node.Expression source =
          expect(
            this::parse_expression,
            "source of the %s statement"
              .formatted(assignment_parser.get().name()));
        affect = assignment_parser.get().initializer().apply(target, source);
      }
      else
        if (parse_token(Token.PlusPlus.class).isPresent()) {
          affect = new Node.Increment(target);
        }
        else
          if (parse_token(Token.MinusMinus.class).isPresent()) {
            affect = new Node.Decrement(target);
          }
    }
    return Optional.of(affect);
  }

  /** Parses an assignment operator. */
  private Optional<AssignmentParser> parse_assignment_operator() {
    if (parse_token(Token.Equal.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.DirectlyAssign::new, "assign"));
    }
    if (parse_token(Token.StarEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.MultiplyAssign::new, "multiply assign"));
    }
    if (parse_token(Token.SlashEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.DivideAssign::new, "divide assign"));
    }
    if (parse_token(Token.SlashSlashEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(
            Node.DivideIntegerAssign::new,
            "divide integer assign"));
    }
    if (parse_token(Token.PercentEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.ModulusAssign::new, "modulus assign"));
    }
    if (parse_token(Token.PlusEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.AddAssign::new, "add assign"));
    }
    if (parse_token(Token.MinusEqual.class).isPresent()) {
      return Optional
        .of(new AssignmentParser(Node.SubtractAssign::new, "subtract assign"));
    }
    if (parse_token(Token.LeftLeftEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(Node.ShiftLeftAssign::new, "shift left assign"));
    }
    if (parse_token(Token.RightRightEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(
            Node.ShiftRightAssign::new,
            "shift right assign"));
    }
    if (parse_token(Token.AmpersandEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(
            Node.AndBitwiseAssign::new,
            "and bitwise assign"));
    }
    if (parse_token(Token.CaretEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(
            Node.XorBitwiseAssign::new,
            "xor bitwise assign"));
    }
    if (parse_token(Token.PipeEqual.class).isPresent()) {
      return Optional
        .of(
          new AssignmentParser(Node.OrBitwiseAssign::new, "or bitwise assign"));
    }
    return Optional.empty();
  }

  /** Parses an expression. */
  private Optional<Node.Expression> parse_expression() {
    return first_of(this::parse_precedence_11);
  }

  /** Parses an expression at precedence level 11. */
  private Optional<Node.Precedence11> parse_precedence_11() {
    return parse_binary_operations(
      this::parse_precedence_10,
      new BinaryOperationParser<>(
        Token.PipePipe.class,
        Node.LogicalOr::new,
        "logical or"));
  }

  /** Parses an expression at precedence level 10. */
  private Optional<Node.Precedence10> parse_precedence_10() {
    return parse_binary_operations(
      this::parse_precedence_09,
      new BinaryOperationParser<>(
        Token.AmpersandAmpersand.class,
        Node.LogicalAnd::new,
        "logical and"));
  }

  /** Parses an expression at precedence level 9. */
  private Optional<Node.Precedence09> parse_precedence_09() {
    return parse_binary_operations(
      this::parse_precedence_08,
      new BinaryOperationParser<>(
        Token.EqualEqual.class,
        Node.EqualTo::new,
        "equal to"),
      new BinaryOperationParser<>(
        Token.ExclamationEqual.class,
        Node.NotEqualTo::new,
        "not equal to"),
      new BinaryOperationParser<>(
        Token.EqualEqualEqual.class,
        Node.StrictlyEqualTo::new,
        "strictly equal to"));
  }

  /** Parses an expression at precedence level 8. */
  private Optional<Node.Precedence08> parse_precedence_08() {
    return parse_binary_operations(
      this::parse_precedence_07,
      new BinaryOperationParser<>(
        Token.Left.class,
        Node.LessThan::new,
        "less than"),
      new BinaryOperationParser<>(
        Token.LeftEqual.class,
        Node.LessThanOrEqualTo::new,
        "less than or equal to"),
      new BinaryOperationParser<>(
        Token.Right.class,
        Node.GreaterThan::new,
        "greater than"),
      new BinaryOperationParser<>(
        Token.RightEqual.class,
        Node.GreaterThanOrEqualTo::new,
        "greater than or equal to"));
  }

  /** Parses an expression at precedence level 7. */
  private Optional<Node.Precedence07> parse_precedence_07() {
    return parse_binary_operations(
      this::parse_precedence_06,
      new BinaryOperationParser<>(
        Token.Pipe.class,
        Node.BitwiseOr::new,
        "bitwise or"));
  }

  /** Parses an expression at precedence level 6. */
  private Optional<Node.Precedence06> parse_precedence_06() {
    return parse_binary_operations(
      this::parse_precedence_05,
      new BinaryOperationParser<>(
        Token.Caret.class,
        Node.BitwiseXor::new,
        "bitwise xor"));
  }

  /** Parses an expression at precedence level 5. */
  private Optional<Node.Precedence05> parse_precedence_05() {
    return parse_binary_operations(
      this::parse_precedence_04,
      new BinaryOperationParser<>(
        Token.Ampersand.class,
        Node.BitwiseAnd::new,
        "bitwise and"));
  }

  /** Parses an expression at precedence level 4. */
  private Optional<Node.Precedence04> parse_precedence_04() {
    return parse_binary_operations(
      this::parse_precedence_03,
      new BinaryOperationParser<>(
        Token.LeftLeft.class,
        Node.LeftShift::new,
        "left shift"),
      new BinaryOperationParser<>(
        Token.RightRight.class,
        Node.RightShift::new,
        "right shift"));
  }

  /** Parses an expression at precedence level 3. */
  private Optional<Node.Precedence03> parse_precedence_03() {
    return parse_binary_operations(
      this::parse_precedence_02,
      new BinaryOperationParser<>(
        Token.Plus.class,
        Node.Addition::new,
        "addition"),
      new BinaryOperationParser<>(
        Token.Minus.class,
        Node.Subtraction::new,
        "subtraction"));
  }

  /** Parses an expression at precedence level 2. */
  private Optional<Node.Precedence02> parse_precedence_02() {
    return parse_binary_operations(
      this::parse_precedence_01,
      new BinaryOperationParser<>(
        Token.Star.class,
        Node.Multiplication::new,
        "multiplication"),
      new BinaryOperationParser<>(
        Token.Slash.class,
        Node.Division::new,
        "division"),
      new BinaryOperationParser<>(
        Token.SlashSlash.class,
        Node.IntegerDivision::new,
        "integer division"),
      new BinaryOperationParser<>(
        Token.Percent.class,
        Node.Modulus::new,
        "modulus"));
  }

  /** Parses a group of binary operators in the same precedence level from left
   * to right. */
  @SafeVarargs
  private <PrecedenceType extends Node.Expression, OperandType extends PrecedenceType> Optional<PrecedenceType> parse_binary_operations(
    Supplier<Optional<OperandType>> operand_parser_function,
    BinaryOperationParser<PrecedenceType>... binary_operation_parsers)
  {
    Optional<OperandType> first_operand = operand_parser_function.get();
    if (first_operand.isEmpty()) { return Optional.empty(); }
    PrecedenceType result = first_operand.get();
    left_to_right_same_precedence_operator_parsing: while (true) {
      for (BinaryOperationParser<PrecedenceType> binary_operation_parser : binary_operation_parsers) {
        if (parse_token(binary_operation_parser.operator_class()).isEmpty()) {
          continue;
        }
        OperandType right_operand =
          expect(
            operand_parser_function,
            "right operand of %s expression"
              .formatted(binary_operation_parser.name()));
        result =
          binary_operation_parser.initializer().apply(result, right_operand);
        continue left_to_right_same_precedence_operator_parsing;
      }
      break;
    }
    return Optional.of(result);
  }

  /** Parses an expression at precedence level 1. */
  private Optional<Node.Precedence01> parse_precedence_01() {
    return parse_unary_operations(
      this::parse_precedence_00,
      new UnaryOperationParser<>(
        Token.Plus.class,
        Node.Promotion::new,
        "promotion"),
      new UnaryOperationParser<>(
        Token.Minus.class,
        Node.Negation::new,
        "negation"),
      new UnaryOperationParser<>(
        Token.Tilde.class,
        Node.BitwiseNot::new,
        "bitwise not"),
      new UnaryOperationParser<>(
        Token.Exclamation.class,
        Node.LogicalNot::new,
        "logical not"));
  }

  /** Parses a group of unary operators in the same precedence level from right
   * to left. */
  @SafeVarargs
  private <PrecedenceType extends Node.Expression, OperandType extends PrecedenceType> Optional<PrecedenceType> parse_unary_operations(
    Supplier<Optional<OperandType>> operand_parser_function,
    UnaryOperationParser<PrecedenceType>... unary_operation_parsers)
  {
    List<UnaryOperationParser<PrecedenceType>> stack = new ArrayList<>();
    left_to_right_same_precedence_operator_parsing: while (true) {
      for (UnaryOperationParser<PrecedenceType> unary_operation_parser : unary_operation_parsers) {
        if (parse_token(unary_operation_parser.operator_class()).isEmpty()) {
          continue;
        }
        stack.add(unary_operation_parser);
        continue left_to_right_same_precedence_operator_parsing;
      }
      break;
    }
    if (stack.isEmpty()) {
      return operand_parser_function.get().map(Function.identity());
    }
    PrecedenceType result =
      expect(
        operand_parser_function,
        "operand of %s expression"
          .formatted(stack.get(stack.size() - 1).name()));
    for (int i = stack.size(); i != 0; i--) {
      result = stack.get(i - 1).initializer().apply(result);
    }
    return Optional.of(result);
  }

  /** Parses an expression at precedence level 0. */
  private Optional<Node.Precedence00> parse_precedence_00() {
    Optional<Node.Precedence00> precedence_0 =
      first_of(
        this::parse_grouping,
        this::parse_symbol_based,
        this::parse_number_constant,
        this::parse_color_constant,
        this::parse_string_constant);
    if (precedence_0.isEmpty()) { return precedence_0; }
    Node.Precedence00 result = precedence_0.get();
    while (true) {
      if (parse_token(Token.Dot.class).isEmpty()) { break; }
      Token.Identifier member =
        expect_token(
          Token.Identifier.class,
          "member name in the member access expression");
      if (parse_token(Token.OpeningParenthesis.class).isPresent()) {
        List<Node.Expression> remaining_arguments =
          separated_of(this::parse_expression);
        expect_token(
          Token.ClosingParenthesis.class,
          "remaining argument list closer `)` of the member call expression");
        result = new Node.MemberCall(result, member, remaining_arguments);
        continue;
      }
      result = new Node.MemberAccess(result, member);
    }
    return Optional.of(result);
  }

  /** Parses a grouping. */
  private Optional<Node.Grouping> parse_grouping() {
    if (parse_token(Token.OpeningParenthesis.class).isEmpty()) {
      return Optional.empty();
    }
    Node.Expression grouped =
      expect(
        this::parse_expression,
        "grouped expression of the grouping expression");
    expect_token(
      Token.ClosingParenthesis.class,
      "closer `)` of the grouping expression");
    Node.Grouping grouping = new Node.Grouping(grouped);
    return Optional.of(grouping);
  }

  /** Parses a symbol based. */
  private Optional<Node.SymbolBased> parse_symbol_based() {
    Optional<Node.Mention> mention = parse_mention();
    if (mention.isEmpty()) { return Optional.empty(); }
    Node.SymbolBased symbol_based = new Node.SymbolAccess(mention.get());
    if (parse_token(Token.OpeningParenthesis.class).isPresent()) {
      List<Node.Expression> arguments = separated_of(this::parse_expression);
      expect_token(
        Token.ClosingParenthesis.class,
        "argument list closer `)` of the call expression");
      symbol_based = new Node.Call(mention.get(), arguments);
    }
    return Optional.of(symbol_based);
  }

  /** Parses a number constant. */
  private Optional<Node.NumberConstant> parse_number_constant() {
    int first = current;
    Optional<Token.NumberConstant> token =
      parse_token(Token.NumberConstant.class);
    if (token.isEmpty()) { return Optional.empty(); }
    Node.NumberConstant number_constant =
      new Node.NumberConstant(first, token.get().value());
    return Optional.of(number_constant);
  }

  /** Parses a color constant. */
  private Optional<Node.ColorConstant> parse_color_constant() {
    int first = current;
    Optional<Token.ColorConstant> token =
      parse_token(Token.ColorConstant.class);
    if (token.isEmpty()) { return Optional.empty(); }
    Node.ColorConstant color_constant =
      new Node.ColorConstant(first, token.get().value());
    return Optional.of(color_constant);
  }

  /** Parses a string constant. */
  private Optional<Node.StringConstant> parse_string_constant() {
    int first = current;
    Optional<Token.StringConstant> token =
      parse_token(Token.StringConstant.class);
    if (token.isEmpty()) { return Optional.empty(); }
    Node.StringConstant string_constant =
      new Node.StringConstant(first, token.get().value());
    return Optional.of(string_constant);
  }

  /** Parses a mention. */
  private Optional<Node.Mention> parse_mention() {
    Optional<Token.Identifier> scope = parse_scope();
    if (scope.isPresent()) {
      Token.Identifier identifier =
        expect_token(
          Token.Identifier.class,
          "identifier of the qualified mention");
      Node.Mention mention = new Node.Mention(scope, identifier);
      return Optional.of(mention);
    }
    Optional<Token.Identifier> identifier = parse_token(Token.Identifier.class);
    if (identifier.isEmpty()) { return Optional.empty(); }
    Node.Mention mention = new Node.Mention(scope, identifier.get());
    return Optional.of(mention);
  }

  /** Parses a scope, which is the name of a source fallowed by `::`. */
  private Optional<Token.Identifier> parse_scope() {
    int first = current;
    Optional<Token.Identifier> scope = parse_token(Token.Identifier.class);
    if (scope.isEmpty()) { return Optional.empty(); }
    if (parse_token(Token.ColonColon.class).isEmpty()) {
      current = first;
      return Optional.empty();
    }
    return scope.map(Function.identity());
  }

  /** Runs the given parser repeatedly and collects the parsed constructs as a
   * list. Parses a separator comma between the constructs. Optionally, there
   * could be a trailing comma. */
  private <ConstructType> List<ConstructType> separated_of(
    Supplier<Optional<ConstructType>> parser_function)
  {
    List<ConstructType> constructs = new ArrayList<>();
    while (true) {
      Optional<ConstructType> construct = parser_function.get();
      if (construct.isEmpty()) { break; }
      constructs.add(construct.get());
      if (parse_token(Token.Comma.class).isEmpty()) { break; }
    }
    return constructs;
  }

  /** Runs the given parser repeatedly and collects the parsed constructs as a
   * list. */
  private <ConstructType> List<ConstructType> repeats_of(
    Supplier<Optional<ConstructType>> parser_function)
  {
    List<ConstructType> constructs = new ArrayList<>();
    while (true) {
      Optional<ConstructType> construct = parser_function.get();
      if (construct.isEmpty()) { break; }
      constructs.add(construct.get());
    }
    return constructs;
  }

  /** Returns the first construct that successfully parsed out of the given
   * parsers. If all fail fails. */
  @SafeVarargs
  private <ConstructType> Optional<ConstructType> first_of(
    Supplier<Optional<? extends ConstructType>>... parser_functions)
  {
    for (Supplier<Optional<? extends ConstructType>> parser_function : parser_functions) {
      Optional<? extends ConstructType> construct = parser_function.get();
      if (construct.isPresent()) { return Optional.of(construct.get()); }
    }
    return Optional.empty();
  }

  /** Ensures that the given token parses. Otherwise throws a diagnostic with
   * the given explanation. Returns the parsed token. */
  private <TokenType extends Token> TokenType expect_token(
    Class<TokenType> token_class,
    String token_explanation)
  {
    return expect(() -> parse_token(token_class), token_explanation);
  }

  /** Parses the next token if it exists and it is of the given class. */
  @SuppressWarnings("unchecked")
  private <TokenType extends Token> Optional<TokenType> parse_token(
    Class<TokenType> token_class)
  {
    if (current == source.tokens.size()) { return Optional.empty(); }
    Token token = source.tokens.get(current);
    if (!token_class.isInstance(token)) { return Optional.empty(); }
    current++;
    return Optional.of((TokenType) token);
  }

  /** Ensures that the given parser parses. Otherwise throws a diagnostic with
   * the given explanation. Returns the parsed construct. */
  private <ConstructType> ConstructType expect(
    Supplier<Optional<ConstructType>> parse_function,
    String construct_explanation)
  {
    Optional<ConstructType> construct = parse_function.get();
    if (construct.isPresent()) { return construct.get(); }
    if (current == source.tokens.size()) {
      Token reported_token = source.tokens.get(current - 1);
      throw source
        .subject(reported_token)
        .to_diagnostic(
          "error",
          "Expected %s at the end of the file after %s!",
          construct_explanation,
          reported_token.explanation())
        .to_exception();
    }
    Token reported_token = source.tokens.get(current);
    throw source
      .subject(reported_token)
      .to_diagnostic(
        "error",
        "Expected %s instead of %s!",
        construct_explanation,
        reported_token.explanation())
      .to_exception();
  }
}
