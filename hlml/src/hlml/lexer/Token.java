package hlml.lexer;

import hlml.FloatingPointFormatter;
import hlml.PackedColorFormatter;

/** Longest sequence of characters that make up a meaningful word in hlml. */
public sealed interface Token {
  /** Keyword `entrypoint`. */
  record Entrypoint(int start) implements Token {
    @Override
    public int end() { return start + "entrypoint".length(); }

    @Override
    public String explanation() { return "keyword `entrypoint`"; }
  }

  /** Keyword `public`. */
  record Public(int start) implements Token {
    @Override
    public int end() { return start + "public".length(); }

    @Override
    public String explanation() { return "keyword `public`"; }
  }

  /** Keyword `link`. */
  record Link(int start) implements Token {
    @Override
    public int end() { return start + "link".length(); }

    @Override
    public String explanation() { return "keyword `link`"; }
  }

  /** Keyword `using`. */
  record Using(int start) implements Token {
    @Override
    public int end() { return start + "using".length(); }

    @Override
    public String explanation() { return "keyword `using`"; }
  }

  /** Keyword `as`. */
  record As(int start) implements Token {
    @Override
    public int end() { return start + "as".length(); }

    @Override
    public String explanation() { return "keyword `as`"; }
  }

  /** Keyword `proc`. */
  record Proc(int start) implements Token {
    @Override
    public int end() { return start + "proc".length(); }

    @Override
    public String explanation() { return "keyword `proc`"; }
  }

  /** Keyword `const`. */
  record Const(int start) implements Token {
    @Override
    public int end() { return start + "const".length(); }

    @Override
    public String explanation() { return "keyword `const`"; }
  }

  /** Keyword `var`. */
  record Var(int start) implements Token {
    @Override
    public int end() { return start + "var".length(); }

    @Override
    public String explanation() { return "keyword `var`"; }
  }

  /** Keyword `if`. */
  record If(int start) implements Token {
    @Override
    public int end() { return start + "if".length(); }

    @Override
    public String explanation() { return "keyword `if`"; }
  }

  /** Keyword `else`. */
  record Else(int start) implements Token {
    @Override
    public int end() { return start + "else".length(); }

    @Override
    public String explanation() { return "keyword `else`"; }
  }

  /** Keyword `while`. */
  record While(int start) implements Token {
    @Override
    public int end() { return start + "while".length(); }

    @Override
    public String explanation() { return "keyword `while`"; }
  }

  /** Keyword `break`. */
  record Break(int start) implements Token {
    @Override
    public int end() { return start + "break".length(); }

    @Override
    public String explanation() { return "keyword `break`"; }
  }

  /** Keyword `continue`. */
  record Continue(int start) implements Token {
    @Override
    public int end() { return start + "continue".length(); }

    @Override
    public String explanation() { return "keyword `continue`"; }
  }

  /** Keyword `return`. */
  record Return(int start) implements Token {
    @Override
    public int end() { return start + "return".length(); }

    @Override
    public String explanation() { return "keyword `return`"; }
  }

  /** Punctuation `{`. */
  record OpeningBrace(int start) implements Token {
    @Override
    public int end() { return start + "{".length(); }

    @Override
    public String explanation() { return "punctuation `{`"; }
  }

  /** Punctuation `}`. */
  record ClosingBrace(int start) implements Token {
    @Override
    public int end() { return start + "}".length(); }

    @Override
    public String explanation() { return "punctuation `}`"; }
  }

  /** Punctuation `(`. */
  record OpeningParenthesis(int start) implements Token {
    @Override
    public int end() { return start + "(".length(); }

    @Override
    public String explanation() { return "punctuation `(`"; }
  }

  /** Punctuation `)`. */
  record ClosingParenthesis(int start) implements Token {
    @Override
    public int end() { return start + ")".length(); }

    @Override
    public String explanation() { return "punctuation `)`"; }
  }

  /** Punctuation `;`. */
  record Semicolon(int start) implements Token {
    @Override
    public int end() { return start + ";".length(); }

    @Override
    public String explanation() { return "punctuation `;`"; }
  }

  /** Punctuation `.`. */
  record Dot(int start) implements Token {
    @Override
    public int end() { return start + ".".length(); }

    @Override
    public String explanation() { return "punctuation `.`"; }
  }

  /** Punctuation `,`. */
  record Comma(int start) implements Token {
    @Override
    public int end() { return start + ",".length(); }

    @Override
    public String explanation() { return "punctuation `,`"; }
  }

  /** Punctuation `~`. */
  record Tilde(int start) implements Token {
    @Override
    public int end() { return start + "~".length(); }

    @Override
    public String explanation() { return "punctuation `~`"; }
  }

  /** Punctuation `:`. */
  record Colon(int start) implements Token {
    @Override
    public int end() { return start + ":".length(); }

    @Override
    public String explanation() { return "punctuation `:`"; }
  }

  /** Punctuation `::`. */
  record ColonColon(int start) implements Token {
    @Override
    public int end() { return start + "::".length(); }

    @Override
    public String explanation() { return "punctuation `::`"; }
  }

  /** Punctuation `*`. */
  record Star(int start) implements Token {
    @Override
    public int end() { return start + "*".length(); }

    @Override
    public String explanation() { return "punctuation `*`"; }
  }

  /** Punctuation `*=`. */
  record StarEqual(int start) implements Token {
    @Override
    public int end() { return start + "*=".length(); }

    @Override
    public String explanation() { return "punctuation `*=`"; }
  }

  /** Punctuation `%`. */
  record Percent(int start) implements Token {
    @Override
    public int end() { return start + "%".length(); }

    @Override
    public String explanation() { return "punctuation `%`"; }
  }

  /** Punctuation `%=`. */
  record PercentEqual(int start) implements Token {
    @Override
    public int end() { return start + "%=".length(); }

    @Override
    public String explanation() { return "punctuation `%=`"; }
  }

  /** Punctuation `^`. */
  record Caret(int start) implements Token {
    @Override
    public int end() { return start + "^".length(); }

    @Override
    public String explanation() { return "punctuation `^`"; }
  }

  /** Punctuation `^=`. */
  record CaretEqual(int start) implements Token {
    @Override
    public int end() { return start + "^=".length(); }

    @Override
    public String explanation() { return "punctuation `^=`"; }
  }

  /** Punctuation `!`. */
  record Exclamation(int start) implements Token {
    @Override
    public int end() { return start + "!".length(); }

    @Override
    public String explanation() { return "punctuation `!`"; }
  }

  /** Punctuation `!=`. */
  record ExclamationEqual(int start) implements Token {
    @Override
    public int end() { return start + "!=".length(); }

    @Override
    public String explanation() { return "punctuation `!=`"; }
  }

  /** Punctuation `+`. */
  record Plus(int start) implements Token {
    @Override
    public int end() { return start + "+".length(); }

    @Override
    public String explanation() { return "punctuation `+`"; }
  }

  /** Punctuation `++`. */
  record PlusPlus(int start) implements Token {
    @Override
    public int end() { return start + "++".length(); }

    @Override
    public String explanation() { return "punctuation `++`"; }
  }

  /** Punctuation `+=`. */
  record PlusEqual(int start) implements Token {
    @Override
    public int end() { return start + "+=".length(); }

    @Override
    public String explanation() { return "punctuation `+=`"; }
  }

  /** Punctuation `-`. */
  record Minus(int start) implements Token {
    @Override
    public int end() { return start + "-".length(); }

    @Override
    public String explanation() { return "punctuation `-`"; }
  }

  /** Punctuation `--`. */
  record MinusMinus(int start) implements Token {
    @Override
    public int end() { return start + "--".length(); }

    @Override
    public String explanation() { return "punctuation `--`"; }
  }

  /** Punctuation `-=`. */
  record MinusEqual(int start) implements Token {
    @Override
    public int end() { return start + "-=".length(); }

    @Override
    public String explanation() { return "punctuation `-=`"; }
  }

  /** Punctuation `&`. */
  record Ampersand(int start) implements Token {
    @Override
    public int end() { return start + "&".length(); }

    @Override
    public String explanation() { return "punctuation `&`"; }
  }

  /** Punctuation `&&`. */
  record AmpersandAmpersand(int start) implements Token {
    @Override
    public int end() { return start + "&&".length(); }

    @Override
    public String explanation() { return "punctuation `&&`"; }
  }

  /** Punctuation `&=`. */
  record AmpersandEqual(int start) implements Token {
    @Override
    public int end() { return start + "&=".length(); }

    @Override
    public String explanation() { return "punctuation `&=`"; }
  }

  /** Punctuation `|`. */
  record Pipe(int start) implements Token {
    @Override
    public int end() { return start + "|".length(); }

    @Override
    public String explanation() { return "punctuation `|`"; }
  }

  /** Punctuation `||`. */
  record PipePipe(int start) implements Token {
    @Override
    public int end() { return start + "||".length(); }

    @Override
    public String explanation() { return "punctuation `||`"; }
  }

  /** Punctuation `|=`. */
  record PipeEqual(int start) implements Token {
    @Override
    public int end() { return start + "|=".length(); }

    @Override
    public String explanation() { return "punctuation `|=`"; }
  }

  /** Punctuation `/`. */
  record Slash(int start) implements Token {
    @Override
    public int end() { return start + "/".length(); }

    @Override
    public String explanation() { return "punctuation `/`"; }
  }

  /** Punctuation `//`. */
  record SlashSlash(int start) implements Token {
    @Override
    public int end() { return start + "//".length(); }

    @Override
    public String explanation() { return "punctuation `//`"; }
  }

  /** Punctuation `/=`. */
  record SlashEqual(int start) implements Token {
    @Override
    public int end() { return start + "/=".length(); }

    @Override
    public String explanation() { return "punctuation `/=`"; }
  }

  /** Punctuation `//=`. */
  record SlashSlashEqual(int start) implements Token {
    @Override
    public int end() { return start + "//=".length(); }

    @Override
    public String explanation() { return "punctuation `//=`"; }
  }

  /** Punctuation `<`. */
  record Left(int start) implements Token {
    @Override
    public int end() { return start + "<".length(); }

    @Override
    public String explanation() { return "punctuation `<`"; }
  }

  /** Punctuation `<<`. */
  record LeftLeft(int start) implements Token {
    @Override
    public int end() { return start + "<<".length(); }

    @Override
    public String explanation() { return "punctuation `<<`"; }
  }

  /** Punctuation `<=`. */
  record LeftEqual(int start) implements Token {
    @Override
    public int end() { return start + "<=".length(); }

    @Override
    public String explanation() { return "punctuation `<=`"; }
  }

  /** Punctuation `<<=`. */
  record LeftLeftEqual(int start) implements Token {
    @Override
    public int end() { return start + "<<=".length(); }

    @Override
    public String explanation() { return "punctuation `<<=`"; }
  }

  /** Punctuation `>`. */
  record Right(int start) implements Token {
    @Override
    public int end() { return start + ">".length(); }

    @Override
    public String explanation() { return "punctuation `>`"; }
  }

  /** Punctuation `>>`. */
  record RightRight(int start) implements Token {
    @Override
    public int end() { return start + ">>".length(); }

    @Override
    public String explanation() { return "punctuation `>>`"; }
  }

  /** Punctuation `>=`. */
  record RightEqual(int start) implements Token {
    @Override
    public int end() { return start + ">=".length(); }

    @Override
    public String explanation() { return "punctuation `>=`"; }
  }

  /** Punctuation `>>=`. */
  record RightRightEqual(int start) implements Token {
    @Override
    public int end() { return start + ">>=".length(); }

    @Override
    public String explanation() { return "punctuation `>>=`"; }
  }

  /** Punctuation `=`. */
  record Equal(int start) implements Token {
    @Override
    public int end() { return start + "=".length(); }

    @Override
    public String explanation() { return "punctuation `=`"; }
  }

  /** Punctuation `==`. */
  record EqualEqual(int start) implements Token {
    @Override
    public int end() { return start + "==".length(); }

    @Override
    public String explanation() { return "punctuation `==`"; }
  }

  /** Punctuation `===`. */
  record EqualEqualEqual(int start) implements Token {
    @Override
    public int end() { return start + "===".length(); }

    @Override
    public String explanation() { return "punctuation `===`"; }
  }

  /** Any word that refers to a user-defined construct. */
  record Identifier(int start, String text) implements Token {
    @Override
    public int end() { return start() + text().length(); }

    @Override
    public String explanation() { return "identifier `%s`".formatted(text); }
  }

  /** A number constant. */
  record NumberConstant(int start, int end, double value) implements Token {
    @Override
    public String explanation() {
      return "number `%s`".formatted(FloatingPointFormatter.format(value));
    }

    @Override
    public String toString() {
      return "NumberConstant[start=%d, end=%d, value=%s]"
        .formatted(start, end, FloatingPointFormatter.format(value));
    }
  }

  /** A packed color constant. */
  record ColorConstant(int start, int end, int value) implements Token {
    @Override
    public String explanation() {
      return "color `0p%s`".formatted(PackedColorFormatter.format(value));
    }

    @Override
    public String toString() {
      return "ColorConstant[start=%d, end=%d, value=0p%s]"
        .formatted(start, end, PackedColorFormatter.format(value));
    }
  }

  /** A string constant. */
  record StringConstant(int start, int end, String value) implements Token {
    @Override
    public String explanation() { return "string `\"%s\"`".formatted(value); }
  }

  /** Index of the token's first character's first byte from the beginning of
   * the file. Used for reporting diagnostics with a source location. */
  int start();

  /** Index of the first byte of the character after the token's last one from
   * the beginning of the file. Used for reporting diagnostics with a source
   * location. */
  int end();

  /** Returns a text to report the token to the user. */
  String explanation();

  /** Returns the text of the token as it was in the source. */
  default String text(String contents) {
    return contents.substring(start(), end());
  }
}
