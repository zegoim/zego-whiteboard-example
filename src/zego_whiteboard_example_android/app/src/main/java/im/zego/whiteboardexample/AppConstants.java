package im.zego.whiteboardexample;

public class AppConstants {
    // RTC(express/liveroom) SDK 日志目录名
    // 该目录名为 RTC SDK、文件共享SDK、互动白板SDK 统一的默认保存目录名，不建议修改
    public static String RTC_LOG_SUBFOLDER = "zegologs";

    // RTC(express/liveroom) SDK 日志文件大小
    public static long RTC_LOG_SIZE = 5L * 1024 * 1024;

    // 思源字体路径
    public static String RECOMMEND_REGULAR_FONT_PATH = "fonts/SourceHanSansSC-Regular.otf";
    public static String RECOMMEND_BOLD_FONT_PATH = "fonts/SourceHanSansSC-Bold.otf";

    // 房间内纯白板的最大数量
    public static int MAX_PURE_WHITEBOARD_COUNT = 10;

    // 房间内文件白板的最大数量
    public static int MAX_FILE_WHITEBOARD_COUNT = 10;
}
