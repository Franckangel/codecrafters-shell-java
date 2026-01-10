public class RedirectionParser {

    public static Redirection parse(String input) {
        boolean single = false, dbl = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '\'' && !dbl) single = !single;
            else if (c == '"' && !single) dbl = !dbl;

            else if (!single && !dbl) {
                if (input.startsWith("2>", i)) {
                    return new Redirection(input.substring(0, i), input.substring(i + 2).trim(), Redirection.Type.STDERR);
                }
                if (input.startsWith("1>", i) || c == '>') {
                    int offset = input.startsWith("1>", i) ? 2 : 1;
                    return new Redirection(input.substring(0, i), input.substring(i + offset).trim(), Redirection.Type.STDOUT);
                }
            }
        }
        return Redirection.none(input);
    }
}
