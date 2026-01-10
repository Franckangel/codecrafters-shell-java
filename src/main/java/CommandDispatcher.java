public class CommandDispatcher {

    private final BuiltinCommandHandler builtinHandler = new BuiltinCommandHandler();
    private final ExternalCommandExecutor externalExecutor = new ExternalCommandExecutor();

    public void dispatch(String input, Shell shell) throws Exception {
        if (builtinHandler.handle(input, shell)) {
            return;
        }
        externalExecutor.execute(input, shell);
    }
}
