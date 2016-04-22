package test;

/** Indicates that a serious testing library error has occurred, such as failure
    to construct a test object, or failure to clean up system resources.

    <p>
    The testing library does not run any more tests after a fatal error has
    occurred. Ordinarily, if a regular <code>TestFailed</code> exception occurs,
    the testing library will still run additional tests that do not depend on
    the test that failed.
 */
class FatalError extends TestFailed
{
    public FatalError()
    {
      
    }
    /** Constructs a <code>FatalError</code> object from an error message. */
    public FatalError(String message)
    {
        super(message);
    }

    /** Constructs a <code>FatalError</code> object from an error message and a
        cause. */
    public FatalError(String message, Throwable cause)
    {
        super(message, cause);
    }
}
