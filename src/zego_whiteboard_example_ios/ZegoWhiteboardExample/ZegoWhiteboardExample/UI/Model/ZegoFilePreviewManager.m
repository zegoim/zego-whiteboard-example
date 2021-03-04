//
//  ZegoFilePreviewViewModel.m
//  ZegoWhiteboardVideoDemo
//
//  Created by MartinNie on 2020/8/21.
//  Copyright © 2020 zego. All rights reserved.
//

#import "ZegoFilePreviewManager.h"
#import "ZegoFilePreviewView.h"
@interface ZegoFilePreviewManager ()<UIGestureRecognizerDelegate>
@property (nonatomic, strong) UIView *coverView;
@property (nonatomic, strong) ZegoFilePreviewView *previewView;//预览视图控件
@end
@implementation ZegoFilePreviewManager


+ (instancetype)shareManager {
    static ZegoFilePreviewManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[ZegoFilePreviewManager alloc] init];
    });
    return manager;
}

- (instancetype)init {
    if (self = [super init]) {
        UITapGestureRecognizer *tap = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(didClickBackgroudView)];
        tap.delegate = self;
        self.coverView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, kScreenWidth, kScreenHeight)];
        [self.coverView addGestureRecognizer:tap];
        [self reset];
    }
    return self;
}

- (void)didClickBackgroudView {
    [self hiddenPreview];
}

- (void)setupPreviewData:(NSArray *)dataArray {
    if (!self.previewView ) {
        _previewView = [[ZegoFilePreviewView alloc] initWithFrame:CGRectMake(kScreenWidth, 0, 200, kScreenHeight)];
        [self.coverView addSubview:_previewView];
    }
    
    _previewView.selectedPageBlock = _selectedPageBlock;
    [self.previewView setDataArray:dataArray];
    
}

- (void)setSelectedPageBlock:(void (^)(NSInteger))selectedPageBlock {
    _selectedPageBlock = selectedPageBlock;
    self.previewView.selectedPageBlock = selectedPageBlock;
}

- (void)reset {
    [_previewView removeFromSuperview];
    _previewView = nil;
    _isShow = NO;
    _currentPage = 0;
}

- (void)setCurrentPageCount:(NSInteger)pageCount {
    if (pageCount < 0) {
        return;
    }
    _currentPage = pageCount;
    [self.previewView setPreviewPageCount:pageCount];
}

- (void)showPreviewWithPage:(NSInteger)pageCount {
    if (!_isShow) {
        _isShow = YES;
        [[UIApplication sharedApplication].keyWindow addSubview:self.coverView];
        [self setCurrentPageCount:pageCount];
        [UIView animateWithDuration:0.5 animations:^{
            CGRect currentFrame = self.previewView.frame;
            self.previewView.frame = CGRectOffset(currentFrame, -currentFrame.size.width, 0);
        } completion:nil];
    }
}

- (void)hiddenPreview {
    if (_isShow) {
        _isShow = NO;
        [UIView animateWithDuration:0.5 animations:^{
            CGRect currentFrame = self.previewView.frame;
            self.previewView.frame = CGRectOffset(currentFrame, currentFrame.size.width, 0);
        } completion:nil];
        [self.coverView removeFromSuperview];
    }
}

- (BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch {
    CGPoint touchPoint = [touch locationInView:self.coverView];
    
    if (CGRectContainsPoint(self.previewView.frame, touchPoint)) {
        return  NO;
    } else {
        return YES;
    }
}
@end
