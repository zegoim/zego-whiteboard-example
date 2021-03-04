package im.zego.whiteboardexample;

/**
 * 当您从ZEGO申请到 APP_ID 和 APP_SIGN 之后，我们强烈建议您将其通过服务器下发到APP，而不是保存在代码当中
 * 这里将其保存在代码当中，只是为了执行 Example
 *
 * APP_ID: 从官网或者技术支持获取，该参数为long型，需要在末尾添加 L，例如 long APP_ID = 12345678L;
 *
 * APP_SIGN: 从官网或者技术支持获取，该参数为String型，需要用双引号包围
 */
public class AuthConstants {

    public final static long APP_ID = YOUR_APP_ID;

    public final static String APP_SIGN = YOUR_APP_SIGN;

}