//
//  ZegoWhiteboardServiceManager.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//

#import <Foundation/Foundation.h>
#import <ZegoWhiteboardView/ZegoWhiteboardDefine.h>
#import <ZegoWhiteboardView/ZegoWhiteboardView.h>
#import "ZegoBoardContainerView.h"
NS_ASSUME_NONNULL_BEGIN

//白板交互错误回调
typedef void(^ZegoWhiteboardBlock)(ZegoWhiteboardViewError errorCode);
//创建白板回调
typedef void(^ZegoCreateWhiteboardBlock)(ZegoWhiteboardViewError errorCode, ZegoWhiteboardView *whiteboardView);
//销毁白板回调
typedef void(^ZegoDestroyWhiteboardBlock)(ZegoWhiteboardViewError errorCode, ZegoWhiteboardID whiteboardID);
//获取白板列表回调
typedef void(^ZegoGetWhiteboardListBlock)(ZegoWhiteboardViewError errorCode,  NSArray *whiteBoardViewList);


@protocol ZegoBoardServiceDelegate <NSObject>

//本地SDK初始化完成回调
- (void)onLocalInintComplementErrorCode:(NSInteger)errorCode;

//本地获取白板列表回调
- (void)onLocalGetWhiteboardList:(NSArray *)whiteboardList errorCode:(NSInteger)errorCode;

//本地创建白板回调
- (void)onLocalCreateWhiteboardView:(ZegoWhiteboardView *)whiteboardView docsView:(ZegoDocsView *)docsView errorCode:(NSInteger)errorCode;

//收到白板新增回调
- (void)onRemoteWhiteboardAdd:(ZegoWhiteboardView *)whiteboardView;
//收到白板移除回调
- (void)onRemoteWhiteboardRemoved:(ZegoWhiteboardID)whiteboardID;
//收到远端动画执行回调
- (void)onRemotePlayAnimation:(NSString *)animationInfo;

- (void)onRemoteWhiteboardAuthChange:(NSDictionary *)authInfo;

- (void)onRemoteWhiteboardGraphicAuthChange:(NSDictionary *)authInfo;

//错误回调
- (void)onError:(ZegoWhiteboardViewError)error
 whiteboardView:(ZegoWhiteboardView *)whiboardView;

@end

@interface ZegoBoardServiceManager : NSObject

@property (nonatomic, weak) id <ZegoBoardServiceDelegate> delegate;

//当前房间下的 白板列表
@property (nonatomic, strong, readonly) NSArray *whiteboardViewList;

@property (nonatomic, weak) ZegoBoardContainerView *boardContainnerView;

// ******SDK设置******
//SDK单例
+ (instancetype)shareManager;

//初始化白板和文件SDK,在onInintComplementResult中接收初始化结果
- (void)initWithAppID:(unsigned int )appID appSign:(NSString *)appSign delegate:(id <ZegoBoardServiceDelegate>)delegate;
//设置文件SDK 自定义配置
- (BOOL)setupCustomConfig:(NSString *)value key:(NSString *)key;
//获取文件SDK 自定义配置
- (NSString *)getCustomizedConfigWithKey:(NSString *)key;
//设置自定义字体及粗体
- (void)setupCustomFontName:(NSString *)fontName boldName:(NSString *)boldName;
//获取白板列表
- (void)getCurrentWhiteboardList;
//根据model创建白板
- (void)createWhiteboardWithModel:(ZegoWhiteboardViewModel *)model fileID:(NSString *)fileID;
//计算文件SDK 缓存大小
- (long)calculateCacheSize;
//清除文件缓存
- (void)clearCacheFolder;
//清除房间资源
- (void)clearRoomSrc;
//反初始化SDK
- (void)uninit;
//获取SDK 版本号
- (NSString *)getVersion;

// ******功能设置******
//设置画笔模式
- (void)setupToolType:(ZegoWhiteboardTool)type;
//设置文本为粗体 可与斜体叠加
- (void)setupEnableBoldFont:(BOOL)enable;
//设置文本为斜体
- (void)setupEnableItalicFont:(BOOL)enable;
//设置画笔颜色
- (void)setupDrawColor:(NSString *)color;
//设置文本字体大小
- (void)setupFontSize:(NSInteger)fontSize;
//设置画笔线宽
- (void)setupDrawLineWidth:(NSInteger)lineWidth;
//设置文本工具占位文本
- (void)setupCustomText:(NSString *)text;
//新增白板
- (void)addNewWhiteboardWithName:(NSString *)wbName fileID:(nullable NSString *)fileID;
//移除白板
- (void)removeBoardWithID:(ZegoWhiteboardID)whiteboardID;
//清除白板缓存
- (void)clearWhiteboardCache;


//****** 文件操作 ******
- (void)nextPageComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (void)previousPageComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (void)nextStepComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (void)previousStepComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (void)turnToPage:(NSInteger)pageCount complementBlock:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (ZegoSeq)uploadFile:(NSString *)filePath renderType:(ZegoDocsViewRenderType)renderType completionBlock:(nonnull ZegoDocsViewUploadBlock)completionBlock;
- (void)cancelUploadFileSeq:(ZegoSeq)seq completionBlock:(ZegoDocsViewCancelUploadComplementBlock)completionBlock;
- (ZegoSeq)cacheFileWithFileId:(NSString *)fileId completionBlock:(ZegoDocsViewCacheBlock)completionBlock;
- (void)cancelCacheFileSeq:(ZegoSeq)seq completionBlock:(ZegoDocsViewCancelCacheComplementBlock)completionBlock;
- (void)queryFileCachedWithFileId:(NSString *)fileId completionBlock:(ZegoDocsViewQueryCachedCompletionBlock)completionBlock;



- (void)setupWhiteboardViewList:(NSArray *)whiteboardList;

@end

NS_ASSUME_NONNULL_END
