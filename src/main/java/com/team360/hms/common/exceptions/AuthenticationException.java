package common.exceptions;

public class AuthenticationException extends RuntimeException {

    public static final int FAILED = -100;
    public static final int EXPIRED = -101;
    private static final long serialVersionUID = 3355770949388484266L;
    int code;

    public AuthenticationException(int code, String message) {
        super(message);
        this.code = code;
    }

    public AuthenticationException(int code) {
        super("");
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
