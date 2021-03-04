//
//  ZegoSelecteSheetManager.m
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/4.
//

#import "ZegoSelecteSheetManager.h"
#import "ZegoCommonCellModel.h"
#import <UIKit/UIKit.h>
@interface ZegoSelecteSheetManager ()
@property (nonatomic, strong) UIAlertController *alertVC;
@property (nonatomic, assign) BOOL isShow;
@property (nonatomic, strong) void(^didSelectedSheetBlock)(NSInteger index);
@end
@implementation ZegoSelecteSheetManager

+ (instancetype)shareManager {
    static ZegoSelecteSheetManager *manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        manager = [[ZegoSelecteSheetManager alloc] init];
    });
    return manager;
}

- (instancetype)init {
    if (self = [super init]) {
        
        self.alertVC = [UIAlertController alertControllerWithTitle:@"" message:@"" preferredStyle:UIAlertControllerStyleActionSheet];
        
        
    }
    return self;
}

- (void)showSheetWithTitle:(NSString *)title optionArray:(NSArray *)optionArray selectedBlock:(void(^)(NSInteger index))selectedBlock {

    if (self.isShow) {
        [self hiddenSheet];
    }
    _didSelectedSheetBlock = selectedBlock;
    self.isShow = YES;
    [self initAlertSheetWithTitle:title optionArray:optionArray];
    UIViewController *vc = [UIApplication sharedApplication].keyWindow.rootViewController;
    [vc presentViewController:self.alertVC animated:YES completion:nil];

}

- (void)initAlertSheetWithTitle:(NSString *)title optionArray:(NSArray *)optionArray {
    self.alertVC = [UIAlertController alertControllerWithTitle:@"" message:title preferredStyle:UIAlertControllerStyleActionSheet];
    for (int i = 0; i < optionArray.count; i++) {
        ZegoCellOptionModel *option = optionArray[i];
        UIAlertAction *ation = [UIAlertAction actionWithTitle:option.title style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            if (self.didSelectedSheetBlock) {
                self.didSelectedSheetBlock(i);
            }
        }];
        [self.alertVC addAction:ation];
    }
    UIAlertAction *action = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:nil];
    [self.alertVC addAction:action];
}

- (void)hiddenSheet {
    self.isShow = NO;
//    UIViewController *vc = [UIApplication sharedApplication].keyWindow.rootViewController;
    [self.alertVC dismissViewControllerAnimated:YES completion:nil];

}
@end
