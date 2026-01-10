public record Redirection(String commandPart, String target, Type type) {

  enum Type {
    NONE,
    STDOUT,
    STDERR
  }

  public static Redirection none(String cmd) {
    return new Redirection(cmd, null, Type.NONE);
  }

  public boolean isStdout() {
    return type == Type.STDOUT;
  }

  public boolean isStderr() {
    return type == Type.STDERR;
  }

  public boolean hasRedirection(){
    return isStdout() || isStderr();
  }
}
