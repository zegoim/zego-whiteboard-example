//
//  ZegoWhiteboardEventHandler.h
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/30.
//

#import <Foundation/Foundation.h>
#import <ZegoWhiteboardView/ZegoWhiteboardView.h>
#import <ZegoWhiteboardView/ZegoWhiteboardManager.h>
#import "ZegoTopBarView.h"

NS_ASSUME_NONNULL_BEGIN

@interface ZegoBoardOperationManager : NSObject
@property (nonatomic, assign, readonly) ZegoWhiteboardTool toolType;
@property (nonatomic, strong, readonly) NSArray *previewArray;
@property (nonatomic, assign) CGRect whiteboardInitialFrame;


+ (instancetype)shareManager;

- (void)setupCurrentWhiteboardView:(ZegoWhiteboardView *)whiteboardView docsView:(nullable ZegoDocsView *)docsView;
- (void)leaveRoom;
- (void)setWhiteboardInitialFrame:(CGRect)frame;

//****** 白板功能设置 ******
- (void)setupWhiteboardOperationMode:(ZegoWhiteboardOperationMode)operationMode;
- (void)setupToolType:(ZegoWhiteboardTool)type;
- (void)setupEnableBoldFont:(BOOL)enable;
- (void)setupEnableItalicFont:(BOOL)enable;
- (void)setupColor:(NSString *)colorString;
- (void)setupFontSize:(NSInteger)fontSize;
- (void)setupDrawLineWidth:(NSInteger)lineWidth;
- (void)addNewWhiteboardWithName:(NSString *)wbName fileID:(NSString *)fileID;
- (void)removeBoardWithID:(ZegoWhiteboardID)whiteboardID;
- (void)setupCustomText:(NSString *)text;
- (void)addGraphicWithText:(NSString *)text postion:(CGPoint)point;
- (void)setCustomImageGraphicWithURLString:(NSString *)urlString complete:(void(^)(int error))complete;
- (void)clearAllGraphic;
- (void)clearCurrentPage;
- (void)clearCurrentSelected;
- (void)redoGraphic;
- (void)undoGraphic;
- (void)clearWhiteboardCache;
- (void)cleanBackgroundImage;
- (void)setCurrentAuthInfo:(NSDictionary *)authInfo;
- (void)setWhiteboardDeltaSize:(CGSize)size;
- (void)setWhiteboardSizeWithString:(NSString *)sizeInfo;

//****** 文件操作 ******
- (void)playAnimationWithInfo:(NSString *)info;
- (void)getThumbnailUrlList;
- (void)showPreview;
- (void)setAlphaEnv;
- (void)setupSetpAutoPaging:(BOOL)autoPaging;
- (void)clearFileCache;
- (void)nextPageComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (void)previousPageComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (void)nextStepComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (void)previousStepComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock;
- (void)turnToPage:(NSInteger)pageCount complementBlock:(ZegoDocsViewScrollCompleteBlock)complementBlock;

//****** 文件上传和缓存 ******
- (ZegoSeq)uploadFile:(NSString *)filePath renderType:(ZegoDocsViewRenderType)renderType completionBlock:(nonnull ZegoDocsViewUploadBlock)completionBlock;
- (void)cancelUploadFileSeq:(ZegoSeq)seq completionBlock:(ZegoDocsViewCancelUploadComplementBlock)completionBlock;
- (ZegoSeq)cacheFileWithFileId:(NSString *)fileId completionBlock:(ZegoDocsViewCacheBlock)completionBlock;
- (void)cancelCacheFileSeq:(ZegoSeq)seq completionBlock:(ZegoDocsViewCancelCacheComplementBlock)completionBlock;
- (void)queryFileCachedWithFileId:(NSString *)fileId completionBlock:(ZegoDocsViewQueryCachedCompletionBlock)completionBlock;



@end

NS_ASSUME_NONNULL_END
