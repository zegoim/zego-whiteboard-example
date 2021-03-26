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


@end
@implementation ZegoBoardContainerView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
        self.whiteboardViewArray = [NSMutableArray array];
        self.docsViewArray = [NSMutableArray array];
        self.tipLabel = [[UILabel alloc] init];
        [self addSubview:self.tipLabel];
        self.tipLabel.textColor = kThemeColorPink;
        self.tipLabel.numberOfLines = 0;
        
        [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(addImage:) name:@"addImage" object:nil];
        [[NSNotificationCenter defaultCenter]addObserver:self selector:@selector(setBackgroundImage:) name:@"setBackgroundImage" object:nil];
        
    }
    return self;
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
    
//    ZegoProgessHUD *hudView = [[ZegoProgessHUD alloc] initWithTitle:@"加载背景图片..." cancelBlock:nil];
//    [self.currentWhiteboardView setBackgroundImageWithPath:path mode:mode.unsignedIntegerValue complete:^(int errorcode, float progress) {
//        if (errorcode == 0) {
//            [hudView updateProgress:progress];
//            if (progress == 1) {
//                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(0.5 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
//                    [ZegoProgessHUD showTipMessage:@"加载完成"];
//                });
//            }
//        }else {
//            [ZegoProgessHUD dismiss];
//            [ZegoToast toastWithError:errorcode];
//        }
//    }];
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

- (void)addWhiteboardView:(ZegoWhiteboardView *)whiteboardView {
    if (!whiteboardView) return;
    //从显示区域移除当前白板和文件视图
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
    
    //查找本地是否已经存在 此白板，如果不存在则不需要添加到列表中
    if ([self fetchWhiteboardView:whiteboardView.whiteboardModel.whiteboardID]) {
        self.currentWhiteboardView = whiteboardView;
        self.currentDocsView = [self fetchDocsViewWithID:whiteboardView.whiteboardModel.whiteboardID];
        [self.currentDocsView setOperationAuth:self.authInfo];
        [self addSubview:self.currentDocsView];
        [self addSubview:self.currentWhiteboardView];
        self.currentWhiteboardView.whiteboardViewDelegate = self;
        self.currentDocsView.delegate = self;
        [self.currentWhiteboardView setWhiteboardOperationMode:ZegoWhiteboardOperationModeDraw|ZegoWhiteboardOperationModeZoom];
        [self whiteboardLoadFinished];
    } else {
        self.currentWhiteboardView = whiteboardView;
        [self.whiteboardViewArray addObject:whiteboardView];
        self.currentWhiteboardView.whiteboardViewDelegate = self;
        __weak typeof(self) weakSelf = self;
        //加载文件视图
        [self loadDocsViewWithComplement:^{
            
            __strong typeof(weakSelf) strongSelf = weakSelf;
            [strongSelf.currentWhiteboardView setWhiteboardOperationMode:ZegoWhiteboardOperationModeDraw|ZegoWhiteboardOperationModeZoom];
            ZegoWhiteboardViewModel *data = strongSelf.currentWhiteboardView.whiteboardModel;
            strongSelf.currentWhiteboardView.backgroundColor = [UIColor whiteColor];
            // 是由 设定的白板宽高比，根据给定的父视图 计算出白板视图的实际frame
            if (strongSelf.currentDocsView) {
                CGSize visibleSize = strongSelf.currentDocsView.visibleSize;
                CGFloat width = self.frame.size.width;
                CGFloat height = self.frame.size.height;
                
                CGRect frame = CGRectMake((width - visibleSize.width) / 2.0, (height - visibleSize.height) / 2.0, visibleSize.width, visibleSize.height);
                strongSelf.currentWhiteboardView.frame = frame;
                strongSelf.currentWhiteboardView.contentSize = strongSelf.currentDocsView.contentSize;
                strongSelf.currentWhiteboardView.backgroundColor = [UIColor clearColor];
            } else {
                strongSelf.currentWhiteboardView.frame = [strongSelf aspectToFitScreen:data.aspectWidth * 1.0 / data.aspectHeight];
                strongSelf.currentWhiteboardView.contentSize = CGSizeMake(strongSelf.currentWhiteboardView.frame.size.width * data.pageCount, strongSelf.currentWhiteboardView.frame.size.height);
            }
            //文件视图需要展示在白板视图下方，所以要在文件视图装载完成后再添加白板视图
            [strongSelf addSubview:whiteboardView];
            strongSelf.currentWhiteboardView.layer.borderWidth = 1;
            strongSelf.currentWhiteboardView.layer.borderColor = [UIColor blackColor].CGColor;
            [strongSelf whiteboardLoadFinished];
        }];
    }
}

- (void)whiteboardLoadFinished {
    //将文件及白板视图传递给 操作中心单例
    [[ZegoBoardOperationManager shareManager] setupCurrentWhiteboardView:self.currentWhiteboardView docsView: self.currentDocsView];
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
        __weak typeof(self) weakSelf = self;
        docsView.delegate = self;
        self.currentDocsView = docsView;
        [self.currentDocsView setOperationAuth:self.authInfo];
        [self.docsViewArray addObject:docsView];
        [docsView loadFileWithFileID:self.currentWhiteboardView.whiteboardModel.fileInfo.fileID authKey:@"" completionBlock:^(ZegoDocsViewError errorCode) {
            __strong typeof(weakSelf) strongSelf = weakSelf;
            if (errorCode == ZegoDocsViewSuccess) {
                docsView.associatedWhiteboardID = strongSelf.currentWhiteboardView.whiteboardModel.whiteboardID;
                if (docsView && docsView.pageCount > 0) {
                    CGSize visibleSize = docsView.visibleSize;
                    CGFloat width = strongSelf.frame.size.width;
                    CGFloat height = strongSelf.frame.size.height;
                    
                    CGRect frame = CGRectMake((width - visibleSize.width) / 2.0, (height - visibleSize.height) / 2.0, visibleSize.width, visibleSize.height);
                    docsView.frame = frame;
                    if (strongSelf.currentDocsView == docsView) {
                        [strongSelf insertSubview:docsView aboveSubview:strongSelf.currentWhiteboardView];
                    }
                }
                //如果登陆房间存在ppt同步信息需要执行同步动画方法
                if (docsView && strongSelf.currentWhiteboardView.whiteboardModel.h5_extra) {
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [docsView playAnimation:strongSelf.currentWhiteboardView.whiteboardModel.h5_extra];
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

- (CGRect)aspectToFitScreen:(CGFloat)aspect {
    CGFloat width = self.frame.size.width;
    CGFloat height = self.frame.size.height;
    
    if (width / height > 16 / 9.0) {
        CGFloat viewWidth = 16 * height / 9.0;
        return CGRectMake((width - viewWidth) * 0.5, 0, viewWidth, height);
    } else {
        CGFloat viewHeight = 9 * width / 16.0;
        return  CGRectMake(0, (height - viewHeight) * 0.5, width, viewHeight);
    }
}

- (void)layoutSubviews {
    [super layoutSubviews];
    [self.tipLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.center.equalTo(self);
    }];
    self.tipLabel.text = [NSString stringWithFormat:@"未加载白板及文件\n W:%.0f * H:%.0f",self.bounds.size.width,self.bounds.size.height];

}

#pragma mark - ZegoWhiteboardViewDelegate
//本地白板滚动完成回调
- (void)onScrollWithHorizontalPercent:(CGFloat)horizontalPercent
                      verticalPercent:(CGFloat)verticalPercent
                       whiteboardView:(ZegoWhiteboardView *)whiteboardView {
    
    if (self.currentDocsView) {
        //判断是否是动态PPT,动态PPT 与静态PPT 是不同的加载方式，所以需要区别处理
        if (self.currentWhiteboardView.whiteboardModel.fileInfo.fileType == ZegoDocsViewFileTypeDynamicPPTH5) {
            if (self.currentWhiteboardView.whiteboardModel.pptStep < 1) {
                return;
            }
            CGFloat yPercent = self.currentWhiteboardView.contentOffset.y / self.currentWhiteboardView.contentSize.height;
            NSInteger pageNo = round(yPercent * self.currentDocsView.pageCount) + 1;
            //同步文件视图内容
            [self.currentDocsView flipPage:pageNo step:MAX(self.currentWhiteboardView.whiteboardModel.pptStep, 1) completionBlock:^(BOOL isScrollSuccess) {
                
            }];
            
            //处理加载完成回调
            NSInteger currentPage = pageNo;
            if ([self.delegate respondsToSelector:@selector(onScrollWithCurrentPage:totalPage:)]) {

                [self.delegate onScrollWithCurrentPage:currentPage totalPage:currentPage?:kWhiteboarPageCount];
            }
            
        } else {
            [self.currentDocsView scrollTo:verticalPercent completionBlock:^(BOOL isScrollSuccess) {
                
            }];
            
            //处理加载完成回调
            NSInteger currentPage = [self getCurrentPage];
            if ([self.delegate respondsToSelector:@selector(onScrollWithCurrentPage:totalPage:)]) {

                [self.delegate onScrollWithCurrentPage:currentPage totalPage:(self.currentDocsView.pageCount)?:kWhiteboarPageCount];
            }
        }
         
    } else {
        //处理加载完成回调
        NSInteger currentPage = [self getCurrentPage];
        if ([self.delegate respondsToSelector:@selector(onScrollWithCurrentPage:totalPage:)]) {

            [self.delegate onScrollWithCurrentPage:currentPage totalPage:(self.currentDocsView.pageCount)?:kWhiteboarPageCount];
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
        currentPage = (NSInteger)(self.currentWhiteboardView.whiteboardModel.horizontalScrollPercent * kWhiteboarPageCount) + 1;
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
    if (self.currentDocsView.fileType != ZegoDocsViewFileTypeDynamicPPTH5) return;
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
