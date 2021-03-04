//
//  ZegoFileSelectView.h
//  ZegoWhiteboardExample
//
//  Created by MartinNie on 2020/12/1.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN
@protocol ZegoFileSelectViewDelegate <NSObject>

- (void)onSelectedWhiteboardIndex:(NSInteger )index;

- (void)onRemoveWhiteboardIdnex:(NSInteger )index;

@end

@interface ZegoFileSelectView : UIView
@property (nonatomic, weak) id <ZegoFileSelectViewDelegate> delegate;
@property (nonatomic, strong) NSArray *fileList;
@property (nonatomic, assign) NSInteger selectedIndex;

- (void)showFileSelectView;

- (void)hiddenFileSelectView;

@end

NS_ASSUME_NONNULL_END
