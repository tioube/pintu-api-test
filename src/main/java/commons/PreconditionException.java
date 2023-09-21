package commons;

public class PreconditionException extends RuntimeException {

    int skipTrace = 0;
    String errorMessage = "";

    public PreconditionException() {
    }

    public PreconditionException(int skipTrace) {
        this.skipTrace = skipTrace;
    }

    public PreconditionException(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public PreconditionException(String errorMessage, int skipTrace) {
        this.errorMessage = errorMessage;
        this.skipTrace = skipTrace;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PreconditionException: thrown by %s ",
                getStackTrace()[skipTrace].toString()));
        if (!errorMessage.equals("")) {
            sb.append(String.format("\nmessage:%s", errorMessage));
        }
        return sb.toString();
    }
}
