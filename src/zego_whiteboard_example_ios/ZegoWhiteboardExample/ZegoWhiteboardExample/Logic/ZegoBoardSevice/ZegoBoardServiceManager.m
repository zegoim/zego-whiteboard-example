//
//  ZegoWhiteboardServiceManager.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/24.
//

#import "ZegoBoardServiceManager.h"
#import <ZegoWhiteboardView/ZegoWhiteboardManager.h>
#import "ZegoLocalEnvManager.h"
#import "ZGAppSignHelper.h"
#import "NSString+ContentOperation.h"
@interface ZegoBoardServiceManager()<ZegoWhiteboardManagerDelegate>
@property (nonatomic, strong) ZegoWhiteboardManager *wbManager;
@property (nonatomic, strong) ZegoDocsViewManager *docsManager;
@property (nonatomic, strong) NSDictionary *systemColorDic;


@end
@implementation ZegoBoardServiceManager

+ (instancetype)shareManager {
    static dispatch_once_t onceToken;
    static ZegoBoardServiceManager *manager = nil;
    dispatch_once(&onceToken, ^{
        manager = [[ZegoBoardServiceManager alloc] init];
        manager.whiteboardAspectSize = CGSizeMake(16, 9);
    });
    return manager;
}

- (void)initWithAppID:(unsigned int )appID appSign:(NSString *)appSign delegate:(id <ZegoBoardServiceDelegate>)delegate {
    ZegoWhiteboardConfig *wbConfig = [[ZegoWhiteboardConfig alloc] init];
    wbConfig.logPath = kZegoLogPath;
    wbConfig.cacheFolder = kZegoImagePath;
    self.delegate = delegate;
    __weak typeof(self) weakSelf = self;
    self.wbManager = [ZegoWhiteboardManager sharedInstance];
    self.wbManager.delegate = self;
    [self.wbManager setConfig:wbConfig];
    [self.wbManager initWithCompleteBlock:^(ZegoWhiteboardViewError errorCode) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        DLog(@"localWhiteboarManagerInitFinish,error:%ld",(long)errorCode);
        if (errorCode == 0) {
            [strongSelf.wbManager setDelegate:self];
            ZegoDocsViewConfig *docsConfig = [[ZegoDocsViewConfig alloc] init];
            docsConfig.dataFolder = kZegoDocsDataPath;
            docsConfig.logFolder = kZegoLogPath;
            docsConfig.cacheFolder = kZegoDocsDataPath;
            docsConfig.appID = [ZegoLocalEnvManager shareManager].appID;
            docsConfig.appSign = [ZGAppSignHelper convertAppSignStringFromString:[ZegoLocalEnvManager shareManager].appSign];
            strongSelf.docsManager = [ZegoDocsViewManager sharedInstance];

            
            [[ZegoBoardOperationManager shareManager] setupSetpAutoPaging:YES];
            
            [strongSelf setupThumbnailDefinition];
            
            [strongSelf setupUnloadVideo];
            
            [strongSelf.docsManager initWithConfig:docsConfig completionBlock:^(ZegoDocsViewError errorCode) {
                DLog(@"localDocsViewManagerInitFinish,error:%lu",(unsigned long)errorCode);
                if ([strongSelf.delegate respondsToSelector:@selector(onLocalInintComplementErrorCode:type:)]) {
                    [strongSelf.delegate onLocalInintComplementErrorCode:errorCode type:1];
                }
            }];
            
            if([ZegoLocalEnvManager shareManager].enableCutomFont){
                [strongSelf.wbManager setCustomFontWithName:@"SourceHanSansSC-Regular" boldFontName:@"SourceHanSansSC-Bold"];
            } else {
                [strongSelf.wbManager setCustomFontWithName:@"" boldFontName:@""];
            }
        } else {
            if ([strongSelf.delegate respondsToSelector:@selector(onLocalInintComplementErrorCode:type:)]) {
                [strongSelf.delegate onLocalInintComplementErrorCode:errorCode type:0];
            }
        }
    }];
}

- (void)setupThumbnailDefinition {
    [self setupCustomConfig:[ZegoLocalEnvManager shareManager].pptThumbnailClarity key:@"thumbnailMode"];
}

- (void)setupUnloadVideo
{
    // ture 不加载  false 加载
    NSString * str = [ZegoLocalEnvManager shareManager].isUnloadVideo ? @"true":@"false";
    [self setupCustomConfig:str key:@"unloadVideoSrc"];
}

- (BOOL)setupCustomConfig:(NSString *)value key:(NSString *)key {
    DLog(@"BoardSevice>>> getCustomizedConfigWithKey:%@",key);
    return [self.docsManager setCustomizedConfig:value key:key];
}

- (NSString *)getCustomizedConfigWithKey:(NSString *)key {
    DLog(@"BoardSevice>>> getCustomizedConfigWithKey:%@",key);
    return [self.docsManager getCustomizedConfigWithKey:key];
}

- (void)setupCustomFontName:(NSString *)fontName boldName:(NSString *)boldName {
    DLog(@"BoardSevice>>> setupCustomFontName:%@ boldName:%@",fontName,boldName);
    [self.wbManager setCustomFontWithName:fontName boldFontName:boldName];
}

- (void)getCurrentWhiteboardList {
    __weak typeof(self) weakSelf = self;
    DLog(@"BoardSevice>>> getCurrentWhiteboardListExcute");
    [self.wbManager getWhiteboardListWithCompleteBlock:^(ZegoWhiteboardViewError errorCode, NSArray *whiteBoardViewList) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if ([strongSelf.delegate respondsToSelector:@selector(onLocalGetWhiteboardList:errorCode:)]) {
            [strongSelf.delegate onLocalGetWhiteboardList:whiteBoardViewList errorCode:errorCode];
        }
    }];
}

- (void)createWhiteboardWithModel:(ZegoWhiteboardViewModel *)model fileID:(nonnull NSString *)fileID {
    
    DLog(@"BoardSevice>>> createWhiteboardWhiteModelExcute, whiteboardID:%llu,name:%@,fileID:%@",model.whiteboardID,model.name,fileID);
    if (fileID.length > 0) {
        ZegoDocsView *docsView = [[ZegoDocsView alloc] initWithFrame:self.boardContainnerView.bounds];
        __weak typeof(self) weakSelf = self;
        [docsView loadFileWithFileID:fileID authKey:@"" completionBlock:^(ZegoDocsViewError errorCode) {
            DLog(@"BoardSevice>>> createWhiteboardWithModel --> loadFileWithFileID,error:%lu",(unsigned long)errorCode);
            if (errorCode == 0) {
                __strong typeof(weakSelf) strongSelf = weakSelf;
                model.fileInfo.fileName = docsView.fileName;
                model.fileInfo.fileID = docsView.fileID;
                model.fileInfo.fileType = docsView.fileType;
                [strongSelf requestCreateWhiteboardWithModel:model docsView:docsView];
            } else {
                [ZegoProgessHUD showTipMessage:[NSString stringWithFormat:@"文件加载失败error：%lu",(unsigned long)errorCode]];
            }
        }];
    } else {
        [self requestCreateWhiteboardWithModel:model docsView:nil];
    }
}

- (void)requestCreateWhiteboardWithModel:(ZegoWhiteboardViewModel *)model docsView: (ZegoDocsView *)docsView {
    
    model.name = [model.name subStringByByteWithLength:128];
    if (model.fileInfo.fileName.length > 0) {
        model.fileInfo.fileName = [model.fileInfo.fileName subStringByByteWithLength:128];
    }

    DLog(@"BoardSevice>>> requestCreateWhiteboardWithModel:%llu docsView:%@",model.whiteboardID,docsView.fileID);
    
    [self.wbManager createWhiteboardView:model completeBlock:^(ZegoWhiteboardViewError errorCode, ZegoWhiteboardView *whiteboardView) {
        if ([self.delegate respondsToSelector:@selector(onLocalCreateWhiteboardView:docsView:errorCode:)]) {
            [self.delegate onLocalCreateWhiteboardView:whiteboardView docsView:docsView errorCode:errorCode];
        }
    }];
}

- (long)calculateCacheSize {
    DLog(@"BoardSevice>>> calculateCacheSize");
    return self.docsManager.calculateCacheSize;
}

- (void)clearCacheFolder {
    DLog(@"BoardSevice>>> clearCacheFolder");
    [self.docsManager clearCacheFolder];
}

- (void)clearRoomSrc {
    DLog(@"BoardSevice>>> clearRoomSrc");
    _whiteboardViewList = nil;
    [self.wbManager clear];
}

- (void)uninit {
    [self clearRoomSrc];
    [self.wbManager uninit];
    [self.docsManager uninit];
    self.wbManager = nil;
    self.docsManager = nil;
    _delegate = nil;
    DLog(@"BoardSevice>>> uninit");
}

- (NSString *)getVersion {
    DLog(@"BoardSevice>>> getVersion");
    return [NSString stringWithFormat:@"Version:\ndocsView:%@\nwhiteboardView:%@",@"",self.wbManager.getVersion];
}

- (void)setupEnableBoldFont:(BOOL)enable {
    DLog(@"BoardSevice>>> setupEnableBoldFont:%@",enable?@"YES":@"NO");
    self.wbManager.isFontBold = enable;
}
- (void)setupEnableItalicFont:(BOOL)enable {
    DLog(@"BoardSevice>>> setupEnableItalicFont:%@",enable?@"YES":@"NO");
    self.wbManager.isFontItalic = enable;
}

- (void)setupToolType:(ZegoWhiteboardTool)type {
    DLog(@"BoardSevice>>> setupToolType:%lu",(unsigned long)type);
    self.wbManager.toolType = type;
}

- (void)setupDrawColor:(NSString *)color {
    DLog(@"BoardSevice>>> setupDrawColor:%@",color);
    if ([self.systemColorDic.allKeys containsObject:color]) {
        UIColor *currentColor = self.systemColorDic[color];
        self.wbManager.brushColor = currentColor;
    } else {
        self.wbManager.brushColor = [UIColor colorWithRGB:color];
    }
}
- (void)setupFontSize:(NSInteger)fontSize {
    DLog(@"BoardSevice>>> setupFontSize:%ld",(long)fontSize);
    self.wbManager.fontSize = fontSize;
}

- (void)setupDrawLineWidth:(NSInteger)lineWidth {
    DLog(@"BoardSevice>>> setupDrawLineWidth:%ld",(long)lineWidth);
    self.wbManager.brushSize = lineWidth;
}

- (void)setupCustomText:(NSString *)text {
    DLog(@"BoardSevice>>> setupCustomText:%@",text);
    self.wbManager.customText = text;
}

- (void)setEnableSendToRoomScale:(BOOL)enableSendToRoomScale
{
    if (self.wbManager.enableSyncScale == enableSendToRoomScale) {
        return;;
    }
    DLog(@"BoardSevice>>> setEnableSendToRoomScale:%d",enableSendToRoomScale);
    self.wbManager.enableSyncScale = enableSendToRoomScale;
}
- (void)setEnableRecvFromRoomScale:(BOOL)enableRecvFromRoomScale
{
    if (self.wbManager.enableResponseScale == enableRecvFromRoomScale) {
        return;
    }
    DLog(@"BoardSevice>>> setEnableRecvFromRoomScale:%d",enableRecvFromRoomScale);
    self.wbManager.enableResponseScale = enableRecvFromRoomScale;
}

- (void)setEnableHandWriting:(BOOL)enableHandWriting {
    if (self.wbManager.enableHandwriting == enableHandWriting) {
        return;
    }
    DLog(@"BoardSevice>>> setEnableHandWriting:%d",enableHandWriting);
    self.wbManager.enableHandwriting = enableHandWriting;
}

- (BOOL)judgeFileIsExists:(NSString *)fileID {
    BOOL isExists = NO;
    for (ZegoWhiteboardView *view in self.whiteboardViewList) {
        if ([fileID isEqualToString:view.whiteboardModel.fileInfo.fileID]) {
            isExists = YES;
            break;
        }
    }
    return isExists;
}

- (void)addNewWhiteboardWithName:(NSString *)wbName fileID:(nullable NSString *)fileID {
    DLog(@"BoardSevice>>> addNewWhiteboardWithName:%@ fileID:%@",wbName,fileID);
    if ([self judgeFileIsExists:fileID]) {
        DLog(@"BoardSevice>>> file is exists:%@ fileID:%@",wbName,fileID);
        [ZegoProgessHUD showTipMessage:[NSString stringWithFormat:@"文件已经创建"]];
        return;
    }
    if (fileID.length > 0) {
        ZegoDocsView *docsView = [[ZegoDocsView alloc] initWithFrame:self.boardContainnerView.bounds];
        __weak typeof(self) weakSelf = self;
        [docsView loadFileWithFileID:fileID authKey:@"" completionBlock:^(ZegoDocsViewError errorCode) {
            DLog(@"BoardSevice>>> addNewWhiteboardWithName --> loadFileWithFileID,error:%lu",(unsigned long)errorCode);
            if (errorCode == 0) {
                ZegoWhiteboardViewModel *model = [[ZegoWhiteboardViewModel alloc] init];
                model.aspectWidth = docsView.contentSize.width;
                model.aspectHeight = docsView.contentSize.height;
                model.pageCount = docsView.pageCount;
                NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
                    [dateFormatter setDateFormat:@"HH-mm-ss"];
                model.name = [NSString stringWithFormat:@"%@-%@",[ZegoLocalEnvManager shareManager].userName,docsView.fileName];
                __strong typeof(weakSelf) strongSelf = weakSelf;
                if (docsView.fileType == ZegoDocsViewFileTypeELS) {
                    model.fileInfo.fileName = docsView.sheetNameList[0];
                }else {
                    model.fileInfo.fileName = docsView.fileName;
                }
                model.fileInfo.fileID = docsView.fileID;
                model.fileInfo.fileType = docsView.fileType;
                [strongSelf requestCreateWhiteboardWithModel:model docsView:docsView];
            } else {
                [ZegoProgessHUD showTipMessage:[NSString stringWithFormat:@"文件加载失败error：%lu",(unsigned long)errorCode]];
            }
        }];
    } else {
        ZegoWhiteboardViewModel *model = [[ZegoWhiteboardViewModel alloc] init];
        CGSize aspectSize = self.whiteboardAspectSize;
        int pageCount = 5;
        model.aspectWidth = aspectSize.width * pageCount;
        model.aspectHeight = aspectSize.height;
//        model.aspectWidth_viewPortStub = aspectSize.width;
//        model.aspectHeight_viewPortStub = aspectSize.height;
        model.pageCount = pageCount;
        
        if (wbName.length > 0) {
            model.name = wbName;
        } else {
            NSDate *currentDate = [NSDate date];
            NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
                [dateFormatter setDateFormat:@"HH-mm-ss"];
            NSString *dateString = [dateFormatter stringFromDate:currentDate];
            model.name = [NSString stringWithFormat:@"%@的白板%@",[ZegoLocalEnvManager shareManager].userName,dateString];
        }
        [self requestCreateWhiteboardWithModel:model docsView:nil];
    }
}

- (void)removeBoardWithID:(ZegoWhiteboardID)whiteboardID {
    DLog(@"BoardSevice>>> removeBoardWithID:%llu",whiteboardID);
    __weak typeof(self) weakSelf = self;
    [self.wbManager destroyWhiteboardID:whiteboardID completeBlock:^(ZegoWhiteboardViewError errorCode, ZegoWhiteboardID whiteboardID) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        if ([strongSelf.delegate respondsToSelector:@selector(onRemoteWhiteboardRemoved:)]) {
            [strongSelf.delegate onRemoteWhiteboardRemoved:whiteboardID];
        }
    }];
}

- (void)clearWhiteboardCache {
    DLog(@"BoardSevice>>> clearWhiteboardCache");
    [self.wbManager clearCacheFolder];
}

#pragma mark - ZegoWhiteboardManagerDelegate
- (void)onWhiteboardAdd:(ZegoWhiteboardView *)whiteboardView {
    DLog(@"BoardSevice>>> onWhiteboardAdd");
    if ([self.delegate respondsToSelector:@selector(onRemoteWhiteboardAdd:)]) {
        [self.delegate onRemoteWhiteboardAdd:whiteboardView];
    }
}

- (void)onWhiteboardRemoved:(ZegoWhiteboardID)whiteboardID {
    DLog(@"BoardSevice>>> onWhiteboardRemoved");
    if ([self.delegate respondsToSelector:@selector(onRemoteWhiteboardRemoved:)]) {
        [self.delegate onRemoteWhiteboardRemoved:whiteboardID];
    }
}

- (void)onWhiteboardAuthChanged:(NSDictionary *)authInfo {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onRemoteWhiteboardAuthChange:)]) {
        [self.delegate onRemoteWhiteboardAuthChange:authInfo];
    }
}

- (void)onWhiteboardGraphicAuthChanged:(NSDictionary *)authInfo {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onRemoteWhiteboardGraphicAuthChange:)]) {
        [self.delegate onRemoteWhiteboardGraphicAuthChange:authInfo];
    }
}

- (void)onPlayAnimation:(NSString *)animationInfo {
    DLog(@"BoardSevice>>> onPlayAnimation");
    if ([self.delegate respondsToSelector:@selector(onRemotePlayAnimation:)]) {
        [self.delegate onRemotePlayAnimation:animationInfo];
    }
}

- (void)onError:(ZegoWhiteboardViewError)error whiteboardView:(nonnull ZegoWhiteboardView *)whiboardView {
    if (self.delegate && [self.delegate respondsToSelector:@selector(onError:whiteboardView:)]) {
        [self.delegate onError:error whiteboardView:whiboardView];
    }
}

- (ZegoSeq)uploadFile:(NSString *)filePath renderType:(ZegoDocsViewRenderType)renderType completionBlock:(nonnull ZegoDocsViewUploadBlock)completionBlock {
    ZegoSeq seq = [self.docsManager uploadFile:filePath renderType:renderType completionBlock:completionBlock];
    DLog(@"BoardSevice>>> uploadFile:%@ renderType:%lu,seq:%d",filePath,(unsigned long)renderType,seq);
    return seq;
}

- (void)cancelUploadFileSeq:(ZegoSeq)seq completionBlock:(ZegoDocsViewCancelUploadComplementBlock)completionBlock{
    DLog(@"BoardSevice>>> cancelUploadFileSeq:%d ",seq);
    [self.docsManager cancelUploadFileWithSeq:seq completionBlock:completionBlock];
}

- (ZegoSeq)cacheFileWithFileId:(NSString *)fileId completionBlock:(ZegoDocsViewCacheBlock)completionBlock {
    ZegoSeq seq = [self.docsManager cacheFileWithFileId:fileId completionBlock:completionBlock];
    DLog(@"BoardSevice>>> cacheFileWithFileId:%@,seq:%d",fileId,seq);
    return seq;
}

- (void)cancelCacheFileSeq:(ZegoSeq)seq completionBlock:(ZegoDocsViewCancelCacheComplementBlock)completionBlock {
    DLog(@"BoardSevice>>> cancelCacheFileSeq:%d",seq);
    [self.docsManager cancelCacheFileWithSeq:seq completionBlock:completionBlock];
}

- (void)queryFileCachedWithFileId:(NSString *)fileId completionBlock:(ZegoDocsViewQueryCachedCompletionBlock)completionBlock {
    DLog(@"BoardSevice>>> queryFileCachedWithFileId:%@",fileId);
    [self.docsManager queryFileCachedWithFileId:fileId completionBlock:completionBlock];
}

- (NSDictionary *)systemColorDic {
    if (!_systemColorDic) {
        _systemColorDic = @{@"black":[UIColor blackColor],
                            @"red":[UIColor redColor],
                            @"yellow":[UIColor yellowColor],
                            @"blue":[UIColor blueColor],
                            @"green":[UIColor greenColor],
                            @"white":[UIColor whiteColor],
                            @"gray":[UIColor grayColor],
                            @"orange":[UIColor orangeColor]
        };
    }
    return _systemColorDic;
}

@end
