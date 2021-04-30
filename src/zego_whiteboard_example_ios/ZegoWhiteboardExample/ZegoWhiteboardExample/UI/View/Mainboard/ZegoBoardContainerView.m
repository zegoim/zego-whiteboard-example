//
//  ZegoBoardContainerView.m
//  ZegoWhiteboardExample
//
//  Created by Xuyang Nie on 2020/11/27.
//

#import "ZegoBoardContainerView.h"
#import "NSString+FormatValidator.h"
#import "ZegoToast.h"

@interface ZegoBoardContainerView()<ZegoWhiteboardViewDelegate,ZegoDocsViewDelegate>
@property (nonatomic, strong) ZegoWhiteboardView *currentWhiteboardView;
@property (nonatomic, strong) ZegoDocsView *currentDocsView;
@property (nonatomic, strong) NSMutableArray *whiteboardViewArray;
@property (nonatomic, strong) NSMutableArray *docsViewArray;
@property (nonatomic, strong) UILabel *tipLabel;
@property (nonatomic, strong) UILabel *sizeLabel;
@property (nonatomic, assign) BOOL isFrameManuallyChanged;


@end
@implementation ZegoBoardContainerView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        self.whiteboardViewArray = [NSMutableArray array];
        self.docsViewArray = [NSMutableArray array];
        
        [self configureSubviews];
        
        [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(addImage:) name:@"addImage" object:nil];
        [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(setBackgroundImage:) name:@"setBackgroundImage" object:nil];
        
    }
    return self;
}

- (void)configureSubviews {
    self.tipLabel = [[UILabel alloc] init];
    [self addSubview:self.tipLabel];
    self.tipLabel.textColor = kThemeColorPink;
    self.tipLabel.numberOfLines = 0;
    [self.tipLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self);
    }];
    
    self.sizeLabel = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, 150, 30)];
    [self addSubview:self.sizeLabel];
    self.sizeLabel.textColor = UIColor.systemRedColor;
    self.sizeLabel.numberOfLines = 0;
    self.sizeLabel.font = [UIFont systemFontOfSize:10];
}

- (void)addImage:(NSNotification *)noti {
    NSDictionary *dict = noti.userInfo;
    CGPoint point  = [dict[@"point"] CGPointValue];
    NSString *path = dict[@"file"];
    
//    if (![[NSFileManager defaultManager] fileExistsAtPath:path] &&
//        ![path isURL]) {
//        [ZegoProgessHUD showTipMessage:@"非法 URL"];
//        return;
//    }
    [ZegoToast toastWithMessage:@"正在加载"];
    [self.currentWhiteboardView addImage:ZegoWhiteboardViewImageTypeGraphic positionX:point.x positionY:point.y address:path complete:^(int errorcode) {
        if (errorcode == 0) {
            [ZegoToast toastWithMessage:@"加载成功"];
        }else {
            [ZegoToast toastWithError:errorcode];
        }
    }];
}

- (void)setBackgroundImage:(NSNotification *)noti {
    NSDictionary *dict = noti.userInfo;
    NSString *path = dict[@"file"];
    NSNumber *mode = dict[@"mode"];
    
    [ZegoToast toastWithMessage:@"正在加载"];
    [self.currentWhiteboardView setBackgroundImageWithPath:path mode:mode.unsignedIntegerValue complete:^(int errorcode) {
        if (errorcode == 0) {
            [ZegoToast toastWithMessage:@"加载成功"];
        }else {
            [ZegoToast toastWithError:errorcode];
        }
    }];
}

#pragma mark - public

- (void)removeCurrentWhiteboardViewAndDocsView {
    if (self.currentWhiteboardView) {
        [self.currentWhiteboardView removeLaser];
        [self.currentWhiteboardView removeFromSuperview];
        self.currentWhiteboardView.whiteboardViewDelegate = nil;
        self.currentWhiteboardView = nil;
    }
    if (self.currentDocsView) {
        [self.currentDocsView removeFromSuperview];
        self.currentDocsView.delegate = nil;
        self.currentDocsView = nil;
    }
}

- (void)addWhiteboardView:(ZegoWhiteboardView *)whiteboardView {
    if (!whiteboardView) return;
    //从显示区域移除当前白板和文件视图
    [self removeCurrentWhiteboardViewAndDocsView];
    
    //查找本地是否已经存在 此白板，如果不存在则不需要添加到列表中
    if ([self fetchWhiteboardView:whiteboardView.whiteboardModel.whiteboardID]) {
        self.currentWhiteboardView = whiteboardView;
        self.currentDocsView = [self fetchDocsViewWithID:whiteboardView.whiteboardModel.whiteboardID];
        [self.currentDocsView setOperationAuth:self.authInfo];
        [self addSubview:self.currentDocsView];
        [self addSubview:whiteboardView];
        whiteboardView.whiteboardViewDelegate = self;
        self.currentDocsView.delegate = self;
        [whiteboardView setWhiteboardOperationMode:ZegoWhiteboardOperationModeDraw|ZegoWhiteboardOperationModeZoom];
        [self whiteboardLoadFinished];
    } else {
        self.currentWhiteboardView = whiteboardView;
        [self.whiteboardViewArray addObject:whiteboardView];
        self.currentWhiteboardView.whiteboardViewDelegate = self;
        //加载文件视图
        [self loadDocsViewWithComplement:^{
            [self.currentWhiteboardView setWhiteboardOperationMode:ZegoWhiteboardOperationModeDraw|ZegoWhiteboardOperationModeZoom];
            ZegoWhiteboardViewModel *whiteboardModel = self.currentWhiteboardView.whiteboardModel;
            self.currentWhiteboardView.backgroundColor = [UIColor whiteColor];
            //文件视图需要展示在白板视图下方，所以要在文件视图装载完成后再添加白板视图
            [self addSubview:whiteboardView];
            // 是由 设定的白板宽高比，根据给定的父视图 计算出白板视图的实际frame
            if (self.currentDocsView) {
                CGSize visibleSize = self.currentDocsView.visibleSize;
                CGFloat width = self.frame.size.width;
                CGFloat height = self.frame.size.height;
                
                CGRect frame = CGRectMake((width - visibleSize.width) / 2.0, (height - visibleSize.height) / 2.0, visibleSize.width, visibleSize.height);
                whiteboardView.frame = frame;
                whiteboardView.contentSize = self.currentDocsView.contentSize;
                whiteboardView.backgroundColor = [UIColor clearColor];
            } else {
                whiteboardView.frame = [self rectToAspectFitContainer];
//                [self setPureWhiteboardConstraints];
            }
            
            whiteboardView.layer.borderWidth = 1;
            whiteboardView.layer.borderColor = [UIColor blackColor].CGColor;
            [self whiteboardLoadFinished];
        }];
    }
}

- (void)whiteboardLoadFinished {
    [self bringSubviewToFront:self.sizeLabel];
    //将文件及白板视图传递给 操作中心单例
    [[ZegoBoardOperationManager shareManager]
     setupCurrentWhiteboardView:self.currentWhiteboardView docsView: self.currentDocsView];
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        //处理加载完成回调
        if ([self.delegate respondsToSelector:@selector(onLoadFileFinish:docsView:currentPage:)]) {
            NSInteger currentPage = [self getCurrentPage];
            [self.delegate onLoadFileFinish:self.currentWhiteboardView docsView:self.currentDocsView currentPage:currentPage];
        }
    });
}

- (void)removeWhiteboardWithID:(ZegoWhiteboardID)whiteboardID {
    ZegoWhiteboardView *removeView = [self fetchWhiteboardView:whiteboardID];
    [self.whiteboardViewArray removeObject:removeView];
    if ([self.currentWhiteboardView isEqual:removeView]) {
        [removeView removeFromSuperview];
        self.currentWhiteboardView.whiteboardViewDelegate = nil;
        self.currentDocsView.delegate = nil;
        self.currentWhiteboardView = nil;
        self.currentDocsView = nil;
    }
    for (ZegoDocsView *docsView in self.docsViewArray) {
        if (docsView.fileID == removeView.whiteboardModel.fileInfo.fileID) {
            [docsView removeFromSuperview];
            [self.docsViewArray removeObject:docsView];
            break;
        }
    }
}

#pragma mark - private

- (ZegoWhiteboardView *)fetchWhiteboardView:(ZegoWhiteboardID)whiteboardViewID {
    ZegoWhiteboardView *targetView;
    for (ZegoWhiteboardView *view in self.whiteboardViewArray) {
        if (view.whiteboardModel.whiteboardID == whiteboardViewID) {
            targetView = view;
            break;
        }
    }
    return targetView;
}

- (ZegoDocsView *)fetchDocsViewWithID:(ZegoWhiteboardID)whiteboardViewID {
    ZegoDocsView *targetView;
    for (ZegoDocsView *view in self.docsViewArray) {
        if (view.associatedWhiteboardID == whiteboardViewID) {
            targetView = view;
            break;
        }
    }
    return targetView;
}

- (void)loadDocsViewWithComplement:(void(^)(void))complement {
    if (self.currentWhiteboardView.whiteboardModel.fileInfo.fileID.length > 0) {
        [ZegoProgessHUD showIndicatorHUDText:@"正在加载文件中"];
        ZegoDocsView * docsView = [[ZegoDocsView alloc] initWithFrame:self.bounds];
        docsView.delegate = self;
        self.currentDocsView = docsView;
        [self.currentDocsView setOperationAuth:self.authInfo];
        [self.docsViewArray addObject:docsView];
        
        __weak ZegoDocsView *weakDocsView = docsView;
        __weak typeof(self) weakSelf = self;
        [docsView loadFileWithFileID:self.currentWhiteboardView.whiteboardModel.fileInfo.fileID authKey:@"" completionBlock:^(ZegoDocsViewError errorCode) {
            __strong typeof(weakSelf) strongSelf = weakSelf;
            if (errorCode == ZegoDocsViewSuccess) {
                weakDocsView.associatedWhiteboardID = strongSelf.currentWhiteboardView.whiteboardModel.whiteboardID;
                if (weakDocsView && weakDocsView.pageCount > 0) {
                    if (strongSelf.currentDocsView == weakDocsView) {
                        [strongSelf insertSubview:weakDocsView aboveSubview:strongSelf.currentWhiteboardView];
                    }
                }
                //如果登陆房间存在ppt同步信息需要执行同步动画方法
                if (self.currentWhiteboardView.whiteboardModel.pptStep > 0 && self.currentWhiteboardView.whiteboardModel.verticalScrollPercent == 0) {
                    [weakDocsView flipPage:1 step:self.currentWhiteboardView.whiteboardModel.pptStep completionBlock:^(BOOL isScrollSuccess) {
                                            
                    }];
                }
                
                if (weakDocsView && strongSelf.currentWhiteboardView.whiteboardModel.h5_extra) {
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [weakDocsView playAnimation:strongSelf.currentWhiteboardView.whiteboardModel.h5_extra];
                    });
                }
                [ZegoProgessHUD dismiss];
            } else {
                [ZegoProgessHUD showTipMessage:[NSString stringWithFormat:@"加载文件失败：%lu",(unsigned long)errorCode]];
                DLog(@"%@", [NSString stringWithFormat:@"加载文件失败：%lu",(unsigned long)errorCode]);
            }
            if (complement) {
                complement();
            }
        }];
    } else {
        if (complement) {
            complement();
        }
    }
}

- (void)setPureWhiteboardConstraints {
    CGSize aspectSize = [ZegoBoardServiceManager shareManager].whiteboardAspectSize;
    CGFloat ratio = aspectSize.width / aspectSize.height;
    // 保持白板比例居中显示
    [self.currentWhiteboardView mas_remakeConstraints:^(MASConstraintMaker *make) {
        make.width.equalTo(self.currentWhiteboardView.mas_height).multipliedBy(ratio).priority(1000);
        make.center.equalTo(self).priority(1000);
        make.edges.lessThanOrEqualTo(self).priority(999);
        make.edges.equalTo(self).priority(998);
    }];
}

- (CGRect)rectToAspectFitContainer {
    CGFloat width = self.frame.size.width;
    CGFloat height = self.frame.size.height;
    CGSize aspectSize = [ZegoBoardServiceManager shareManager].whiteboardAspectSize;
    CGFloat aspectWidth = aspectSize.width;
    CGFloat aspectHeight = aspectSize.height;
    
    if (width / height > aspectWidth / aspectHeight) {
        CGFloat viewWidth = aspectWidth * height / aspectHeight;
        return CGRectMake((width - viewWidth) * 0.5, 0, viewWidth, height);
    } else {
        CGFloat viewHeight = aspectHeight * width / aspectWidth;
        return CGRectMake(0, (height - viewHeight) * 0.5, width, viewHeight);
    }
}

- (void)layoutWhiteboardView:(ZegoWhiteboardView *)whiteboardView docsView:(ZegoDocsView *)docsView {
    if (self.isFrameManuallyChanged) {
        self.isFrameManuallyChanged = NO;
        return;
    }
    ZegoWhiteboardViewModel *whiteboardModel = whiteboardView.whiteboardModel;
    if (docsView) {
        [self layoutWhiteboardView:whiteboardView withDocsView:docsView frame:[self rectToAspectFitContainer]];
    } else {
        whiteboardView.frame = [self rectToAspectFitContainer];
//        [self setPureWhiteboardConstraints];
    }
}

- (void)setWhiteboardAndDocsToInitFrame {
    [self layoutWhiteboardView:self.currentWhiteboardView docsView:self.currentDocsView];
}

- (void)layoutWhiteboardView:(ZegoWhiteboardView *)whiteboardView withDocsView:(ZegoDocsView *)docsView frame:(CGRect)docsViewFrame {
    docsView.frame = docsViewFrame;
    [docsView layoutIfNeeded];  //更新 visibleSize
    CGSize visibleSize = docsView.visibleSize;
    whiteboardView.frame = [self frameWithSize:visibleSize docsViewFrame:docsViewFrame];
    whiteboardView.contentSize = docsView.contentSize;
}
// 根据 docsView 的 frame 计算白板的 frame
- (CGRect)frameWithSize:(CGSize)visibleSize docsViewFrame:(CGRect)frame {
    CGFloat x = frame.origin.x + (frame.size.width - visibleSize.width) / 2;
    CGFloat y = frame.origin.y + (frame.size.height - visibleSize.height) / 2;
    return CGRectMake(x, y, visibleSize.width, visibleSize.height);
}

- (void)manualSetFrame:(CGRect)frame {
    self.isFrameManuallyChanged = YES;
    if (self.currentWhiteboardView.whiteboardModel.fileInfo.fileID.length > 0) {
        [self layoutWhiteboardView:self.currentWhiteboardView withDocsView:self.currentDocsView frame:frame];
    }else {
        self.currentWhiteboardView.frame = frame;
    }
    
    [self setNeedsLayout];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [self updateSizeLabel];
    });
    self.tipLabel.text = [NSString stringWithFormat:@"未加载白板及文件\n W:%.0f * H:%.0f",self.bounds.size.width,self.bounds.size.height];
    [self setWhiteboardAndDocsToInitFrame];
}

- (void)updateSizeLabel {
    NSString *str = [NSString stringWithFormat:@"白板容器W:%.2f H:%.2f", self.currentWhiteboardView.frame.size.width, self.currentWhiteboardView.frame.size.height];
    str = [str stringByAppendingFormat:@"\n文件容器W:%.2f H:%.2f", self.currentDocsView.frame.size.width, self.currentDocsView.frame.size.height];
    self.sizeLabel.text = str;
}

#pragma mark - ZegoWhiteboardViewDelegate
//本地白板滚动完成回调
- (void)onScrollWithHorizontalPercent:(CGFloat)horizontalPercent
                      verticalPercent:(CGFloat)verticalPercent
                       whiteboardView:(ZegoWhiteboardView *)whiteboardView {
    
    if (self.currentDocsView) {
        //判断是否是动态PPT,动态PPT 与静态PPT 是不同的加载方式，所以需要区别处理
        if (self.currentWhiteboardView.whiteboardModel.fileInfo.fileType == ZegoDocsViewFileTypeDynamicPPTH5
            || self.currentWhiteboardView.whiteboardModel.fileInfo.fileType == ZegoDocsViewFileTypeCustomH5) {
            if (self.currentWhiteboardView.whiteboardModel.pptStep < 1) {
                return;
            }
            CGFloat yPercent = self.currentWhiteboardView.contentOffset.y / self.currentWhiteboardView.contentSize.height;
            NSInteger pageNo = round(yPercent * self.currentDocsView.pageCount) + 1;
            //同步文件视图内容
            if ([UIApplication sharedApplication].applicationState != UIApplicationStateActive) {
                return;
            }
            [self.currentDocsView flipPage:pageNo step:MAX(self.currentWhiteboardView.whiteboardModel.pptStep, 1) completionBlock:^(BOOL isScrollSuccess) {
                
            }];
            
            //处理加载完成回调
            NSInteger currentPage = pageNo;
            if ([self.delegate respondsToSelector:@selector(onScrollWithCurrentPage:totalPage:)]) {

                [self.delegate onScrollWithCurrentPage:currentPage totalPage:self.currentDocsView.pageCount?:kWhiteboardPageCount];
            }
            
        } else {
            [self.currentDocsView scrollTo:verticalPercent completionBlock:^(BOOL isScrollSuccess) {
                
            }];
            
            //处理加载完成回调
            NSInteger currentPage = [self getCurrentPage];
            if ([self.delegate respondsToSelector:@selector(onScrollWithCurrentPage:totalPage:)]) {

                [self.delegate onScrollWithCurrentPage:currentPage totalPage:(self.currentDocsView.pageCount)?:kWhiteboardPageCount];
            }
        }
         
    } else {
        //处理加载完成回调
        NSInteger currentPage = [self getCurrentPage];
        if ([self.delegate respondsToSelector:@selector(onScrollWithCurrentPage:totalPage:)]) {

            [self.delegate onScrollWithCurrentPage:currentPage totalPage:(self.currentDocsView.pageCount)?:kWhiteboardPageCount];
        }
    }
    
       
}

//白板放大操作回调
- (void)onScaleChangedWithScaleFactor:(CGFloat)scaleFactor
                         scaleOffsetX:(CGFloat)scaleOffsetX
                         scaleOffsetY:(CGFloat)scaleOffsetY
                       whiteboardView:(ZegoWhiteboardView *)whiteboardView {
    //同步文件放大比例
    [self.currentDocsView scaleDocsViewWithScaleFactor:scaleFactor scaleOffsetX:scaleOffsetX scaleOffsetY:scaleOffsetY];
    
}

- (NSInteger)getCurrentPage {
    //处理加载完成回调
    NSInteger currentPage = 0;
    if (self.currentDocsView) {
        currentPage = self.currentDocsView.currentPage;
    } else {
        currentPage = (NSInteger)(self.currentWhiteboardView.whiteboardModel.horizontalScrollPercent * kWhiteboardPageCount) + 1;
    }
    return currentPage;
}


#pragma mark - ZegoDocsViewDelegate
/// 用户手动滑动时的回调
/// @param isScrollFinish 是否停止滚动
- (void)onScroll:(BOOL)isScrollFinish {
    
}

///用户步骤变化通知
- (void)onStepChange {
    
}

/// 文档展示异常错误通知，例如网络超时错误
/// @param errorCode 错误码
- (void)onError:(ZegoDocsViewError)errorCode {
    
}

/// 用户通过手指点击播放动画时产生的回调，仅对动态PPT有效
//  @param animationInfo: 播放动画信息（带有元素ID）
// docsView 的回调, 需要调用白板 -playAnimation: 方法
- (void)onPlayAnimation:(NSString *)animationInfo {
    if ([UIApplication sharedApplication].applicationState != UIApplicationStateActive) {
        return;
    }
    [self.currentWhiteboardView playAnimation:animationInfo];
}

///点击工具下用户步骤变化通知
- (void)onStepChangeForClick {
    /*
     每次动态 ppt 动画成功回调后:
     1. 需要查看页数是否需要更新
     2. 页数更新后, 需要移动白板 view 的 offset
     */
    if (self.currentDocsView.fileType != ZegoDocsViewFileTypeDynamicPPTH5 && self.currentDocsView.fileType != ZegoDocsViewFileTypeCustomH5) return;
    if (self.currentDocsView.currentPage <= self.currentDocsView.pageCount) {
        NSInteger pageNum = MAX((self.currentDocsView.currentPage - 1), 0);
        CGFloat verticalPercent = (CGFloat)pageNum / self.currentDocsView.pageCount;
        __weak typeof(self) weakSelf = self;
        [self.currentWhiteboardView scrollToHorizontalPercent:0
                                       verticalPercent: verticalPercent
                                               pptStep:self.currentDocsView.currentStep
                                       completionBlock:^(ZegoWhiteboardViewError error_code, float horizontalPercent, float verticalPercent, unsigned int pptStep) {
            __strong typeof(weakSelf) strongSelf = weakSelf;
            double round1 = round(verticalPercent * self.currentDocsView.pageCount);
            NSInteger pageNumFinal = round1 + 1;
            if (error_code == 0) {
                DLog(@"onStepChangeForClick docsViewSysPPTStep: %ld Step: %d verticalPercent: %f 当前page: %ld step:%ld", (long)pageNumFinal, pptStep, verticalPercent,(long)strongSelf.currentDocsView.currentPage, (long)self.currentDocsView.currentStep);
            } else {
                if (error_code == ZegoWhiteboardViewErrorNoAuthScroll) {
                    [ZegoToast toastWithError:error_code];
                }
            }
        }];
    }

}

- (void)dealloc {
    DLog(@" %@ dealloc",self.class);
}
@end
