//
//  ZegoWhiteboardEventHandler.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/30.
//

#import "ZegoBoardOperationManager.h"
#import "ZegoFilePreviewManager.h"

@interface ZegoBoardOperationManager()
@property (nonatomic, weak) ZegoWhiteboardView *currentWhiteboardView;
@property (nonatomic, weak) ZegoDocsView *currentDocsView;

@end

@implementation ZegoBoardOperationManager


+ (instancetype)shareManager {
    static dispatch_once_t onceToken;
    static ZegoBoardOperationManager *manager = nil;
    dispatch_once(&onceToken, ^{
        manager = [[ZegoBoardOperationManager alloc] init];
    });
    return manager;
}

- (instancetype)init {
    if (self = [super init]) {
       
        [self initDefaultParameter];
    }
    return self;
}

//初始化默认设置
- (void)initDefaultParameter {
    DLog(@"BoardOperation>>> localInitOperationManagerParameter---start");
    _previewArray = nil;
    [self setupWhiteboardOperationMode:ZegoWhiteboardOperationModeDraw];
    [self setupColor:@"0x000000"];
    [self setupFontSize:18];
    [self setupEnableBoldFont:NO];
    [self setupEnableItalicFont:NO];
    [self setupToolType:ZegoWhiteboardViewToolPen];
    [self setupDrawLineWidth:4];
    [self setupCustomText:@"文本"];
    DLog(@"BoardOperation>>> localInitOperationManagerParameter---end");
}

- (void)setupCurrentWhiteboardView:(ZegoWhiteboardView *)wbView docsView:(nullable ZegoDocsView *)docsView {
    _currentWhiteboardView = wbView;
    _currentDocsView = docsView;
    [self initDefaultParameter];
    DLog(@"BoardOperation>>> sysSettingCurrentWhiteboard&CurrentDocsViewExcute,wb:%@,doc:%@",wbView,docsView);
}

- (void)setupWhiteboardOperationMode:(ZegoWhiteboardOperationMode)operationMode {
    [self.currentWhiteboardView setWhiteboardOperationMode:operationMode];
    DLog(@"BoardOperation>>> setupWhiteboardOperationMode,mode:%lu",(unsigned long)operationMode);
}

- (void)setupToolType:(ZegoWhiteboardTool)type {
    _toolType = type;
    if (type != ZegoWhiteboardViewToolLaser) {
        [self.currentWhiteboardView removeLaser];
    }
    if (type == ZegoWhiteboardViewToolClick) {
        self.currentWhiteboardView.userInteractionEnabled = NO;
    } else {
        self.currentWhiteboardView.userInteractionEnabled = YES;
    }
    
    [[ZegoBoardServiceManager shareManager] setupToolType:type];
    DLog(@"BoardOperation>>> sysSettingToolTypeExcute，type:%lu",(unsigned long)type);
    
#if (!TARGET_IPHONE_SIMULATOR)
    if (type == ZegoWhiteboardViewToolText) {
        [self.currentWhiteboardView addTextEdit];
    }
#endif
}

- (void)setupEnableBoldFont:(BOOL)enable {
    [[ZegoBoardServiceManager shareManager] setupEnableBoldFont:enable];
    DLog(@"BoardOperation>>> sysSettingBoldFontExcute,result:%d",enable?1:0);
}

- (void)setupEnableItalicFont:(BOOL)enable {
    [[ZegoBoardServiceManager shareManager] setupEnableItalicFont:enable];
    DLog(@"BoardOperation>>> sysSettingItalicFontExcute,result:%d",enable?1:0);
}

- (void)setupColor:(NSString *)colorString {
    [[ZegoBoardServiceManager shareManager] setupDrawColor:colorString];
    DLog(@"BoardOperation>>> sysSettubgDrawColorExcute,color:%@",colorString);
}

- (void)setupFontSize:(NSInteger)fontSize {
    [[ZegoBoardServiceManager shareManager] setupFontSize:fontSize];
    DLog(@"BoardOperation>>> sysSettingFontSizeExcute,size:%ld",(long)fontSize);
}

- (void)setupDrawLineWidth:(NSInteger)lineWidth {
    [[ZegoBoardServiceManager shareManager] setupDrawLineWidth:lineWidth];
    DLog(@"BoardOperation>>> sysSettingDrawLineWidthExcute,lineWidth:%ld",(long)lineWidth);

}

- (void)setupCustomText:(NSString *)text {
    [[ZegoBoardServiceManager shareManager] setupCustomText:text];
    DLog(@"BoardOperation>>> sysSettingCustomTextExcute,text:%@",text);
}

- (void)addGraphicWithText:(NSString *)text postion:(CGPoint)point{
    [self.currentWhiteboardView addText:text positionX:point.x positionY:point.y];
    DLog(@"BoardOperation>>> sysSettingTextGraphicExcute,text:%@ ,position{%f,%f}",text,point.x,point.y);
}

- (void)setCustomImageGraphicWithURLString:(NSString *)urlString complete:(void(^)(int error))complete {
    DLog(@"BoardOperation>>> setCustomImageUrlString:%@", urlString);
    [self.currentWhiteboardView addImage:ZegoWhiteboardViewImageTypeCustom positionX:0 positionY:0 address:urlString complete:^(int errorcode) {
        DLog(@"BoardOperation>>> setCustomImageUrlString --> addImage,error:%d", errorcode);
        if (complete) {
            complete(errorcode);
        }
    }];
}

- (void)addNewWhiteboardWithName:(NSString *)wbName fileID:(NSString *)fileID {
    [[ZegoBoardServiceManager shareManager] addNewWhiteboardWithName:wbName fileID:fileID];
    DLog(@"BoardOperation>>> sysaddNewWhiteboardWithFileExcute");
}

- (void)clearWhiteboardCache {
    [[ZegoBoardServiceManager shareManager] clearWhiteboardCache];
    [ZegoProgessHUD showTipMessage:@"缓存已清除"];
    DLog(@"BoardOperation>>> clearWhiteboardCache");
}

- (void)removeBoardWithID:(ZegoWhiteboardID)whiteboardID {
    [[ZegoBoardServiceManager shareManager] removeBoardWithID:whiteboardID];
    DLog(@"BoardOperation>>> sysRemoveWhiteboardExcute,id:%llu",whiteboardID);
}

- (void)clearAllGraphic {
    [self.currentWhiteboardView clear];
    DLog(@"BoardOperation>>> sysClearAllGraphicExcute");
}

- (void)clearCurrentPage {
    //清空文件当前页图元
    if (self.currentDocsView) {
        DLog(@"BoardOperation>>> clearCurrentPage (has docsView)");
        ZegoDocsViewPage *pageInfo = [self.currentDocsView getCurrentPageInfo];
        [self.currentWhiteboardView clear:pageInfo.rect];
    } else {
        DLog(@"BoardOperation>>> clearCurrentPage (has not docsView)");
        //清空纯白板当前页图元
//        CGFloat width = self.currentWhiteboardView.bounds.size.width;
//        CGFloat height = self.currentWhiteboardView.bounds.size.height;
//        NSInteger currentPage = (NSInteger)((self.currentWhiteboardView.contentOffset.x/self.currentWhiteboardView.contentSize.width) * self.currentWhiteboardView.whiteboardModel.pageCount);
//        CGRect rect = CGRectMake(width * currentPage , 0, width, height);
//        [self.currentWhiteboardView clear:rect];
        CGFloat percent = self.currentWhiteboardView.whiteboardModel.horizontalScrollPercent;
        CGFloat width = self.currentWhiteboardView.frame.size.width;
        CGFloat height = self.currentWhiteboardView.frame.size.height;
        CGFloat offsetX = percent * width * self.currentWhiteboardView.whiteboardModel.pageCount;
        CGFloat offsetY = 0;
        CGRect rect = CGRectMake(offsetX, offsetY, width, height);
        [self.currentWhiteboardView clear:rect];
    }
}

- (void)clearCurrentSelected {
    DLog(@"BoardOperation>>> clearCurrentSelected");
    [self.currentWhiteboardView deleteSelectedGraphics];
}

- (void)redoGraphic {
    [self.currentWhiteboardView redo];
    DLog(@"BoardOperation>>> redoGraphic");
}

- (void)undoGraphic {
    [self.currentWhiteboardView undo];
    DLog(@"BoardOperation>>> undoGraphic");
}

- (void)playAnimationWithInfo:(NSString *)info {
    [self.currentDocsView playAnimation:info];
    DLog(@"BoardOperation>>> playAnimationWithInfo");
}

- (void)getThumbnailUrlList {
    
    _previewArray = [self.currentDocsView getThumbnailUrlList];
    DLog(@"BoardOperation>>> getThumbnailUrlList : %@",_previewArray);
    if (self.previewArray.count < 1) {
        [ZegoProgessHUD showTipMessage:@"无预览数据"];
        return;
    } else {
        [ZegoProgessHUD showTipMessage:@"已获取预览数据"];
    }
}

- (void)showPreview {
    DLog(@"BoardOperation>>> showPreview: %@",_previewArray);
    if (self.previewArray.count < 1) {
        [ZegoProgessHUD showTipMessage:@"无预览数据"];
        return;
    }
    [[ZegoFilePreviewManager shareManager] setupPreviewData:self.previewArray];
    [[ZegoFilePreviewManager shareManager] showPreviewWithPage:self.currentDocsView.currentPage - 1];
    __weak typeof(self) weakSelf = self;
    [ZegoFilePreviewManager shareManager].selectedPageBlock = ^(NSInteger index) {
        __strong typeof(weakSelf) strongSelf = weakSelf;
        [strongSelf turnToPage:index + 1 complementBlock:^(BOOL isScrollSuccess) {
            DLog(@"BoardOperation>>> previewList turn page complement,state:%d",isScrollSuccess?1:0);
        }];
    };
}

- (void)setupSetpAutoPaging:(BOOL)autoPaging {
    NSString *value = autoPaging ? @"1":@"2";
    [[ZegoBoardServiceManager shareManager] setupCustomConfig:value key:@"pptStepMode"];
    DLog(@"BoardOperation>>> setupSetpAutoPaging:%d",autoPaging);
}

- (void)clearFileCache {
    [[ZegoBoardServiceManager shareManager] clearCacheFolder];
    DLog(@"BoardOperation>>> clearFileCache");
    [ZegoProgessHUD showTipMessage:@"文件缓存已清除"];
}

- (void)nextPageComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock {
    DLog(@"BoardOperation>>> nextStepComplement");
    CGFloat xPercent = self.currentWhiteboardView.whiteboardModel.horizontalScrollPercent;
    if (self.currentDocsView) {
        if (self.currentDocsView.currentPage + 1 <= self.currentDocsView.pageCount) {
            [self scrollToPage:self.currentDocsView.currentPage + 1 pptStep:1 completionBlock:complementBlock];
        }
    } else {

        CGFloat currentPage = (xPercent + (1.0 / kWhiteboarPageCount));
        if (currentPage > 1.0) {
            return;
        }
        [self.currentWhiteboardView scrollToHorizontalPercent:currentPage verticalPercent:0 pptStep: 0 completionBlock:^(ZegoWhiteboardViewError error_code, float horizontalPercent, float verticalPercent, unsigned int step) {
            if (complementBlock) {
                complementBlock(error_code == 0);
            }
        }];
        DLog(@"BoardOperation>>> nextPageComplement --> scrollToHorizontalPercent,page:%f",currentPage);
    }
}

- (void)previousPageComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock {
    DLog(@"BoardOperation>>> previousPageComplement");
    CGFloat xPercent = self.currentWhiteboardView.whiteboardModel.horizontalScrollPercent;
    if (self.currentDocsView) {
        if (self.currentDocsView.currentPage - 1 >= 1) {
            [self scrollToPage:self.currentDocsView.currentPage - 1 pptStep:1 completionBlock:complementBlock];
        }
    } else {
        CGFloat pageNo = xPercent  - (1.0 / kWhiteboarPageCount);
        pageNo = MAX(pageNo, 0);
        [self.currentWhiteboardView scrollToHorizontalPercent:pageNo verticalPercent:0 pptStep:0  completionBlock:^(ZegoWhiteboardViewError error_code, float horizontalPercent, float verticalPercent, unsigned int pptStep) {
            if (complementBlock) {
                complementBlock(error_code == 0);
            }
        }];
        DLog(@"BoardOperation>>> previousPageComplement --> scrollToHorizontalPercent,page:%f",pageNo);
    }
}

- (void)nextStepComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock {
    DLog(@"BoardOperation>>> nextStepComplement");
    if (![self isDynamicPPT]) return;
    __weak typeof(self) weakSelf = self;
    [self.currentDocsView nextStepWithCompletionBlock:^(BOOL isScrollSuccess) {
        DLog(@"BoardOperation>>> docsViewNextStepFinish,result:%d",isScrollSuccess?1:0);
        if (isScrollSuccess) {
            float pageNum = (float)MAX((weakSelf.currentDocsView.currentPage - 1), 0);
            NSInteger step = weakSelf.currentDocsView.currentStep;
           
            [weakSelf.currentWhiteboardView scrollToHorizontalPercent:0 verticalPercent: pageNum/ (float)weakSelf.currentDocsView.pageCount pptStep:step completionBlock:^(ZegoWhiteboardViewError error_code, float horizontalPercent, float verticalPercent, unsigned int pptStep) {
                if (complementBlock) {
                    complementBlock(error_code == 0);
                    
                }
                DLog(@"BoardOperation>>> sysWhiteboardStep,step:%ld,page:%f,error:%ld",(long)step,pageNum,(long)error_code);
            }];
        } else {
            if (complementBlock) {
                complementBlock(isScrollSuccess);
            }
        }
    }];
}

- (void)previousStepComplement:(ZegoDocsViewScrollCompleteBlock)complementBlock {
    DLog(@"BoardOperation>>> previousStepComplement");
    if (![self isDynamicPPT]) return;
    __weak typeof(self) weakSelf = self;
    [self.currentDocsView previousStepWithCompletionBlock:^(BOOL isScrollSuccess) {
        DLog(@"BoardOperation>>> docsViewPreviousStepFinish,result:%d",isScrollSuccess?1:0);
        if (isScrollSuccess) {
            float pageNum = (float)MAX((weakSelf.currentDocsView.currentPage - 1), 0);
            NSInteger step = weakSelf.currentDocsView.currentStep;
        
            [weakSelf.currentWhiteboardView scrollToHorizontalPercent:0 verticalPercent: pageNum/ (float)weakSelf.currentDocsView.pageCount pptStep:step completionBlock:^(ZegoWhiteboardViewError error_code, float horizontalPercent, float verticalPercent, unsigned int pptStep) {
                if (complementBlock) {
                    complementBlock(error_code == 0);
                }
                DLog(@"BoardOperation>>> sysWhiteboardStep,step:%ld,page:%f,error:%ld",(long)step,pageNum,(long)error_code);
            }];
        } else {
            if (complementBlock) {
                complementBlock(isScrollSuccess);
            }
        }
    }];
}

- (void)turnToPage:(NSInteger)pageCount complementBlock:(nonnull ZegoDocsViewScrollCompleteBlock)complementBlock {
    DLog(@"BoardOperation>>> turnToPage:%ld",(long)pageCount);
    [self scrollToPage:pageCount pptStep:1 completionBlock:complementBlock];
}

- (void)leaveRoom {
    DLog(@"BoardOperation>>> leavRoomExcute");
    [self reset];
    [ZegoRoomSeviceCenter logoutRoom];
    [[ZegoBoardServiceManager shareManager] clearRoomSrc];
    [[ZegoBoardServiceManager shareManager] uninit];
    [ZegoRoomSeviceCenter uninit];
    UINavigationController *navVC = (UINavigationController *)[UIApplication sharedApplication].keyWindow.rootViewController;
    [navVC popViewControllerAnimated:YES];
}

- (void)reset {
    DLog(@"BoardOperation>>> reset");
    self.currentDocsView = nil;
    self.currentWhiteboardView = nil;
    _toolType = 0;
}

- (void)scrollToPage:(NSInteger)page pptStep:(NSInteger)step completionBlock:(ZegoDocsViewScrollCompleteBlock _Nullable)completionBlock {
    DLog(@"BoardOperation>>> scrollToPage:%ld pptStep:%ld",(long)page,(long)step);
    __weak typeof(self) weakSelf = self;
    if (self.currentDocsView) {
        [self.currentDocsView flipPage:page step:step completionBlock:^(BOOL isScrollSuccess) {
            if (completionBlock) {
                completionBlock(isScrollSuccess);
            }
            DLog(@"BoardOperation>>> scrollToPage --> flipPage:%ld,step:%ld",(long)page,(long)step);
            float pageNum = (float)MAX((weakSelf.currentDocsView.currentPage - 1), 0);
            [weakSelf.currentWhiteboardView scrollToHorizontalPercent:0 verticalPercent: pageNum/ (float)weakSelf.currentDocsView.pageCount pptStep:weakSelf.currentDocsView.currentStep completionBlock:^(ZegoWhiteboardViewError error_code, float horizontalPercent, float verticalPercent, unsigned int pptStep) {
                if (completionBlock) {
                    completionBlock(error_code == 0);
                }
                DLog(@"BoardOperation>>> scrollToPage --> flipPage --> scrollToHorizontalPercent,page:%f",pageNum);
            }];
        }];
    } else {
        if (page > 0 && page <= kWhiteboarPageCount) {
            CGFloat pagePrecent = (1.0 / kWhiteboarPageCount) * (page - 1);
            [weakSelf.currentWhiteboardView scrollToHorizontalPercent:pagePrecent verticalPercent:0 completionBlock:^(ZegoWhiteboardViewError error_code, float horizontalPercent, float verticalPercent, unsigned int pptStep) {
                if (completionBlock) {
                    completionBlock(error_code == 0);
                }
                DLog(@"BoardOperation>>> scrollToPage --> scrollToHorizontalPercent:%lf",pagePrecent);
            }];
        }
    }
    
}

- (ZegoSeq)uploadFile:(NSString *)filePath renderType:(ZegoDocsViewRenderType)renderType completionBlock:(nonnull ZegoDocsViewUploadBlock)completionBlock {
    ZegoSeq seq = [[ZegoBoardServiceManager shareManager] uploadFile:filePath renderType:renderType completionBlock:completionBlock];
    DLog(@"BoardOperation>>> uploadFile:%@ renderType:%lu, seq:%d",filePath,(unsigned long)renderType,seq);
    return seq;

}

- (void)cancelUploadFileSeq:(ZegoSeq)seq completionBlock:(ZegoDocsViewCancelUploadComplementBlock)completionBlock {
    [[ZegoBoardServiceManager shareManager] cancelUploadFileSeq:seq completionBlock:completionBlock];
}

- (ZegoSeq)cacheFileWithFileId:(NSString *)fileId completionBlock:(ZegoDocsViewCacheBlock)completionBlock {
    ZegoSeq seq = [[ZegoBoardServiceManager shareManager] cacheFileWithFileId:fileId completionBlock:completionBlock];
    return seq;
}

- (void)cancelCacheFileSeq:(ZegoSeq)seq completionBlock:(ZegoDocsViewCancelCacheComplementBlock)completionBlock {
    [[ZegoBoardServiceManager shareManager] cancelCacheFileSeq:seq completionBlock:completionBlock];
}

- (void)queryFileCachedWithFileId:(NSString *)fileId completionBlock:(ZegoDocsViewQueryCachedCompletionBlock)completionBlock {
    [[ZegoBoardServiceManager shareManager] queryFileCachedWithFileId:fileId completionBlock:completionBlock];
}

- (BOOL)isDynamicPPT {
    BOOL result = self.currentDocsView.fileType == ZegoDocsViewFileTypeDynamicPPTH5;
    DLog(@"BoardOperation>>> isDynamicPPT:%@",result?@"YES":@"NO");
    return result;
}

- (BOOL)isExcel {
    BOOL result = self.currentDocsView.fileType == ZegoDocsViewFileTypeELS;
    DLog(@"BoardOperation>>> isExcel:%@",result?@"YES":@"NO");
    return result;
}



@end