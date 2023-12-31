package hlml.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Holder of local definitions. */
final class Scope {
  /** Creates an empty scope. */
  static Scope create() {
    return new Scope(new ArrayList<>(), 0);
  }

  /** Ordered collection of locals in the parent scopes, this scope and the
   * child scopes. */
  private final List<Semantic.LocalVar> locals;

  /** Number of defined locals excluding the ones defined in the child
   * scopes. */
  private int last;

  /** Constructor. */
  private Scope(List<Semantic.LocalVar> locals, int last) {
    this.locals = locals;
    this.last = last;
  }

  /** Finds the last local with the matching identifier. */
  Optional<Semantic.LocalVar> find(String identifier) {
    for (int i = last; i != 0; i--) {
      Semantic.LocalVar local = locals.get(i - 1);
      if (local.identifier().equals(identifier)) { return Optional.of(local); }
    }
    return Optional.empty();
  }

  /** Introduces a new local to the scope. Invalidates all the child scopes! */
  void introduce(Semantic.LocalVar local) {
    while (locals.size() > last) { locals.remove(locals.size() - 1); }
    locals.add(local);
    last++;
  }

  /** Creates a new child scope, which cannot invalidate this scope and gets
   * invalidated when a new local is introduced to the parent scope. */
  Scope create_child() {
    return new Scope(locals, last);
  }
}
